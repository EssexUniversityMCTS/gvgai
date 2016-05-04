package core.content;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/10/13
 * Time: 07:01
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class MappingContent extends Content
{
    /**
     * Object(s) this content refers to.
     */
    public ArrayList<String> reference;

    /**
     * Character that identifies this content in the level map.
     */
    public Character charId;


    /**
     * Default constructor.
     */
    public MappingContent(){}

    /**
     * Constructor that extracts the contents from a String line
     * @param line String with the contents in VGDL format, to be mapped to the
     *             data structures of this class.
     */
    public MappingContent(String line)
    {
        this.line = line;

        //Init structures of node content.
        parameters = new HashMap<String, String>();

        //Take the pieces and the first one is the name that defines the content
        String pieces[] = line.split(" ");

        if(pieces.length < 2)
        {
            //This is the LevelMapping line. Just finish here
            identifier = pieces[0].trim();
            return;
        }

        identifier = pieces[0].trim();
        charId = identifier.charAt(0);

        reference = new ArrayList<String>();
        for(int i = 2; i < pieces.length; ++i)
        {
            String spriteType = pieces[i].trim();
            reference.add(spriteType);
        }
    }


}
