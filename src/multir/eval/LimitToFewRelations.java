package multir.eval;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class LimitToFewRelations {

	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset05-06.10.pb.gz";
	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";
	static String input3 = "/projects/pardosa/data14/raphaelh/t/data/subsetN87-06.10.pb.gz";
	static String input4 = "/projects/pardosa/data14/raphaelh/t/data/subset06.10.pb.gz";
	static String input5 = "/projects/pardosa/data14/raphaelh/t/data/subset05.10.pb.gz";
	static String input6 = "/projects/pardosa/data14/raphaelh/t/data/subset05_1.10.pb.gz";
	static String input7 = "/projects/pardosa/data14/raphaelh/t/data/subset87-06.1.pb.gz";
	static String input8 = "/projects/pardosa/data14/raphaelh/t/data/subset05-06.1.pb.gz";
	static String input9 = "/projects/pardosa/data14/raphaelh/t/data/subset05.1.pb.gz";
	static String input10 = "/projects/pardosa/data14/raphaelh/t/data/subset05_1.1.pb.gz";
	static String input11 = "/projects/pardosa/data14/raphaelh/t/data/subsetN87-06.5.pb.gz";

	static String output1 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels05-06.10.pb.gz";
	static String output2 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels07.100.pb.gz";
	static String output3 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels87-06.10.pb.gz";
	static String output4 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels06.10.pb.gz";
	static String output5 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels05.10.pb.gz";
	static String output6 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels05_1.10.pb.gz";
	static String output7 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels87-06.1.pb.gz";
	static String output8 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels05-06.1.pb.gz";
	static String output9 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels05.1.pb.gz";
	static String output10 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels05_1.1.pb.gz";
	static String output11 = "/projects/pardosa/data14/raphaelh/t/data/subsetRels87-06.5.pb.gz";
	
	static String[] targets = { "/location/location/contains", "/people/person/nationality",
		"/location/neighborhood/neighborhood_of", "/people/person/children" };
	
	static Set<String> targetsSet;
	
	public static void main(String[] args) throws IOException {
		targetsSet = new HashSet<String>();
		for (String t : targets) targetsSet.add(t);
		
		//convert(input1, output1);
		//convert(input2, output2);
		convert(input3, output3);
		////convert(input4, output4);
		//convert(input5, output5);
		//convert(input6, output6);
		//convert(input7, output7);
		//convert(input8, output8);
		//convert(input9, output9);
		//convert(input10, output10);
		convert(input11, output11);
	}
	
	private static void convert(String in, String out) throws IOException {
		
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(
				new FileOutputStream(out)));
	    InputStream is = new GZIPInputStream
	    	(new BufferedInputStream(
	    		new FileInputStream(in)));
	    Relation r = null;
	    
	    int count = 0;
	    
		Builder relBuilder = null;
	    while ((r = Relation.parseDelimitedFrom(is))!=null) {
	    	if (++count % 10000 == 0) System.out.println(count);

	    	relBuilder = Relation.newBuilder();	    	
	    	relBuilder.setSourceGuid(r.getSourceGuid());
	    	relBuilder.setDestGuid(r.getDestGuid());
	    	
	    	StringBuilder sb = new StringBuilder();
	    	{
		    	String[] rels = r.getRelType().split(",");
		    	for (String rel : rels) {
		    		if (!targetsSet.contains(rel)) continue;
		    		if (sb.length() > 0) sb.append(",");
		    		sb.append(rel);	    		
		    	}
		    	if (sb.length() == 0) sb.append("NA");
	    	}
	    	relBuilder.setRelType(sb.toString());
	    	
	    	for (int i=0; i < r.getMentionCount(); i++) {
	    		RelationMentionRef rmf = r.getMention(i);
    			relBuilder.addMention(rmf);
	    	}
    		if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
    			relBuilder.build().writeDelimitedTo(os);
		}
		is.close();
		os.close();
	}
}
