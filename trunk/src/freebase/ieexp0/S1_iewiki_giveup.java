package freebase.ieexp0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
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

import javatools.filehandlers.DelimitedReader;

import multir.util.delimited.DelimitedWriter;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

import freebase.jointmatch2.Main;
import freebase.jointmatch2.NellOntology;
import freebase.jointmatch2.NellRelation;

public class S1_iewiki_giveup {

	public static void relabelWithSeedByFBSearchNoNA(NellOntology no, String in, String out) throws IOException {

		DelimitedWriter dw = new DelimitedWriter(out + ".temp");
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_enid_mid_wid_argname_otherarg_relation_label_top1);
			String[] l;
			while ((l = dr.read()) != null) {
				name2wid.put(l[3], Integer.parseInt(l[2]));
			}
			dr.close();
		}
		{
			for (NellRelation nr : no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					if (name2wid.containsKey(s[0]) && name2wid.containsKey(s[1])) {
						int wid1 = name2wid.get(s[0]);
						int wid2 = name2wid.get(s[1]);
						dw.write(wid1, wid2, nr.relation_name);
					}
				}
			}
		}
		dw.close();
		relabelWithNoNA(out + ".temp", in, out);
	}

	public static void relabelWithNoNA(String extendpairs, String in, String out) throws NumberFormatException,
			IOException {
		DelimitedWriter dw = new DelimitedWriter(out + ".seerelabel");
		HashMap<String, List<Integer>> name2id = new HashMap<String, List<Integer>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
					Main.file_gnid_mid_wid_title)));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				int id = Integer.parseInt(c[2]);

				String[] names = c[3].split(" ");
				for (String name : names) {
					name = name.replace("_", " ");
					List<Integer> ids = name2id.get(name);
					if (ids == null) {
						ids = new ArrayList<Integer>(1);
						name2id.put(name, ids);
						//System.out.println("adding '" + name + "'");
					}
					ids.add(id);
				}
			}
			r.close();
		}

		HashMap<Integer, List<String>> arg1ToRel = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> arg2ToRel = new HashMap<Integer, List<String>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(extendpairs)));
			String l = null;
			while ((l = r.readLine()) != null) {
				String[] c = l.split("\t");
				int arg1 = Integer.parseInt(c[0]);
				int arg2 = Integer.parseInt(c[1]);
				c[2] = c[2].replace("_inverse", "");
				String rel = c[0] + "\t" + c[1] + "\t" + c[2];
				List<String> l1 = arg1ToRel.get(arg1);
				if (l1 == null) {
					l1 = new ArrayList<String>(1);
					arg1ToRel.put(arg1, l1);
				}
				l1.add(rel);
				List<String> l2 = arg2ToRel.get(arg2);
				if (l2 == null) {
					l2 = new ArrayList<String>(1);
					arg2ToRel.put(arg2, l2);
				}
				l2.add(rel);
			}
			r.close();
		}

		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;

		int count = 0;
		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);

			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range

			String name1 = r.getSourceGuid();
			String name2 = r.getDestGuid();

			StringBuilder sb = new StringBuilder();

			List<Integer> id1 = name2id.get(name1);
			List<Integer> id2 = name2id.get(name2);
			if (id1 != null && id2 != null) {
				//System.out.println(name1 + "\t" + name2);

				HashSet<String> rels1 = new HashSet<String>();
				for (Integer id : id1) {
					List<String> l = arg1ToRel.get(id);
					if (l != null)
						rels1.addAll(l);
				}

				HashSet<String> rels2 = new HashSet<String>();
				for (Integer id : id2) {
					List<String> l = arg2ToRel.get(id);
					if (l != null)
						rels2.addAll(l);
				}
				//if (rels1.size() > 0 && rels2.size() > 0)
				//System.out.println(rels1.size() + "\t" + rels2.size());

				rels1.retainAll(rels2);
				//if (rels1.size() != 0) {
				//	System.out.println(name1 + "\t" + name2);
				//	System.out.println(rels1.size());
				//}

				HashSet<String> relTypes = new HashSet<String>();
				for (String rel : rels1) {
					String[] c = rel.split("\t");
					relTypes.add(c[2]);
				}

				for (String rt : relTypes) {
					if (sb.length() > 1)
						sb.append(",");
					sb.append(rt);
				}
			}
			if (sb.length() == 0) {
				continue;
			}
			dw.write(name1, name2, sb.toString());
			relBuilder.setRelType(sb.toString());

			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
				relBuilder.build().writeDelimitedTo(os);
		}
		is.close();
		os.close();
		dw.close();
	}

	public static void relabelBySeedExtendAndSampleOthers() throws NumberFormatException, IOException {
		String inputpb = "/projects/pardosa/s5/clzhang/ontologylink/wikidump/featurizedData.pb";
		String dir = "/projects/pardosa/s5/clzhang/ontologylink/wikirph/";
		(new File(dir)).mkdir();
		String extendpb = dir + "/extend.pb";
		String seedpb = dir + "/seed.pb";
		String napb = dir + "/na.pb";
		relabelWithNoNA(Main.file_extendedwidpairs_filter, inputpb, extendpb);
		relabelWithSeedByFBSearchNoNA(Main.no, inputpb, seedpb);

	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		relabelBySeedExtendAndSampleOthers();
	}
}
