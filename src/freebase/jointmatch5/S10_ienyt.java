package freebase.jointmatch5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import multir.util.delimited.Sort;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class S10_ienyt {

	static HashMap<String, Integer> str2wid;

	static void loadstr2wid() throws IOException {
		str2wid = new HashMap<String, Integer>(1000000);
		DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_gnid_mid_wid_title);
		String[] l;
		while ((l = dr.read()) != null) {
			int wid = Integer.parseInt(l[2]);
			String[] titles = l[3].split(" ");
			for (String t : titles) {
				str2wid.put(t.toLowerCase(), wid);
			}
		}
		dr.close();
	}

	private static boolean nytArgPair2Wid(String arg1, String arg2, int[] widpair) {
		Integer wid1 = str2wid.get(arg1.toLowerCase().replaceAll(" ", "_"));
		Integer wid2 = str2wid.get(arg2.toLowerCase().replaceAll(" ", "_"));
		if (wid1 != null && wid2 != null) {
			widpair[0] = wid1;
			widpair[1] = wid2;
			return true;
		}
		return false;
	}

	static void step1_getnytWid(String file_pb) throws IOException {

		//String temp1 = file_trainpb+".tmp1allentity";
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file_pb)));
		String output = file_pb + ".widpair";
		DelimitedWriter dw = new DelimitedWriter(file_pb + ".widpair");
		//		if (!(new File(trainpbdir)).exists()) {
		//			(new File(trainpbdir)).mkdir();
		//		}
		//		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(trainpbdir + "/trainpb")));
		Relation r = null;
		int count = 0;
		Builder relBuilder = null;
		int size = 0;
		int nasize = 0;
		//DelimitedWriter dw = new DelimitedWriter(Main.file_allentitypair);
		List<Relation> narelations = new ArrayList<Relation>();
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			//if(count>10000)break;
			String arg1 = r.getSourceGuid();
			String arg2 = r.getDestGuid();
			/**replace the type id with relation id in S0_reid_nyt0507*/
			String myid = r.getRelType();
			int widpair[] = new int[2];
			if (nytArgPair2Wid(arg1, arg2, widpair)) {
				dw.write(widpair[0], widpair[1], myid, arg1, arg2);
			}
		}
		D.p("success", size);
		dw.close();
		Sort.sort(output, output + ".sortbyarg", Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] arg0, String[] arg1) {
				// TODO Auto-generated method stub
				String k1 = arg0[0] + " " + arg0[1];
				String k2 = arg1[0] + " " + arg1[1];
				return k1.compareTo(k2);
			}
		});
	}

	/**Only consider those arg1 and arg2 having some map into wikipedia,
	 * including both NA and NotNA */
	static void step2_relabelNytWithQueryResult(String file_queryresult, String file_myidnytpb, String outputpb)
			throws IOException {
		D.p("Dealing with", file_myidnytpb);
		String file_myidnyt_widpair = file_myidnytpb + ".widpair";
		String outputdebug = outputpb + ".mylabel";
		HashMap<Long, String[]> map_pairwid_strline = new HashMap<Long, String[]>(2000000);
		HashMap<Integer, String> map_nytInstanceId2nellrelation = new HashMap<Integer, String>(1000000);
		HashSet<Integer> allnytInstanceHavingWids = new HashSet<Integer>();
		DelimitedWriter dw = new DelimitedWriter(outputdebug);
		{
			DelimitedReader dr = new DelimitedReader(file_myidnyt_widpair);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				long pairid = wid1 * (10 ^ 10) + wid2;
				long pairid_rev = wid2 * (10 ^ 10) + wid1;
				map_pairwid_strline.put(pairid, l);
				map_pairwid_strline.put(pairid_rev, l);
				allnytInstanceHavingWids.add(Integer.parseInt(l[2]));
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(file_queryresult);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				long pairid = wid1 * (10 ^ 10) + wid2;
				if (map_pairwid_strline.containsKey(pairid)) {
					String[] nytl = map_pairwid_strline.get(pairid);
					String nellrelation = l[2].replace("_inverse", "");
					int nytInstanceId = Integer.parseInt(nytl[2]);
					map_nytInstanceId2nellrelation.put(nytInstanceId, nellrelation);

				}
			}
			dr.close();
		}
		{
			int count = 0;
			OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(outputpb)));
			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file_myidnytpb)));
			Relation r = null;

			Builder relBuilder = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				if (++count % 10000 == 0)
					System.out.println(count);
				int myid = Integer.parseInt(r.getRelType());
				String rel = "NA";

				if (map_nytInstanceId2nellrelation.containsKey(myid)) {
					rel = map_nytInstanceId2nellrelation.get(myid);
				} else if (allnytInstanceHavingWids.contains(myid)) {
					rel = "NA";
				} else {
					rel = null;
				}

				if (rel != null) {
					dw.write(rel, r.getSourceGuid(), r.getDestGuid());
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
				//D.p(r.getSourceGuid(), r.getDestGuid());
			}
			is.close();
			os.close();
		}
		dw.close();
	}

	/**
	 * naratio is the ratio between NA and NotNA
	 * if naratio<0, then take all
	 * @throws IOException 
	 * */
	static void step3_shuffleAndSample(String filepball, String filepbsample, double naratio, int not_na_size)
			throws IOException {
		HashSet<Integer> used = new HashSet<Integer>();
		if (naratio >= 0) {
			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(filepball)));
			Relation r = null;
			Builder relBuilder = null;
			int count = 0;
			List<Integer> na_index = new ArrayList<Integer>();
			List<Integer> notna_index = new ArrayList<Integer>();
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				String rel = r.getRelType();
				if (rel.equals("NA")) {
					na_index.add(count);
				} else {
					notna_index.add(count);
				}
				count++;
			}
			is.close();
			Collections.shuffle(na_index);
			Collections.shuffle(notna_index);

			for (int i = 0; i < not_na_size && i < notna_index.size(); i++) {
				used.add(notna_index.get(i));
			}

			for (int i = 0; i < not_na_size * naratio && i < notna_index.size() * naratio && i < na_index.size(); i++) {
				used.add(na_index.get(i));
			}
		}
		{
			OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filepbsample)));
			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(filepball)));
			Relation r = null;
			int count = 0;
			Builder relBuilder = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				if (used.contains(count) || naratio < 0) {
					relBuilder = Relation.newBuilder();
					// need to iterate over mentions, keep only those in the range 
					relBuilder.setRelType(r.getRelType());
					relBuilder.setSourceGuid(r.getSourceGuid());
					relBuilder.setDestGuid(r.getDestGuid());
					for (int i = 0; i < r.getMentionCount(); i++) {
						RelationMentionRef rmf = r.getMention(i);
						relBuilder.addMention(rmf);
					}
					relBuilder.build().writeDelimitedTo(os);
				}
				count++;
			}
			is.close();
			os.close();
		}
	}

	static void stat_dataset(String dir) throws FileNotFoundException, IOException {

		{
			D.p("Number of instance for every class in training");
			HashCount<String> dist = new HashCount<String>();
			int count = 0;

			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(dir + "/trainpb")));
			Relation r = null;

			Builder relBuilder = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				String rel = r.getRelType();
				dist.add(rel);
			}
			dist.printAll();
			is.close();
		}
		{
			D.p("Number of instance for every class in testing");
			HashCount<String> dist = new HashCount<String>();
			int count = 0;

			InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(dir + "/testpb")));
			Relation r = null;

			Builder relBuilder = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				String rel = r.getRelType();
				dist.add(rel);
			}
			dist.printAll();
			is.close();
		}
	}

	/**no NA*/
	public static void exp0() throws Exception {
		String expdir = Main.exp_mylabel0;
		String fileall05 = expdir + "/train.100.pb";
		String fileall07 = expdir + "/test.100.pb";
		String train = expdir + "/trainpb";
		String test = expdir + "/testpb";
		if (!new File(expdir).exists()) {
			(new File(expdir)).mkdir();
		}
		step2_relabelNytWithQueryResult(Main.file_extendedwidpairs, Main.file_myidnyt05pb, fileall05);
		step2_relabelNytWithQueryResult(Main.file_extendedwidpairs, Main.file_myidnyt07pb, fileall07);
		step3_shuffleAndSample(fileall05, train, 0, 100000);
		step3_shuffleAndSample(fileall07, test, 0, 100000);
		stat_dataset(expdir);
		SX_RphTrainTest.rphTrainTest(expdir);
	}

	public static void exp1() throws Exception {
		String expdir = Main.exp_mylabel1;
		String fileall05 = expdir + "/train.100.pb";
		String fileall07 = expdir + "/test.100.pb";
		String train = expdir + "/trainpb";
		String test = expdir + "/testpb";
		if (!new File(expdir).exists()) {
			(new File(expdir)).mkdir();
		}
		step2_relabelNytWithQueryResult(Main.file_extendedwidpairs, Main.file_myidnyt05pb, fileall05);
		step2_relabelNytWithQueryResult(Main.file_extendedwidpairs, Main.file_myidnyt07pb, fileall07);
		step3_shuffleAndSample(fileall05, train, 1, 100000);
		step3_shuffleAndSample(fileall07, test, -1, 100000);
		stat_dataset(expdir);
		SX_RphTrainTest.rphTrainTest(expdir);
	}

	public static void main(String[] args) throws Exception {
		//		D.p(Integer.MAX_VALUE);
		//		D.p(Long.MAX_VALUE);
		//		D.p(1<<30);
		/**I don't need to sort the file now*/
		{
			//			loadstr2wid();
			//			step1_getnytWid(Main.file_myidnyt05pb);
			//			step1_getnytWid(Main.file_myidnyt07pb);
		}
		/**
		 * 
		 * */
		exp0();
		exp1();
	}

}
