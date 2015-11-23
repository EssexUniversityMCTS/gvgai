package controllers.decisionTree;

public class Node {
	public int property;
	public double upperBound;
	public Node[] children;
	public int output;
	
	public Node(){
		property = 0;
		upperBound = 0;
		children = null;
		output = 5;
	}
	
	public Node(int output){
		this();
		this.output = output;
	}
	
	public Node(int property, int upperBound, int slots){
		this();
		this.property = property;
		this.upperBound = upperBound;
		this.children = new Node[slots];
	}
	
	public int decide(Tuple data){
		if(children == null){
			return output;
		}
		for(int i=0; i<children.length; i++){
			if(data.values.get(property) < children[i].upperBound){
				return children[i].decide(data);
			}
		}
		
		return children[children.length - 1].decide(data);
	}
}
