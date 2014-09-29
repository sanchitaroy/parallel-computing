package concurrentBST;

import java.util.concurrent.locks.ReentrantLock;

import commonFiles.BSTLockNode;
import commonFiles.BSTNode;

/**
 * HandOverHandLockingBST implements hand over hand locking pattern on the binary search tree
 * this class has root of the tree as a field
 * @param <E>
 */
public class HandOverHandLockingBST<E extends Comparable<? super E>> {

	BSTLockNode<E> root;
	
	/**
	 * Getter for the root of the Tree
	 * @return
	 */
	public BSTLockNode<E> getRoot() {
		return root;
	}

	/**
	 * setter for root of the tree
	 * @param root
	 */
	public void setRoot(BSTLockNode<E> root) {
		this.root = root;
	}

	ReentrantLock headLock;

	/**
	 * Constructor for the HandOverHandLockingBST
	 */
	public HandOverHandLockingBST() {
		root = null;
		headLock = new ReentrantLock();
	}

	/**
	 * Method to insert data into the tree
	 * @param data
	 * @return
	 */
	public boolean insert(E data) {
		BSTLockNode<E> newNode = new BSTLockNode<E>(data, null, null);
		BSTLockNode<E> parentNode = null;
		BSTLockNode<E> currentNode = null;
		int compare = 0;
		headLock.lock();
		if(root == null) {
			root = newNode;
			headLock.unlock();
		} else {
			currentNode = root;
			currentNode.lock();
			headLock.unlock();
			while(true) {
				parentNode = currentNode;
				compare = currentNode.getKey().compareTo(data);
				if(compare > 0) {
					currentNode = currentNode.getLeft();
				} else if(compare < 0) {
					currentNode = currentNode.getRight();
				} else {
					currentNode = currentNode.getLeft();
				}
				
				if(currentNode == null) {
					break;
				} else {
					currentNode.lock();
					parentNode.unlock();
				}
			}
			if(compare > 0)
				parentNode.setLeft(newNode);
			else if(compare < 0)
				parentNode.setRight(newNode);
			else
				parentNode.setLeft(newNode);
			parentNode.unlock();
		}
		return true;
	}

	/**
	 * Method to lookup data in the HandOverHandLocking BinarySearchTree
	 * @param data
	 * @return
	 */
	public boolean lookUp(E data){
		BSTLockNode<E> curNode = null;
		BSTLockNode<E> parentNode = null;
		int compare = 0;

		headLock.lock();
		if(root != null) {
			curNode = root;
			curNode.lock();
			headLock.unlock();
			while(curNode != null) {
				compare = curNode.getKey().compareTo(data);
				parentNode = curNode;
				if(compare > 0) {
					curNode = curNode.getLeft();
				} else if(compare < 0) {
					curNode = curNode.getRight();
				} else {
					curNode.unlock();
					return true;
				}

				if(curNode == null) {
					break;
				} else {
					curNode.lock();
					parentNode.unlock();
				}
			}
		} else {
			headLock.unlock();
			return false;
		}
		parentNode.unlock();
		return false;
	}
	
	/**
	 * Deletes data from the HandOverHandLocking BinarySearchTree
	 * @param data
	 * @return
	 */
	public E delete(E data) {

		BSTLockNode<E> curNode = null;
		BSTLockNode<E> parentNode = null;
		int compare = 0;
		int oldCompare = 0;

		headLock.lock();
		if(root != null) {
			curNode = root;
			parentNode = curNode;
			curNode.lock();
			compare = curNode.getKey().compareTo(data);
			if(compare > 0) {
				curNode = curNode.getLeft();
				oldCompare = compare;
			} else if(compare < 0) {
				curNode = curNode.getRight();
				oldCompare = compare;
			} else {
				BSTLockNode<E> replacement = findReplacement(curNode);

				root = replacement;

				if(replacement != null) {
					replacement.setLeft(curNode.getLeft());
					replacement.setRight(curNode.getRight());
				}

				curNode.unlock();
				headLock.unlock();
				return curNode.getKey();
			}
			if(curNode==null){
				return null;
			}
				
			curNode.lock();
			headLock.unlock();

			while(true) {
				compare = curNode.getKey().compareTo(data);
				if(compare != 0) {
					parentNode.unlock();
					parentNode = curNode;
					if(compare > 0) {
						curNode = curNode.getLeft();
						oldCompare = compare;
					} else if(compare < 0) {
						curNode = curNode.getRight();
						oldCompare = compare;
					}
				} else {
					BSTLockNode<E> replacement = findReplacement(curNode);
					if(oldCompare > 0)
						parentNode.setLeft(replacement);
					else
						parentNode.setRight(replacement);

					if(replacement != null) {
						replacement.setLeft(curNode.getLeft());
						replacement.setRight(curNode.getRight());
					}

					curNode.unlock();
					parentNode.unlock();
					return curNode.getKey();
				}

				if(curNode == null) {
					break;
				} else {
					curNode.lock();
				}
			}
		} else {
			headLock.unlock();
			return null;
		}

		parentNode.unlock();
		return null;
	}
	
	/**
	 * Method for the traversing the HandOverHandLocking Binary search tree
	 * @param root
	 */
	public void traversal(BSTLockNode<E> root){

		if(null == root){
			return;
		}	
		traversal(root.getLeft());
		System.out.println(root.getKey());
		traversal(root.getRight());
	}

	/**
	 * findReplacement is a helper function used in the delete operation to find the replacement
	 * for the deleted element
	 * @param subRoot
	 * @return
	 */
	private BSTLockNode<E> findReplacement(BSTLockNode<E> subRoot) {
		BSTLockNode<E> curNode = null;
		BSTLockNode<E> parentNode = null;

		if(subRoot.getLeft() != null) {
			parentNode = subRoot;
			curNode = subRoot.getLeft();
			curNode.lock();
			while(curNode.getRight() != null) {
				if(parentNode != subRoot)
					parentNode.unlock();
				parentNode = curNode;
				curNode = curNode.getRight();
				curNode.lock();
			}
			if(curNode.getLeft() != null)
				curNode.getLeft().lock();
			if(parentNode == subRoot)
				parentNode.setLeft(curNode.getLeft());
			else {
				parentNode.setRight(curNode.getLeft());
				parentNode.unlock();
			}
			if(curNode.getLeft() != null)
				curNode.getLeft().unlock();
			curNode.unlock();
		} else if(subRoot.getRight() != null) {
			parentNode = subRoot;
			curNode = subRoot.getRight();
			curNode.lock();
			while(curNode.getLeft() != null) {
				if(parentNode != subRoot)
					parentNode.unlock();
				parentNode = curNode;
				curNode = curNode.getLeft();
				curNode.lock();
			}
			if(curNode.getRight() != null)
				curNode.getRight().lock();
			if(parentNode == subRoot)
				parentNode.setRight(curNode.getRight());
			else {
				parentNode.setLeft(curNode.getRight());
				parentNode.unlock();
			}
			if(curNode.getRight() != null)
				curNode.getRight().unlock();
			curNode.unlock();
		} else {
			return null;
		}
		return curNode;
	}
}