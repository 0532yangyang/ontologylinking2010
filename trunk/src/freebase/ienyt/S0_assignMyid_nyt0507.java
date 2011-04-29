package freebase.ienyt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class S0_assignMyid_nyt0507 {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		int count = 0;
		{
			OutputStream os = new GZIPOutputStream(
					new BufferedOutputStream(new FileOutputStream(Main.file_myidnyt05pb)));
			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(Main.file_nyt05pb)));
			Relation r = null;

			Builder relBuilder = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				if (++count % 10000 == 0)
					System.out.println(count);
				//D.p(r.getSourceGuid(), r.getDestGuid());

				relBuilder = Relation.newBuilder();
				// need to iterate over mentions, keep only those in the range 
				relBuilder.setRelType(count + "");
				relBuilder.setSourceGuid(r.getSourceGuid());
				relBuilder.setDestGuid(r.getDestGuid());
				for (int i = 0; i < r.getMentionCount(); i++) {
					RelationMentionRef rmf = r.getMention(i);
					relBuilder.addMention(rmf);
				}
				relBuilder.build().writeDelimitedTo(os);
			}

			is.close();
			os.close();
		}
		{
			OutputStream os = new GZIPOutputStream(
					new BufferedOutputStream(new FileOutputStream(Main.file_myidnyt07pb)));
			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(Main.file_nyt07pb)));
			Relation r = null;

			Builder relBuilder = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				if (++count % 10000 == 0)
					System.out.println(count);
				//D.p(r.getSourceGuid(), r.getDestGuid());
				relBuilder = Relation.newBuilder();
				// need to iterate over mentions, keep only those in the range 
				relBuilder.setRelType(count + "");
				relBuilder.setSourceGuid(r.getSourceGuid());
				relBuilder.setDestGuid(r.getDestGuid());
				for (int i = 0; i < r.getMentionCount(); i++) {
					RelationMentionRef rmf = r.getMention(i);
					relBuilder.addMention(rmf);
				}
				relBuilder.build().writeDelimitedTo(os);
			}

			is.close();
			os.close();
		}
	}

}
