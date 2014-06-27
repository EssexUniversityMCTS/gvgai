package tools;

import ontology.Types;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 17/10/13 Time: 12:13 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public final class Utils {
  private static final Pattern COMPILE = Pattern.compile("(?m)^\\s|\\s$");

  private Utils() {
  }

  public static Object choice(Object[] elements, Random rnd) {
    return elements[rnd.nextInt(elements.length)];
  }

  public static Vector2d choice(List<Vector2d> elements, Random rnd) {
    return elements.get(rnd.nextInt(elements.size()));
  }

  public static String formatString(String str) {
    // 1st replaceAll: compresses all non-newline whitespaces to single
    // space
    // 2nd replaceAll: removes spaces from beginning or end of lines
    return COMPILE.matcher(str.replaceAll("[\\s&&[^\\n]]+", " ")).replaceAll("");
  }

  /**
   * Returns the Polygon for a triangle in the middle of the provided rect, pointing in the orientation (given as angle
   * from upwards, or orientation vector)
   * 
   * @param rect rectangle with the location
   * @param orientation orientation of the sprite.
   * @return a polygon (triangle) with the specified orientation.
   */
  public static Polygon triPoints(Rectangle rect, Vector2d orientation) {
    Vector2d p1 =
        new Vector2d(rect.getCenterX() + orientation.x * rect.getWidth() / 3.0, rect.getCenterY()
            + orientation.y * rect.getHeight() / 3.0);
    Vector2d p2 =
        new Vector2d(rect.getCenterX() + orientation.x * rect.getWidth() / 4.0, rect.getCenterY()
            + orientation.y * rect.getHeight() / 4.0);
    Vector2d orthdir = new Vector2d(orientation.y, -orientation.x);

    Vector2d p2a =
        new Vector2d(p2.x - orthdir.x * rect.getWidth() / 6.0, p2.y - orthdir.y * rect.getHeight()
            / 6.0);
    Vector2d p2b =
        new Vector2d(p2.x + orthdir.x * rect.getWidth() / 6.0, p2.y + orthdir.y * rect.getHeight()
            / 6.0);

    return new Polygon(new int[] {(int) p1.x, (int) p2a.x, (int) p2b.x}, new int[] {
        (int) p1.y, (int) p2a.y, (int) p2b.y}, 3);
  }

  public static Polygon roundedPoints(Rectangle rect) {
    System.out.println("Utils.roundedPoints not implemented yet");
    return null;
  }

  public static Vector2d processMovementActionKeys(boolean... key_pressed) {

    int vertical = 0;

    if (key_pressed[Types.ACTIONS.ACTION_UP.getKey()[0]]) {
      vertical = -1;
    }
    if (key_pressed[Types.ACTIONS.ACTION_DOWN.getKey()[0]]) {
      vertical = 1;
    }

    int horizontal = 0;
    if (key_pressed[Types.ACTIONS.ACTION_LEFT.getKey()[0]]) {
      horizontal = -1;
    }
    if (key_pressed[Types.ACTIONS.ACTION_RIGHT.getKey()[0]]) {
      horizontal = 1;
    }

    if (0 == horizontal) {
      if (1 == vertical)
        return Types.DOWN;
      else if (-1 == vertical)
        return Types.UP;
    } else if (0 == vertical) {
      if (1 == horizontal)
        return Types.RIGHT;
      else if (-1 == horizontal)
        return Types.LEFT;
    }
    return Types.NONE;
  }

  // Normalizes a value between its MIN and MAX.
  public static double normalise(double a_value, double a_min, double a_max) {
    return (a_value - a_min) / (a_max - a_min);
  }

  public static boolean processUseKey(boolean... key_pressed) {
    return key_pressed[Types.ACTIONS.ACTION_USE.getKey()[0]];
  }

  public static int argmax(double... values) {
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
