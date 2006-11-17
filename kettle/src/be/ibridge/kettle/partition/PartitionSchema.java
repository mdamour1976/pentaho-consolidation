package be.ibridge.kettle.partition;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;

/**
 * A partition schema allow you to partition a step according into a number of partitions that run independendly.
 * It allows us to "map" 
 * 
 * @author Matt
 *
 */
public class PartitionSchema extends ChangedFlag implements Cloneable
{
    public static final String XML_TAG = "partitionschema";

    private String   name;

    private String[] partitionIDs;

    public PartitionSchema()
    {
        partitionIDs=new String[] {};
    }
    
    /**
     * @param name
     * @param partitionIDs
     */
    public PartitionSchema(String name, String[] partitionIDs)
    {
        this.name = name;
        this.partitionIDs = partitionIDs;
    }

    public Object clone()
    {
        String[] ids = new String[partitionIDs.length];
        for (int i=0;i<ids.length;i++) ids[i] = partitionIDs[i];
        
        return new PartitionSchema(name, ids);
    }
    
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object obj)
    {
        if (obj==null) return false;
        return name.equals(((PartitionSchema)obj).name);
    }

    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the partitionIDs
     */
    public String[] getPartitionIDs()
    {
        return partitionIDs;
    }

    /**
     * @param partitionIDs the partitionIDs to set
     */
    public void setPartitionIDs(String[] partitionIDs)
    {
        this.partitionIDs = partitionIDs;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("        <"+XML_TAG+">"+Const.CR);
        xml.append("          "+XMLHandler.addTagValue("name", name));
        for (int i=0;i<partitionIDs.length;i++)
        {
        xml.append("          <partition>");
        xml.append("            "+XMLHandler.addTagValue("id", partitionIDs[i]));
        xml.append("            </partition>");
        }
        xml.append("          </"+XML_TAG+">"+Const.CR);
        return xml.toString();
    }
    
    public PartitionSchema(Node partitionSchemaNode)
    {
        name = XMLHandler.getTagValue(partitionSchemaNode, "name");
        
        int nrIDs = XMLHandler.countNodes(partitionSchemaNode, "partition");
        partitionIDs = new String[nrIDs];
        for (int i=0;i<nrIDs;i++)
        {
            Node partitionNode = XMLHandler.getSubNodeByNr(partitionSchemaNode, "partition", i);
            partitionIDs[i] = XMLHandler.getTagValue(partitionNode, "id");
        }
    }

}
