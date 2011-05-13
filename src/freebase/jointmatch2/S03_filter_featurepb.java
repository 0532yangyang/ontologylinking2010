package freebase.jointmatch2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.mydb.StringTable;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class S03_filter_featurepb {
	public static void relabel(String in, String out) throws NumberFormatException, IOException {
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				String[] names = l[3].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		HashSet<Long> wikilinked = new HashSet<Long>();
		{
			D.p("load wikipedia page link");
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			String[] l;
			while ((l = dr.read()) != null) {
				wikilinked.add(StringTable.intPair2Long(l[0], l[1]));
			}
			dr.close();
		}

		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is = new BufferedInputStream(new FileInputStream(in));
		Relation r = null;

		int count = 0;
		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			// need to iterate over mentions, keep only those in the range
			String arg1 = r.getSourceGuid();
			String arg2 = r.getDestGuid();
			String arg1check = arg1.toLowerCase().trim().replaceAll(" ", "_");
			String arg2check = arg2.toLowerCase().trim().replaceAll(" ", "_");
			if (name2wid.containsKey(arg1check) && name2wid.containsKey(arg2check)) {

				int wid1 = name2wid.get(arg1check);
				int wid2 = name2wid.get(arg2check);
				if (wikilinked.contains(StringTable.intPair2Long(wid1, wid2)) && !arg1.equals(arg2)) {
					relBuilder = Relation.newBuilder();
					relBuilder.setRelType("NA");
					relBuilder.setSourceGuid(r.getSourceGuid());
					relBuilder.setDestGuid(r.getDestGuid());
					for (int i = 0; i < r.getMentionCount(); i++) {
						RelationMentionRef rmf = r.getMention(i);
						//D.p(rmf.getFilename());
						relBuilder.addMention(rmf);
					}
					if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
						relBuilder.build().writeDelimitedTo(os);
				}
			}
		}
		is.close();
		os.close();
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		String nytfb = "/projects/pardosa/s5/clzhang/ontologylink/nyt/featurizedData.pb";
		relabel(nytfb, nytfb + "filter.pbgz");
	}
}
