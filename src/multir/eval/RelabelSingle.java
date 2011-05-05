package multir.eval;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javatools.administrative.D;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class RelabelSingle {

	static String dir = "o:/unix/projects/pardosa/s5/clzhang/ontologylink/ienyt";
	static String in = dir + "/subset05.100.pb.gz";
	//static String in = "/projects/pardosa/data14/raphaelh/t/data/train-Multiple.pb.gz";
	//static String input2 = "/projects/pardosa/data14/raphaelh/t/data/test-Multiple.pb.gz";

	//static String out = "/projects/pardosa/data14/raphaelh/t/data/train-Single.pb.gz";
	static String out = dir + "/cllabel_subset05.100.pb.gz";

	public static void main(String[] args) throws IOException {

		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;

		int count = 0;

		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			D.p(r.getSourceGuid(), r.getDestGuid());
			String[] rels = r.getRelType().split(",");
			if (rels.length > 1)
				continue;

			for (String rel : rels) {
				relBuilder = Relation.newBuilder();
				// need to iterate over mentions, keep only those in the range 
				relBuilder.setRelType(rel);
				relBuilder.setSourceGuid(r.getSourceGuid());
				relBuilder.setDestGuid(r.getDestGuid());
				for (int i = 0; i < r.getMentionCount(); i++) {
					RelationMentionRef rmf = r.getMention(i);
					relBuilder.addMention(rmf);
				}
				relBuilder.build().writeDelimitedTo(os);
			}
		}
		is.close();
		os.close();
	}
}
