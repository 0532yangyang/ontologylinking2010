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
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.PbReader;
import javatools.ml.rphacl2011extractor.RphExtractorWrapper;
import multir.util.delimited.DelimitedWriter;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;
import freebase.jointmatch2.Main;
import freebase.jointmatch2.NellOntology;
import freebase.jointmatch2.NellRelation;

public class S0_ienytbaseline {

	public static void splitFeaturizedpbToTrainTest() throws IOException {
		int start2007 = 43425267;
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(Main.file_allnyt_withlink_pb)));
		OutputStream ostrain = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(
				Main.file_trainnyt_withlink_pb)));
		OutputStream ostest = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(
				Main.file_testnyt_withlink_pb)));
		Relation r = null;

		int count = 0;
		Random ran = new Random();
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			Builder relBuilder1 = Relation.newBuilder();
			Builder relBuilder2 = Relation.newBuilder();

			relBuilder1.setRelType(r.getRelType());
			relBuilder1.setSourceGuid(r.getSourceGuid());
			relBuilder1.setDestGuid(r.getDestGuid());
			relBuilder2.setRelType(r.getRelType());
			relBuilder2.setSourceGuid(r.getSourceGuid());
			relBuilder2.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				int filename = Integer.parseInt(rmf.getFilename());
				//double a = ran.nextDouble();
				if (filename > start2007) {//test 30%
					relBuilder2.addMention(rmf);
				} else {
					relBuilder1.addMention(rmf);
				}
			}
			if (relBuilder1.getMentionList() != null && relBuilder1.getMentionCount() > 0) {
				relBuilder1.build().writeDelimitedTo(ostrain);
			}
			if (relBuilder2.getMentionList() != null && relBuilder2.getMentionCount() > 0) {
				relBuilder2.build().writeDelimitedTo(ostest);
			}
		}
		ostrain.close();
		ostest.close();
	}

	public static void relabelWithSeedByName(NellOntology no, String in, String out) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(out + ".debug");
		HashMap<String, String> seedpair2rel = new HashMap<String, String>();
		for (NellRelation nr : no.nellRelationList) {
			for (String[] s : nr.seedInstances) {
				seedpair2rel.put(s[0] + "\t" + s[1], nr.relation_name);
			}
		}
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		Relation r = null;

		int count = 0;
		Builder relBuilder = null;
		int notnacount = 0;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);

			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range

			String name1 = r.getSourceGuid();
			String name2 = r.getDestGuid();
			dw.write(name1, name2);
			String look = name1 + "\t" + name2;
			String rel = "NA";
			if (seedpair2rel.containsKey(name1 + "\t" + name2)) {
				rel = seedpair2rel.get(name1 + "\t" + name2);
				notnacount++;
			}

			relBuilder.setRelType(rel);

			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
				relBuilder.build().writeDelimitedTo(os);
		}
		D.p("Not na count", notnacount);
		is.close();
		os.close();
		dw.close();
	}

	public static void relabelWithSeedByFBSearch(NellOntology no, String in, String out) throws IOException {

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
		relabel(out + ".temp", in, out);
	}

	public static void reduceNegativeTraining(String in, String out, double naratio) throws IOException {
		Random random = new Random();
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		InputStream is;
		try {
			is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(in)));
		} catch (Exception e) {
			is = new BufferedInputStream(new FileInputStream(in));
		}

		Relation r = null;
		int count = 0;
		int pos = 0, neg = 0;
		int newNeg = 0;
		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range 
			relBuilder.setRelType(r.getRelType());
			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			if (r.getRelType().equals("NA"))
				neg++;
			else
				pos++;

			if (r.getRelType().equals("NA"))
				if (random.nextDouble() < naratio)
					continue;
			if (r.getRelType().equals("NA"))
				newNeg++;
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			if (relBuilder.getMentionList() != null && relBuilder.getMentionCount() > 0)
				relBuilder.build().writeDelimitedTo(os);
		}
		is.close();
		os.close();
		System.out.println("before neg: " + neg);
		System.out.println("before pos: " + pos);
		System.out.println("new neg: " + newNeg);
		System.out.println("new pos pos: " + pos);
	}

	public static void relabel(String extendpairs, String in, String out) throws NumberFormatException, IOException {
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
				sb.append("NA");
			} else {
				dw.write(name1, name2, sb.toString());
			}

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

	public static void relabel2(String extendpairs, String in, String out) throws NumberFormatException, IOException {
		DelimitedWriter dw = new DelimitedWriter(out + ".seerelabel");
		//		HashSet<Integer> usedwid = new HashSet<Integer>();
		//		{
		//			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
		//					Main.file_gnid_mid_wid_title)));
		//			String l = null;
		//			while ((l = r.readLine()) != null) {
		//				String[] c = l.split("\t");
		//				int id = Integer.parseInt(c[2]);
		//				usedwid.add(id);
		//			}
		//			r.close();
		//		}
		HashMap<String, Integer> guid2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_guid_wikiid_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				String a = l[0];
				int wid = Integer.parseInt(l[1]);
				guid2wid.put(a, wid);
			}
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
		InputStream is = new BufferedInputStream(new FileInputStream(in));
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
			//System.out.println(name1 + "\t" + name2);
			StringBuilder sb = new StringBuilder();

			Integer id1 = guid2wid.get(name1);
			Integer id2 = guid2wid.get(name2);
			dw.write(name1, name2);
			if (id1 != null && id2 != null) {

				HashSet<String> rels1 = new HashSet<String>();
				HashSet<String> rels2 = new HashSet<String>();
				{
					List<String> l = arg1ToRel.get(id1);
					if (l != null)
						rels1.addAll(l);
				}
				{
					List<String> l = arg2ToRel.get(id2);
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
				sb.append("NA");
			} else {
				dw.write(name1, name2, sb.toString());
			}

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

	/**
	 * Training instances: distant supervision; with seed byname only
	 * dataset: nyt before 07 & after 07, no na at all;
	 * @throws IOException 
	 * */
	public static void eval_seedbyname_extend() throws IOException {
		String dir = Main.dir + "/nyt/seedbyname_vs_extend";
		(new File(dir)).mkdir();
		String trainpbraw = dir + "/trainpbraw";
		String testpbraw = dir + "/testpbraw";
		String trainpb = dir + "/trainpb";
		String testpb = dir + "/testpb";
		relabelWithSeedByName(Main.no, Main.file_trainnyt_withlink_pb, trainpbraw);
		relabel(Main.file_extendedwidpairs_filter, Main.file_testnyt_withlink_pb, testpb);
		reduceNegativeTraining(trainpbraw, trainpb, 0.99);
		//reduceNegativeTraining(testpbraw, testpb, 0);
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, dir);
		rew.learningThenTesting();
	}

	/**
	 * Training instances: distant supervision; with seed by FB search engine result only
	 * dataset: nyt before 07 & after 07, no na at all;
	 * @throws IOException 
	 * */
	public static void eval_seedbyfb_extend() throws IOException {
		String dir = Main.dir + "/nyt/seedbyfb_vs_extend";
		(new File(dir)).mkdir();
		String trainpbraw = dir + "/trainpbraw";
		String testpbraw = dir + "/testpbraw";
		String trainpb = dir + "/trainpb";
		String testpb = dir + "/testpb";

		relabelWithSeedByFBSearch(Main.no, Main.file_trainnyt_withlink_pb, trainpbraw);
		relabel(Main.file_extendedwidpairs_filter, Main.file_testnyt_withlink_pb, testpb);
		reduceNegativeTraining(trainpbraw, trainpb, 0.99);
		//reduceNegativeTraining(testpbraw, testpb, 1);
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, dir);
		rew.learningThenTesting();
	}

	/**
	 * Training instances: distant supervision; with seed by FB search engine result only
	 * dataset: nyt before 07 & after 07, no na at all;
	 * @throws IOException 
	 * */
	public static void eval_extend_extend() throws IOException {
		String dir = Main.dir + "/nyt/extend_vs_extend";
		(new File(dir)).mkdir();
		String trainpbraw = dir + "/trainpbraw";
		String testpbraw = dir + "/testpbraw";
		String trainpb = dir + "/trainpb";
		String testpb = dir + "/testpb";

		//		relabel(Main.file_extendedwidpairs_filter, Main.file_trainnyt_withlink_pb, trainpbraw);
		//		relabel(Main.file_extendedwidpairs_filter, Main.file_testnyt_withlink_pb, testpb);
		//		reduceNegativeTraining(trainpbraw, trainpb, 0.9);
		//		//reduceNegativeTraining(testpbraw, testpb, 1);
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, dir);
		rew.learningThenTesting();
	}

	public static void eval_extend_extend_0507() throws IOException {
		String dir = Main.dir + "/nyt/extend_vs_extend_0507";
		(new File(dir)).mkdir();
		String trainpbraw = dir + "/trainpbraw";
		String testpbraw = dir + "/testpbraw";
		String trainpb = dir + "/trainpb";
		String testpb = dir + "/testpb";

		//		relabel(Main.file_extendedwidpairs_filter, Main.file_nyt05pb, trainpbraw);
		//		relabel(Main.file_extendedwidpairs_filter, Main.file_nyt07pb, testpb);
		reduceNegativeTraining(trainpbraw, trainpb, 0.9);
		//reduceNegativeTraining(testpbraw, testpb, 1);
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, dir);
		rew.learningThenTesting();
	}

	public static void eval_extend_extend_seb() throws IOException {
		String dir = Main.dir + "/nyt/extend_vs_extend_seb";
		(new File(dir)).mkdir();
		String trainpbraw = dir + "/trainpbraw";
		String testpbraw = dir + "/testpbraw";
		String trainpb = dir + "/trainpb";
		String testpb = dir + "/testpb";

		PbReader.analyzePbData(Main.file_sebtrain);
		PbReader.analyzePbData(Main.file_sebtest);
		//		relabel2(Main.file_extendedwidpairs_filter, Main.file_sebtrain, trainpbraw);
		//		relabel2(Main.file_extendedwidpairs_filter, Main.file_sebtest, testpb);
		reduceNegativeTraining(Main.file_sebtrain, trainpb, 0);
		reduceNegativeTraining(Main.file_sebtest, testpb, 0);
		//reduceNegativeTraining(testpbraw, testpb, 1);
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, dir);
		rew.learningThenTesting();
	}

	public static void eval_extend_extend_mylabel_07() throws IOException {
		String dir = Main.dir + "/nyt/extend_vs_extend_mylabel_07";
		(new File(dir)).mkdir();
		String trainpbraw = dir + "/trainpbraw";
		String testpbraw = dir + "/testpbraw";
		String trainpb = dir + "/trainpb";
		String testpb = dir + "/testpb";

		relabel(Main.file_extendedwidpairs_filter, Main.file_trainnyt_withlink_pb, trainpbraw);
		relabel(Main.file_extendedwidpairs_filter, Main.file_nyt07pb, testpb);
		reduceNegativeTraining(trainpbraw, trainpb, 0.99);
		//reduceNegativeTraining(testpbraw, testpb, 1);
		RphExtractorWrapper rew = new RphExtractorWrapper(trainpb, testpb, dir);
		rew.learningThenTesting();
	}

	public static void main(String[] args) throws IOException {
		//S0_ienytbaseline
		//splitFeaturizedpbToTrainTest();
		//		eval_seedbyname_extend();
		//		eval_seedbyfb_extend();
		//		eval_extend_extend();
		//eval_extend_extend_0507();
		eval_extend_extend_seb();
		//		eval_extend_extend_mylabel_07();
	}
}
