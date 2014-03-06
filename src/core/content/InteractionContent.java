package core.content;

import tools.Utils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/10/13
 * Time: 07:01
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class InteractionContent extends Content
{
    public String object1;
    public String object2;
    public String function;

    /**
     * Default constructor.
     */
    public InteractionContent(){}

    /**
     * Simple constructor that receives an identifier and a reference class
     * @param id1 identifier of the first object in the interaction.
     * @param id2 identifier of the second object in the interaction.
     */
    public InteractionContent(String id1, String id2, String function)
    {
        super();
        this.object1 = id1;
        this.object2 = id2;
        this.function = function;
    }

    /**
     * Constructor that extracts the contents from a String line
     * @param line String with the contents in VGDL format, to be mapped to the
     *             data structures of this class.
     */
    public InteractionContent(String line)
    {
        this.line = line;

        //Init structures of node content.
        parameters = new HashMap<String, String>();

        //Take the pieces and the first one is the name that defines the content
        String pieces[] = line.split(" ");
        object1 = pieces[0].trim();

        if(pieces.length < 2)
        {
            //This is the InteractionSet line. Just finish here
            identifier = pieces[0].trim();
            return;
        }
        object2 = pieces[1].trim();

        //Take the other pieces and extract properties and parameters key-value.
        for(int i = 2; i < pieces.length; ++i)
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
                function = piece; //I'm assuming there is only one function per line... ?
            }
        }
    }
}
