package core.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/10/13
 * Time: 07:01
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ParameterContent extends Content
{
    /**
     * Number of points that can be sampled from minValue
     */
    protected int nPoints;

    /**
     * Is final value set
     */
    protected boolean isFinalValueSet;

    /**
     * Debug only: Shows the values given to parameters.
     */
    protected boolean VERBOSE = false;

    /**
     * Default constructor.
     */
    public ParameterContent(){}

    /**
     * Constructor that extracts the contents from a String line
     * @param line String with the contents in VGDL format, to be mapped to the
     *             data structures of this class.
     */
    public ParameterContent(String line)
    {
        this.nPoints = -1;
        this.line = line;

        //Init structures of node content.
        parameters = new HashMap<String, String>();

        //Take the pieces and the first one is the name that defines the content
        String pieces[] = line.split(" ");

        if(pieces.length < 2)
        {
            //This is the ParameterSet line. Just finish here
            identifier = pieces[0].trim();
            return;
        }

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
            }
        }

    }


    public static ParameterContent create(String line)
    {
        ParameterContent pc = new ParameterContent(line);
        if(pc.parameters.size() == 0)
            return pc; //This happens when parsing lines like "ParameterSet"

        String[] valuesToRead = (pc.parameters.get("values")).split(":");

        if(valuesToRead.length == 3)
        {
            if(valuesToRead[0].contains(".") || valuesToRead[1].contains(".") || valuesToRead[2].contains(".")) {
                return new ParameterDoubleContent(pc, line);
            }
            return new ParameterIntContent(pc, line);
        }else{
            //Assuming it's a definition True, False.
            if((valuesToRead[0].equalsIgnoreCase("true") || valuesToRead[0].equalsIgnoreCase("false")) &&
                    (valuesToRead[1].equalsIgnoreCase("true") || valuesToRead[1].equalsIgnoreCase("false"))){
                return new ParameterBoolContent(pc, line);
            }
        }
        return null;
    }

    @Override
    public void decorate(HashMap<String, ParameterContent> pcs) {
        //Nothing to do here (too recursive to be true :-))
    }

    public void init() { }
    public void setRunningValue(int value) { }
    public int getnPoints() {return nPoints;}
    public String getStValue() {return "";}

    public String toString()
    {
        if(parameters.containsKey("string"))
            return parameters.get("string");

        return "Undefined";
    }
    public String values()
    {
        if(parameters.containsKey("values"))
            return parameters.get("values");

        return "Undefined-values";
    }
}
