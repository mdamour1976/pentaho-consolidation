package org.pentaho.di.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.w3c.dom.Node;

public class JobFileListener implements FileListener {

    public boolean open(Node jobNode, String fname, boolean importfile)
    {
    	Spoon spoon = Spoon.getInstance();
        try
        {
            JobMeta jobMeta = new JobMeta(spoon.getLog());
            jobMeta.loadXML(jobNode, spoon.getRepository());
            spoon.getProperties().addLastFile(LastUsedFile.FILE_TYPE_JOB, fname, null, false, null);
            spoon.addMenuLast();
            if (!importfile) jobMeta.clearChanged();
            jobMeta.setFilename(fname);
            spoon.delegates.jobs.addJobGraph(jobMeta);
            
            spoon.refreshTree();
            spoon.refreshHistory();
            return true;
            
        }
        catch(KettleException e)
        {
            new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorOpening.Title"), Messages.getString("Spoon.Dialog.ErrorOpening.Message")+fname, e);
        }
        return false;
    }

    public boolean save(EngineMetaInterface meta, String fname) {
    	Spoon spoon = Spoon.getInstance();
    	return spoon.saveMeta(meta, fname);
    }
}
