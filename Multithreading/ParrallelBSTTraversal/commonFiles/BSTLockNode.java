package commonFiles;

import java.util.concurrent.locks.ReentrantLock;

/**
 * BSTLockNode is used to represent the Node of the Binary Search Tree used to implement
 * the HandOverHand locking of the Binary Search Tree
 * In addition to regular fields of the Binary Search Tree, each node has a lock associated with it.
 *
 * @param <E>
 */
public class BSTLockNode<E> {
	
	private E key;
	private BSTLockNode<E> left, right;
	private ReentrantLock lock;
	
	/**
	 * Constructor to initialize the reference fields of the BSTLockNode 
	 * @param key
	 * @param left
	 * @param right
	 */
	public BSTLockNode(E key, BSTLockNode<E> left, BSTLockNode<E> right){
		this.key = key;
		this.left = left;
		this.right = right;
		this.lock = new ReentrantLock();
	}
	
	/**
	 * getter for the key
	 * @return
	 */
	public E getKey(){
		return key;
	}
	
	/**
	 * getter for the reference of the left child
	 * @return
	 */
	public BSTLockNode<E> getLeft(){
		return left;
	}
	
	/**
	 * getter for the reference of the right child
	 * @return
	 */
	public BSTLockNode<E> getRight(){
		return right;
	}
	
	/**
	 * Setter for the Key
	 * @param newK
	 */
	public void setKey(E newK) {
		key = newK;
	}
	
	/**
	 * Setter for the left child node reference
	 * @param newL
	 */
	public void setLeft(BSTLockNode<E> newL){
		left = newL;
	}
	
	/**
	 * Setter for the right child node reference
	 * @param newR
	 */
	public void setRight(BSTLockNode<E> newR){
		right = newR;
	}
	
	/**
	 * Method to acquire the lock on the node
	 */
	public void lock() {
		lock.lock();
	}

	/**
	 * Method to release the acquired lock on the node
	 */
	public void unlock() {
		lock.unlock();
	}

}
