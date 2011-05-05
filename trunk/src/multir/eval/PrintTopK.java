package multir.eval;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import multir.eval.GenerateFullSententialPrecisionRecallCurve.Example;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class PrintTopK {

	//static String testFile = "/projects/pardosa/data14/raphaelh/t/data/test-Multiple.pb.gz";
	//static String exp = "/test-neg10-min5-8706";
	//static String exp = "/test-neg1-min1-8706";
	//static String exp = "/test-neg10-min1-06";
	//static String exp = "/test-neg10-min2-0506";
	static String exp = "";
	
	static String testFile = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";
	static String resultsFile = "/projects/pardosa/data14/raphaelh/t/" + exp + "/results";
	//static String target = "/location/location/contains";
	static String target = "/people/person/children";
	//static String target = "/business/company_advisor/companies_advised";
	//static String target = "/people/person/religion";
	
	public static void run(String exp, String target) throws IOException {
		PrintTopK.target = target;
		//targetsSet = new HashSet<String>();
		//for (String t : targets) targetsSet.add(t);
		PrintTopK.resultsFile = "/projects/pardosa/data14/raphaelh/t" + exp + "/results";
		main(new String[0]);
	}
		
	public static void main(String[] args) throws IOException {
		
		Map<Integer,String> relID2rel = new HashMap<Integer,String>();
		
		List<Example> predictions = new ArrayList<Example>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(resultsFile), "utf-8"));
			String[] h = r.readLine().split(" ");
			for (int i=0; i < h.length; i++) relID2rel.put(i, h[i]);
			
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");

				
				//final String[] rels = c[3].split(" ");
				final String[] scores = c[5].split(" ");
				/*
				Integer[] pos = new Integer[scores.length];
				for (int i=0; i < pos.length; i++) pos[i] = i;
				Arrays.sort(pos, new Comparator<Integer>() {
					public int compare(Integer i1, Integer i2) {
						double d1 = Double.parseDouble(scores[i1]);
						double d2 = Double.parseDouble(scores[i2]);
						if (d1 > d2) return -1; else return 1;
					}
				});
				*/
				
				for (int i=0; i < scores.length; i++) {
					Example e = new Example();
					e.arg1 = c[0];
					e.arg2 = c[1];
					e.mentionID = Integer.parseInt(c[2]);
					
					//int p = pos[i];
					//e.predRelation = rels[p];
					e.predRelation = relID2rel.get(i);
					//e.predScore = Double.parseDouble(scores[i]) - Double.parseDouble(scores[0]);
					//e.predScore = Double.parseDouble(scores[p]);
					//e.rank = i;
					
					// HACK; diff rel - NA
					e.predScore = Double.parseDouble(scores[i]); // - Double.parseDouble(scores[0]);
					//e.rank = 0;
					//if (e.rank == 0)
					if (e.predRelation.equals(target))					
						predictions.add(e);
				}
			}
			r.close();
		}
		
		// sort predictions by decreasing score
		Collections.sort(predictions, new Comparator<Example>() {
			public int compare(Example e1, Example e2) {
				if (e1.predScore > e2.predScore) return -1; else return 1;
			}
		});

		
		Map<String,String> key2snt = new HashMap<String,String>();
		/*
		{
		    InputStream is = new GZIPInputStream(
		    		new BufferedInputStream
		    		(new FileInputStream(testFile)));
		    Relation r = null;

		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	String arg1 = r.getSourceGuid();
		    	String arg2 = r.getDestGuid();
		    	for (int m = 0; m < r.getMentionCount(); m++) {
		    		RelationMentionRef rmf = r.getMention(m);
		    		key2snt.put(arg1 + "\t" + arg2 + "\t" + m, rmf.getSentence());			    	
		    	}
		    }
		    is.close();
		}
		*/
		
		{
			// load sentences
			Map<String,Integer> snts = new HashMap<String,Integer>();
			Set<Integer> sentenceIDs = new HashSet<Integer>();
			{
			    InputStream is = new GZIPInputStream(
			    		new BufferedInputStream
			    		(new FileInputStream(testFile)));
			    Relation r = null;
	
			    while ((r = Relation.parseDelimitedFrom(is))!=null) {
	
			    	String arg1 = r.getSourceGuid();
			    	String arg2 = r.getDestGuid();
			    	for (int m = 0; m < r.getMentionCount(); m++) {
			    		RelationMentionRef rmf = r.getMention(m);
			    		int sentenceID = Integer.parseInt(rmf.getFilename());
				    	snts.put(arg1 + "\t" + arg2 + "\t" + m, sentenceID);
				    	sentenceIDs.add(sentenceID);
			    	}
			    }
			    is.close();
			}
			Map<Integer,String> snts2 = new HashMap<Integer,String>();
			{
				BufferedReader r =new BufferedReader(new InputStreamReader
						(new FileInputStream("/projects/pardosa/data14/raphaelh/t/raw/sentences.07"), "utf-8"));
				String l = null;
				while ((l = r.readLine())!= null) {
					String[] c = l.split("\t");
					int sentenceID = Integer.parseInt(c[0]);
					if (sentenceIDs.contains(sentenceID)) snts2.put(sentenceID, c[1]);
				}			
				r.close();
			}
			for (Map.Entry<String,Integer> e : snts.entrySet())
				key2snt.put(e.getKey(), snts2.get(e.getValue()));
		}
		
		// print top-K
		for (int i=0; i < 25; i++) {
			Example e = predictions.get(i);
			String snt = key2snt.get(e.arg1 + "\t" + e.arg2 + "\t" + e.mentionID);
			//Integer sentenceID = snts.get(e.arg1 + "\t" + e.arg2 + "\t" + e.mentionID);
			//String snt = snts2.get(sentenceID);
			System.out.println("[" + e.arg1 + "," + e.arg2 + "]\t" + e.predScore + "\t" + snt);
		}
	}
}
