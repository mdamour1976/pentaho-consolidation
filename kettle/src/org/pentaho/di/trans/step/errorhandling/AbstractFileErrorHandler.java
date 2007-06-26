package org.pentaho.di.trans.step.errorhandling;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.step.BaseStep;

public abstract class AbstractFileErrorHandler implements FileErrorHandler {
	private static final String DD_MMYYYY_HHMMSS = "ddMMyyyy-HHmmss"; //$NON-NLS-1$

	public static final String NO_PARTS = "NO_PARTS"; //$NON-NLS-1$

	private final LogWriter log = LogWriter.getInstance();

	private final String destinationDirectory;

	private final String fileExtension;

	private final String encoding;

	private String processingFilename;

	private Map<Object,Writer> writers;

	private String dateString;

	private BaseStep baseStep;

	public AbstractFileErrorHandler(Date date, String destinationDirectory,
			String fileExtension, String encoding, BaseStep baseStep) {
		this.destinationDirectory = destinationDirectory;
		this.fileExtension = fileExtension;
		this.encoding = encoding;
		this.baseStep = baseStep;
		this.writers = new HashMap<Object,Writer>();
		initDateFormatter(date);
	}

	private void initDateFormatter(Date date) {
		dateString = createDateFormat().format(date);
	}

	public static DateFormat createDateFormat() {
		return new SimpleDateFormat(DD_MMYYYY_HHMMSS);
	}

	public static FileObject getReplayFilename(String destinationDirectory,
			String processingFilename, String dateString, String extension, Object source) throws IOException 
    {
		String name = null;
		String sourceAdding = ""; //$NON-NLS-1$
		if (source != NO_PARTS) {
			sourceAdding = "_" + source.toString();
		}
		if (extension == null || extension.length() == 0)
			name = processingFilename + sourceAdding + "." + dateString; //$NON-NLS-1$
		else
			name = processingFilename + sourceAdding + "." + dateString + "." + extension; //$NON-NLS-1$ //$NON-NLS-2$
		return KettleVFS.getFileObject(StringUtil.environmentSubstitute(destinationDirectory)+"/"+name);
	}

	public static FileObject getReplayFilename(String destinationDirectory,
			String processingFilename, Date date, String extension, Object source) throws IOException {
		return getReplayFilename(destinationDirectory, processingFilename,
				createDateFormat().format(date), extension, source);
	}

	/**
	 * returns the OutputWiter if exists. Otherwhise it will create a new one.
	 * 
	 * @return
	 * @throws KettleException
	 */
	Writer getWriter(Object source) throws KettleException 
    {
        try
        {
    		Writer outputStreamWriter = (Writer) writers.get(source);
    		if (outputStreamWriter != null)
    			return outputStreamWriter;
    		FileObject file = getReplayFilename(destinationDirectory, processingFilename, dateString, fileExtension, source);
    		ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, baseStep.getTransMeta().getName(), baseStep.getStepname());
    		baseStep.addResultFile(resultFile);
    		try {
    			if (encoding == null)
                {
    				outputStreamWriter = new OutputStreamWriter(file.getContent().getOutputStream());
                }
    			else
                {
    				outputStreamWriter = new OutputStreamWriter(file.getContent().getOutputStream(), encoding);
                }
    		} 
            catch (Exception e) 
            {
    			throw new KettleException(Messages.getString("AbstractFileErrorHandler.Exception.CouldNotCreateFileErrorHandlerForFile") //$NON-NLS-1$
    							+ file.getName().getURI(), e);
    		}
    		writers.put(source, outputStreamWriter);
    		return outputStreamWriter;
        }
        catch(IOException e)
        {
            throw new KettleException(Messages.getString("AbstractFileErrorHandler.Exception.CouldNotCreateFileErrorHandlerForFile"), e);
        }
	}

	public void close() throws KettleException {
		for (Iterator iter = writers.values().iterator(); iter.hasNext();) {
			close((Writer) iter.next());
		}
		writers = new HashMap<Object,Writer>();

	}

	private void close(Writer outputStreamWriter) throws KettleException {
		if (outputStreamWriter != null) {
			try {
				outputStreamWriter.flush();
			} catch (IOException exception) {
				log.logError(Messages.getString("AbstractFileErrorHandler.Log.CouldNotFlushContentToFile"), exception //$NON-NLS-1$
						.getLocalizedMessage());
			}
			try {
				outputStreamWriter.close();
			} catch (IOException exception) {
				throw new KettleException(Messages.getString("AbstractFileErrorHandler.Exception.CouldNotCloseFile"), exception); //$NON-NLS-1$
			} finally {
				outputStreamWriter = null;
			}
		}
	}

	public void handleFile(FileObject file) throws KettleException {
		close();
		this.processingFilename = file.getName().getBaseName();
	}

}
