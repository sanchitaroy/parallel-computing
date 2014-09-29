package commonFiles;

/**
 * BSTNode is the node of the Binary Search tree
 * Each Node of this type consists of a Key and 
 * Left and Right Node references.
 * @param <E>
 */
public class BSTNode<E> {
	
	private E key;
	private BSTNode<E> left, right;
	
	/**
	 * This is the constructor for the Binary Search Tree Node
	 * 
	 * @param key: Key is the parameter which we want to store on the data structure
	 */
	public BSTNode(E key){
		this.key = key;
		this.left = null;
		this.right = null;
	}
	
	/**
	 * This method is a constructor for the Binary Search Tree Node.
	 * This method constructs a new node based on the Node passed to it as an argument
	 * @param root
	 */
	public BSTNode(BSTNode<E> root){
		this.key = root.key;
		this.left = null;
		this.right = null;
	}
	
	/**
	 * This method is a constructor for the Binary Search tree node.
	 * This method constructs the Binary Search Tree Node based on the Key, left and right
	 * arguments sent to it.
	 * @param key
	 * @param left
	 * @param right
	 */
	public BSTNode(E key, BSTNode<E> left, BSTNode<E> right){
		this.key = key;
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Below method is a getter for the key
	 * @return
	 */
	public E getKey(){
		return key;
	}
	
	/**
	 * Below method is a getter for the left reference of the Binary Search Tree Node
	 * @return
	 */
	public BSTNode<E> getLeft(){
		return left;
	}
	
	/**
	 * Below method is a getter for the right reference of the binary search tree Node
	 * @return
	 */
	public BSTNode<E> getRight(){
		return right;
	}
	
	/**
	 * Below method is a setter for the Key for the Binary search Tree node
	 * @param newK
	 */
	public void setKey(E newK) {
		key = newK;
	}
	
	/**
	 * Below method is a setter for the Left reference of the Binary Search Tree Node
	 * @param newL
	 */
	public void setLeft(BSTNode<E> newL){
		left = newL;
	}
	
	/**
	 * Below method is a setter for the Right reference of the Binary Search tree Node
	 * @param newR
	 */
	public void setRight(BSTNode<E> newR){
		right = newR;
	}

}
