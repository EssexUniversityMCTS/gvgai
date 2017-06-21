package core.content;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/10/13
 * Time: 07:01
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class GameContent extends Content
{

    /**
     * Class of the element defined by this content.
     * It is only assigned when processing the tree of contents.
     */
    public String referenceClass;

    /**
     * Default constructor.
     */
    public GameContent(){}



    /**
     * Constructor that extracts the contents from a String line
     * @param line String with the contents in VGDL format, to be mapped to the
     *             data structures of this class.
     */
    public GameContent(String line)
    {
        super();
        this.referenceClass = null;
        this.line = line;
        this.is_definition = false;

        //Init structures of node content.
        parameters = new HashMap<String, String>();

        //Take the pieces and the first one is the name that defines the content
        String pieces[] = line.split(" ");
        referenceClass = pieces[0].trim();

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
            }
        }
    }


    @Override
    public void decorate(HashMap<String, ParameterContent> pcs) {
        //Nothing to do here, for the moment.
    }

}
