package parallel;

import static java.lang.System.err;


import static java.lang.System.out;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Random;

/**
 * @author SRoy
 *
 */
public class MPMergeSortTest {
	private static int SAMPLES;
	private static int POINTS_PER_RUN;
	private static int INITIAL_SIZE;
	private static int NPROCS;

	private final static long SEED = 353L;

	public static long[] fillRandom(int size, long seed) {
		long[] data = new long[size];
		Random r = new Random(seed);
		Thread[] MPThreads=new Thread[NPROCS];
		int len = size/NPROCS;
		int start = 0;
		int end = 0;
		int i;

		//run random number generation in NPROCS number of threads. Join back to return collated data
		for(i=0;i<NPROCS-1;i++)
		{
			start = i*len;
			end = (i+1)*len;
			MPThreads[i]=new Thread(new MPParallelInRandomGenCl(start, end, r, data));
			MPThreads[i].start();
		}
		MPThreads[i]=new Thread(new MPParallelInRandomGenCl(i*len,size, r, data));
		MPThreads[i].start();

		for(i=0;i<NPROCS;i++)
		{
			try {
				MPThreads[i].join();
			} catch (InterruptedException e) {
				err.print("error in thread join || random generation");
				e.printStackTrace();
			}
		}
		return data;		
	}	

	public static void MPParallelRandomGen(int start, int end, Random r, long[] data){

		for (int i = start; i < end; ++i) {
			data[i] = r.nextLong();
		}
	}

	public static boolean isSorted(long[] data, int start, int end) {
		for (int i = start + 1; i < end; i++) {
			if (data[i - 1] > data[i]) {
				System.out.println("Error:  i=" + i + " start= " + start
						+ " end=" + end + " data[i-1]=" + data[i - 1]
								+ " data[i]=" + data[i]);
				return false;
			}
		}
		return true;
	}

	public static void seqMergeSort(long[] input, int start, int end,
			long scratch[]) {
		int size = end - start;
		if (size < 2)
			return;
		int pivot = (end - start) / 2 + start;
		seqMergeSort(input, start, pivot, scratch);
		seqMergeSort(input, pivot, end, scratch);
		System.arraycopy(input, start, scratch, start, size);
		merge(scratch, input, start, pivot, end);
	}

	static void merge(long[] src, long[] dest, int start, int pivot, int end) {
		int l = start;
		int r = pivot;
		int i = start;
		while (l < pivot && r < end) {
			if (src[l] <= src[r])
				dest[i++] = src[l++];
			else
				dest[i++] = src[r++];
		}
		while (l < pivot) {
			dest[i++] = src[l++];
		}
		while (r < end) {
			dest[i++] = src[r++];
		}
	}

	public static void parallelMergeSort(final long[] input, final int start,
			int end, final long scratch[], final int arg) {

		if(arg<2)
		{
			MergeSortTest.seqMergeSort(input, start, end, scratch);
		}
		else
		{
			int i=0;
			int divArr=end/arg;
			Thread[] parallelThread=new Thread[arg];
			for(i=0;i<arg-1;i++)
			{
				parallelThread[i]=new Thread(new Sort(input,i*divArr,(i+1)*divArr,scratch));
				parallelThread[i].start();
			}
			parallelThread[i]=new Thread(new Sort(input,i*divArr,end,scratch));
			parallelThread[i].start();
			for(i=0;i<arg;i++)
			{
				try {
					parallelThread[i].join();
				} catch (InterruptedException e) {
					err.println("thread join error");
					e.printStackTrace();
				}
			}

			for(i=0;i<arg-2;i++)
			{	
				System.arraycopy(input, start, scratch, start,(i+2)*divArr );	
				merge(scratch, input, start, (i+1)*divArr, (i+2)*divArr);

			}
			System.arraycopy(input, start, scratch, start,end);
			merge(scratch, input, start, (i+1)*divArr, end);	
		}
	}

