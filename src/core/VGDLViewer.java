package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import core.game.Game;
import core.player.AbstractPlayer;
import core.player.Player;
import ontology.Types;

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

    /**
    * Paint on BufferedImage
    */
    private BufferedImage mapImage;

    /**
     * Creates the viewer for the game.
     * @param game game to be displayed
     */
    public VGDLViewer(Game game, Player player)
    {
        this.game = game;
        this.size = game.getScreenSize();
        this.player = player;
        this.mapImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_3BYTE_BGR);
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

        // Following part added by Jialin for learning track test // TODO: 19/10/2016
//        g.drawImage(mapImage, 0, 0, null);
    }

    /**
    * Save the game state to png file
    * @throws IOException
    */
    public File saveToFile() {
        Graphics2D graphics = mapImage.createGraphics();
        this.paintComponent(graphics);
        File file = new File("gameStateAtT.png");
        try {
            ImageIO.write(mapImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Save the game state to byte array
     * @throws IOException
     */
    public byte[] saveToByte() {
        Graphics2D graphics = mapImage.createGraphics();
        System.out.println( mapImage.getWidth() + " " +  mapImage.getHeight());
        this.paintComponent(graphics);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(mapImage, "png", baos);
            ImageIO.write(mapImage, "png", new File("game.png"));
            baos.flush();
            byte[] bytes = baos.toByteArray();
            baos.close();
            return compress(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    /**
     * Save the game state to png file
     * @throws IOException
     */
    public int[][] save() {
        Graphics2D graphics = mapImage.createGraphics();
        this.paintComponent(graphics);
        int[][] pixels = convertTo2DWithoutUsingGetRGB(mapImage);
//        int height = pixels.length;
//        int width = pixels[0].length;
//        final BufferedImage image =
//            new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//
//        System.out.println( width + " " + height);
//        for (int y = 0; y < height; ++y) {
//            for (int x = 0; x < width; ++x) {
//
//                image.setRGB(x, y, pixels[y][x]);
//            }
//        }
//
//        try {
//            ImageIO.write(image, "png", new File("./gamestate2.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return pixels;
    }

    /**
     * Paints the sprites.
     * @param spriteGroupsGame sprites to paint.
     */
    public void paint(SpriteGroup[] spriteGroupsGame)
    {
        //this.spriteGroups = spriteGroupsGame;
        this.spriteGroups = new SpriteGroup[spriteGroupsGame.length];
        for(int i = 0; i < this.spriteGroups.length; ++i)
        {
            this.spriteGroups[i] = new SpriteGroup(spriteGroupsGame[i].getItype());
            this.spriteGroups[i].copyAllSprites(spriteGroupsGame[i].getSprites());
        }

        this.repaint();


    }

    /**
     * Gets the dimensions of the window.
     * @return the dimensions of the window.
     */
    public Dimension getPreferredSize() {
        return size;
    }


  /**
   * Code from stackOverFlow
   * @param image
   * @return
   */
  public int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

}