package core.content;

import java.util.Random;

/**
 * Created by dperez on 21/02/2017.
 */
public class ParameterBoolContent extends ParameterContent {


    /**
     * Boolean values
     */
    public boolean[] bValues;

    /**
     * Final boolean value
     */
    public boolean finalBooleanValue;



    public ParameterBoolContent(ParameterContent pc, String line) {
        this.line = line;
        this.parameters = pc.parameters;
        this.identifier = pc.identifier;
        this.is_definition = pc.is_definition;
        init();
    }

    public void init()
    {
        String[] valuesToRead = (parameters.get("values")).split(":");
        bValues = new boolean[]{valuesToRead[0].equalsIgnoreCase("true"), valuesToRead[1].equalsIgnoreCase("true")};
        if(bValues[0] != bValues[1])
            nPoints = 2;
        else nPoints = 1;

        isFinalValueSet = false;
    }

    public String getStValue()
    {
        String param = "";
        if(parameters.containsKey("value"))
            return param + Boolean.parseBoolean(parameters.get("value"));

        if(isFinalValueSet)
            return param + finalBooleanValue;

        //We might have not worked through the range values
        if(nPoints == -1)
        {
            init();
        }

        //We DO NOT assign the value, only return it.
        int samplePoint = new Random().nextInt(nPoints);
        boolean randomValue = bValues[samplePoint];

//        if(VERBOSE)
//            System.out.println("PARAMETER " + this + " set to a sampled value: " + randomValue);

        return (param + randomValue);
    }

    public void setRunningValue(int value)
    {
        finalBooleanValue = (value == 1);

        if(!(value >= 0 && value <= 1))
            throw new RuntimeException("finalValue=" + finalBooleanValue + " outside range [0,1]");

        if(super.VERBOSE)
            System.out.println("PARAMETER " + this + " set to a FINAL value: " + finalBooleanValue);
        isFinalValueSet = true;
    }

}