	public static void main(String[] args) {
		if (args.length < 3){
			err.println("Not enougg arguments.  Need  INITIAL_SIZE POINTS_PER_RUN SAMPLES");
			return;
		}

		long startTimeOfTotalRun = System.nanoTime();

		INITIAL_SIZE = Integer.parseInt(args[0]);
		POINTS_PER_RUN = Integer.parseInt(args[1]);
		SAMPLES = Integer.parseInt(args[2]); // number of times to run each test

		NPROCS = Runtime.getRuntime().availableProcessors();

		// initialize array containing sizes of arrays to sort. Double the size each time.
		int size[] = new int[POINTS_PER_RUN];
		for (int i = 0, s = INITIAL_SIZE; i < POINTS_PER_RUN; i++, s*=2) {
			size[i] = s;
		}

		// create arrays for holding measurements (nanosecs)
		long timedJavaSort[] = new long[POINTS_PER_RUN];
		long timedSeqSort[] = new long[POINTS_PER_RUN];
		long timedParallelSort[] = new long[POINTS_PER_RUN];

		/* Repeat each test SAMPLES time and accumulate results. Divide by
		 *    SAMPLES before printing to get average
		 */

		// Use sorting routing from java.util.Arrays.sort
		out.print("java.util.Arrays.sort");
		for (int rep = 0; rep < SAMPLES; rep++) {
			for (int i = 0; i < POINTS_PER_RUN; i++) {
				long[] data = fillRandom(size[i], SEED);
				long startTime = System.nanoTime();
				Arrays.sort(data);
				timedJavaSort[i] += (System.nanoTime() - startTime);
				if (!isSorted(data, 0, size[i])){
					err.println("Error: java.util.Arrays.sort");
				}
				out.print('.');
			}
		}
		out.println();

		// Sequential merge sort
		out.print("sequential merge sort");
		for (int rep = 0; rep < SAMPLES; rep++) {
			for (int i = 0; i < POINTS_PER_RUN; i++) {
				long[] data = fillRandom(size[i], SEED);
				//long[] data = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
				long startTime = System.nanoTime();
				//System.out.println("sanchita test stop");
				seqMergeSort(data, 0, data.length, new long[data.length]);
				timedSeqSort[i] += (System.nanoTime() - startTime);
				if (!isSorted(data, 0, size[i])){
					err.println("Error: sequential merge sort");
				}
				out.print('.');
			}
		}
		out.println();

		// Parallel merge sort
		out.print("parallel merge sort");
		for (int rep = 0; rep < SAMPLES; rep++) {
			for (int i = 0; i < POINTS_PER_RUN; i++) {
				long[] data = fillRandom(size[i], SEED);
				long startTime = System.nanoTime();
				parallelMergeSort(data, 0, data.length, new long[data.length],NPROCS);
				timedParallelSort[i] += (System.nanoTime() - startTime);
				if (!isSorted(data, 0, size[i])){
					err.println("Error: parallel merge sort");
				}
				out.print('.');
			}
		}
		out.println();

		// output formatted to be easily copied into a spreadsheet
		StringBuilder sb = new StringBuilder();
		OperatingSystemMXBean sysInfo = ManagementFactory
				.getOperatingSystemMXBean();
		sb.append("Architecture = ").append(sysInfo.getArch()).append('\n');
		sb.append("OS = ").append(sysInfo.getName()).append(" version ")
		.append(sysInfo.getVersion()).append('\n');
		sb.append("Number of available processors = ").append(NPROCS).append('\n');
		sb.append("size \t Java sort \t seq merge sort \t parallel merge sort \n");
		//divide by SAMPLES to get average.  
		for (int i = 0; i < POINTS_PER_RUN; ++i) {
			sb.append(size[i]).append('\t').
			append(timedJavaSort[i] / SAMPLES ).append('\t'). 
			append(timedSeqSort[i] / SAMPLES ).append('\t').
			append(timedParallelSort[i] / SAMPLES ).append('\n');
		}
		System.out.println(sb);

		System.out.println("Total time taken : "+ (System.nanoTime() - startTimeOfTotalRun));

	}
}
