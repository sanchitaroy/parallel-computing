package wikiPageRank;

import java.io.FileWriter;


import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

/**
 * @author SRoy
 *
 */
public class RankCalculateMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{
	
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        int pageTabIndex = value.find("\t");
        int rankTabIndex = value.find("\t", pageTabIndex+1);
        String page = Text.decode(value.getBytes(), 0, pageTabIndex);
        String pageWithRank = Text.decode(value.getBytes(), 0, rankTabIndex+1);
         
        // Mark page as Existing page 
        output.collect(new Text(page), new Text("!"));

        // Skip empty pages
        if(rankTabIndex == -1) return;
        
        String links = Text.decode(value.getBytes(), rankTabIndex+1, value.getLength()-(rankTabIndex+1));
        String[] allOtherPages = links.split("\t");
        int totalLinks = allOtherPages.length;
        
        for (String otherPage : allOtherPages){
            Text pageRankTotalLinks = new Text(pageWithRank + totalLinks);
            output.collect(new Text(otherPage), pageRankTotalLinks);
        }       
        // Put only original links 
        output.collect(new Text(page), new Text("|"+links));
    }
}
