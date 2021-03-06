package wikiPageRank;

import java.io.IOException;



import java.nio.charset.CharacterCodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

/**
 * @author SRoy
 * Extracts the relevant wiki pages. excludes inter-wiki links, non existing pages in the dump
 *
 */
public class ExtractWikiPageMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    
    private static final Pattern matchWikiLinkPattern = Pattern.compile("\\[.+?\\]");
        
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

        String[] titleAndText = parseTitleAndText(value);
        
        String pageString = titleAndText[0];

        if(notValidPage(pageString))
            return;
        
        Text page = new Text(pageString.replace(' ', '_'));

        Matcher matcher = matchWikiLinkPattern.matcher(titleAndText[1]);
        
        //Loop through the matched links in [CONTENT]
        while (matcher.find()) {
            String restPages = matcher.group();
            
            //Filter only wiki pages.
            //[[realPage|linkName]] or [realPage]
            //link to files or external pages.
            //link to paragraphs into other pages.
            restPages = getWikiPageFromLink(restPages);
            if(restPages == null || restPages.isEmpty()) { 
            	continue; 
            }
                
             // add valid otherPages to the map.
            output.collect(page, new Text(restPages)); 
            
        }
     }
    
    private boolean notValidPage(String pageString) {
        return pageString.contains(":");
    }

    private String getWikiPageFromLink(String theLink){
        if(isNotWikiLink(theLink)) return null;
        
        int start = theLink.startsWith("[[") ? 2 : 1;
        int endLink = theLink.indexOf("]");

        int pipePos = theLink.indexOf("|");
        if(pipePos > 0){
            endLink = pipePos;
        }
        
        int part = theLink.indexOf("#");
        if(part > 0){
            endLink = part;
        }
        
        theLink =  theLink.substring(start, endLink);
        theLink = theLink.replaceAll("\\s", "_");
        theLink = theLink.replaceAll(",", "");
        theLink = replaceAmp(theLink);
        
        return theLink;
    }
    
    private boolean isNotWikiLink(String theLink) {
        int start = 1;
        if(theLink.startsWith("[[")){
            start = 2;
        }
        
        if( theLink.length() < start+2 || theLink.length() > 100) return true;
        char firstChar = theLink.charAt(start);
        
        if( firstChar == '#') return true;
        if( firstChar == ',') return true;
        if( firstChar == '.') return true;
        if( firstChar == '&') return true;
        if( firstChar == '\'') return true;
        if( firstChar == '-') return true;
        if( firstChar == '{') return true;
        
        if( theLink.contains(":")) return true; // Matches: external links and translations links
        if( theLink.contains(",")) return true; // Matches: external links and translations links
        if( theLink.contains("&")) return true;
        
        return false;
    }
    
    private String replaceAmp(String theLinkTest) {
        if(theLinkTest.contains("&amp;"))
            return theLinkTest.replace("&amp;", "&");

        return theLinkTest;
    }

    private String[] parseTitleAndText(Text value) throws CharacterCodingException {
        String[] titleAndText = new String[2];
        
        int start = value.find("<title>");
        int end = value.find("</title>", start);
        start += 7; //add length of <title>
        
        titleAndText[0] = Text.decode(value.getBytes(), start, end-start);

        start = value.find("<text");
        start = value.find(">", start);
        end = value.find("</text>", start);
        start += 1;
        
        if(start == -1 || end == -1) {
            return new String[]{"",""};
        }
        
        titleAndText[1] = Text.decode(value.getBytes(), start, end-start);
        
        return titleAndText;
    }
}
