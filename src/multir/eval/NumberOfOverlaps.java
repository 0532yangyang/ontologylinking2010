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

public class NumberOfOverlaps {
	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";

	static String output = "/projects/pardosa/data16/raphaelh/camera/numberOfMatches";
	
	public static void main(String[] args) throws IOException {
 
		Map<String,int[]> mSep = new HashMap<String,int[]>();
		
		Map<String,int[]> mCom = new HashMap<String,int[]>();

		{
	        InputStream is = new FileInputStream(input1); //new GZIPInputStream(new FileInputStream(input1));
	        Relation r = null;
	        
	        while ((r = Relation.parseDelimitedFrom(is))!=null) {
	        	
	        	String rels = r.getRelType();

	        	if (rels.contains(",")) {
	        		int[] i = mCom.get(rels);
	        		if (i == null) {
	        			i = new int[] {0, 0};
	        			mCom.put(rels, i);
	        		}
	        		i[0]++;
	        		i[1] += r.getMentionCount();
	        		
	        		
	        		String[] rs = r.getRelType().split(",");
	        		for (String ri : rs) {
	        			int[] j = mSep.get(ri);
	        			if (j == null) {
	        				j = new int[] { 0, 0 };
	        				mSep.put(ri, j);
	        			}
	        			j[0]++;
	        			j[1] += r.getMentionCount();
	        		}
	        		
	        	}
	        	
	        	
	        	
	        	
	        }
	        is.close();
		}

		for (Map.Entry<String,int[]> e : mCom.entrySet()) {			
			System.out.println(e.getValue()[0] + "\t" + e.getValue()[1] + "\t" + e.getKey());
		}
		System.out.println("---------");
		for (Map.Entry<String,int[]> e : mSep.entrySet()) {			
			System.out.println(e.getValue()[0] + "\t" + e.getValue()[1] + "\t" + e.getKey());
		}
		
	}
}
