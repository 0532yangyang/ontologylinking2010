package multir.preprocess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class CreateSubsetDataset {

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/data";

	static String input = dir + "/featurizedData.pb";
	//static String input = dir + "/subsetSpan5.100.pb.gz";
	//static String output = dir + "/subset87-05.100.pb";
	//static String output = dir + "/subset05.100.pb.gz";
	//static String output = dir + "/subsetTrain.pb.gz";
	static String output = dir+"/subsetTest.pb.gz";
	// 2005: start articleID 1638661.xml
	static int start2005 = 40962186;
	// 2006: start articleID 1728666.xml
	static int start2006 = 43425267;
	// 2007: start articleID 1815718.xml
	static int start2007 = 45961658;
	
	static int start2005_2 = 42193727;
	
	// split 1987 to 2005 (incl) into 5 chunks
	// end is always 43425267 (start2006)
	static int startSpan1 = 0;
	static int startSpan2 = 8685053;
	static int startSpan3 = 17370107;
	static int startSpan4 = 26055161;
	static int startSpan5 = 34740214;
		
	/*
	//OLD:
	// 2004
	static int startArticleId = 1547299;
	static int endEndArticleId = 1638461;
	static int startSentenceId = 46560915;
	static int endSentenceId = 48996899;
	
	// split 2004 into training and test set
	static int trainStartSentenceID2004 = 46560915;
	static int trainEndSentenceID2004 = 47778907;
	static int testStartSentenceID2004 = 47778907;
	static int testEndSentenceID2004 = 48996899;
	// 2005
	
	// NYT05-06
	// split 80/20  24months: 20 training, 4 test
	//   train: Jan-05 - Aug-05
	//   test: Sept-06 - Dec-06
	
	//   train: 1638661 - 1786685 (excl.)
	//   test: 1786685 - 1815718 (excl.)
	
	static int startSentenceID2005 = 48996899;
	static int startSentenceID2006 = 51719787;
	static int startSentenceID2007 = 54331360;
	static int endSentenceID2006 = 54331360;

	static int startSentenceID2005_2 = 50358343;
	*/
	
	
	// divide 51719787 into 5 blocks, a 10343957.4
	/*
	2003-2005
	2000-2005
	1997-2005
	1994-2005
	1991-2005
	1987-2005
	*/
	
	public static void main(String[] args) throws IOException {
		Random random = new Random();
		
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(
				new FileOutputStream(output)));		
	    InputStream is = new GZIPInputStream
	    	(new BufferedInputStream(
	    		new FileInputStream(input)));
	    Relation r = null;
	    
	    int count = 0;
	    
	    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    
	    int pos = 0, neg = 0;
	    
		Builder relBuilder = null;
	    while ((r = Relation.parseDelimitedFrom(is))!=null) {
	    	if (++count % 10000 == 0) System.out.println(count);

	    	relBuilder = Relation.newBuilder();
	    	// need to iterate over mentions, keep only those in the range 
	    	relBuilder.setRelType(r.getRelType());
	    	relBuilder.setSourceGuid(r.getSourceGuid());
	    	relBuilder.setDestGuid(r.getDestGuid());
	    	if (r.getRelType().equals("NA")) neg++; else pos++;

	    	//if (r.getRelType().equals("NA"))
	    	//	if (random.nextDouble() < .9) continue;
	    	//	if (random.nextDouble() < .5) continue;
	    	
	    	for (int i=0; i < r.getMentionCount(); i++) {
	    		RelationMentionRef rmf = r.getMention(i);
	    		//System.out.println(rmf.getFilename());
	    		int sentenceID = Integer.parseInt(rmf.getFilename());
	    		min = Math.min(min, sentenceID);
	    		max = Math.max(max, sentenceID);

	    		//if (sentenceID >= trainStartSentenceID2004 &&
	    		//		sentenceID < trainEndSentenceID2004) 
	    		//if (sentenceID < startSentenceID2007 &&
	    		//		sentenceID >= startSentenceID2006)
	    		//if (sentenceID >= startSentenceID2005_2 && 
	    		//		sentenceID < startSentenceID2006)
	    			
	    		//if (sentenceID >= startSpan5 && sentenceID < start2006)
	    		//if (sentenceID >= start2006 && sentenceID < start2007)
	    		//if (sentenceID >= start2005 && sentenceID < start2006)
	    		//if (sentenceID >= start2007)
	    		//if (sentenceID >= start2005-2 && sentenceID < start2006)
	    		if(sentenceID>start2006){
	    			relBuilder.addMention(rmf);
	    		}
	    	}
    		if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
    			relBuilder.build().writeDelimitedTo(os);
		}
		is.close();
		os.close();
		System.out.println("min " + min);
		System.out.println("max " + max);
		System.out.println("pos " + pos);
		System.out.println("neg " + neg);
	}
}
