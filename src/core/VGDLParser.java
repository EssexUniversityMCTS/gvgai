package core;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.content.GameContent;
import core.content.InteractionContent;
import core.content.MappingContent;
import core.content.SpriteContent;
import core.content.TerminationContent;
import core.game.Game;
import core.game.GameDescription;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.termination.Termination;
import ontology.Types;
import ontology.effects.Effect;
import tools.IO;
import tools.Pair;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:52
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class VGDLParser
{
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
    private static boolean VERBOSE_PARSER = false;

    /**
     * Default constructor.
     */
    public VGDLParser()
    {
        currentSet = Types.VGDL_GAME_DEF;
        spriteOrderTmp = new ArrayList<Integer>();
        singletonTmp = new ArrayList<Integer>();
        constructors = new HashMap<Integer, SpriteContent>();
    }

    /**
     * Parses a game passed whose file is passed by parameter.
     * @param gamedesc_file filename of the file containing the game
     * @return the game created
     */
    public Game parseGame(String gamedesc_file)
    {
        String[] desc_lines = new IO().readFile(gamedesc_file);
        if(desc_lines != null)
        {
            Node rootNode = indentTreeParser(desc_lines);

            //Parse here game and arguments of the first line
            game = VGDLFactory.GetInstance().createGame((GameContent) rootNode.content);

            //Parse here blocks of VGDL.
            for(Node n : rootNode.children)
            {
                if(n.content.identifier.equals("SpriteSet"))
                {
                    parseSpriteSet(n.children);
                }else if(n.content.identifier.equals("InteractionSet"))
                {
                    parseInteractionSet(n.children);
                }else if(n.content.identifier.equals("LevelMapping"))
                {
                    parseLevelMapping(n.children);
                }else if(n.content.identifier.equals("TerminationSet"))
                {
                    parseTerminationSet(n.children);
                }
            }
        }

        return game;
    }
    
    /**
     * Builds the tree structure that defines the game.
     * @param lines array with the lines read from the game description file.
     * @return the root of the final game tree
     */
    private Node indentTreeParser(String[] lines)
    {
        //By default, let's make tab as four spaces
        String tabTemplate = "    ";
        Node last = null;

        for(String line : lines)
        {
            line.replaceAll("\t",tabTemplate);
            line.replace('(',' ');
            line.replace(')',' ');
            line.replace(',',' ');

            // remove comments starting with "#"
            if(line.contains("#"))
                line = line.split("#")[0];

            // handle whitespace and indentation
            String content = line.trim();

            if(content.length() > 0)
            {
                updateSet(content); //Identify the set we are in.
                char firstChar = content.charAt(0);
                //figure out the indent of the line.
                int indent = line.indexOf(firstChar);
                last = new Node(content, indent, last, currentSet);
            }
        }

        return last.getRoot();
    }

    /**
     * Updates the set we are in (game-def, spriteset, interactionset, levelmapping, terminationset)
     * @param line line to read
     */
    private void updateSet(String line)
    {
        if(line.equalsIgnoreCase("SpriteSet"))
            currentSet = Types.VGDL_SPRITE_SET;
        if(line.equalsIgnoreCase("InteractionSet"))
            currentSet = Types.VGDL_INTERACTION_SET;
        if(line.equalsIgnoreCase("LevelMapping"))
            currentSet = Types.VGDL_LEVEL_MAPPING;
        if(line.equalsIgnoreCase("TerminationSet"))
            currentSet = Types.VGDL_TERMINATION_SET;
    }

    /**
     * Parses the sprite set, and then initializes the game structures for the sprites.
     * @param elements children of the root node of the game description sprite set.
     */
    private void parseSpriteSet(ArrayList<Node> elements)
    {
        //We need these 2 here:
        spriteOrderTmp.add(VGDLRegistry.GetInstance().getRegisteredSpriteValue("wall"));
        spriteOrderTmp.add(VGDLRegistry.GetInstance().getRegisteredSpriteValue("avatar"));

        _parseSprites(elements, null, new HashMap<String, String>(), new ArrayList<String>());

        //Set the order of sprites.
        game.initSprites(spriteOrderTmp, singletonTmp, constructors);
    }

    /**
     * Recursive method to parse the tree of sprites.
     * @param elements set of sibling nodes
     * @param parentclass String that identifies the class of the parent node. If null, no class defined yet.
     * @param parentargs Map with the arguments of the parent, that are inherited to all its children.
     * @param parenttypes List of types the parent of elements belong to.
     */
    private void _parseSprites(ArrayList<Node> elements, String parentclass,
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
            if(spriteClassName != null)
                parentclass = spriteClassName;
            if(parentclass != null)
                sc.referenceClass = parentclass;

            //Take all parameters and add them to the argument list.
            HashMap<String, String> parameters = sc.parameters;
            Set<Map.Entry<String, String>> entries = parameters.entrySet();
            for(Map.Entry ent : entries)
            {
                args.put((String)ent.getKey(), (String)ent.getValue());
            }

            //Check for singleton objects.
            if(parameters.containsKey("singleton"))
            {
                if(parameters.get("singleton").equalsIgnoreCase("true"))
                {
                    singletonTmp.add(intId);
                }
            }
            sc.parameters = (HashMap<String, String>) args.clone();

            //If this is a leaf node, set the information on Game to create objects of this type.
            if(el.children.size() == 0)
            {
                if(VERBOSE_PARSER)
                    System.out.println("Defining: " + identifier + " " + sc.referenceClass
                        + " " + el.content.toString());

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
                    for(Integer subtype : childContent.subtypes)
                    {
                        if(!sc.subtypes.contains(subtype))
                            sc.subtypes.add(subtype);
                    }
                }

            }

        }
    }

    /**
     * Parses the interaction set.
     * @param elements all interactions defined for the game.
     */
    private void parseInteractionSet(ArrayList<Node> elements)
    {
        for(Node n : elements)
        {
            InteractionContent ic = (InteractionContent)n.content;
            if(ic.is_definition) // === contains ">"
            {
                Effect ef = VGDLFactory.GetInstance().createEffect(ic);

                //Get the identifiers of both sprites taking part in the effect
                int obj1 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ic.object1);
                int obj2 = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ic.object2);
                if(obj1 != -1 && obj2 != -1)
                {
                    Pair newPair = new Pair(obj1,obj2);
                    if(!game.getDefinedEffects().contains(newPair))
                        game.getDefinedEffects().add(newPair);

                    //game.collisionEffects[obj1][obj2].add(ef);
                    game.getCollisionEffects(obj1, obj2).add(ef);

                    if(VERBOSE_PARSER)
                        System.out.println("Defining interaction " + ic.object1 + "+" + ic.object2 +
                                       " > " + ic.function);
                }else if(obj1 != -1 && obj2 == -1)
                {
                    //Only one sprite is defined in SpriteSet, this might be an EOS effect.
                    if(ic.object2.equalsIgnoreCase("EOS"))
                    {
                        game.getDefinedEosEffects().add(obj1);
                        game.getEosEffects(obj1).add(ef);
                    }

                    if(VERBOSE_PARSER)
                        System.out.println("Defining interaction " + ic.object1 + "+" + ic.object2 +
                            " > " + ic.function);
                }

                //update game stochasticity.
                if(ef.is_stochastic)
                {
                    game.setStochastic(true);
                }
            }else{
                System.out.println("[PARSE ERROR] bad format interaction entry: " + ic.line);
            }
        }
    }

    /**
     * Parses the level mapping.
     * @param elements all mapping units.
     */
    private void parseLevelMapping(ArrayList<Node> elements)
    {
        for(Node n : elements)
        {
            MappingContent mc = (MappingContent)n.content;
            game.getCharMapping().put(mc.charId, mc.reference);
        }

    }

    /**
     * Parses the termination set.
     * @param elements all terminations defined for the game.
     */
    private void parseTerminationSet(ArrayList<Node> elements)
    {
        for(Node n : elements)
        {
            TerminationContent tc = (TerminationContent)n.content;
            Termination ter = VGDLFactory.GetInstance().createTermination(tc);
            game.getTerminations().add(ter);
        }

    }
}
