package core;

import core.content.*;
import core.game.Game;
import core.termination.Termination;
import ontology.Types;
import ontology.effects.Effect;
import tools.IO;
import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:52 This is a Java port from Tom Schaul's VGDL -
 * https://github.com/schaul/py-vgdl
 */
public class VGDLParser {
  private static final Pattern COMMENT = Pattern.compile("#");
  /**
   * Game which description is being read.
   */
  public Game game;

  /**
   * Current set through the game description file.
   */
  public int currentSet;

  /**
   * Temporal structure to hold spriteOrder (before the final array is created and sorted).
   */
  private ArrayList<Integer> spriteOrderTmp;

  /**
   * Temporal structure to hold which sprites are singleton.
   */
  private ArrayList<Integer> singletonTmp;

  /**
   * Maps integer identifier with sprite constructors
   */
  private HashMap<Integer, SpriteContent> constructors;

  /**
   * Set to true to print out debug information about parsing.
   */
  private static boolean VERBOSE_PARSER;

  /**
   * Default constructor.
   */
  public VGDLParser() {
    currentSet = Types.VGDL_GAME_DEF;
    spriteOrderTmp = new ArrayList<>();
    singletonTmp = new ArrayList<>();
    constructors = new HashMap<>();
  }

  /**
   * Parses a game passed whose file is passed by parameter.
   * 
   * @param gamedesc_file filename of the file containing the game
   * @return the game created
   */
  public Game parseGame(String gamedesc_file) {
    String[] desc_lines = new IO().readFile(gamedesc_file);
    if (null != desc_lines) {
      Node rootNode = indentTreeParser(desc_lines);

      // Parse here game and arguments of the first line
      game = VGDLFactory.GetInstance().createGame((GameContent) rootNode.content);

      // Parse here blocks of VGDL.
      for (Node n : rootNode.children) {
        switch (n.content.identifier) {
          case "SpriteSet":
            parseSpriteSet(n.children);
            break;
          case "InteractionSet":
            parseInteractionSet(n.children);
            break;
          case "LevelMapping":
            parseLevelMapping(n.children);
            break;
          case "TerminationSet":
            parseTerminationSet(n.children);
            break;
        }
      }
    }

    return game;
  }

  /**
   * Builds the tree structure that defines the game.
   * 
   * @param lines array with the lines read from the game description file.
   * @return the root of the final game tree
   */
  private Node indentTreeParser(String... lines) {
    // By default, let's make tab as four spaces
    String tabTemplate = "    ";
    Node last = null;

    for (String line : lines) {
      line.replaceAll("\t", tabTemplate).replace('(', ' ').replace(')', ' ').replace(',', ' ');

      // remove comments starting with "#"
      if (line.contains("#"))
        line = COMMENT.split(line)[0];

      // handle whitespace and indentation
      String content = line.trim();

      if (!content.isEmpty()) {
        updateSet(content); // Identify the set we are in.
        char firstChar = content.charAt(0);
        // figure out the indent of the line.
        int indent = line.indexOf(firstChar);
        last = new Node(content, indent, last, currentSet);
      }
    }

    return last.getRoot();
  }

  /**
   * Updates the set we are in (game-def, spriteset, interactionset, levelmapping, terminationset)
   * 
   * @param line line to read
   */
  private void updateSet(String line) {
    if ("SpriteSet".equalsIgnoreCase(line))
      currentSet = Types.VGDL_SPRITE_SET;
    if ("InteractionSet".equalsIgnoreCase(line))
      currentSet = Types.VGDL_INTERACTION_SET;
    if ("LevelMapping".equalsIgnoreCase(line))
      currentSet = Types.VGDL_LEVEL_MAPPING;
    if ("TerminationSet".equalsIgnoreCase(line))
      currentSet = Types.VGDL_TERMINATION_SET;
  }

  /**
   * Parses the sprite set, and then initializes the game structures for the sprites.
   * 
   * @param elements children of the root node of the game description sprite set.
   */
  private void parseSpriteSet(ArrayList<Node> elements) {
    // We need these 2 here:
    spriteOrderTmp.add(VGDLRegistry.GetInstance().getRegisteredSpriteValue("wall"));
    spriteOrderTmp.add(VGDLRegistry.GetInstance().getRegisteredSpriteValue("avatar"));

    _parseSprites(elements, null, new HashMap<>(), new ArrayList<>());

    // Set the order of sprites.
    game.initSprites(spriteOrderTmp, singletonTmp, constructors);
  }

