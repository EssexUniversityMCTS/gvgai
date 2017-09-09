package tools;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import ontology.Types;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 17/10/13
 * Time: 12:13
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Utils
{
    public static Object choice(Object[] elements, Random rnd)
    {
        return elements[rnd.nextInt(elements.length)];
    }

    public static int choice(int[] elements, Random rnd)
    {
        return elements[rnd.nextInt(elements.length)];
    }

    public static Direction choiceDir(ArrayList<Direction> elements, Random rnd)
    {
        return elements.get(rnd.nextInt(elements.size()));
    }

    public static Vector2d choice(ArrayList<Vector2d> elements, Random rnd)
    {
        return elements.get(rnd.nextInt(elements.size()));
    }

    public static String formatString(String str)
    {
        // 1st replaceAll: compresses all non-newline whitespaces to single space
        // 2nd replaceAll: removes spaces from beginning or end of lines
        return str.replaceAll("[\\s&&[^\\n]]+", " ").replaceAll("(?m)^\\s|\\s$", "");
    }

    /**
     *  Returns the Polygon for a triangle in the middle of the provided
     *  rect, pointing in the orientation
     *  (given as angle from upwards, or orientation vector)
     * @param rect rectangle with the location
     * @param orientation orientation of the sprite.
     * @return a polygon (triangle) with the specified orientation.
     */
    public static Polygon triPoints(Rectangle rect, Direction orientation)
    {
        Vector2d p1 = new Vector2d(rect.getCenterX()+orientation.x()*rect.getWidth()/3.0,
                                   rect.getCenterY()+orientation.y()*rect.getHeight()/3.0);
        Vector2d p2 = new Vector2d(rect.getCenterX()+orientation.x()*rect.getWidth()/4.0,
                                   rect.getCenterY()+orientation.y()*rect.getHeight()/4.0);
        Vector2d orthdir = new Vector2d(orientation.y(), -orientation.x());

        Vector2d p2a = new Vector2d(p2.x-orthdir.x*rect.getWidth()/6.0,
                                    p2.y-orthdir.y*rect.getHeight()/6.0);
        Vector2d p2b = new Vector2d(p2.x+orthdir.x*rect.getWidth()/6.0,
                                    p2.y+orthdir.y*rect.getHeight()/6.0);

        return new Polygon(new int[]{(int)p1.x, (int)p2a.x, (int)p2b.x},
                           new int[]{(int)p1.y, (int)p2a.y, (int)p2b.y}, 3);
    }


    public static Polygon roundedPoints(Rectangle rect)
    {
        System.out.println("utils.roundedPoints not implemented yet");
        return null;
    }

    public static Direction processMovementActionKeys(boolean[] key_pressed, int idx) {

        int vertical = 0;
        int horizontal = 0;

        if (key_pressed[Types.ACTIONS.ACTION_UP.getKey()[idx]]) {
            vertical = -1;
        }
        if (key_pressed[Types.ACTIONS.ACTION_DOWN.getKey()[idx]]) {
            vertical = 1;
        }


        if (key_pressed[Types.ACTIONS.ACTION_LEFT.getKey()[idx]]) {
            horizontal = -1;
        }
        if (key_pressed[Types.ACTIONS.ACTION_RIGHT.getKey()[idx]]) {
            horizontal = 1;
        }

        if (horizontal == 0) {
            if (vertical == 1)
                return Types.DDOWN;
            else if (vertical == -1)
                return Types.DUP;
        } else if (vertical == 0) {
            if (horizontal == 1) {
                return Types.DRIGHT;
            }
            else if (horizontal == -1)
                return Types.DLEFT;
        }
        return Types.DNONE;
    }

    //Normalizes a value between its MIN and MAX.
    public static double normalise(double a_value, double a_min, double a_max)
    {
        if(a_min < a_max)
            return (a_value - a_min)/(a_max - a_min);
        else    // if bounds are invalid, then return same value
            return a_value;
    }



    /**
     * Adds a small noise to the input value.
     * @param input value to be altered
     * @param epsilon relative amount the input will be altered
     * @param random random variable in range [0,1]
     * @return epsilon-random-altered input value
     */
    public static double noise(double input, double epsilon, double random)
    {
        if(input != -epsilon) {
            return (input + epsilon) * (1.0 + epsilon * (random - 0.5));
        }else {
            //System.out.format("utils.tiebreaker(): WARNING: value equal to epsilon: %f\n",input);
            return (input + epsilon) * (1.0 + epsilon * (random - 0.5));
        }
    }

    public static boolean processUseKey(boolean[] key_pressed, int idx)
    {
        return key_pressed[Types.ACTIONS.ACTION_USE.getKey()[idx]];
    }

    public static int argmax (double[] values)
    {
        int maxIndex = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            double elem = values[i];
            if (elem > max) {
                max = elem;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static String toStringArray(String[] array)
    {
        if (array != null && array.length > 0) {
            StringBuilder nameBuilder = new StringBuilder();

            for (String elem : array)
                nameBuilder.append(elem).append(",");

            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            return nameBuilder.toString();
        } else {
            return "";
        }
    }

    public static int findMaxDivisor(int value) {
        int divisor = 1;
        for (int i=1; i<=Math.sqrt(value)+1; i++) {
            if (value % i == 0) {
                divisor = i;
            }
        }
        return divisor;
    }

    /**
     * Processes a file with game names (containing full path).
     * Assumes that the input is csv, with each line with the format:
     *   N, fullPath
     * Where N is the index and fullPath the full path to the file, including extension (.txt).
     * @returns double String array, such as [fullPath][gameName]
     */
    public static String[][] readGames(String filename)
    {
        String[] lines = new IO().readFile(filename);
        String[][] allGames = new String[lines.length][2];
        int idx = 0;

        for(String line : lines)
        {
            String path = line.split(",")[1];
            int loc = path.lastIndexOf("/");
            String gameName = path.substring(loc + 1, path.length() - 4);

            allGames[idx][0] = path;
            allGames[idx][1] = gameName;

            idx++;
        }

        return allGames;
    }

}
