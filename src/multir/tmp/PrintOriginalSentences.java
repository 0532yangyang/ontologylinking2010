package multir.tmp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multir.preprocess.Mappings;
import multir.util.delimited.DelimitedReader;

public class PrintOriginalSentences {
	
	static String dir = "/projects/pardosa/data14/raphaelh/t";

	//static String input1 = dir + "/data/subset05-2.100.pb.gz";
	static String input1 = dir + "/raw/sentences";
	static String input2 = dir + "/ftexp/topFalse";
	//static String input3 = "/projects/pardosa/data16/raphaelh/tmp/ecmlOrigRelationsFull";
	static String input3 = dir + "/ftexp/mapping"; 
	static String output = dir + "/ftexp/topFalseDetails";
	
	public static void main(String[] args) throws IOException {
		
		
		class QPE {
			int iteration;
			String arg1;
			String arg2;
			int mentionID;
			int relID;
			String sentence;
			String relation;
			QPE(int iteration, String arg1, String arg2, int mentionID, int relID) {
				this.iteration = iteration;
				this.arg1 = arg1;
				this.arg2 = arg2;
				this.mentionID = mentionID;
				this.relID = relID;
			}
		}
		
		List<QPE> l = new ArrayList<QPE>();
		//HashMap<String,List<QPE>> m = new HashMap<String,List<QPE>>();
		HashMap<Integer,List<QPE>> m = new HashMap<Integer,List<QPE>>();
		{
			DelimitedReader r = new DelimitedReader(input2);
			String[] t = null;
			while ((t = r.read())!= null) {
				QPE qpe = new QPE(Integer.parseInt(t[0]), t[1], t[2],
						Integer.parseInt(t[3]), Integer.parseInt(t[4]));
				l.add(qpe);
				//List<QPE> ml = m.get(t[1] + "\t" + t[2]);
				List<QPE> ml = m.get(qpe.mentionID);
				if (ml == null) {
					ml = new ArrayList<QPE>();
					//m.put(t[1] + "\t" + t[2], ml);
					m.put(qpe.mentionID, ml);
				}
				ml.add(qpe);
			}
			r.close();
		}

		
		// add relation name
		{
			Mappings ma = new Mappings();
			ma.read(input3);
			
			HashMap<Integer,String> relIdsToRel = new HashMap<Integer,String>();
			Map<String,Integer> tmp = ma.getRel2RelID();
			for (Map.Entry<String,Integer> e : tmp.entrySet()) {
				relIdsToRel.put(e.getValue(), e.getKey());
			}
			
			/*
			// read mapping rels to ids
			HashMap<Integer,String> relIdsToRel = new HashMap<Integer,String>();
			{
				DelimitedReader r = new DelimitedReader(input3);
				String[] t = null;
				while ((t = r.read())!= null) {
					String rel = t[1];
					Integer relId = Integer.parseInt(t[0]);
					relIdsToRel.put(relId, rel);
				}
				r.close();
			}*/
			for (QPE qpe : l)
				qpe.relation = relIdsToRel.get(qpe.relID);				
		}
		
		// add sentence
		/*
		{
			InputStream is = new GZIPInputStream(new BufferedInputStream
		    		(new FileInputStream(input1)));
		    Relation r = null;
	
		    int count = 0;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	if (++count % 10000 == 0) System.out.println(count);
	
		    	String key = r.getSourceGuid() + "\t" + r.getDestGuid();
		    	List<QPE> ml = m.get(key);
		    	if (ml == null) continue;
		    	for (QPE qpe : ml) {
		    		RelationMentionRef rmf = r.getMention(qpe.mentionID);
		    		qpe.sentence = rmf.getSentence();
		    	}
		    }
			is.close();
		}*/
		{
			DelimitedReader is = new DelimitedReader(input1);
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(input1), "utf-8"));
			
			String[] t = null;
			String li = null;
			while ((li = r.readLine())!= null) {
				t = li.split("\t");
			//while ((t = is.read())!= null) {
				int sentenceID = Integer.parseInt(t[0]);
		    	List<QPE> ml = m.get(sentenceID);
		    	if (ml == null) continue;
		    	for (QPE qpe : ml) {
		    		qpe.sentence = t[1];
		    	}
			}
			is.close();
		}
		
		// write augmented info
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(output), "utf-8"));
			for (QPE qpe : l) {
				w.write(qpe.iteration + "\t" + qpe.arg1 + "\t" + qpe.arg2 + "\t" + qpe.sentence + "\t" + qpe.relation + "\n");
			}
			w.close();
		}
	}
}
