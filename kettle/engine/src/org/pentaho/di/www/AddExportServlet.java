/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;

/**
 * This servlet allows you to transport an exported job or transformation over to the carte server as a zip file. It
 * ends up in a temporary file.
 *
 * The servlet returns the name of the file stored.
 *
 * @author matt
 *
 */
public class AddExportServlet extends BaseHttpServlet implements CartePluginInterface {
  public static final String PARAMETER_LOAD = "load";
  public static final String PARAMETER_TYPE = "type";

  public static final String TYPE_JOB = "job";
  public static final String TYPE_TRANS = "trans";

  private static final long serialVersionUID = -6850701762586992604L;
  public static final String CONTEXT_PATH = "/kettle/addExport";

  public AddExportServlet() {
  }

  public AddExportServlet( JobMap jobMap, TransformationMap transformationMap ) {
    super( transformationMap, jobMap );
  }

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    if ( isJettyMode() && !request.getRequestURI().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( "Addition of export requested" );
    }

    PrintWriter out = response.getWriter();
    BufferedReader in = request.getReader(); // read from the client
    if ( log.isDetailed() ) {
      logDetailed( "Encoding: " + request.getCharacterEncoding() );
    }

    boolean isJob = TYPE_JOB.equalsIgnoreCase( request.getParameter( PARAMETER_TYPE ) );
    String load = request.getParameter( PARAMETER_LOAD ); // the resource to load

    response.setContentType( "text/xml" );
    out.print( XMLHandler.getXMLHeader() );

    response.setStatus( HttpServletResponse.SC_OK );

    OutputStream outputStream = null;

    try {
      FileObject tempFile = KettleVFS.createTempFile( "export", ".zip", System.getProperty( "java.io.tmpdir" ) );
      outputStream = KettleVFS.getOutputStream( tempFile, false );

      // Pass the input directly to a temporary file
      //
      // int size = 0;
      int c;
      while ( ( c = in.read() ) != -1 ) {
        outputStream.write( c );
        // size++;
      }

      outputStream.flush();
      outputStream.close();
      outputStream = null; // don't close it twice

      String archiveUrl = tempFile.getName().toString();
      String fileUrl = null;

      String carteObjectId = null;
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );

      // Now open the top level resource...
      //
      if ( !Const.isEmpty( load ) ) {

        fileUrl = "zip:" + archiveUrl + "!" + load;

        if ( isJob ) {
          // Open the job from inside the ZIP archive
          //
          KettleVFS.getFileObject( fileUrl );

          JobMeta jobMeta = new JobMeta( fileUrl, null ); // never with a repository
          // Also read the execution configuration information
          //
          String configUrl = "zip:" + archiveUrl + "!" + Job.CONFIGURATION_IN_EXPORT_FILENAME;
          Document configDoc = XMLHandler.loadXMLFile( configUrl );
          JobExecutionConfiguration jobExecutionConfiguration =
            new JobExecutionConfiguration( XMLHandler.getSubNode( configDoc, JobExecutionConfiguration.XML_TAG ) );

          carteObjectId = UUID.randomUUID().toString();
          servletLoggingObject.setContainerObjectId( carteObjectId );
          servletLoggingObject.setLogLevel( jobExecutionConfiguration.getLogLevel() );

          Job job = new Job( null, jobMeta, servletLoggingObject );

          // Do we need to expand the job when it's running?
          // Note: the plugin (Job and Trans) job entries need to call the delegation listeners in the parent job.
          //
          if ( jobExecutionConfiguration.isExpandingRemoteJob() ) {
            job.addDelegationListener( new CarteDelegationHandler( getTransformationMap(), getJobMap() ) );
          }

          // store it all in the map...
          //
          synchronized ( getJobMap() ) {
            getJobMap().addJob(
              job.getJobname(), carteObjectId, job, new JobConfiguration( jobMeta, jobExecutionConfiguration ) );
          }

          // Apply the execution configuration...
          //
          log.setLogLevel( jobExecutionConfiguration.getLogLevel() );
          job.setArguments( jobExecutionConfiguration.getArgumentStrings() );
          jobMeta.injectVariables( jobExecutionConfiguration.getVariables() );

          // Also copy the parameters over...
          //
          Map<String, String> params = jobExecutionConfiguration.getParams();
          for ( String param : params.keySet() ) {
            String value = params.get( param );
            jobMeta.setParameterValue( param, value );
          }

        } else {
          // Open the transformation from inside the ZIP archive
          //
          TransMeta transMeta = new TransMeta( fileUrl );
          // Also read the execution configuration information
          //
          String configUrl = "zip:" + archiveUrl + "!" + Trans.CONFIGURATION_IN_EXPORT_FILENAME;
          Document configDoc = XMLHandler.loadXMLFile( configUrl );
          TransExecutionConfiguration executionConfiguration =
            new TransExecutionConfiguration( XMLHandler.getSubNode(
              configDoc, TransExecutionConfiguration.XML_TAG ) );

          carteObjectId = UUID.randomUUID().toString();
          servletLoggingObject.setContainerObjectId( carteObjectId );
          servletLoggingObject.setLogLevel( executionConfiguration.getLogLevel() );

          Trans trans = new Trans( transMeta, servletLoggingObject );

          // store it all in the map...
          //
          getTransformationMap().addTransformation(
            trans.getName(), carteObjectId, trans, new TransConfiguration( transMeta, executionConfiguration ) );
        }
      } else {
        fileUrl = archiveUrl;
      }

      out.println( new WebResult( WebResult.STRING_OK, fileUrl, carteObjectId ) );
    } catch ( Exception ex ) {
      out.println( new WebResult( WebResult.STRING_ERROR, Const.getStackTracker( ex ) ) );
    } finally {
      if ( outputStream != null ) {
        outputStream.close();
      }
    }
  }

  public String toString() {
    return "Add export";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
