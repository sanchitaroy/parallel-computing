package nonConcurrentBST;

import commonFiles.BSTNode;

/**
 * NonConcurrentBST represents nonconcurrent version of the Binary Search Tree
 *
 * @param <E>
 */
public class NonConcurrentBST<E extends Comparable<E>> {
	
	private BSTNode<E> root;
	
	/**
	 * Constructor for the NonConcurrentBST
	 */
	public NonConcurrentBST(){
		root = null;
	}
	
	/**
	 * Getter for root
	 * @return
	 */
	public BSTNode<E> getRoot() {
		return root;
	}

	/**
	 * Setter for root
	 * @param root
	 */
	public void setRoot(BSTNode<E> root) {
		this.root = root;
	}

	/**
	 * insert method to insert key into the tree
	 * @param key
	 */
	public void insert(E key){
		root = insert(root, key);
	}
	
	/**
	 * insert method to insert key into the BST provided as an argument
	 * @param n
	 * @param key
	 * @return
	 */
	private BSTNode<E> insert(BSTNode<E> n, E key) {
	    if (n == null) {
	        return new BSTNode<E>(key, null, null);
	    }
	     
	    if (n.getKey().equals(key)) {
	    	n.setLeft( insert(n.getLeft(), key) );
	        return n;
	    }
	    
	    if (key.compareTo(n.getKey()) < 0) {
	        n.setLeft( insert(n.getLeft(), key) );
	        return n;
	    }
	    
	    else {
	        n.setRight( insert(n.getRight(), key) );
	        return n;
	    }
	}
	
	/**
	 * Delete method to remove a key from the NonConcurrentBST
	 * @param key
	 */
	public void delete(E key){
		root = delete(root, key);
	}
	
	/**
	 * Delete method to remove the key from the BST provided as an argument
	 * @param n
	 * @param key
	 * @return
	 */
	private BSTNode<E> delete(BSTNode<E> n, E key) {
	    if (n == null) {
	        return null;
	    }
	    
	    if (key.equals(n.getKey())) {

	        if (n.getLeft() == null && n.getRight() == null) {
	            return null;
	        }
	        if (n.getLeft() == null) {
	            return n.getRight();
	        }
	        if (n.getRight() == null) {
	            return n.getLeft();
	        }
	       
	        E smallVal = smallest(n.getRight());
	        n.setKey(smallVal);
	        n.setRight( delete(n.getRight(), smallVal) );
	        return n; 
	    }
	    
	    else if (key.compareTo(n.getKey()) < 0) {
	        n.setLeft( delete(n.getLeft(), key) );
	        return n;
	    }
	    
	    else {
	        n.setRight( delete(n.getRight(), key) );
	        return n;
	    }
	}
	
	/**
	 * Method to lookup a key in the NonConcurrent BST
	 * @param key
	 * @return
	 */
	public boolean lookup(E key){
		return lookup(root, key);		
	}
	
	/**
	 * Method to lookup a key in the NonConcurrent BST provided as an argument
	 * to this method
	 * @param n
	 * @param key
	 * @return
	 */
	private boolean lookup(BSTNode<E> n, E key){
		if(n == null){
			return false;
		}
		
		if(n.getKey().equals(key)){
			return true;
		}
		
		if(key.compareTo(n.getKey()) < 0){
			return lookup(n.getLeft(), key);
		}
		
		else{
			return lookup(n.getRight(), key);
		}
	}
	
	/**
	 * Traversal method implements inorder traversal of the nonconcurrent Binary Search Tree
	 * @param root
	 */
	public void traversal(BSTNode<E> root){

		if(null == root){
			return;
		}	
		traversal(root.getLeft());
		System.out.println(root.getKey());
		traversal(root.getRight());
	}
	
	/**
	 * Method to find the Smallest in the nonconcurrent Binary Search Tree
	 * @param n
	 * @return
	 */
	private E smallest(BSTNode<E> n){
		
	    if (n.getLeft() == null) {
	        return n.getKey();
	    } else {
	        return smallest(n.getLeft());
	    }
	}

}