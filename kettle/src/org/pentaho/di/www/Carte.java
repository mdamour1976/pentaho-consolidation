package org.pentaho.di.www;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

public class Carte
{
    public static void main(String[] args) throws Exception
    {
        if (args.length<2 || (Const.isEmpty(args[0]) && Const.isEmpty(args[1])) )
        {
            System.err.println("Usage: Carte [Interface address] [Port]");
            System.err.println();
            System.err.println("Example1: Carte 127.0.0.1 8080");
            System.err.println("Example2: Carte 192.168.1.221 8081");
            System.exit(1);
        }
        
        init();
        
        TransformationMap map = new TransformationMap(Thread.currentThread().getName());
        
        Trans trans = generateTestTransformation();
        map.addTransformation(trans.getName(), trans, new TransConfiguration(trans.getTransMeta(), new TransExecutionConfiguration()));
        
        String hostname = args[0];
        int port = WebServer.PORT;
        if (args.length>=2)
        {
            try
            {
                port = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                System.out.println("Unable to parse port ["+args[0]+"], using port ["+port+"]");
            }
        }
        new WebServer(map, hostname, port);
    }

    private static void init() throws Exception
    {
        EnvUtil.environmentInit();
        LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
        
        StepLoader stepLoader = StepLoader.getInstance();
        if (!stepLoader.read())
        {
            throw new Exception("Unable to load steps & plugins");
        }
    }
    
    public static Trans generateTestTransformation()
    {
        RowGeneratorMeta A = new RowGeneratorMeta();
        A.allocate(3);
        A.setRowLimit("100000000");

        A.getFieldName()[0]   = "ID"; 
        A.getFieldType()[0]   = ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_INTEGER);
        A.getFieldLength()[0] = 7; 
        A.getValue()[0]       = "1234"; 
        
        A.getFieldName()[1]   = "Name"; 
        A.getFieldType()[1]   = ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_STRING);
        A.getFieldLength()[1] = 35; 
        A.getValue()[1]       = "Some name"; 

        A.getFieldName()[2]   = "Last updated"; 
        A.getFieldType()[2]   = ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_DATE);
        A.getFieldFormat()[2] = "yyyy/MM/dd"; 
        A.getValue()[2]       = "2006/11/13"; 

        TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation(null, A, "A");
        transMeta.setName("Row generator test");
        transMeta.setSizeRowset(2500);
        transMeta.setFeedbackSize(50000);
        transMeta.setUsingThreadPriorityManagment(false);

        return new Trans(transMeta);
        
    }
}
