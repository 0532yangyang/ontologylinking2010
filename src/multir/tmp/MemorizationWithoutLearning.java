package multir.tmp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;

public class MemorizationWithoutLearning {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	static String input = dir + "/data/subsetSpan1.100.pb.gz";
	//static String input = dir + "/subset05-2.100.pb.gz";
	static String input2 = dir + "/data/subset06.100.pb.gz";
	
	public static void main(String[] args) throws IOException {

		// memorize all pairs in training set
		HashSet<String> hsTest = new HashSet<String>();
		{
		    InputStream is = new GZIPInputStream
		    	(new BufferedInputStream(new FileInputStream(input2)));
		    Relation r = null;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {		    	
		    	if (r.getRelType().equals("NA")) continue;
		    	hsTest.add(r.getSourceGuid() + "\t" + r.getDestGuid());
		    }
		    is.close();
		}

		HashSet<String> intersect = new HashSet<String>();
		{
		    InputStream is = new GZIPInputStream
		    	(new BufferedInputStream(new FileInputStream(input)));
		    Relation r = null;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	if (r.getRelType().equals("NA")) continue;
		    	String key = r.getSourceGuid() + "\t" + r.getDestGuid();
		    	if (hsTest.contains(key)) 
		    		intersect.add(key);
		    }
		    is.close();
		}
		
		// compute performance on test set, on a pair level
		{
			int tp = 0;
			int fp = 0;
			int fn = 0;
			
		    InputStream is = new GZIPInputStream
		    	(new BufferedInputStream(new FileInputStream(input2)));
		    Relation r = null;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	// not possible that we predict too many		    	
		    	if (r.getRelType().equals("NA")) continue;
		    	
		    	int rels = r.getRelType().split(",").length;
		    	
		    	String key = r.getSourceGuid() + "\t" + r.getDestGuid();
		    	if (intersect.contains(key))
		    		tp += rels;
		    	else
		    		fn += rels;
		    }
		    is.close();

		    double recall = tp / (double)(tp + fn);
		    double precision = 1;
		    
		    System.out.println("recall " + recall + ", precision " + precision);
		}
		
		// compute performance on test set, on a sentence level
		
		
		
		
		
		// 1. overall
		
		
		
		
		// 2. only where fb has label
		
		
	}
}
