package core.vgdl;

import java.util.ArrayList;

import core.content.*;
import core.logging.Logger;
import core.logging.Message;
import ontology.Types;
import tools.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:34
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Node
{
    /**
     * Parent of this node.
     */
    public Node parent;

    /**
     * Contents of this node.
     */
    public Content content;

    /**
     *  Indent of the node in the tree.
     */
    public int indent;

    /**
     * Children of this node.
     */
    public ArrayList<Node> children;

    /**
     * The line number of the node's contents
     */
    public int lineNumber;

    /**
     * Constructor of the node.
     * @param contentLine string with the node information
     * @param indent indent level of this node, to determine its place on the tree.
     * @param parent indicates the parent of the new node, if any.
     * @throws Exception 
     */
    public Node(String contentLine, int indent, Node parent, int set) throws Exception
    {
        children = new ArrayList<Node>();
        this.content = createContent(contentLine, set);
        this.indent = indent;
        if(parent == null)
            this.parent = null;
        else
            parent.insert(this);
    }

    public Node(String contentLine, int indent, Node parent, int set, int lineNumber) throws Exception {
        children = new ArrayList<Node>();
        this.content = createContent(contentLine, set);
        this.indent = indent;
        if(parent == null)
            this.parent = null;
        else
            parent.insert(this);

        this.lineNumber = lineNumber;
    }
    /**
     * Creates a content for later creation of objects
     * @param line line in VGDL format.
     * @param set indicates the set the line belongs to (see Types.java).
     * @return the line parsed in a content object.
     * @throws Exception 
     */
    private Content createContent(String line, int set) throws Exception
    {
        line = Utils.formatString(line);
        switch(set){
            case Types.VGDL_GAME_DEF:
                return new GameContent(line);

            case Types.VGDL_SPRITE_SET:
                return new SpriteContent(line);
            //
            case Types.VGDL_INTERACTION_SET:
                return new InteractionContent(line);

            case Types.VGDL_LEVEL_MAPPING:
                return new MappingContent(line);

            case Types.VGDL_TERMINATION_SET:
                return new TerminationContent(line);

            case Types.VGDL_PARAMETER_SET:
                return ParameterContent.create(line);
        }
        return null;
    }


    /**
     * Inserts a new node in the tree structure. Navigates from this node up towards the
     * root, inserting the new node at the correct indent.
     * @param node new node to add.
     */
    public void insert(Node node)
    {
        if(this.indent < node.indent)
        {
            if(this.children.size() > 0)
            {
                if(this.children.get(0).indent != node.indent)
                    Logger.getInstance().addMessage(new Message(Message.ERROR, "children indentations must match."));
            }
            this.children.add(node);
            node.parent = this;

        }else
        {
            if(this.parent == null)
                throw new RuntimeException("Root node too indented?");
            this.parent.insert(node);
        }
    }

    /**
     * Parses the node and children to a String for debug and printing.
     * @return representative String
     */
    public String toString()
    {
        String allStr = content.toString();
        if(this.children.size() == 0)
            return allStr;


        allStr += ": ";
        for(Node n : this.children)
            allStr += n.toString() + "; ";

        return allStr;
    }

    /**
     * Returns the root of the tree structure
     * @return the root
     */
    public Node getRoot()
    {
        if(this.parent != null)
            return this.parent.getRoot();
        else
            return this;
    }

}
