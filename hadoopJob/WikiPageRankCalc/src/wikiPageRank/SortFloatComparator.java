package wikiPageRank;

import java.io.*;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @author SRoy
 * Taken from 
 * https://github.com/geofferyzh/Spreading-Activation/blob/master/src/multinodeactivation/SortFloatComparator.java
 * 
 * Sorts in descending order
 *
 */
public class SortFloatComparator extends WritableComparator {

	//Constructor.
	protected SortFloatComparator() {
		super(FloatWritable.class, true);
	}
	@SuppressWarnings("rawtypes")

	@Override
	public int compare(WritableComparable w1, WritableComparable w2) {
		FloatWritable k1 = (FloatWritable)w1;
		FloatWritable k2 = (FloatWritable)w2;
		return -1 * k1.compareTo(k2);
	}
}