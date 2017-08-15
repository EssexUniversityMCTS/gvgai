package core.game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import core.competition.CompetitionParameters;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import core.logging.Logger;
import core.logging.Message;
import core.content.GameContent;
import tools.IO;
import tools.Vector2d;
import tools.pathfinder.PathFinder;

import static tools.Utils.findMaxDivisor;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 16/10/13 Time: 14:00 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class BasicGame extends Game {

	/**
	 * Allows definition of sprite size from the VGDL description. If indicated,
	 * super.block_size is set to this variable. square_size should be divisible
	 * by all speeds in the game definition.
	 */
	public int square_size;

	/**
	 * List of sprites that should not be traversable for the pathfinder. This
	 * list can be specified with sprite string identifiers separated by commas.
	 */
	public String obs;

	// List of IDs of the sprites should not be traversable for the pathfinder.
	private ArrayList<Integer> obstacles;

	/**
	 * Default constructor for a basic game.
	 *
	 * @param content
	 *            Contains parameters for the game.
	 */
	public BasicGame(GameContent content) {
		super();

		// Add here whatever mappings are common for all BasicGames.
		charMapping.put('w', new ArrayList<String>());
		charMapping.get('w').add("wall");

		charMapping.put('A', new ArrayList<String>());
		charMapping.get('A').add("avatar");

		// Default values for frame rate and maximum number of sprites allowed.
		square_size = -1;
		MAX_SPRITES = 10000;

		// Parse the arguments.
		this.parseParameters(content);
	}

	/**
	 * Builds a level, receiving a file name.
	 *
	 * @param gamelvl
	 *            file name containing the level.
	 */
	public void buildLevel(String gamelvl, int randomSeed) {
		String[] lines = new IO().readFile(gamelvl);

		// Pathfinder
		obstacles = new ArrayList<>();
		boolean doPathf = false;

		if (obs != null) {
			doPathf = true;
			int obsArray[] = VGDLRegistry.GetInstance().explode(obs);
			for (Integer it : obsArray)
				obstacles.add(it);
		}

		if (doPathf)
			pathf = new PathFinder(obstacles);

		buildStringLevel(lines, randomSeed);

		if (doPathf) {
			long t = System.currentTimeMillis();

			pathf.run(this.getObservation());
			System.out.println(System.currentTimeMillis() - t);
		}
	}

	@Override
	/**
	 * Builds a level from this game, reading it from file.
	 *
	 * @param gamelvl
	 *            filename of the level to load.
	 */
	public void buildStringLevel(String[] lines, int randomSeed) {
		// Read the level description
		String[] desc_lines = lines;

		// Dimensions of the level read from the file.
		size.width = desc_lines[0].length();
		size.height = desc_lines.length;

		if (square_size != -1) {
			block_size = square_size;
		} else {
			block_size = Math.max(2, (int) CompetitionParameters.MAX_WINDOW_SIZE / Math.max(size.width, size.height));
		}

		if (CompetitionParameters.IS_LEARNING) {
			block_size =CompetitionParameters.LEARNING_BLOCK_SIZE;
		}
		screenSize = new Dimension(size.width * block_size, size.height * block_size);

		for (int i = 0; i < size.height; ++i) {
			String line = desc_lines[i];
			if (line.length() < size.width) {
				// This might happen. We just concat ' ' until size.
				desc_lines[i] = completeLine(line, size.width - line.length(), " ");
			}
		}

		ArrayList<VGDLSprite> avatars = new ArrayList<VGDLSprite>();
		// All sprites are created and placed here:
		for (int i = 0; i < size.height; ++i) {
			String line = desc_lines[i];

			// For each character
			for (int j = 0; j < size.width; ++j) {
				Character c = line.charAt(j);

				// If this character is defined in the array of mappings.
				if (charMapping.containsKey(c)) {
					for (String obj : charMapping.get(c)) {
						int similarTiles = 0;
						for (int x = -1; x <= 1; x++) {
							for (int y = -1; y <= 1; y++) {
								if (Math.abs(x) != Math.abs(y)
										&& (j + x >= 0 && j + x < size.width && i + y >= 0 && i + y < size.height)) {
									if (charMapping.containsKey(desc_lines[i + y].charAt(j + x))) {
										ArrayList<String> neighborTiles = charMapping
												.get(desc_lines[i + y].charAt(j + x));
										if (neighborTiles.contains(obj)) {
											similarTiles += Math.floor(Math.abs(x) * (x + 3) / 2)
													+ Math.abs(y) * (y + 3) * 2;
										}
									}
								}
							}
						}

						// Get its position and add it to the game.
						Vector2d position = new Vector2d(j * block_size, i * block_size);
						VGDLSprite s = addSpriteIn(obj, position);
						if(s == null){
							continue;
						}
						if (s.is_avatar) {
							avatars.add(s);
						}
						if (s.autotiling) {

							ArrayList<Image> images = s.images.get("NONE");
							if(images.size() > 0)
								s.image = images.get(similarTiles);
						}
						if (s.randomtiling >= 0) {
							Random random = new Random(randomSeed);
							ArrayList<Image> allImages = s.images.get("NONE");
							if (random.nextDouble() > s.randomtiling && allImages.size() > 0) {
								s.image = allImages.get(random.nextInt(allImages.size()));
							}
						}
					}
				}
				else if(c != ' '){
					Logger.getInstance().addMessage(new Message(Message.WARNING, "\"" + c + "\" is not defined in the level mapping."));
				}
			}
		}

		if (avatars.size() > no_players) {
			Logger.getInstance().addMessage(new Message(Message.WARNING,
					"No more than " + no_players + " avatar(s) allowed (Others are destroyed)."));
			for(int i=0; i<this.spriteGroups.length; i++){
				for(int j=no_players; j<avatars.size(); j++){
					this.spriteGroups[i].removeSprite(avatars.get(j));
				}
			}
		}

		// Nobody has been killed... yet!
		kill_list = new ArrayList<VGDLSprite>();

		// Generate the initial state observation.
		this.createAvatars(-1);
		this.initForwardModel();
	}

	/**
	 * Reads the parameters of a game type.
	 *
	 * @param content
	 *            list of parameter-value pairs.
	 */
	protected void parseParameters(GameContent content) {
		super.parseParameters(content);

		VGDLFactory factory = VGDLFactory.GetInstance();
		Class refClass = VGDLFactory.registeredGames.get(content.referenceClass);
		// System.out.println("refClass" + refClass.toString());
		if (!this.getClass().equals(refClass)) {
			System.out.println("Error: Game subclass instance not the same as content.referenceClass" + " "
					+ this.getClass() + " " + refClass);
			return;
		}

		factory.parseParameters(content, this);
	}

	@Override
	public boolean isGameOver() {
		return false;
	}

	/**
	 * Adds one sprites in the position indicated.
	 *
	 * @param key
	 *            sprite type to add.
	 * @param position
	 *            position where the sprite will be placed
	 */
	public VGDLSprite addSpriteIn(String key, Vector2d position) {
		int itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(key);
		return addSprite(itype, position);
	}

	/**
	 * Adds all sprites that 'c' represents in the position indicated.
	 *
	 * @param keys
	 *            List of sprite types to add.
	 * @param position
	 *            position where all these sprites will be placed.
	 */
	public void addSpritesIn(ArrayList<String> keys, Vector2d position) {
		// We might have more than one sprite in the same position.
		for (String objectType : keys) {
			addSpriteIn(objectType, position);
		}
	}

	/**
	 * Takes a line and concats filler as many times as specified.
	 *
	 * @param base
	 *            initial string.
	 * @param occurrences
	 *            how many times filler is appended
	 * @param filler
	 *            string to append occurrences times to base.
	 * @return the resultant string.
	 */
	private String completeLine(String base, int occurrences, String filler) {
		for (int i = 0; i < occurrences; ++i)
			base = base.concat(filler);
		return base;
	}

	public int getSquareSize() {
		return square_size;
	}
}
