package concurrentBST;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import commonFiles.BSTNode;

/**
 * ReadWriteBST implements ReadWrite pattern for the Binary Search tree
 * In this implementation we use java reentrant read write locks.
 *
 * @param <E>
 */
public class ReadWriteBST<E extends Comparable<E>> {
	
	private BSTNode<E> root;
	
	/**
	 * getter for the Root
	 * @return
	 */
	public BSTNode<E> getRoot() {
		return root;
	}

	/**
	 * setter for the root
	 * @param root
	 */
	public void setRoot(BSTNode<E> root) {
		this.root = root;
	}

	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock read  = readWriteLock.readLock();
	private final Lock write = readWriteLock.writeLock();
	
	/**
	 * Constructor for the ReadWriteBST
	 */
	public ReadWriteBST(){
		root = null;
	}
	
	/**
	 * insert method to insert a key into the BST
	 * @param key
	 */
	public void insert(E key){
		write.lock();
		try{
			root = insert(root, key);
		} catch(Exception e){
			//todo -- introduce reqd exception
		} finally{
			write.unlock();
		}		
	}
	
	/**
	 * Insert method to insert a key into the BST provided as an argument
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
	        //System.out.println("Duplicate key");
	        //return null;
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
	 * Delete method removes a key from the ReadWrite Binary Search Tree
	 * @param key
	 */
	public void delete(E key){
		write.lock();
		try{
			root = delete(root, key);
		} catch(Exception e){
			//todo -- introduce reqd exception
		} finally{
			write.unlock();
		}			
	}
	
	/**
	 * Delete method removes a key from the BST given as an argument
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
	 * Lookup operation to search for key in the BST
	 * @param key
	 * @return
	 */
	public boolean lookup(E key){
		
		boolean var = false;
		read.lock();
		try{
			var = lookup(root, key);
		} catch(Exception e){
			//todo -- introduce reqd exception
		} finally{
			read.unlock();
		}	
		return var;
	}
	
	/**
	 * Lookup method for searching the key in the BST provided as an argument
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
	 * Method for the Traversal of Binary Search Tree
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
	 * Method to find the smallest in the Binary Search tree
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
