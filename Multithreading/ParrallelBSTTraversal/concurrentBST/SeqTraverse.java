package concurrentBST;

import java.util.ArrayList;

import commonFiles.*;

/**
 * SeqTraverse class implements Runnable interface
 * The run method in this class is used to run Depth First Search of the BST sequentially
 *  
 */
public class SeqTraverse implements Runnable{
	
	BSTNode<Object> root;
	ArrayList<Object> traverseArr;

	
	/**
	 * Constructor for the SeqTraverse
	 * @param root
	 * @param traverseArr
	 */
	public SeqTraverse(BSTNode<Object> root, ArrayList<Object> traverseArr){
		this.root = root;
		this.traverseArr = traverseArr;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ParallelDepthFirstSearch.sequentialTraverse(root, traverseArr);
	}

}
