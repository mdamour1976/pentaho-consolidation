package org.pentaho.di.trans;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

public class TransPreviewFactory
{
    public static final TransMeta generatePreviewTransformation(VariableSpace parent, StepMetaInterface oneMeta, String oneStepname)
    {
        StepLoader stepLoader = StepLoader.getInstance();

        TransMeta previewMeta = new TransMeta(parent);
        
        // At it to the first step.
        StepMeta one = new StepMeta(stepLoader.getStepPluginID(oneMeta), oneStepname, oneMeta);
        one.setLocation(50,50);
        one.setDraw(true);
        previewMeta.addStep(one);
        
        DummyTransMeta twoMeta = new DummyTransMeta();
        StepMeta two = new StepMeta(stepLoader.getStepPluginID(twoMeta), "dummy", twoMeta); //$NON-NLS-1$
        two.setLocation(250,50);
        two.setDraw(true);
        previewMeta.addStep(two);
        
        TransHopMeta hop = new TransHopMeta(one, two);
        previewMeta.addTransHop(hop);
        
        return previewMeta;
    }
}
