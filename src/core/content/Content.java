package core.content;

import tools.Utils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 16/10/13
 * Time: 14:08
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public abstract class Content
{
    /**
     * Original line with the content, in VGDL format.
     */
    public String line;
    
    /**
     * Original line number.
     */
    public int lineNumber;
    /**
     * Main definition of the content.
     * It is always the first word of each line in VGDL
     */
    public String identifier;

    /**
     * List of parameters of this content (key => value).
     * List of all pairs of the form key=value on the line.
     */
    public HashMap<String, String> parameters;

    /**
     * Indicates if this content is definition (i.e., includes character ">" in VGDL).
     */
    public boolean is_definition;

    /**
     * Returns the original line of the content.
     * @return original line, in VGDL format.
     */
    public String toString() {  return line; }


    /**
     * Takes a ParameterContent object to decorate the current Content object.
     * @param pcs ParameterContent hashmap that defines values for variables specified in a GameSpace.
     */
    public abstract void decorate (HashMap<String, ParameterContent> pcs);


    protected void _decorate(HashMap<String, ParameterContent> pcs)
    {
        for (String parameter : this.parameters.keySet()) {
            String value = this.parameters.get(parameter);
            String[] tokens = value.split(",");
            String[] allValues = new String[tokens.length];
            int idx = 0;


            //For compatibility with N players, this might have more than one value.
            String[] values = value.split(",");
            String builtStValue = "";
            for(int i = 0; i < values.length; ++i)
            {
                String v = values[i];
                if(pcs.containsKey(v)) //Try to decode this parameter
                    builtStValue +=  pcs.get(v).getStValue();
                else //If not, we leave it there (it'll fail later, but good for quickly find the error).
                    builtStValue += v;

                if(i < values.length-1)
                    builtStValue += ","; //We want the exact number of comas here.
            }

            if(builtStValue.length() > 0)
                this.parameters.put(parameter, builtStValue);

        }
    }


}
