package org.pentaho.di.core.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Comparator;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.pentaho.di.core.Const;

public class KettleVFS
{
    private int fileNumber;
    
    private KettleVFS()
    {
        fileNumber=1;
    }
    
    private int getNextFileNumber()
    {
        return fileNumber++;
    }
    
    private static KettleVFS kettleVFS = new KettleVFS();
    
    public static FileObject getFileObject(String vfsFilename) throws IOException
    {
        FileSystemManager fsManager = VFS.getManager();
        
        // We have one problem with VFS: if the file is in a subdirectory of the current one: somedir/somefile
        // In that case, VFS doesn't parse the file correctly.
        // We need to put file: in front of it to make it work.
        // However, how are we going to verify this?
        // 
        // We are going to see if the filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
        // If not, we are going to assume it's a file.
        //
        boolean relativeFilename=true;
        String[] schemes = VFS.getManager().getSchemes();
        for (int i=0;i<schemes.length && relativeFilename;i++)
        {
            if (vfsFilename.startsWith(schemes[i]+":")) relativeFilename=false;
        }
        
        String filename;
        if (vfsFilename.startsWith("\\\\"))
        {
            File file = new File(vfsFilename);
            filename = file.toURI().toString();
        }
        else
        {
            if (relativeFilename)
            {
                File file = new File(vfsFilename);
                filename = file.getAbsolutePath();
            }
            else
            {
                filename = vfsFilename;
            }
        }
        
        FileObject fileObject = fsManager.resolveFile( filename );
        
        return fileObject;
    }
    
    /**
     * Read a text file (like an XML document).  WARNING DO NOT USE FOR DATA FILES.
     * 
     * @param vfsFilename the filename or URL to read from
     * @param charSetName the character set of the string (UTF-8, ISO8859-1, etc)
     * @return The content of the file as a String
     * @throws IOException
     */
    public static String getTextFileContent(String vfsFilename, String charSetName) throws IOException
    {
        InputStream inputStream = getInputStream(vfsFilename);
        InputStreamReader reader = new InputStreamReader(inputStream, charSetName);
        int c;
        StringBuffer stringBuffer = new StringBuffer();
        while ( (c=reader.read())!=-1) stringBuffer.append((char)c);
        reader.close();
        inputStream.close();
        
        return stringBuffer.toString();
    }
    
    public static boolean fileExists(String vfsFilename) throws IOException
    {
        FileObject fileObject = getFileObject(vfsFilename);
        return fileObject.exists();
    }
    
    public static InputStream getInputStream(FileObject fileObject) throws FileSystemException
    {
        FileContent content = fileObject.getContent();
        return content.getInputStream();
    }
    
    public static InputStream getInputStream(String vfsFilename) throws IOException
    {
        FileObject fileObject = getFileObject(vfsFilename);
        return getInputStream(fileObject);
    }
    
    public static OutputStream getOutputStream(FileObject fileObject, boolean append) throws IOException
    {
        FileObject parent = fileObject.getParent();
        if (parent!=null)
        {
            if (!parent.exists())
            {
                throw new IOException(Messages.getString("KettleVFS.Exception.ParentDirectoryDoesNotExist", getFilename(parent)));
            }
        }
        fileObject.createFile();
        FileContent content = fileObject.getContent();
        return content.getOutputStream(append);
    }
    
    public static OutputStream getOutputStream(String vfsFilename, boolean append) throws IOException
    {
        FileObject fileObject = getFileObject(vfsFilename);
        return getOutputStream(fileObject, append);
    }
    
    public static String getFilename(FileObject fileObject)
    {
        FileName fileName = fileObject.getName();
        String root = fileName.getRootURI();
        if (!root.startsWith("file:")) return fileName.getURI(); // nothing we can do about non-normal files.
        if (root.endsWith(":/")) // Windows
        {
            root = root.substring(8,10);
        }
        else // *nix & OSX
        {
            root = "";
        }
        String fileString = root + fileName.getPath();
        if (!"/".equals(Const.FILE_SEPARATOR))
        {
            fileString = Const.replace(fileString, "/", Const.FILE_SEPARATOR);
        }
        return fileString;
    }
    
    public static FileObject createTempFile(String prefix, String suffix, String directory) throws IOException
    {
        FileObject fileObject;
        do
        {
            String filename = directory+"/"+prefix+"_"+kettleVFS.getNextFileNumber()+suffix;
            fileObject = getFileObject(filename);
        }
        while (fileObject.exists());
        return fileObject;
    }
    
    public static Comparator<FileObject> getComparator()
    {
        return new Comparator<FileObject>()
        {
            public int compare(FileObject o1, FileObject o2)
            {
                String filename1 = getFilename( o1);
                String filename2 = getFilename( o2);
                return filename1.compareTo(filename2);
            }
        };
    }

    /**
     * Get a FileInputStream for a local file.  Local files can be read with NIO.
     * 
     * @param fileObject
     * @return a FileInputStream
     * @throws IOException
     */
	public static FileInputStream getFileInputStream(FileObject fileObject) throws IOException {
		
		if (!(fileObject instanceof LocalFile)) {
			// We can only use NIO on local files at the moment, so that's what we limit ourselves to.
			//
			throw new IOException(Messages.getString("FixedInput.Log.OnlyLocalFilesAreSupported"));
		}
				
		return (FileInputStream)((LocalFile)fileObject).getInputStream();
	}

}
