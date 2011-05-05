package multir.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multir.util.delimited.DelimitedReader;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class CreateTestSetsByPredictedRelation2 {
	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";
	static String resultsFile = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/results";
	static String input4 = "/projects/pardosa/data16/raphaelh/tmp/seb/guid2name.all";
	
	static String output = "/projects/pardosa/data16/raphaelh/camera/bypredictedrelation_X.del";
	
	public static void main(String[] args) throws IOException {
		
		Map<String,List<String[]>> reltype2snt = new HashMap<String,List<String[]>>();
		Map<String,List<String[]>> key2snt = new HashMap<String,List<String[]>>();
		
		// read non-NA predictions
		{
			Map<Integer,String> relID2rel = new HashMap<Integer,String>();
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(resultsFile), "utf-8"));
			String[] h = r.readLine().split(" ");
			for (int i=0; i < h.length; i++) relID2rel.put(i, h[i]);
			
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
	
				String arg1 = c[0];
				String arg2 = c[1];
				String mnt = c[2];
				String rel = c[3];
				
				if (rel.equals("NA")) continue;
				
				List<String[]> rli = reltype2snt.get(rel);
				if (rli == null) {
					rli = new ArrayList<String[]>();
					reltype2snt.put(rel, rli);
				}
				
				String key = arg1 + "\t" + arg2 + "\t" + mnt;
				List<String[]> kli = key2snt.get(key);
				if (kli == null) {
					kli = new ArrayList<String[]>();
					key2snt.put(key, kli);
				}
				
				//String[] s = new String[] { arg1, arg2, mnt, null, null, null }; 
				String[] s = new String[10];
				s[5] = rel;
				rli.add(s);
				kli.add(s);
			}
			r.close();
		}
		
		// read guid2name
		HashMap<String,String> guid2name = new HashMap<String,String>();
		{
			DelimitedReader r = new DelimitedReader(input4);
			String[] t = null;
			while ((t = r.read())!= null)
				guid2name.put(t[0], t[1]);
			r.close();
		}
		
        InputStream is = new FileInputStream(input1); //new GZIPInputStream(new FileInputStream(input1));
        Relation r = null;
        
        while ((r = Relation.parseDelimitedFrom(is))!=null) {

        	String guid1 = r.getSourceGuid();
        	String guid2 = r.getDestGuid();
        	
        	for (int i=0; i < r.getMentionCount(); i++) {
        		RelationMentionRef rmf = r.getMention(i);
            	String key = guid1 + "\t" + guid2 + "\t" + i;
            	
            	List<String[]> kli = key2snt.get(key);
            	if (kli == null) continue;
            	
            	//System.out.println("found some");
            	
            	for (String[] s : kli) {
            		
    	        	String name1 = guid2name.get(r.getSourceGuid());
    	        	String name2 = guid2name.get(r.getDestGuid());
    	        	if (name1 == null) name1 = "";
    	        	if (name2 == null) name2 = "";
    				//int sentenceID = Integer.parseInt(rmf.getFilename());
    	        	String snt = rmf.getSentence();
    	    		StringBuilder sb = new StringBuilder();
    	    		for (int j=0; j < Math.min(10, rmf.getFeatureList().size()); j++) {
    					if (sb.length() > 0) sb.append(";");
    					sb.append(rmf.getFeature(j));
    	    		}

    				String sentence = snt.replaceAll(name1, "[" + name1 + "]1").
    					replaceAll(name2, "[" + name2 + "]2");

            		s[0] = i + "";
            		s[1] = sentence;
            		s[2] = "";
            		s[3] = name1;
            		s[4] = name2;
            		// already set: s[5]
            		s[6] = r.getSourceGuid();
            		s[7] = r.getDestGuid();
            		s[8] = snt;
            		s[9] = sb.toString();
            	}
        	}
        }
		is.close();

		
		
		for (Map.Entry<String,List<String[]>> e : reltype2snt.entrySet()) {
			System.out.println(e.getKey() + "\t" + e.getValue().size());
		}
		
		// write sample
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(output), "utf-8"));

		for (Map.Entry<String,List<String[]>> e : reltype2snt.entrySet()) {

			List<Integer> ids = new ArrayList<Integer>();
			for (int i=0; i < e.getValue().size(); i++) ids.add(i);			
			Collections.shuffle(ids);

			System.out.println(e.getKey() + "  " + ids.size());
			
			// take up to 100 sentences for each relation
			for (int i=0; i < Math.min(ids.size(), 100); i++) {
				//int id = ids.get(i);
				
				String[] t = e.getValue().get(ids.get(i));

				StringBuilder sb = new StringBuilder();
				for (int j=0; j < 10; j++) {
					if (sb.length() > 0) sb.append("\t");
					sb.append(t[j]);
				}
				sb.append("\n");
				
				w.write(sb.toString());
			}			
		}
		w.close();		
	}	

}
