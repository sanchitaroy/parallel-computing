package recursiveParallel;


import parallel.MPParallelInRandomGenCl;

import java.lang.management.ManagementFactory;

import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;
import static java.lang.System.err;

/**
 * @author SROY
 * Each thread calls parallel merge sort iteratively
 *
 */
public class MPRecursiveMergeSortTest {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
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
		//IMPLEMENT ME!!!
		long[] finalInputArr;
		long[] inputArr = input;
		int inputStart = 0;
		int inputEnd = inputArr.length;
		int threadC = arg;
		long[] scr = new long[inputArr.length];
		Lock loc = new ReentrantLock();

		if(threadC < 2) {
			seqMergeSort(inputArr, inputStart, inputEnd, scr); 
		}
		else if (inputArr.length >= 2){			
			int size = inputEnd - inputStart;
			int pivot = (inputEnd - inputStart) / 2 + inputStart;
			// split array in half
			long[] left  = Arrays.copyOfRange(inputArr, inputStart, inputEnd/ 2);
			long[] right = Arrays.copyOfRange(inputArr, inputEnd / 2, inputEnd);

			//recursive call to parallel merge sort
			Thread lThread = new Thread(new MPRecursiveSort(left,  threadC / 2, 0, left.length, new long[left.length]));
			Thread rThread = new Thread(new MPRecursiveSort(right, threadC / 2, 0, right.length, new long[right.length]));
			lThread.start();
			rThread.start();

			try {
				lThread.join();
				rThread.join();
			} catch (InterruptedException ie) {
				System.out.println("thread error");
			}

			loc.lock();
			try {
				finalInputArr = mergeThreads(left, right);	
				merge(finalInputArr, inputArr, start, pivot, inputEnd);
			}
			finally {
				loc.unlock();
			}
		}		
	}

	public static long[] mergeThreads(long[] left, long[] right){
		long[] finalInputArr = new long[left.length + right.length];
		System.arraycopy(right, 0, finalInputArr, left.length, right.length);
		return finalInputArr;
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
				long startTime = System.nanoTime();
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
		int ppr = POINTS_PER_RUN/2;
		int newNPROCS = NPROCS-2;//since 2 threads we are using up here
		int start = ppr;
		for (int rep = 0; rep < SAMPLES; rep++) {
			Thread lParallelThread = new Thread(new MPParallelInMain(ppr, newNPROCS, SEED, timedParallelSort, 0, size));
			Thread rParallelThread = new Thread(new MPParallelInMain(POINTS_PER_RUN, newNPROCS, SEED, timedParallelSort, start, size));
			lParallelThread.start();
			rParallelThread.start();
			try {
				lParallelThread.join();
				rParallelThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				err.println("error in thread join in main");
				e.printStackTrace();
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
	
	public static void parallelInMain(int ppr, int newPROCS, long SEED, long[] timedParallelSort, int start, int[] size){
		for (int i = start; i < ppr; i++) {
			long[] data = fillRandom(size[i], SEED);
			long startTime = System.nanoTime();
			//System.out.println("sanchita test stop");
			parallelMergeSort(data, 0, data.length, new long[data.length],newPROCS);
			timedParallelSort[i] += (System.nanoTime() - startTime);
			if (!isSorted(data, 0, size[i])){
				err.println("Error: parallel merge sort");
			}
			out.print('.');
		}
		
	}

}
