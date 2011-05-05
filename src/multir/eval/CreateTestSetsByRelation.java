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
import java.util.zip.GZIPInputStream;

import multir.util.delimited.DelimitedReader;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class CreateTestSetsByRelation {
	//static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";
	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";
	
	static String input2 = "/projects/pardosa/data14/raphaelh/t/raw/sentences";
	//static String input1 = "/projects/pardosa/data14/raphaelh/riedel0506/test-all-newlabel-Multiple.pb";
	//static String input4 = "/projects/pardosa/data16/raphaelh/tmp/seb/guid2name.all";
	
	static String output = "/projects/pardosa/data16/raphaelh/camera/byrelation_owndata";
	
	static int start2007 = 45961658;
	
	public static void main(String[] args) throws IOException {
		/*
		// read guid2name
		HashMap<String,String> guid2name = new HashMap<String,String>();
		{
			DelimitedReader r = new DelimitedReader(input4);
			String[] t = null;
			while ((t = r.read())!= null)
				guid2name.put(t[0], t[1]);
			r.close();
		}*/
		
		// read sentences for test set
		HashMap<Integer,String> sentences = new HashMap<Integer,String>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(input2), "utf-8"));
			String l = null;
			while ((l = r.readLine())!= null) {				
				String[] c = l.split("\t");
				int sentenceID = Integer.parseInt(c[0]);
				if (sentenceID > start2007)
					sentences.put(sentenceID, c[1]);
			}
			r.close();
		}
		

		Map<RelationMentionRef,Integer> mnt2mntID = new HashMap<RelationMentionRef,Integer>();
		Map<RelationMentionRef,Relation> map = new HashMap<RelationMentionRef,Relation>();
		Map<String,List<RelationMentionRef>> reltype2snt = new HashMap<String,List<RelationMentionRef>>(); 
        InputStream is = new GZIPInputStream(new FileInputStream(input1));
        Relation r = null;
        
        while ((r = Relation.parseDelimitedFrom(is))!=null) {
        	String[] relTypes = r.getRelType().split(",");

        	for (String relType : relTypes) {
        		if (relType.equals("NA")) continue;
        		
	        	for (int i=0; i < r.getMentionCount(); i++) {
	        		RelationMentionRef rmf = r.getMention(i);
	        		mnt2mntID.put(rmf, i);
	        		map.put(rmf, r);
	        		List<RelationMentionRef> l = reltype2snt.get(relType);
	        		if (l == null) {
	        			l = new ArrayList<RelationMentionRef>();
	        			reltype2snt.put(relType, l);
	        		}
	        		l.add(rmf);
	        	}
        	}
        }
		is.close();

		
		
		for (Map.Entry<String,List<RelationMentionRef>> e : reltype2snt.entrySet()) {
			System.out.println(e.getKey() + "\t" + e.getValue().size());
		}
		
		// write sample
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(output), "utf-8"));

		for (Map.Entry<String,List<RelationMentionRef>> e : reltype2snt.entrySet()) {

			List<Integer> ids = new ArrayList<Integer>();
			for (int i=0; i < e.getValue().size(); i++) ids.add(i);			
			Collections.shuffle(ids);

			// take up to 100 sentences for each relation
			for (int i=0; i < Math.min(ids.size(), 100); i++) {
				int id = ids.get(i);
				RelationMentionRef rmf = e.getValue().get(ids.get(i));
				r = map.get(rmf);
				
	        	//String name1 = guid2name.get(r.getSourceGuid());
	        	//String name2 = guid2name.get(r.getDestGuid());
	        	String name1 = r.getSourceGuid();
	        	String name2 = r.getDestGuid();
	        	if (name1 == null) name1 = "";
	        	if (name2 == null) name2 = "";

	    		// our prediction
				int sentenceID = Integer.parseInt(rmf.getFilename());
	        	
	        	//String snt = rmf.getSentence();
	    		String snt = sentences.get(sentenceID);
	    		
	    		StringBuilder sb = new StringBuilder();
	    		for (int j=0; j < Math.min(10, rmf.getFeatureList().size()); j++) {
					if (sb.length() > 0) sb.append(";");
					sb.append(rmf.getFeature(j));
	    		}
				int mntID = mnt2mntID.get(rmf);
	    		
				String sentence = snt;					
				sentence = sentence.replaceAll(name1, "[" + name1 + "]1");
				sentence = sentence.replaceAll(name2, "[" + name2 + "]2");
				
				//w.write(id + "\t" + sentence + "\t" + "" + "\t" + name1 + "\t" + name2 + 
				//		"\t" + e.getKey() + "\t" + r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + snt + "\n" );
				
				w.write(mntID + "\t" + sentence + "\t" + "" + "\t" + name1 + "\t" + name2 + 
						"\t" + e.getKey() + "\t" + r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + snt + "\t" + sb.toString() + "\n" );
			}			
		}
		w.close();
		
	}	

}
