package freebase.jointmatch6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S4 {

	public static void step1_name_similarity(FreebaseRelation fbr) throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
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

	public static void step2_shareInstance_similarity(FreebaseRelation fbr) throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
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

	public static void step3_explosivejoin(FreebaseRelation fbr) throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
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

	public static void step5_weight_wikilink() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_weight_wikilink_nellrel_fbrel);
		HashMap<Long, Set<Integer>> unionwidpair2relationId = new HashMap<Long, Set<Integer>>();
		HashSet<Long> hasLink = new HashSet<Long>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance_union);
			String[] l;
			while ((l = dr.read()) != null) {
				long v = StringTable.intPair2Long(l[0], l[1]);
				if (!unionwidpair2relationId.containsKey(v)) {
					unionwidpair2relationId.put(v, new HashSet<Integer>());
				}
				unionwidpair2relationId.get(v).add(Integer.parseInt(l[4]));
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			String[] l;
			while ((l = dr.read()) != null) {
				long v1 = StringTable.intPair2Long(l[0], l[1]);
				long v2 = StringTable.intPair2Long(l[1], l[0]);
				if (unionwidpair2relationId.containsKey(v1)) {
					hasLink.add(v1);
				}
				if (unionwidpair2relationId.containsKey(v2)) {
					hasLink.add(v2);
				}
			}
		}
		HashCount<Integer> urid2allpairs = new HashCount<Integer>();
		HashCount<Integer> urid2linkedpairs = new HashCount<Integer>();
		{
			for (Entry<Long, Set<Integer>> e : unionwidpair2relationId.entrySet()) {
				long v = e.getKey();
				boolean ok = false;
				if (hasLink.contains(v)) {
					ok = true;
				}
				for (int urid : e.getValue()) {
					urid2allpairs.add(urid);
					if (ok) {
						urid2linkedpairs.add(urid);
					}
				}
			}
			Iterator<Entry<Integer, Integer>> it = urid2allpairs.iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> e = it.next();
				int urid = e.getKey();
				int all = e.getValue();
				int linkvalue = urid2linkedpairs.see(urid);
				dw.write(urid, linkvalue, all);
			}
		}
		dw.close();
	}

	public static void stepX_inference(FreebaseRelation fbr, String file_inference_relation) throws IOException {
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
				ur.setScore(UnionRelationScoreIndex.EXPLOSIVEJOIN, 1);
			}
		}
		{
			/**seed negative*/
			List<String[]> all = (new DelimitedReader(Main.file_weight_seednegativepair_nellrel_fbrel)).readAll();
			HashCount<Integer> urid_badtimes = new HashCount<Integer>();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[4]);
				String nellrel = l[5];
				UnionRelation ur = fbr.getUnionRelationById(urid);
				if (ur.nellRelationName.equals(nellrel)) {
					urid_badtimes.add(urid);
				}
			}
			Iterator<Entry<Integer, Integer>> it = urid_badtimes.iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> e = it.next();
				//more than two negative matches
				if (e.getValue() > 1) {
					int urid = e.getKey();
					UnionRelation ur = fbr.getUnionRelationById(urid);
					ur.setScore(UnionRelationScoreIndex.SEEDNEGATIVE, 1);
				}
			}
		}
		{
			/**pairs have wikipedia page links*/
			List<String[]> all = (new DelimitedReader(Main.file_weight_wikilink_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[0]);
				int haslink = Integer.parseInt(l[1]);
				int total = Integer.parseInt(l[2]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.WIKILINK, (total - haslink) * 1.0 / total);
				ur.setScore(UnionRelationScoreIndex.NUMINSTANCE, total);
			}
		}
		{
			/**Set weights for differen features*/
			UnionRelation.setWeight(UnionRelationScoreIndex.EXPLOSIVEJOIN, -100);
			UnionRelation.setWeight(UnionRelationScoreIndex.SEEDNEGATIVE, -100);
			UnionRelation.setWeight(UnionRelationScoreIndex.SHAREWORDS, 1);
			UnionRelation.setWeight(UnionRelationScoreIndex.SHAREINSTANCE, 1);
			UnionRelation.setWeight(UnionRelationScoreIndex.NUMINSTANCE, 0);
			UnionRelation.setWeight(UnionRelationScoreIndex.WIKILINK, 0);
		}
		{
			/**Inference*/
			List<String[]> towrite = new ArrayList<String[]>();
			for (UnionRelation ur : fbr.filteredUnionRelations) {
				double sum = 0;
				StringBuilder sb = new StringBuilder();
				for (Entry<UnionRelationScoreIndex, Double> e : ur.scores.entrySet()) {
					sum += UnionRelation.getWeight(e.getKey()) * e.getValue();
					sb.append(e.getKey() + "=" + e.getValue() + " ");
				}
				if (!ur.nellRelationName.contains("_inverse")) {
					towrite.add(new String[] { ur.nellRelationName, sum + "", ur.unionRelId + "", ur.unionRelationName,
							sb.toString() });
				}
				//dw.write(ur.nellRelationName, sum + "", ur.unionRelId, ur.unionRelationName, sb.toString());
			}
			StringTable.sortByColumn(towrite, new int[] { 0, 1 }, new boolean[] { false, true });
			StringTable.delimitedWrite(towrite, file_inference_relation);
		}

	}

	public static void stepX_setGoodUnionRelation(FreebaseRelation fbr, //all union relations 
			String file_inference_relation,//input: details about the inferece 
			int topk,//take top k as the result match
			String output //output match result
	) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(file_inference_relation);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			for (int i = 0; i < topk && i < b.size(); i++) {
				String[] l = b.get(i);
				double sum = Double.parseDouble(l[1]);
				int urid = Integer.parseInt(l[2]);
				if (sum > 0) {
					UnionRelation ur = fbr.getUnionRelationById(urid);
					ur.isGood = true;
					dw.write(ur.nellRelationName, ur.unionRelId);
				}
			}
		}
		dw.close();
	}

	public static void stepX_getTypeConstrain(FreebaseRelation fbr, String output) throws IOException {
		List<String[]> argstuff = new ArrayList<String[]>();
		HashMap<Integer, String> gnid2type = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				int gnid = Integer.parseInt(l[0]);
				gnid2type.put(gnid, l[5]);
			}
			dr.close();
		}

		for (UnionRelation ur : fbr.filteredUnionRelations) {
			if (ur.isGood) {
				for (String ins : ur.instances) {
					String[] arg1arg2 = ins.split("\t");
					int gnid1 = Integer.parseInt(arg1arg2[0]);
					int gnid2 = Integer.parseInt(arg1arg2[1]);
					String type1 = gnid2type.containsKey(gnid1) ? gnid2type.get(gnid1) : "NA";
					String type2 = gnid2type.containsKey(gnid2) ? gnid2type.get(gnid2) : "NA";

					String[] domainrange = Main.no.relname2DomainRange.get(ur.nellRelationName);
					argstuff.add(new String[] { domainrange[0], type1, gnid1 + "", });
					argstuff.add(new String[] { domainrange[1], type2, gnid2 + "" });
				}
			}
		}
		StringTable.sortUniq(argstuff);
		DelimitedWriter dw = new DelimitedWriter(output);
		for (String[] w : argstuff) {
			dw.write(w);
		}
		dw.close();
	}

	public static void main(String[] args) throws IOException {

		String file_inference_relation = Main.file_inference_relation;
		String file_afterrelmatch_typeconstrain = Main.file_afterrelmatch_typeconstrain;
		String file_afterrelmatch_relmatchres = Main.file_afterrelmatch_relmatchres;
		String file_Nellstr2Mid = Main.dir + "/nellstr2mid";

		FreebaseRelation fbr = new FreebaseRelation(Main.file_seedbfspath_result);
		step1_name_similarity(fbr);
		step2_shareInstance_similarity(fbr);
		step3_explosivejoin(fbr);
		step4_negativeseed(file_Nellstr2Mid);
		step5_weight_wikilink();
		stepX_inference(fbr, file_inference_relation);
		stepX_setGoodUnionRelation(fbr, file_inference_relation, 3, file_afterrelmatch_relmatchres);
		stepX_getTypeConstrain(fbr, file_afterrelmatch_typeconstrain);

	}
}
