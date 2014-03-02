package recursiveParallel;
/**
 * @author SRoy
 *
 */
public class MPRecursiveSort implements Runnable {
	
	private long[] a;
	private int threadCount;
	private int start;
	private int end;
	private long scratch[]; 
	
	public MPRecursiveSort(long[] a, int threadCount, int start, int end, long scratch[]) {
		this.a = a;
		this.threadCount = threadCount;
		this.start = start;
		this.end = end;
		this.scratch = scratch;
	}
	
	public void run() {
		//implement the run method
		MPRecursiveMergeSortTest.parallelMergeSort(a, start, end, scratch, threadCount);
	}
}
