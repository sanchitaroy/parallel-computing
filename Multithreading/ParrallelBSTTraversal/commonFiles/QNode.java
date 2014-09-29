package commonFiles;

/**
 * QNode is a node of the Queue. Each Node of this type will have 2 fields.
 * availanleProcs is the field which is used to determine the number of the processes available
 * for that particular node.
 * node is a reference for the BST node.
 *
 */
public class QNode {
	
	private BSTNode<Object> node;
	private int availableProcs;
	
	/**
	 * Constructor for the Class QNode
	 * @param node
	 * @param availableProcs
	 */
	public QNode(BSTNode<Object> node, int availableProcs){
		this.node = node;
		this.availableProcs = availableProcs;
	}
	
	/**
	 * Getter for the avaliableProcs
	 * @return
	 */
	public int getProcs(){
		return availableProcs;
	}
	
	/**
	 * Getter for Node
	 * @return
	 */
	public BSTNode<Object> getNode(){
		return node;
	}
	
	/**
	 * Setter for the Node
	 * @param newNode
	 */
	public void setNode(BSTNode<Object> newNode){
		node = newNode;
	}
	
	/**
	 * Setter for Procs
	 * @param newProcs
	 */
	public void setProcs(int newProcs){
		availableProcs = newProcs;
	}
}
