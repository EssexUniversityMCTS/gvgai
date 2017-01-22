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
     * Minimum value that this parameter can take.
     */
    private double minValue;

    /**
     * Maximum value that this parameter can take.
     */
    private double maxValue;

    /**
     * Value for this parameter.
     */
    private double incValue;

    /**
     * Number of points that can be sampled from minValue
     */
    private int nPoints;

    /**
     * Is the parameter an integer
     */
    public boolean isInt;


    /**
     * Debug only: Shows the values given to parameters.
     */
    private boolean VERBOSE = true;

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
        this.isInt = true;
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

    /**
     * Returns a value that this Parameter Content allows. If a value is defined in a parameter 'value',
     * it returns that value. If not, samples from the defined possibilities.
     * @return a value for a parameter.
     */
    public double getValue()
    {
        if(parameters.containsKey("value"))
            return Double.parseDouble(parameters.get("value"));

        //ELSE: Need to sample. We might have not worked through the range values
        if(nPoints == -1)
        {
            //Process this:
            String[] valuesToRead = (parameters.get("values")).split(":");

            if(valuesToRead[0].contains(".") || valuesToRead[1].contains(".") || valuesToRead[2].contains(".")) {
                this.isInt = false;
            }

            minValue = Double.parseDouble(valuesToRead[0]);
            incValue = Double.parseDouble(valuesToRead[1]);
            maxValue = Double.parseDouble(valuesToRead[2]);

            nPoints = 1 + (int)((maxValue - minValue) / incValue);
        }

        //We DO NOT assign the value, only return it. So, if it's not specified in VGDL, it's random ALWAYS.
        int samplePoint = new Random().nextInt(nPoints);
        double finalValue = minValue + samplePoint*incValue;

        if(VERBOSE)
            System.out.println("PARAMETER " + parameters.get("string") + ": " + finalValue);

        return finalValue;
    }



    @Override
    public void decorate(HashMap<String, ParameterContent> pcs) {
        //Nothing to do here (too recursive to be true :-))
    }
}
