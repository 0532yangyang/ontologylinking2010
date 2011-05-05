package multir.eval;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class RawToString {

	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test.pb";
	
	public static void main(String[] args) throws IOException {
		
        InputStream is = new FileInputStream(input1);
        Relation r = null;
        
        while ((r = Relation.parseDelimitedFrom(is))!=null) {
        	for (int i=0; i < r.getMentionCount(); i++) {
        		RelationMentionRef rmf = r.getMention(i);

        		System.out.println(r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + r.getRelType());
        		
        	}
        }
		is.close();
		

		
	}
}
