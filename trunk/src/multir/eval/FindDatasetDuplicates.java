package multir.eval;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class FindDatasetDuplicates {
	
	// weird: why are there so many duplicates in the datasets??
	
	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";
	
	public static void main(String[] args) throws IOException {
		
		Set<String> s = new HashSet<String>();
		
        InputStream is = new FileInputStream(input1);
        Relation r = null;
        
        int total = 0;
        while ((r = Relation.parseDelimitedFrom(is))!=null) {
        	
        	for (int i=0; i < r.getMentionCount(); i++) {
        		RelationMentionRef rmf = r.getMention(i);
        		
        		StringBuilder sb = new StringBuilder();
        		HashSet<String> fts = new HashSet<String>();
        		for (int j=0; j < rmf.getFeatureCount(); j++)
        			fts.add(rmf.getFeature(j));
        		List<String> l = new ArrayList<String>(fts);
        		Collections.sort(l);
        		for (int j=0; j < l.size(); j++) sb.append("\t" + l.get(j));

        		s.add(rmf.getSentence()+ "\t" + r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + sb.toString());
        		
        		total++;
        	}
        }
        System.out.println(total);
        System.out.println(s.size());
	}

}
