package core.content;

import java.util.Random;

/**
 * Created by dperez on 21/02/2017.
 */
public class ParameterIntContent extends ParameterContent {


    /**
     * Minimum value that this parameter can take.
     */
    private int minValue;

    /**
     * Maximum value that this parameter can take.
     */
    private int maxValue;

    /**
     * Value for this parameter.
     */
    private int incValue;

    /**
     * Final value
     */
    private int finalValue;


    public ParameterIntContent(ParameterContent pc, String line) {
        this.line = line;
        this.parameters = pc.parameters;
        this.identifier = pc.identifier;
        this.is_definition = pc.is_definition;
        init();
    }

    public void init()
    {
        String[] valuesToRead = (parameters.get("values")).split(":");

        minValue = Integer.parseInt(valuesToRead[0]);
        incValue = Integer.parseInt(valuesToRead[1]);
        maxValue = Integer.parseInt(valuesToRead[2]);

        nPoints = 1 + (int)((double)(maxValue - minValue) / incValue);

        isFinalValueSet = false;
    }

    public String getStValue()
    {
        String param = "";
        if(parameters.containsKey("value"))
            return param + Integer.parseInt(parameters.get("value"));

        if(isFinalValueSet)
            return param + finalValue;

        //We might have not worked through the range values
        if(nPoints == -1)
        {
            init();
        }

        //We DO NOT assign the value, only return it.
        int samplePoint = new Random().nextInt(nPoints);
        int randomValue = minValue + samplePoint*incValue;

        //if(VERBOSE)
        //    System.out.println("PARAMETER " + this + " set to a sampled value: " + randomValue);

        return (param + randomValue);
    }

    public void setRunningValue(int value)
    {
        finalValue = minValue + value * incValue;
        if(!(finalValue >= minValue && finalValue <= maxValue))
            throw new RuntimeException("finalValue=" + finalValue + " outside [" + minValue + "," + maxValue + "] range");

        if(VERBOSE)
            System.out.println("PARAMETER " + this + " set to a FINAL value: " + finalValue);

        isFinalValueSet = true;
    }

}
