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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multir.util.delimited.DelimitedReader;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class TmpMapToMentionIDs {

	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";

	//static String input1 = "/projects/pardosa/data14/raphaelh/riedel0506/test-all-newlabel-Multiple.pb";
	static String input4 = "/projects/pardosa/data16/raphaelh/tmp/seb/guid2name.all";
	
	static String input5 = "/projects/pardosa/data16/raphaelh/camera/byrelation_labeled";

	static String output = "/projects/pardosa/data16/raphaelh/camera/byrelation_X";

	public static void main(String[] args) throws IOException {
		// read guid2name
		HashMap<String,String> guid2name = new HashMap<String,String>();
		{
			DelimitedReader r = new DelimitedReader(input4);
			String[] t = null;
			while ((t = r.read())!= null)
				guid2name.put(t[0], t[1]);
			r.close();
		}

		Map<RelationMentionRef,Integer> mnt2mntID = new HashMap<RelationMentionRef,Integer>();
		Map<RelationMentionRef,Relation> map = new HashMap<RelationMentionRef,Relation>();
		Map<String,List<RelationMentionRef>> reltype2snt = new HashMap<String,List<RelationMentionRef>>();
		{
	        InputStream is = new FileInputStream(input1);
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
		}
		
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(output), "utf-8"));
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(input5), "utf-16"));
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
				String rel = c[5];
				List<RelationMentionRef> lr = reltype2snt.get(rel);
				int id = -1;
				try {
					id = Integer.parseInt(c[0]);
				} catch (Exception e) {
					System.out.println(l);
					e.printStackTrace();
					System.exit(1);
					//throw e;
				}
				RelationMentionRef rmf = lr.get(id);
				int mntID = mnt2mntID.get(rmf);

				// throw features into string
				StringBuilder sb = new StringBuilder();
				for (int i=0; i < Math.min(10, rmf.getFeatureCount()); i++) {
					if (sb.length() > 0) sb.append(";");
					sb.append(rmf.getFeature(i));
				}
				
				w.write(mntID + "\t" + c[1] + "\t" + c[2] + "\t" + c[3] + "\t" + c[4] + 
						"\t" + c[5] + "\t" + c[6] + "\t" + c[7] + "\t" + c[8] + "\t" + sb.toString() + "\n" );

			}
			r.close();
			w.close();
		}
	}	
}
