package commonFiles;
import java.util.*;

import concurrentBST.*;
import nonConcurrentBST.*;

public class Test {

	/**
	 * @param args
	 * In the main method, we have written code to test our concurrent BST Implementations.
	 * This method also has code to test the Parallel implementation of the Depth first travel of Binary Search Tree
	 * 
	 */
	public static void main(String[] args) {
		final int minRange = 1;	
		int NUM_THREADS = Runtime.getRuntime().availableProcessors();
		long start, end;
		System.out.println("Choose a Mode you want to run from below ");
		System.out.println("press t for parallel and Sequential tree traversal comparison or ");
		System.out.println("press c for concurrent BST implementations");
	    Scanner scanner = new Scanner(System.in);
	    String input = scanner.nextLine();
	    System.out.println("Trees will be generated randomly from 50 nodes to maximum number of Nodes.Please "
	    		+ "enter maximum number of Nodes");
	    int umax = scanner.nextInt();
	    if("c".equals(input)){
		HashMap<Integer,ArrayList<Long>> hm = new HashMap<Integer,ArrayList<Long>>();
		HashMap<Integer,ArrayList<Long>> id = new HashMap<Integer,ArrayList<Long>>();
		HashMap<Integer,ArrayList<Long>> hd = new HashMap<Integer,ArrayList<Long>>();
		HashMap<Integer,ArrayList<Long>> hlook = new HashMap<Integer,ArrayList<Long>>();
		for(int maxRange=51;maxRange<umax;maxRange+=500){
		
/**************************************** Concurrent Operations test ***************************************/
		
		ArrayList<Long> al = new ArrayList<Long>();
		ArrayList<Long> id1 = new ArrayList<Long>();
		ArrayList<Long> ad1 = new ArrayList<Long>();
		int totalNumber = maxRange-minRange;
		System.out.println("Created a tree with "+totalNumber+" Nodes");
		ArrayList<Long> alook1 = new ArrayList<Long>();
		int [] randomArr = new int[maxRange-minRange];

		ArrayList<ArrayList> buckets = new ArrayList<ArrayList>(NUM_THREADS);
		for(int i=0;i<NUM_THREADS;i++)
			buckets.add(new ArrayList());
		for(int i=0,j=0;i<randomArr.length;i++){
			randomArr[i] = 1+(int)(Math.random()*maxRange);
			buckets.get(i%NUM_THREADS).add(randomArr[i]);
		}
		
/**************************************** Insert test : Building a tree *************************************/
	
		/**********************Insert into NonConcurrent BST************************/
		NonConcurrentBST<Integer> ncbst = new NonConcurrentBST<Integer>();
		start = System.currentTimeMillis();
		for(int i = 0; i<randomArr.length; i++){
			ncbst.insert(randomArr[i]);
		}
				
		end = System.currentTimeMillis();
		al.add(end-start);
		
		/**********************Insert into ReadWriteBST************************/
		ReadWriteBST<Integer> rwTree = new ReadWriteBST<Integer>();
		Thread[] insertThreadsForRWBST = new Thread[NUM_THREADS];
		for(int i=0;i<NUM_THREADS;i++){
			insertThreadsForRWBST[i] = new Thread(new runInsertInThreads(buckets.get(i), null, null, rwTree));
		}
		
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			insertThreadsForRWBST[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				insertThreadsForRWBST[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		al.add(end-start);
		
		/**********************Insert into HandOverHandLockingBST************************/
		HandOverHandLockingBST<Integer> HOHTree = new HandOverHandLockingBST<Integer>();
		Thread[] insertThreadsForHOHBST = new Thread[NUM_THREADS];
		
		for(int i=0;i<NUM_THREADS;i++){
			insertThreadsForHOHBST[i] = new Thread(new runInsertInThreads(buckets.get(i), null, HOHTree, null));
		}
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			insertThreadsForHOHBST[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				insertThreadsForHOHBST[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		al.add(end-start);

		/**********************Insert into CopyOnWriteBST************************/
		CopyOnWriteBST<Integer> COWTree = new CopyOnWriteBST<Integer>();
		Thread[] insertThreadsForCOW = new Thread[NUM_THREADS];
		
		for(int i=0;i<NUM_THREADS;i++){
			insertThreadsForCOW[i] = new Thread(new runInsertInThreads(buckets.get(i), COWTree, null, null));
		}
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			insertThreadsForCOW[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				insertThreadsForCOW[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		al.add(end-start);
		hm.put(maxRange, al);
				
/**************************************** Delete test : delete with multiple threads ********/
		
		/*********************************Testing Delete Operation for non concurrent BST***************************/
		start = System.currentTimeMillis();
		for(int i = 0; i<randomArr.length; i++){
			ncbst.delete(randomArr[i]);
		}
		end = System.currentTimeMillis();
		ad1.add(end-start);
		
		/*********************************Testing Delete Operation for ReadWriteBST***************************/
		Thread[] deleteThreadsForRWBST = new Thread[NUM_THREADS];
		for(int i=0;i<NUM_THREADS;i++){
			deleteThreadsForRWBST[i] = new Thread(new runDeleteInThreads(buckets.get(i), null, null, rwTree));
		}
		
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			deleteThreadsForRWBST[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				deleteThreadsForRWBST[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		ad1.add(end-start);
		
		/*********************************Testing Delete Operation for HandOverHandLocking BST***************************/
		Thread[] deleteThreadsForHOHBST = new Thread[NUM_THREADS];
		
		for(int i=0;i<NUM_THREADS;i++){
			deleteThreadsForHOHBST[i] = new Thread(new runDeleteInThreads(buckets.get(i), null, HOHTree, null));
		}
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			deleteThreadsForHOHBST[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				deleteThreadsForHOHBST[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		ad1.add(end-start);
		
		/*********************************Testing Delete Operation for CopyOnWriteBST***************************/
		Thread[] deleteThreadsForCOW = new Thread[NUM_THREADS];
		
		for(int i=0;i<NUM_THREADS;i++){
			deleteThreadsForCOW[i] = new Thread(new runDeleteInThreads(buckets.get(i), COWTree, null, null));
		}
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			deleteThreadsForCOW[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				deleteThreadsForCOW[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		ad1.add(end-start);
		hd.put(maxRange,ad1);
		
/**************************************** lookup test : lookup with multiple threads ********/
		
		/*********************************Testing LookUp Operation for NonConcurrentBST***************************/
		start = System.currentTimeMillis();
		for(int i = 0; i<randomArr.length; i++){
			ncbst.lookup(randomArr[i]);
		}
		end = System.currentTimeMillis();
		alook1.add(end-start);
		
		/*********************************Testing LookUp Operation for ReadWriteBST***************************/
		Thread[] lookupThreadsForRWBST = new Thread[NUM_THREADS];
		for(int i=0;i<NUM_THREADS;i++){
			lookupThreadsForRWBST[i] = new Thread(new runLookupInThreads(buckets.get(i), null, null, rwTree));
		}
		
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			lookupThreadsForRWBST[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				lookupThreadsForRWBST[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		alook1.add(end-start);
		
		/*********************************Testing LookUp Operation for HandOverHandBST***************************/
		Thread[] lookupThreadsForHOHBST = new Thread[NUM_THREADS];
		
		for(int i=0;i<NUM_THREADS;i++){
			lookupThreadsForHOHBST[i] = new Thread(new runLookupInThreads(buckets.get(i), null, HOHTree, null));
		}
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			lookupThreadsForHOHBST[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				lookupThreadsForHOHBST[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		alook1.add(end-start);
		
		/*********************************Testing LookUp Operation for CopyOnWriteBST***************************/
		Thread[] lookupThreadsForCOW = new Thread[NUM_THREADS];
		
		for(int i=0;i<NUM_THREADS;i++){
			lookupThreadsForCOW[i] = new Thread(new runLookupInThreads(buckets.get(i), COWTree, null, null));
		}
		start = System.currentTimeMillis();
		for(int i=0;i<NUM_THREADS;i++){
			lookupThreadsForCOW[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				lookupThreadsForCOW[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		end = System.currentTimeMillis();
		alook1.add(end-start);
		hlook.put(maxRange,alook1);
		
/***********************************Insert and Lookup**********************************************************/

		/*Testing if simultaneous insert and lookup operations are happening*/
		Thread[] insertAndLookUp = new Thread[NUM_THREADS];
		Boolean insertAndLookUpNotWorking = false;
		for(int i=0;i<NUM_THREADS/2;i++){
			insertAndLookUp[i] = new Thread(new runInsertInThreads(buckets.get(i), COWTree, HOHTree , rwTree));
		}
		for(int i=NUM_THREADS/2;i<NUM_THREADS;i++){
			insertAndLookUp[i] = new Thread(new runLookupInThreads(buckets.get(i), COWTree, HOHTree , rwTree));
		}
		for(int i=0;i<NUM_THREADS;i++){
			insertAndLookUp[i].start();
		}
		for(int i=0;i<NUM_THREADS;i++){
			try {
				insertAndLookUp[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Oh oh! concurrent stuff not working");
				insertAndLookUpNotWorking = true;
			}
		}
		if(!insertAndLookUpNotWorking)
			System.out.println("Concurrent Insert and LookUp is Working fine for tree with nodes " +totalNumber);
		
		}
		
		/*Accumulating results for pretty printing the performance results*/
		
		System.out.println("Time taken for Insert Operation");
		System.out.println("Number_of_Records\tNCBST\tRWBST\tHOH\tCOW");
		for(int maxRange=51;maxRange<umax;maxRange+=500){
			ArrayList<Long> temp = hm.get(maxRange);
			System.out.print(maxRange-minRange+"\t");
			for(int i=0;i<temp.size();i++){
				
				System.out.print(temp.get(i)+"\t");
			}
			System.out.println("");
		}
		
		System.out.println("Time taken for Delete Operation");
		System.out.println("Number_of_Records\tNCBST\tRWBST\tHOH\tCOW");
		for(int maxRange=51;maxRange<umax;maxRange+=500){
			ArrayList<Long> temp = hd.get(maxRange);
			System.out.print(maxRange-minRange+"\t");
			for(int i=0;i<temp.size();i++){
				
				System.out.print(temp.get(i)+"\t");
			}
			System.out.println("");
			
			
		}
		
		System.out.println("Time taken for Lookup Operation");
		System.out.println("Number_of_Records\tNCBST\tRWBST\tHOH\tCOW");
		for(int maxRange=51;maxRange<umax;maxRange+=500){
			ArrayList<Long> temp = hlook.get(maxRange);
			System.out.print(maxRange-minRange+"\t");
			for(int i=0;i<temp.size();i++){
				
				System.out.print(temp.get(i)+"\t");
			}
			System.out.println("");
			
			
		}
		
	    }else if("t".equals(input)){
	    	System.out.println("Time comparision for sequential and parallel depth first traversal of Binary Search tree");
	    	System.out.println("Number_of_Records\tSequential_traverse\tParallel_traverse");
	    	for(int maxRange=51;maxRange<umax;maxRange+=500){
	    		
	    		/**************************************** Parallel Depth First Traversal test ***************************************/
	    				
	    				int totalNumber = maxRange-minRange;
	    				int [] randomArr = new int[maxRange-minRange];
	    				ArrayList<ArrayList> buckets = new ArrayList<ArrayList>(NUM_THREADS);
	    				for(int i=0;i<NUM_THREADS;i++)
	    					buckets.add(new ArrayList());
	    				for(int i=0,j=0;i<randomArr.length;i++){
	    					randomArr[i] = 1+(int)(Math.random()*maxRange);
	    					buckets.get(i%NUM_THREADS).add(randomArr[i]);
	    				}
	    	NonConcurrentBST<Integer> ncbst = new NonConcurrentBST<Integer>();
			for(int i = 0; i<randomArr.length; i++){
				ncbst.insert(randomArr[i]);
			}
			System.out.println(totalNumber+"\t"+TestNonConcurrentBST(ncbst)+"\t"+TestConcurrentDFBST(ncbst));
			
	    }
	    	
	    }
	}

	/**
	 * @param tree: This method takes a Non Concurrent Binary Search tree as Input 
	 * and traverses the Binary search tree parallely
	 * @return Long : This method returns a Long which represents the time taken to Depth First Traverse the 
	 * Binary Search Tree Parallely
	 */
	private static Long TestConcurrentDFBST(NonConcurrentBST<Integer> tree) {
		ParallelDepthFirstSearch con= new ParallelDepthFirstSearch();

		long endTime = 0;
		long startTime = System.currentTimeMillis();
		con.ConcurrentTraverse(tree.getRoot());
		endTime = System.currentTimeMillis();
		return endTime-startTime;
		
	}

	/**
	 * @param tree: This method takes a Non Concurrent Binary Search tree as Input 
	 * and traverses the Binary search tree sequentially
	 * @return Long : This method returns a Long which represents the time taken to Depth First Traverse the 
	 * Binary Search Tree sequentially
	 */
	private static Long TestNonConcurrentBST(NonConcurrentBST<Integer> tree) {
		long endTime = 0;
		long startTime = System.currentTimeMillis();
		ParallelDepthFirstSearch.sequentialTraverse(tree.getRoot(), new ArrayList<Integer>(1));
		endTime = System.currentTimeMillis();
		return endTime-startTime;
	}

}