package recursiveParallel;


/**
 * @author SRoy
 *
 */
public class MPParallelInMain implements Runnable{

	int ppr;
	int newPROCS;
	long SEED;
	long[] timedParallelSort; 
	int start;
	int[] size;

	public MPParallelInMain(int ppr, int newPROCS, long SEED, long[] timedParallelSort, int start, int[] size){
		this.newPROCS = newPROCS;
		this.ppr = ppr;
		this.SEED = SEED;
		this.timedParallelSort = timedParallelSort;
		this.start = start;
		this.size = size;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		MPRecursiveMergeSortTest.parallelInMain(ppr, newPROCS, SEED, timedParallelSort, start, size);

	}

}
