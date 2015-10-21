/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.alxio;

/**
 *
 * @author ALX
 */
public class Strategus {

    private static int lastTreeSize = 0;

    public static boolean FAIL = false;

    public static double ROLLOUT_CHANCE = 0;
    public static double SIGTH_RADIUS = 1;
    public static double SAME_COUNT_TO_BE_SURE = 2;
    public static double TOTAL_COUNT_TO_BE_SURE = 10;

    public static boolean DETERMINISTIC_CHOICE = false;

    public static int MAX_TREE_SIZE = 4000;

    private static int sight = 109;

    private static double lastShrink = 0;

    private static int lastSame = 0;
    private static int lastOther = 0;
    public static boolean MCTSChoice = false;

    public static void updateStrategy(int treeSize) {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = (long) (0.5 * runtime.maxMemory());

        if (used > max && used > lastShrink) {
            MAX_TREE_SIZE = (int) (0.5 * MAX_TREE_SIZE);
            lastShrink = used;
        }

        if (used > 0.9 * runtime.maxMemory()) {
            FAIL = true;
        }

        if (treeSize > lastTreeSize && treeSize > 3000) {
            sight++;
        } else if (treeSize < lastTreeSize * 0.5
                || (lastTreeSize < 10 && treeSize < 10)
                || (ChildNodes.sameCount - lastSame) < 2 * (ChildNodes.otherCount - lastOther)) {
            sight -= 2;
        }

        SIGTH_RADIUS = Math.pow(0.1 * (sight / 10), 2);

        Debug.log(4, "SIGHT " + SIGTH_RADIUS);
        lastTreeSize = treeSize;

        ROLLOUT_CHANCE = Math.pow(treeSize / MAX_TREE_SIZE, 8);

        Debug.log(4, (ChildNodes.sameCount) + " / " + (ChildNodes.otherCount));

        lastSame = ChildNodes.sameCount;
        lastOther = ChildNodes.otherCount;

        if (ChildNodes.otherCount == 0 && ChildNodes.sameCount > 200) {
            SAME_COUNT_TO_BE_SURE = 1;
            TOTAL_COUNT_TO_BE_SURE = 2;
            DETERMINISTIC_CHOICE = true;
        } else {
            SAME_COUNT_TO_BE_SURE = (10 + treeSize) * (ChildNodes.otherCount + 5) / (ChildNodes.sameCount + 5);
            TOTAL_COUNT_TO_BE_SURE = 10 * SAME_COUNT_TO_BE_SURE;
        }
    }
}
