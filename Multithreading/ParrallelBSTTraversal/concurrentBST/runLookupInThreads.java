package concurrentBST;

import java.util.ArrayList;

/**
 * runLookupInThreads class implements runnable interface
 * The run method in this class is defined to perform lookup operations on three types of Trees
 * HandOverHandLockingBST, ReadWriteBST and CopyOnWriteBST
 *
 */
public class runLookupInThreads implements Runnable{
	
	ArrayList<Integer> arr = new ArrayList<Integer>();
	ReadWriteBST<Integer> rwTree;
	HandOverHandLockingBST<Integer> HOHTree;
	CopyOnWriteBST<Integer> COWTree;

	/**
	 * Constructor to initialize the references of the COWTree,HOHTree and rwTree
	 * @param arr
	 * @param COWTree
	 * @param HOHTree
	 * @param rwTree
	 */
	public runLookupInThreads(ArrayList arr, CopyOnWriteBST COWTree, HandOverHandLockingBST HOHTree, ReadWriteBST rwTree) {
		this.arr = arr;
		this.COWTree = COWTree;
		this.HOHTree = HOHTree;
		this.rwTree = rwTree;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		
		for(int i = 0; i<arr.size(); i++){
			
			if(rwTree != null)
				rwTree.lookup(arr.get(i));
			if(COWTree != null)
				COWTree.lookup(arr.get(i));
			if(HOHTree != null)
				HOHTree.lookUp(arr.get(i));
		}
	}

}
