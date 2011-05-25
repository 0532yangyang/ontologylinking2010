package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S42 {

	public static void step1_name_similarity(List<UnionRelation> filtererelations) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_weight_similarity_nellrel_fbrel);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			String fbname = ur.unionRelationName;
			int urid = ur.unionRelId;
			List<String> t1 = StringUtil.tokenize(nrname.replace("_inverse", ""), new char[] { '_' });
			List<String> t2 = StringUtil.tokenize(fbname, new char[] { ' ', '_', '/', '|' });
			int share = StringUtil.numOfShareWords(t1, t2, new boolean[] { true, true, true });
			// double score = share * 1.0 / t1.size();
			dw.write(nrname, urid + "", share, t1.size(), t2.size(), fbname);
		}
		dw.close();
	}

	public static void step2_shareInstance_similarity(List<UnionRelation> filtererelations) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_weight_sharepair_nellrel_fbrel);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			String fbname = ur.unionRelationName;
			int urid = ur.unionRelId;
			// double score = share * 1.0 / t1.size();
			dw.write(nrname, urid + "", ur.instances.size(), fbname);
		}
		dw.close();
	}

	public static void step3_explosivejoin(List<UnionRelation> filtererelations) throws IOException {
		HashSet<String> explosive = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_union_explosive);
			String[] l;
			while ((l = dr.read()) != null) {
				explosive.add(l[2]);
			}
			dr.close();
		}
		DelimitedWriter dw = new DelimitedWriter(Main.file_weight_explosive_nellrel_fbrel);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			//String fbname = ur.unionRelationName;
			String[] singleNames = ur.getSingleRelNames();
			int urid = ur.unionRelId;
			boolean isExplosive = false;
			for (String n : singleNames) {
				if (explosive.contains(n)) {
					isExplosive = true;
				}
			}
			if (isExplosive) {
				dw.write(nrname, urid + "", ur.unionRelationName);
			}
		}
		dw.close();
	}

	public static void step4_negativeseed(String file_nellstrmap) throws IOException {
		HashMap<String, Integer> nellstr2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(file_nellstrmap);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellstr = l[0];
				int wid = Integer.parseInt(l[2]);
				if (!nellstr2wid.containsKey(nellstr)) {
					nellstr2wid.put(nellstr, wid);
				}
			}
		}
		HashMap<String, List<String>> seed_negative = new HashMap<String, List<String>>();
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] a : nr.known_negatives) {
				if (nellstr2wid.containsKey(a[0]) && nellstr2wid.containsKey(a[1])) {
					int wid1 = nellstr2wid.get(a[0]);
					int wid2 = nellstr2wid.get(a[1]);
					String key = wid1 + "\t" + wid2;
					if (!seed_negative.containsKey(key)) {
						seed_negative.put(key, new ArrayList<String>());
					}

					seed_negative.get(key).add(nr.relation_name);
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(Main.file_weight_seednegativepair_nellrel_fbrel);
		List<String[]> towrite = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_union);
			String[] l;
			while ((l = dr.read()) != null) {
				String wid1str = l[0];
				String wid2str = l[1];
				String key1 = wid1str + "\t" + wid2str;
				if (seed_negative.containsKey(key1)) {
					List<String> negnellstrs = seed_negative.get(key1);
					for (String r : negnellstrs) {
						towrite.add(new String[] { l[0], l[1], l[2], l[3], l[4], r });
					}
				}
				String key2 = wid2str + "\t" + wid1str;
				if (seed_negative.containsKey(key2)) {
					List<String> negnellstrs = seed_negative.get(key2);
					for (String r : negnellstrs) {
						towrite.add(new String[] { l[0], l[1], l[2], l[3], l[4], r });
					}
				}
			}
			dr.close();
		}
		StringTable.sortUniq(towrite);
		for (String[] w : towrite) {
			dw.write(w);
		}
		dw.close();
	}

	public static void stepX_inference(FreebaseRelation fbr) throws IOException {
		{
			/**name*/
			List<String[]> all = (new DelimitedReader(Main.file_weight_similarity_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				double score = Integer.parseInt(l[2]);
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.SHAREWORDS, score);
			}
		}
		{
			/**share instances*/
			List<String[]> all = (new DelimitedReader(Main.file_weight_sharepair_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				double score = Integer.parseInt(l[2]);
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.SHAREINSTANCE, score);
			}
		}
		{
			/**explosive during join*/
			List<String[]> all = (new DelimitedReader(Main.file_weight_explosive_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.EXPLOSIVEJOIN, -100);
			}
		}
		{
			/**seed negative*/
			List<String[]> all = (new DelimitedReader(Main.file_weight_seednegativepair_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[4]);
				String nellrel = l[5];
				UnionRelation ur = fbr.getUnionRelationById(urid);
				if (ur.nellRelationName.equals(nellrel)) {
					ur.setScore(UnionRelationScoreIndex.SEEDNEGATIVE, -100);
				}
			}
		}
		{
			/**Inference*/

			List<String[]> towrite = new ArrayList<String[]>();
			for (UnionRelation ur : fbr.filteredUnionRelations) {
				double sum = 0;
				StringBuilder sb = new StringBuilder();
				for (Entry<UnionRelationScoreIndex, Double> e : ur.scores.entrySet()) {
					sum += e.getValue();
					sb.append(e.getKey() + "=" + e.getValue() + " ");
				}
				if (!ur.nellRelationName.contains("_inverse")) {
					towrite.add(new String[] { ur.nellRelationName, sum + "", ur.unionRelId + "", ur.unionRelationName,
							sb.toString() });
				}
				//dw.write(ur.nellRelationName, sum + "", ur.unionRelId, ur.unionRelationName, sb.toString());
			}
			StringTable.sortByColumn(towrite, new int[] { 0, 1 }, new boolean[] { false, true });
			StringTable.delimitedWrite(towrite, Main.file_inference_relation);
		}
	}

	public static void main(String[] args) throws IOException {
		FreebaseRelation fbr = new FreebaseRelation(Main.file_seedbfspath_result);
		step1_name_similarity(fbr.filteredUnionRelations);
		step2_shareInstance_similarity(fbr.filteredUnionRelations);
		step3_explosivejoin(fbr.filteredUnionRelations);
		step4_negativeseed(Main.file_fbsearch2);
		stepX_inference(fbr);

	}

}
