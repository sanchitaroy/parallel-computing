package concurrentBST;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import commonFiles.BSTNode;

/**
 * CopyOnWrite is a Copy on Write Implementation of the Binary Search tree
 * Fields: bst: BSTNode Reference to hold the root of the binary search tree
 *
 * @param <E>
 */
public class CopyOnWriteBST<E extends Comparable<E>> {

	private BSTNode<E> bst;

	/**
	 * Constructor for the CopyOnWriteBST
	 */
	public CopyOnWriteBST() {
		bst = null;

	}

	/**
	 * Constructor for the CopyOnWriteBST.
	 * Initializes the field bst
	 * @param bst
	 */
	public CopyOnWriteBST(BSTNode bst) {

		this.bst = bst;

	}

	/**
	 * Constructor of the CopyOnWrite Binary Search Tree which sets the root 
	 * @param E
	 */
	public CopyOnWriteBST(Object E) {
		bst = new BSTNode(E);
	}

	/**
	 * Gets the root of the binary search tree
	 * @return
	 */
	public BSTNode<E> getRoot() {
		return bst;
	}

	/**
	 * Makes a copy of the binary search tree passed as an argument
	 * @param root
	 * @return the root of the new binary search tree
	 */
	public synchronized BSTNode CopyTree(BSTNode root) {
		if (root == null)
			return null;
		BSTNode newRoot = null;
		BSTNode curr = root, copyNode;
		HashMap<BSTNode, BSTNode> hm = new HashMap<BSTNode, BSTNode>();

		Queue<BSTNode> q = new LinkedList<BSTNode>();
		q.add(root);
		while (!q.isEmpty()) {
			curr = q.remove();

			copyNode = new BSTNode<E>(curr);
			hm.put(curr, copyNode);

			if (curr.getLeft() != null) {
				q.add(curr.getLeft());
			}
			if (curr.getRight() != null) {
				q.add(curr.getRight());
			}

		}

		q.add(root);
		while (!q.isEmpty()) {
			curr = q.remove();
			copyNode = hm.get(curr);
			copyNode.setLeft(hm.get(curr.getLeft()));
			copyNode.setRight(hm.get(curr.getRight()));
			if (curr.getLeft() != null) {
				q.add(curr.getLeft());
			}
			if (curr.getRight() != null) {
				q.add(curr.getRight());
			}
		}

		return hm.get(root);
	}

	/**
	 * inserts a key into the copy on write BST
	 * @param key
	 */
	public synchronized void insert(E key) {

		bst = insert(CopyTree(bst), key);
		return;
	}

	/**
	 * inserts a key into the tree provided as an argument
	 * @param n root of the tree into which the key has to be inserted
	 * @param key, key which is being inserted into the tree n
	 * @return
	 */
	private synchronized BSTNode<E> insert(BSTNode<E> n, E key) {
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
	 * Deletes the key provided as an argument
	 * @param key
	 * @return
	 */
	public BSTNode<E> delete(E key) {
		return delete(bst, key);
	}

	/**
	 * Deletes a key from the tree provided as an argument 
	 * @param root
	 * @param key
	 * @return
	 */
	private synchronized BSTNode<E> delete(BSTNode<E> root, E key) {
		if (root == null) {
			return null;
		}
		BSTNode<E> copyOfTree = CopyTree(root);
		if (key.equals(copyOfTree.getKey())) {

			if (copyOfTree.getLeft() == null && copyOfTree.getRight() == null) {
				return null;
			}
			if (copyOfTree.getLeft() == null) {
				return copyOfTree.getRight();
			}
			if (copyOfTree.getRight() == null) {
				return copyOfTree.getLeft();
			}

			E smallVal = smallest(copyOfTree.getRight());
			copyOfTree.setKey(smallVal);
			copyOfTree.setRight(delete(copyOfTree.getRight(), smallVal));
			return copyOfTree;
		}

		else if (key.compareTo(copyOfTree.getKey()) < 0) {
			copyOfTree.setLeft(delete(copyOfTree.getLeft(), key));
			return copyOfTree;
		}

		else {
			copyOfTree.setRight(delete(copyOfTree.getRight(), key));
			return copyOfTree;
		}
	}

	/**
	 * finds the smallest element in the tree
	 * @param n
	 * @return
	 */
	private E smallest(BSTNode<E> n) {

		if (n.getLeft() == null) {
			return n.getKey();
		} else {
			return smallest(n.getLeft());
		}
	}

	/**
	 * Looks up the key in the binary search tree
	 * @param key
	 * @return
	 */
	public boolean lookup(E key) {
		return lookup(bst, key);
	}

	/**
	 * Looks up a binary search tree for the key in the tree provided
	 * @param n
	 * @param key
	 * @return
	 */
	private boolean lookup(BSTNode<E> n, E key) {
		if (n == null) {
			return false;
		}

		if (n.getKey().equals(key)) {
			return true;
		}

		if (key.compareTo(n.getKey()) < 0) {
			return lookup(n.getLeft(), key);
		}

		else {
			return lookup(n.getRight(), key);
		}
	}

	/**
	 * Traverses the Binary search tree in an inorded fashion
	 * @param root
	 */
	public void traversal(BSTNode<E> root) {

		if (null == root) {
			return;
		}
		traversal(root.getLeft());
		System.out.println(root.getKey());
		traversal(root.getRight());
	}
}