package wikiPageRank;
import java.io.FileWriter;

import java.io.IOException;


import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;


/**
 * @author SRoy
 * calculates total number of valid titles using line count implementation
 *
 */
public class TotalPageCalculateMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable>{
         private final static IntWritable one = new IntWritable(1);
         private Text line = new Text("");
         public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
        	 output.collect(line, one);
         }
}
