package wikiPageRank;

import java.io.IOException;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;


/**
 * @author SRoy
 * Initializes each page by 1/n.
 *
 */
public class CalcPrepMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{
   
	private static double pgCount;
	public void configure(JobConf job){	   
		String name = job.get("NumberOfPages");
		pgCount = Double.valueOf(name);	  
   }
	
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		double temp = 1.0/pgCount;
		BigDecimal bd = new BigDecimal(temp);
		String rank = bd.toPlainString();
		
		int pageTabIndex = value.find("\t");

        String page = Text.decode(value.getBytes(), 0, pageTabIndex);
        int a = value.getLength();
        String otherPages = Text.decode(value.getBytes(), pageTabIndex+2, value.getLength()-(pageTabIndex+2));
        Text pageAndRank = new Text(rank + "\t" + otherPages);
        
        output.collect(new Text(page), new Text(pageAndRank));
    }
}
