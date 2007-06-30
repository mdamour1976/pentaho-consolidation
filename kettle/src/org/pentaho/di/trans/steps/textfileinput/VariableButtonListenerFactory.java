package org.pentaho.di.trans.steps.textfileinput;

import java.util.Arrays;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.widget.GetCaretPositionInterface;
import org.pentaho.di.core.widget.InsertTextInterface;


public class VariableButtonListenerFactory
{
    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination, VariableSpace space)
    {
        return getSelectionAdapter(composite, destination, null, null, space);
    }

    // Listen to the Variable... button
    @SuppressWarnings("unchecked")
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination, final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface, final VariableSpace space)
    {
        return new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) 
            {
            	String keys[] = space.listVariables();
                Arrays.sort(keys);
                
                int size = keys.length;
                String key[] = new String[size];
                String val[] = new String[size];
                String str[] = new String[size];
                
                for (int i=0;i<keys.length;i++)
                {
                    key[i] = keys[i];
                    val[i] = space.getVariable(key[i]);
                    str[i] = key[i]+"  ["+val[i]+"]";
                }
                
                // Before focus is lost, we get the position of where the selected variable needs to be inserted.
                int position=0;
                if (getCaretPositionInterface!=null)
                {
                    position = getCaretPositionInterface.getCaretPosition();
                }
                
                EnterSelectionDialog esd = new EnterSelectionDialog(composite.getShell(), str, Messages.getString("System.Dialog.SelectEnvironmentVar.Title"), Messages.getString("System.Dialog.SelectEnvironmentVar.Message"));
                if (esd.open()!=null)
                {
                    int nr = esd.getSelectionNr();
                    String var = "${"+key[nr]+"}";
                    
                    if (insertTextInterface==null)
                    {
                        destination.insert(var);
                        //destination.setToolTipText(StringUtil.environmentSubstitute( destination.getText() ) );
                        e.doit=false;
                    }
                    else
                    {
                        insertTextInterface.insertText(var, position);
                    }
                }
            }
        };
    }
}