package concurrentBST;

import java.util.ArrayList;

/**
 * runDeleteInThreads class implements runnable interface
 * The run method in this class is defined to perform delete operations on three types of Trees
 * HandOverHandLockingBST, ReadWriteBST and CopyOnWriteBST
 *
 */
public class runDeleteInThreads implements Runnable{
	
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
	public runDeleteInThreads(ArrayList arr, CopyOnWriteBST COWTree, HandOverHandLockingBST HOHTree, ReadWriteBST rwTree) {
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
				rwTree.delete(arr.get(i));
			if(COWTree != null)
				COWTree.delete(arr.get(i));
			if(HOHTree != null)
				HOHTree.delete(arr.get(i));
		}
	}

}
