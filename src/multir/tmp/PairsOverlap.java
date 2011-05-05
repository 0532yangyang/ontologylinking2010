package multir.tmp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;

public class PairsOverlap {

	static String dir = "/projects/pardosa/data14/raphaelh/t/data";

	static String input = dir + "/subsetSpan1.100.pb.gz";
	//static String input = dir + "/subset05-2.100.pb.gz";
	static String input2 = dir + "/subset06.100.pb.gz";
	
	public static void main(String[] args) throws IOException {
		
		// percentage of pairs in test set which are contained in training set
		
		// put training set in map
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
		
		// iterate over training sets
		//for (int i=1; i <= 5; i++) {
		    int contained = 0;
		    InputStream is = new GZIPInputStream
		    	//(new BufferedInputStream(new FileInputStream(dir + "/subsetSpan" + i + ".100.pb.gz")));
		    	(new BufferedInputStream(new FileInputStream(input)));
		    Relation r = null;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	if (r.getRelType().equals("NA")) continue;
		    	String key = r.getSourceGuid() + "\t" + r.getDestGuid();
		    	if (hsTest.contains(key)) contained++;
		    }
		    is.close();
			System.out.println("xx" + ": contained " + contained + ", out of " + hsTest.size());
		//}
		
	}
	
}
