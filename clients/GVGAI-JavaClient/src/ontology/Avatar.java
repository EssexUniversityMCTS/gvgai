package ontology;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Daniel on 04/03/2017.
 */
public class Avatar
{
    public ArrayList<String> actionList = new ArrayList<String>();
    public double[] position = new double[2];
    public double[] orientation = new double[2];
    public double speed = 0;
    public String lastAction = "NIL";
    public HashMap<Integer, Integer> resources = new HashMap<Integer, Integer>();

    public void printToFile(int gameN)
    {
        try{
            String lineSep = System.getProperty("line.separator");
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("javaclient-test-avatar-" + gameN + ".txt")));

            writer.write("Position X: " +  position[0] + ", Position Y: " +  position[1] +
                    ", Orientation X: " +  orientation[0] + ", Orientation Y: " +  orientation[1] +
                    ", Speed: " + speed + ", Last Action: " +  lastAction + lineSep + "Actions: {");

            for (String act : actionList)
            {
                writer.write(act + ",");
            }
            writer.write("}" + lineSep);

            writer.write("Resources: {");
            Set<Integer> keys = resources.keySet();
            for (Integer k : keys)
            {
                writer.write("(" + k + "," + resources.get(k) + ")");
            }
            writer.write("}" + lineSep);

            writer.close();

        }catch (Exception e)
        {

        }
    }
}
