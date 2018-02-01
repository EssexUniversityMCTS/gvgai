package utilsUI;

import core.game.Observation;
import core.game.StateObservationMulti;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

import static utilsUI.Constants.*;

/**
 * Created by rdgain on 6/28/2017.
 */
public class DrawingAgent {

    public static boolean drawing = false;

    // Drawing
    protected ArrayList<Observation> grid[][];
    protected int block_size, itype;
    public ArrayList<Vector2d> positions, positionsThinking;
    ArrayList<Vector2d> newpos;

    public DrawingAgent(StateObservationMulti stateObs, int id) {
        grid = stateObs.getObservationGrid();
        block_size = stateObs.getBlockSize();
        itype = stateObs.getAvatarType(id);
        positions = new ArrayList<>();
        positionsThinking = new ArrayList<>();
        newpos = new ArrayList<>();
    }

    public void init(StateObservationMulti stateObs, int id) {
        grid = stateObs.getObservationGrid();
        itype = stateObs.getAvatarType();
        positionsThinking = new ArrayList<>();
        updatePos(stateObs.getAvatarPosition(id));
    }

    public void updatePos(Vector2d position) {
        positions.add(position);
    }

    public void updatePosThinking(Vector2d position) {
        positionsThinking.add(position);
    }

    public void draw(Graphics2D g, int drawCode) {
        /**
         * Draw exploration
         */

        if (drawCode == DRAW_EXPLORATION || drawCode == DRAW_ET) {

            g.setColor(new Color(0, 0, 0, 10));
//            g.fillRect(0, 0, grid.length * block_size, grid[0].length * block_size);

            newpos.clear();
            newpos.addAll(positions);
            if (!newpos.isEmpty()) {
                for (Vector2d p : newpos) {
                    g.fillRect((int) p.x, (int) p.y, block_size, block_size);
                }
            }
        }

        /**
         * Draw thinking
         */

        if (drawCode == DRAW_THINKING || drawCode == DRAW_ET) {

            g.setColor(new Color(255, 255, 255, 25));
            newpos.clear();
            newpos.addAll(positionsThinking);
            if (!newpos.isEmpty()) {
                for (Vector2d pos : newpos) {
                    g.fillOval((int) pos.x + block_size / 2, (int) pos.y + block_size / 2, block_size / 2, block_size / 2);
                }
            }
        }
    }
}
