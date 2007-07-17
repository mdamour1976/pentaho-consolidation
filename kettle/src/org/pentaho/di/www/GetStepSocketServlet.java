package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;

public class GetStepSocketServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    
    public static final String CONTEXT_PATH = "/kettle/getStepSocket";
    
    
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public GetStepSocketServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), "Reservation of port number of step requested");
        response.setStatus(HttpServletResponse.SC_OK);
        
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );
        String transName = request.getParameter("transName");
        String stepName = request.getParameter("stepName");
        String stepCopy = request.getParameter("stepCopy");
        
        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
        }
        else
        {
            response.setContentType("text/html");
        }

        int port = transformationMap.getServerSocketPort(transName, stepName, stepCopy);

        PrintStream out = new PrintStream(response.getOutputStream());
        if (useXML)
        {
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
            out.print(XMLHandler.addTagValue("port", port));
        }
        else
        {    
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Request for reservation of step socket</TITLE></HEAD>");
            out.println("<BODY>");
            out.println("<H1>Status</H1>");
    
            out.println("<p>");
            out.println("Transformation name : "+transName+"<br>");
            out.println("Step name : "+stepName+"<br>");
            out.println("Step copy: "+stepCopy+"<br>");
            out.println("<p>");
            out.println("--> port : "+port+"<br>");
    
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }        
    }

    public String toString()
    {
        return "Step port reservation request";
    }
}
