package wikiPageRank;

import java.io.IOException;


import java.util.Iterator;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

/**
 * @author SRoy
 *
 */
public class RankConfigureOutputReducer extends MapReduceBase implements Reducer<FloatWritable, Text, FloatWritable, Text>{
	
	public void reduce(FloatWritable key, Iterator<Text> value, OutputCollector<FloatWritable, Text> output, Reporter rep) throws IOException {
		while(value.hasNext()){
			output.collect(key, new Text(value.next().toString()));
		}
	}
}
