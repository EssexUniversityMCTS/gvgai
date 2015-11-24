package tools;

import java.awt.Polygon;
import java.awt.Rectangle;
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
    public static Polygon triPoints(Rectangle rect, Vector2d orientation)
    {
        Vector2d p1 = new Vector2d(rect.getCenterX()+orientation.x*rect.getWidth()/3.0,
                                   rect.getCenterY()+orientation.y*rect.getHeight()/3.0);
        Vector2d p2 = new Vector2d(rect.getCenterX()+orientation.x*rect.getWidth()/4.0,
                                   rect.getCenterY()+orientation.y*rect.getHeight()/4.0);
        Vector2d orthdir = new Vector2d(orientation.y, -orientation.x);

        Vector2d p2a = new Vector2d(p2.x-orthdir.x*rect.getWidth()/6.0,
                                    p2.y-orthdir.y*rect.getHeight()/6.0);
        Vector2d p2b = new Vector2d(p2.x+orthdir.x*rect.getWidth()/6.0,
                                    p2.y+orthdir.y*rect.getHeight()/6.0);

        return new Polygon(new int[]{(int)p1.x, (int)p2a.x, (int)p2b.x},
                           new int[]{(int)p1.y, (int)p2a.y, (int)p2b.y}, 3);
    }


    public static Polygon roundedPoints(Rectangle rect)
    {
        System.out.println("Utils.roundedPoints not implemented yet");
        return null;
    }

    public static Vector2d processMovementActionKeys(boolean[] key_pressed) {

        int vertical = 0;
        int horizontal = 0;


        if (key_pressed[Types.ACTIONS.ACTION_UP.getKey()[0]]) {
            vertical = -1;
        }
        if (key_pressed[Types.ACTIONS.ACTION_DOWN.getKey()[0]]) {
            vertical = 1;
        }


        if (key_pressed[Types.ACTIONS.ACTION_LEFT.getKey()[0]]) {
            horizontal = -1;
        }
        if (key_pressed[Types.ACTIONS.ACTION_RIGHT.getKey()[0]]) {
            horizontal = 1;
        }

        if (horizontal == 0) {
            if (vertical == 1)
                return Types.DOWN;
            else if (vertical == -1)
                return Types.UP;
        } else if (vertical == 0) {
            if (horizontal == 1)
                return Types.RIGHT;
            else if (horizontal == -1)
                return Types.LEFT;
        }
        return Types.NONE;
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
            //System.out.format("Utils.tiebreaker(): WARNING: value equal to epsilon: %f\n",input);
            return (input + epsilon) * (1.0 + epsilon * (random - 0.5));
        }
    }

    public static boolean processUseKey(boolean[] key_pressed)
    {
        return key_pressed[Types.ACTIONS.ACTION_USE.getKey()[0]];
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
}
