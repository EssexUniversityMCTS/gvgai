package core.content;

import java.util.ArrayList;
import java.util.HashMap;

import core.vgdl.VGDLRegistry;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/10/13
 * Time: 07:01
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class SpriteContent extends Content
{

    /**
     * Class of the element defined by this content.
     * It is only assigned when processing the tree of contents.
     */
    public String referenceClass;

    /**
     * Indicates the int - types of the sprite (hierarchical identifiers). From the type
     * till the top of the hierarchy.
     */
    public ArrayList<Integer> itypes;


    /**
     * All types under this type in the hierarchy, including itself.
     */
    public ArrayList<Integer> subtypes;


    /**
     * Default constructor.
     */
    public SpriteContent(){
        itypes = new ArrayList<Integer>();
        subtypes = new ArrayList<Integer>();
    }

    /**
     * Simple constructor that receives and identifier and a reference class
     * @param id object identifier.
     * @param refClass class this object is mapped to.
     */
    public SpriteContent(String id, String refClass)
    {
        this.line = null;
        this.referenceClass = refClass;
        this.identifier = id;
        parameters = new HashMap<String, String>();
        itypes = new ArrayList<Integer>();
        subtypes = new ArrayList<Integer>();
    }

    /**
     * Constructor that extracts the contents from a String line
     * @param line String with the contents in VGDL format, to be mapped to the
     *             data structures of this class.
     */
    public SpriteContent(String line)
    {
        this.referenceClass = null;
        this.line = line;
        parameters = new HashMap<String, String>();
        itypes = new ArrayList<Integer>();
        subtypes = new ArrayList<Integer>();

        //Init structures of node content.
        parameters = new HashMap<String, String>();

        //Take the pieces and the first one is the name that defines the content
        String pieces[] = line.split(" ");
        identifier = pieces[0].trim();

        //Take the other pieces and extract properties and parameters key-value.
        for(int i = 1; i < pieces.length; ++i)
        {
            String piece = pieces[i].trim();
            if(piece.contains("="))
            {
                String keyValue[] = piece.split("=");
                String key = keyValue[0];
                String value = keyValue[1];

                parameters.put(key, value);
            }else if(piece.equals(">"))
            {
                this.is_definition = true;
            }else if(piece.length() > 0){
                referenceClass = piece; //I don't expect more than one keyword alone in the line... ?
            }
        }
    }

    public void assignTypes(ArrayList<String> types)
    {
        if(itypes == null)
            itypes = new ArrayList<Integer>();

        for(String stype : types)
        {
            int itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
            this.itypes.add(itype);
        }
    }

    /**
     * Returns the original line of the content.
     * @return original line, in VGDL format.
     */
    @Override
    public String toString()
    {
        String line = "[" + identifier + ":" + referenceClass + "]";
        return line;
    }


    @Override
    public void decorate(HashMap<String, ParameterContent> pcs) {
        super._decorate(pcs);
    }
}
