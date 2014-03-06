package core;

import core.game.Game;
import ontology.Types;
import ontology.sprites.missile.Missile;
import ontology.sprites.npc.RandomNPC;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 24/10/13
 * Time: 10:54
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class VGDLViewer extends JComponent
{
    /**
     * Reference to the game to be painted.
     */
    public Game game;

    /**
     * Dimensions of the window.
     */
    private Dimension size;

    /**
     * Sprites to draw
     */
    public ArrayList<VGDLSprite>[] spriteGroups;

    /**
     * Creates the viewer for the game.
     * @param game game to be displayed
     */
    public VGDLViewer(Game game)
    {
        this.game = game;
        this.size = game.getScreenSize();
    }

    /**
     * Main method to paint the game
     * @param gx Graphics object.
     */
    public void paintComponent(Graphics gx)
    {
        Graphics2D g = (Graphics2D) gx;

        //For a better graphics, enable this: (be aware this could bring performance issues depending on your HW & OS).
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //g.setColor(Types.LIGHTGRAY);
        g.setColor(Types.BLACK);
        g.fillRect(0, size.height, size.width, size.height);

        //Possible efficiency improvement: static image with immovable objects.
        /*
        BufferedImage mapImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D gImage = mapImage.createGraphics();
        */

        int[] gameSpriteOrder = game.getSpriteOrder();
        if(this.spriteGroups!= null) for(Integer spriteTypeInt : gameSpriteOrder)
        {
            ArrayList<VGDLSprite> sprites =  this.spriteGroups[spriteTypeInt];

            if(sprites != null)
            {
                try
                {
                    int numSprites = sprites.size();
                    for(int j = 0; j < numSprites; j++)
                    {
                        VGDLSprite sp = sprites.get(j);
                        sp.draw(g, game);
                    }

                }catch(Exception e)
                {
                    //System.out.println("Exception while drawing.");
                }
            }
        }
    }

    /**
     * Paints the sprites.
     * @param spriteGroupsGame sprites to paint.
     */
    public void paint(ArrayList<VGDLSprite>[] spriteGroupsGame)
    {
        spriteGroups = spriteGroupsGame;
        this.repaint();
    }

    /**
     * Gets the dimensions of the window.
     * @return the dimensions of the window.
     */
    public Dimension getPreferredSize() {
        return size;
    }

}
