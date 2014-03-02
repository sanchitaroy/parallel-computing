package parallel;

import java.util.Random;

/**
 * @author SRoy
 *
 */
public class MPParallelInRandomGenCl implements Runnable{

	int start; 
	int end; 
	Random r;
	long[] data;

	public MPParallelInRandomGenCl(int start, int end, Random r, long[] data){
		this.start = start;
		this.end = end;
		this.r = r;
		this.data = data;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		MPMergeSortTest.MPParallelRandomGen(start, end, r, data);

	}

}
