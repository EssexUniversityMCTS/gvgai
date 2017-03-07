package ontology;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created by Daniel on 04/03/2017.
 */
public class Game
{
    public double score = 0.0;
    public int gameTick = -1;
    public String gameWinner = "NO_WINNER";
    public boolean gameOver = false;
    public Dimension worldDim = new Dimension(0,0);
    public int blockSize = 0;
    public int remMillis = 0;

    public HashMap<Integer, BitGrid> grid = new HashMap<Integer, BitGrid>();

    public void printToFile(int gameN)
    {
        try{
            String lineSep = System.getProperty("line.separator");
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("javaclient-test-game-" + gameN + ".txt")));

            writer.write("Score " + score + ", GameTick: " + gameTick + ", Winner: " + gameWinner
                    + ", GameOver: " + gameOver+ ", WorldDimW: " + worldDim.width + ", WorldDimW: " + worldDim.height
                    + ", BlockSize: " + blockSize + ", RemMillis: " + remMillis + lineSep);

            Set<Integer> keys = grid.keySet();
            for(Integer k : keys)
            {
                writer.write("Sprite Type: " + k + " bit array: " + lineSep);
                BitGrid bg = grid.get(k);
                for(int r = 0; r < bg.grid.length; ++r)
                {
                    for (int c = 0; c < bg.grid[r].length; ++c)
                        writer.write(bg.grid[r][c] ? '1' : '0');
                    writer.write(lineSep);
                }
            }

            writer.close();

        }catch (Exception e)
        {

        }
    }
}
