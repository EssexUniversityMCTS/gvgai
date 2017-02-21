package core.content;

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

            if(pcs.containsKey(value))
            {
                ParameterContent pc = pcs.get(value);
                this.parameters.put(parameter, pc.getStValue());
            }
        }
    }


}