  /**
   * Recursive method to parse the tree of sprites.
   * 
   * @param elements set of sibling nodes
   * @param parentclass String that identifies the class of the parent node. If null, no class defined yet.
   * @param parentargs Map with the arguments of the parent, that are inherited to all its children.
   * @param parenttypes List of types the parent of elements belong to.
   */
  private void _parseSprites(Iterable<Node> elements, String parentclass,
                               HashMap<String, String> parentargs, ArrayList<String> parenttypes)
    {
        HashMap<String, String> args = (HashMap<String, String>) parentargs.clone();
        ArrayList<String> types = (ArrayList<String>) parenttypes.clone();
        String prevParentClass = parentclass;
        for(Node el : elements)
        {
            SpriteContent sc = (SpriteContent) el.content;
            if(!sc.is_definition) //This checks if line contains ">"
                return;

            //Take the identifier of this node.
            String identifier =  sc.identifier;
            types.add(identifier);

            //Register this entry.
            Integer intId = VGDLRegistry.GetInstance().registerSprite(identifier);
            constructors.put(intId, sc); //Ad the constructor for these objects.

            //Assign types and subtypes.
            sc.assignTypes(types);
            sc.subtypes.addAll(sc.itypes);

            //Get the class of the object
            String spriteClassName =sc.referenceClass;

            //This is the class of the object, and parent of the next.
            if(null != spriteClassName)
                parentclass = spriteClassName;
            if(null != parentclass)
                sc.referenceClass = parentclass;

            //Take all parameters and add them to the argument list.
            Map<String, String> parameters = sc.parameters;
            Set<Map.Entry<String, String>> entries = parameters.entrySet();
            for(Map.Entry ent : entries)
            {
                args.put((String)ent.getKey(), (String)ent.getValue());
            }

            //Check for singleton objects.
            if(parameters.containsKey("singleton"))
            {
                if("true".equalsIgnoreCase(parameters.get("singleton")))
                {
                    singletonTmp.add(intId);
                }
            }
            sc.parameters = (HashMap<String, String>) args.clone();

            //If this is a leaf node, set the information on Game to create objects of this type.
            if(el.children.isEmpty())
            {
                if(VGDLParser.VERBOSE_PARSER)
                    System.out.println("Defining: " + identifier + ' ' + sc.referenceClass
                        + ' ' + el.content);

                if(spriteOrderTmp.contains(intId))
                {
                    //last one counts
                    spriteOrderTmp.remove(intId);
                }
                spriteOrderTmp.add(intId);

                //Reset the parameters to the ones from the parent
                args = (HashMap<String, String>) parentargs.clone();
                //Reset the types to the ones from the parent
                types = (ArrayList<String>) parenttypes.clone();

            }else{
                _parseSprites(el.children, parentclass, args, types);
                args = (HashMap<String, String>) parentargs.clone();
                types = (ArrayList<String>) parenttypes.clone();
                parentclass = prevParentClass;

                //To my subtypes, add all from my children (recursively, this adds everything in my subtree).
                for(Node child: el.children)
                {
                    SpriteContent childContent = (SpriteContent) child.content;
                    childContent.subtypes.stream().filter(subtype -> !sc.subtypes.contains(subtype)).forEach(sc.subtypes::add);
                }

            }

        }
    }

  /**
   * Parses the interaction set.
   * 
   * @param elements all interactions defined for the game.
   */
  private void parseInteractionSet(Iterable<Node> elements) {
    for (Node n : elements) {
      InteractionContent ic = (InteractionContent) n.content;
      if (ic.is_definition) // === contains ">"
      {
        Effect ef = VGDLFactory.GetInstance().createEffect(ic);

        // Get the identifiers of both sprites taking part in the effect
        int obj1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ic.object1);
        int obj2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ic.object2);
        if (-1 != obj1 && -1 != obj2) {
          Pair newPair = new Pair(obj1, obj2);
          if (!game.getDefinedEffects().contains(newPair))
            game.getDefinedEffects().add(newPair);

          // game.collisionEffects[obj1][obj2].add(ef);
          game.getCollisionEffects(obj1, obj2).add(ef);

          if (VGDLParser.VERBOSE_PARSER)
            System.out.println("Defining interaction " + ic.object1 + '+' + ic.object2 + " > "
                + ic.function);
        } else if (-1 != obj1 && -1 == obj2) {
          // Only one sprite is defined in SpriteSet, this might be an
          // EOS effect.
          if ("EOS".equalsIgnoreCase(ic.object2)) {
            game.getDefinedEosEffects().add(obj1);
            game.getEosEffects(obj1).add(ef);
          }

          if (VGDLParser.VERBOSE_PARSER)
            System.out.println("Defining interaction " + ic.object1 + '+' + ic.object2 + " > "
                + ic.function);
        }

        // update game stochasticity.
        if (ef.is_stochastic) {
          game.setStochastic(true);
        }
      } else {
        System.out.println("[PARSE ERROR] bad format interaction entry: " + ic.line);
      }
    }
  }

  /**
   * Parses the level mapping.
   * 
   * @param elements all mapping units.
   */
  private void parseLevelMapping(Iterable<Node> elements) {
    for (Node n : elements) {
      MappingContent mc = (MappingContent) n.content;
      game.getCharMapping().put(mc.charId, mc.reference);
    }

  }

  /**
   * Parses the termination set.
   * 
   * @param elements all terminations defined for the game.
   */
  private void parseTerminationSet(Iterable<Node> elements) {
    for (Node n : elements) {
      TerminationContent tc = (TerminationContent) n.content;
      Termination ter = VGDLFactory.GetInstance().createTermination(tc);
      game.getTerminations().add(ter);
    }

  }

}
