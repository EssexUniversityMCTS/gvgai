package core.vgdl;

import core.competition.CompetitionParameters;
import core.game.Game;
import core.player.LearningPlayer;
import core.player.Player;
import ontology.Types;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
    public SpriteGroup[] spriteGroups;

    /**
     * Player of the game
     */
    public Player player;

    public boolean justImage = false;

    /**
     * Creates the viewer for the game.
     * @param game game to be displayed
     */
    public VGDLViewer(Game game, Player player)
    {
        this.game = game;
        this.size = game.getScreenSize();
        this.player = player;
        if (player instanceof LearningPlayer) {
            LearningPlayer learningPlayer = (LearningPlayer) player;
            Types.LEARNING_SSO_TYPE ssoType = learningPlayer.getLearningSsoType();
            if (ssoType == Types.LEARNING_SSO_TYPE.IMAGE ||
                ssoType == Types.LEARNING_SSO_TYPE.BOTH) {
                saveImage(CompetitionParameters.SCREENSHOT_FILENAME);
            }
        }
    }

    /**
     * Main method to paint the game
     * @param gx Graphics object.
     */
    public void paintComponent(Graphics gx)
    {
        Graphics2D g = (Graphics2D) gx;
        paintWithGraphics(g);
    }

    public void paintWithGraphics(Graphics2D g) {
        //For a better graphics, enable this: (be aware this could bring performance issues depending on your HW & OS).
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //g.setColor(Types.LIGHTGRAY);
        g.setColor(Types.BLACK);
        g.fillRect(0, size.height, size.width, size.height);

        try {
            int[] gameSpriteOrder = game.getSpriteOrder();
            if (this.spriteGroups != null) for (Integer spriteTypeInt : gameSpriteOrder) {
                if (spriteGroups[spriteTypeInt] != null) {
                    ArrayList<VGDLSprite> spritesList = spriteGroups[spriteTypeInt].getSprites();
                    for (VGDLSprite sp : spritesList) {
                        if (sp != null) sp.draw(g, game);
                    }

                }
            }
        }catch(Exception e) {}

        g.setColor(Types.BLACK);
        player.draw(g);
    }



    /**
     * Paints the sprites.
     * @param spriteGroupsGame sprites to paint.
     */
    public void paint(SpriteGroup[] spriteGroupsGame)
    {
        this.spriteGroups = new SpriteGroup[spriteGroupsGame.length];
        for(int i = 0; i < this.spriteGroups.length; ++i)
        {
            this.spriteGroups[i] = new SpriteGroup(spriteGroupsGame[i].getItype());
            this.spriteGroups[i].copyAllSprites(spriteGroupsGame[i].getSprites());
        }
        this.repaint();
        if (player instanceof LearningPlayer) {
            LearningPlayer learningPlayer = (LearningPlayer) player;
            Types.LEARNING_SSO_TYPE ssoType = learningPlayer.getLearningSsoType();

            if (ssoType == Types.LEARNING_SSO_TYPE.IMAGE ||
                ssoType == Types.LEARNING_SSO_TYPE.BOTH) {
                saveImage(CompetitionParameters.SCREENSHOT_FILENAME);
            }
        }
    }

    /**
     * Gets the dimensions of the window.
     * @return the dimensions of the window.
     */
    public Dimension getPreferredSize() {
        return size;
    }

    public void saveImage(String fileName)  {
        try {
            BufferedImage bi = new BufferedImage( (int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = bi.createGraphics();
            paintWithGraphics(graphics);
            File file = new File(fileName);
            if (justImage) {
                graphics.dispose();
            }
            ImageIO.write(bi, "png", file);
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }
}
