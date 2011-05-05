package multir.eval;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import cc.factorie.protobuf.DocumentProtos.Relation;

public class NumberOfMatches {
	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";

	static String output = "/projects/pardosa/data16/raphaelh/camera/numberOfMatches";
	
	public static void main(String[] args) throws IOException {

		Map<String,int[]> m = new HashMap<String,int[]>();
		int allSentences = 0;
		{
	        InputStream is = new FileInputStream(input1); //new GZIPInputStream(new FileInputStream(input1));
	        Relation r = null;
	        
	        while ((r = Relation.parseDelimitedFrom(is))!=null) {
	        	
	        	String rels = r.getRelType();
	        	//if (rels.equals("NA")) continue;
	        	
	        	String[] rs = rels.split(",");
	        	for (String rr : rs) {
	        		int[] i = m.get(rr);
	        		if (i == null) {
	        			i = new int[] { 0, 0};
	        			m.put(rr, i);
	        		}
	        		i[0]++;
	        		i[1] += r.getMentionCount();
	        	}
	        	allSentences += r.getMentionCount();
	        }
	        is.close();
		}

		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"));
		for (Map.Entry<String,int[]> e : m.entrySet()) {
			System.out.println(e.getValue()[0] + "\t" + e.getValue()[1] + "\t" + e.getKey());
			w.write(e.getValue()[0] + "\t" + e.getValue()[1] + "\t" + e.getKey() + "\n");
		}
		w.close();
		System.out.println(allSentences);
	}
	
	
}
