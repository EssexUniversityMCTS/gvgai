package core;

import core.content.*;
import ontology.Types;
import tools.Utils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:34 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Node {
	/**
	 * Parent of this node.
	 */
	public Node parent;

	/**
	 * Contents of this node.
	 */
	public Content content;

	/**
	 * Indent of the node in the tree.
	 */
	public int indent;

	/**
	 * Children of this node.
	 */
	public ArrayList<Node> children;

	/**
	 * Constructor of the node.
	 * 
	 * @param contentLine
	 *            string with the node information
	 * @param indent
	 *            indent level of this node, to determine its place on the tree.
	 * @param parent
	 *            indicates the parent of the new node, if any.
	 */
	public Node(String contentLine, int indent, Node parent, int set) {
		children = new ArrayList<>();
		content = createContent(contentLine, set);
		this.indent = indent;
		if (null == parent)
			this.parent = null;
		else
			parent.insert(this);
	}

	/**
	 * Creates a content for later creation of objects
	 * 
	 * @param line
	 *            line in VGDL format.
	 * @param set
	 *            indicates the set the line belongs to (see Types.java).
	 * @return the line parsed in a content object.
	 */
	private Content createContent(String line, int set) {
		line = Utils.formatString(line);
		switch (set) {
			case Types.VGDL_GAME_DEF :
				return new GameContent(line);

			case Types.VGDL_SPRITE_SET :
				return new SpriteContent(line);
				//
			case Types.VGDL_INTERACTION_SET :
				return new InteractionContent(line);

			case Types.VGDL_LEVEL_MAPPING :
				return new MappingContent(line);

			case Types.VGDL_TERMINATION_SET :
				return new TerminationContent(line);
		}
		return null;
	}

	/**
	 * Inserts a new node in the tree structure. Navigates from this node up
	 * towards the root, inserting the new node at the correct indent.
	 * 
	 * @param node
	 *            new node to add.
	 */
	public void insert(Node node) {
		if (indent < node.indent) {
			if (!children.isEmpty()) {
				if (children.get(0).indent != node.indent)
					throw new RuntimeException(
							"children indentations must match");
			}
			children.add(node);
			node.parent = this;

		} else {
			if (null == parent)
				throw new RuntimeException("Root node too indented?");
			parent.insert(node);
		}
	}

	/**
	 * Parses the node and children to a String for debug and printing.
	 * 
	 * @return representative String
	 */
	public String toString() {
		if (!children.isEmpty()) {

			StringBuilder allStr = new StringBuilder(content.toString());

			allStr.append(": ");
			for (Node n : children)
				allStr.append(n).append("; ");

			return String.valueOf(allStr);
		}
		return content.toString();
	}

	/**
	 * Returns the root of the tree structure
	 * 
	 * @return the root
	 */
	public Node getRoot() {
		return null != parent ? parent.getRoot() : this;
	}

}
