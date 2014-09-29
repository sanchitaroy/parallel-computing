package concurrentBST;

import static java.lang.System.err;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import commonFiles.*;

/**
 * Parallel Implementation of the Binary Search Tree traversal
 * In this implementation we use 2 queues.One to track the parent Nodes and another to track
 * child nodes.
 * numprocs field is used to track the number of processors available for usage
 *
 */
public class ParallelDepthFirstSearch {

	Queue<QNode> parentQueue = new LinkedList<QNode>();
	Queue<QNode> childQueue = new LinkedList<QNode>();

	int numProcs = Runtime.getRuntime().availableProcessors(); 

	/**
	 * Implementation of the Sequential Traversal of the binary search tree 
	 * @param root
	 * @param traverseArr
	 */
	public static void sequentialTraverse(BSTNode root, ArrayList traverseArr){

		if(null == root){
			return;
		}	
		traverseArr.add(root.getKey());
		sequentialTraverse(root.getLeft(), traverseArr);
		sequentialTraverse(root.getRight(), traverseArr);
	}

	/**
	 * prepareQueue method initializes the parent and child queues by adding nodes
	 * until the numprocs of child queue nodes become 1.
	 * @param n
	 */
	public void prepareQueue(QNode n){

		QNode leftChild;
		QNode rightChild;

		if(n.getNode().getLeft() != null && n.getNode().getRight() != null){
			if(n.getProcs() > 1){
				childQueue.remove();
				numProcs = numProcs/2;
				parentQueue.add(n);

				leftChild = new QNode(n.getNode().getLeft(), numProcs);
				rightChild = new QNode(n.getNode().getRight(), numProcs);

				childQueue.add(leftChild);
				childQueue.add(rightChild);	

				prepareQueue(childQueue.element());
			}
		}
		else if(n.getNode().getLeft() == null && n.getNode().getRight() != null){
			if(n.getProcs() > 1){
				childQueue.remove();
				parentQueue.add(n);

				rightChild = new QNode(n.getNode().getRight(), numProcs);

				childQueue.add(rightChild);
				
				prepareQueue(childQueue.element());

			}
		}
		else if(n.getNode().getRight() == null && n.getNode().getLeft() != null){
			if(n.getProcs() > 1){
				childQueue.remove();
				parentQueue.add(n);

				leftChild = new QNode(n.getNode().getLeft(), numProcs);

				childQueue.add(leftChild);

				prepareQueue(childQueue.element());
			}
		}
		else{
			childQueue.remove();
			parentQueue.add(n);
			
		}
	}
	
	/**
	 * printDFT prints pre order of the Binary Search Tree after the Parallel Depth Search Traversal
	 * @param arr
	 */
	public void printDFT(ArrayList<ArrayList<Object>> arr){
		
		int i = 0;
		
		while(!parentQueue.isEmpty()){
			if(parentQueue.element().getProcs() >2){
				System.out.print(parentQueue.element().getNode().getKey() + "\t");
				parentQueue.remove();				
			}
			else {
				System.out.print(parentQueue.element().getNode().getKey() + "\t");
				if(parentQueue.element().getNode().getLeft() != null && parentQueue.element().getNode().getRight() != null){
					for(int j = 0; j <arr.get(i).size(); j++){					
						System.out.print(arr.get(i).get(j) + "\t");
					}
					i++;
				}
				parentQueue.remove();
				for(int j = 0; j <arr.get(i).size(); j++){					
					System.out.print(arr.get(i).get(j) + "\t");
				}
				i++;
			}
			
		}
		
	}
	
	/**
	 * printSeqDFT prints the sequential Depth first traversal of the binary search tree
	 * @param traverseArr
	 */
	public void printSeqDFT(ArrayList<Object> traverseArr){
		for(int j = 0; j<traverseArr.size(); j++){
			System.out.print(traverseArr.get(j) + "\t");
		}
	}
	
	/**
	 * ConcurrentTraverse traverses the Binary Search Tree in a Depth First fashion parllely 
	 * 
	 * @param root
	 */
	public void ConcurrentTraverse(BSTNode root){
		if(numProcs < 2){
			ArrayList<Object> traverseArr = new ArrayList<Object>(1);
			sequentialTraverse(root, traverseArr);
			//printSeqDFT(traverseArr);
		}

		else{

			QNode p = new QNode(root, numProcs);
			childQueue.add(p);				
			QNode n = childQueue.element();

			prepareQueue(n);	

			ArrayList<ArrayList<Object>> arr = new ArrayList<ArrayList<Object>>();
			ArrayList<Thread> DFTThreads = new ArrayList<Thread>();
			int i = 0;

			while(!childQueue.isEmpty()){
				ArrayList<Object> traverseArr = new ArrayList<Object>(1);
				arr.add(i, traverseArr);
				DFTThreads.add(new Thread(new SeqTraverse(childQueue.element().getNode(), arr.get(i))));
				DFTThreads.get(i).start();
				childQueue.remove();
				i++;				
			}

			for (int j = 0; j < DFTThreads.size(); j++){
				try {
					DFTThreads.get(j).join();
				} catch (InterruptedException e) {
					err.print("error in thread join\n");
					e.printStackTrace();
				}

			}			
		}	
	}
}