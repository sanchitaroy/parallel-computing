package wikiPageRank;



import java.io.FileWriter;



import java.io.IOException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;


/**
 * @author SRoy
 * Also produces an inlink graph for all pages of the dump
 *
 */
public class ExtractWikiPageReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
	   
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        String pgTab = "\t";
        //FileWriter out = new FileWriter("/home/hduser/hadoopExamples/output/output1.txt", true);

        boolean first = true;
        while(values.hasNext()){
            if(!first) pgTab += "\t";
            
            pgTab += values.next().toString();
            first = false;
        }
        
        output.collect(key, new Text(pgTab));
        //out.write("page  " + key.toString() + " otherPage " + otherPage+"\n");
    }
}
