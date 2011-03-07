package multir.preprocess;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class WriteFeaturesToFile {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	//static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subsetSpan5.100.pb.gz";
	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subsetSpan1.10.pb.gz";
	
	//static String output = dir + "/fts/ftsSpan5.100.gz";
	static String output = dir + "/fts/ftsSpan1.10.gz";
	
	public static void main(String[] args) throws IOException {
		
		{
			//BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
			//		new FileOutputStream(output), "utf-8"));
			DelimitedWriter w = new DelimitedWriter(output, "utf-8", true);
			
		    InputStream is = new GZIPInputStream(new BufferedInputStream
		    		(new FileInputStream(input1)));
		    Relation r = null;
		    
		    int count = 0;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	if (++count % 10000 == 0) System.out.println(count);
		    	
		    	for (int m = 0; m < r.getMentionCount(); m++) {
		    		RelationMentionRef rmf = r.getMention(m);
		    		for (int f = 0; f < rmf.getFeatureCount(); f++) {
		    			//w.write(rmf.getFeature(f));
		    			//w.write('\n');
		    			w.write(rmf.getFeature(f));
		    		}
		    	}
		    }
		    w.close();
		    is.close();
		}
		/*
		Sort.MEMORY = 4l * 1024l * 1024l *1024l;
		
	    Sort.sort(output, output + ".sorted", dir, new Comparator<String[]>() {
	    	public int compare(String[] t1, String[] t2) {
	    		return t1[0].compareTo(t2[0]);
	    	}});

	    
	    int minThresh = 3;//1;
	    int maxThresh = 3;//5;
	    {
	    	for (int thresh=minThresh; thresh <= maxThresh; thresh++) {
			    // write unique features (with at least 2 occurances)
			    DelimitedWriter w = new DelimitedWriter(output + ".unique" + thresh);
			    DelimitedReader r = new DelimitedReader(output + ".sorted");
			    String[] t = null;
			    int count = 0;
			    String last = "";
			    while ((t = r.read())!= null) {
			    	if (t[0].equals(last)) {
			    		count++;
			    	} else {
			    		if (count >= thresh)
			    			w.write(last);
			    		last = t[0];
			    		count = 1;
			    	}
			    }
			    if (count >= minThresh)
			    	w.write(last);
				r.close();
			    w.close();
	    	}
	    }
	    
		//output = dir + "/ttt";
		/*
	    {
		    // write unique features (with at least 2 occurances)
		    DelimitedWriter w = new DelimitedWriter(output + ".unique2");
		    DelimitedReader r = new DelimitedReader(output + ".sorted");
		    String[] t = null, p = null;
		    String lastWritten = "";
		    while ((t = r.read())!= null) {
		    	if (p != null) {
		    		if (t[0].equals(p[0]) && !lastWritten.equals(t[0])) {
		    			w.write(t[0]);
		    			lastWritten = t[0];
		    		}
		    	}
		    	p = t;
		    }
			r.close();
		    w.close();
	    }
	    */

	}
	
}
