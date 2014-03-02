package parallel;


/**
 * @author SRoy
 *
 */
public class Sort implements Runnable {
	
	long[] input;
	int start;
	int end;
	long scratch[];
	
	public Sort(long[] input, int start, int end,long scratch[]) {
		this.input=input;
		this.start=start;
		this.end=end;
		this.scratch=scratch;
	}
	
	public void run() {
		//implement the run method
		MergeSortTest.seqMergeSort(input, start, end, scratch);
	}
}
