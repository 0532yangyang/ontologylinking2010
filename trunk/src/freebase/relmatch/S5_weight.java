package freebase.relmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S5_weight {

	// input: seedbfspathes_show_group
	// output:weight_similarity_nellrel_fbrel
	public static void step1_surfacestring_similarity() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_show_group);
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_similarity_nellrel_fbrel);
			String[] l;
			while ((l = dr.read()) != null) {
				List<String> t1 = StringUtil.tokenize(l[0].replace("_inverse", ""), new char[] { '_' });
				List<String> t2 = StringUtil.tokenize(l[2], new char[] { ' ', '_', '/', '|' });
				int share = StringUtil.numOfShareWords(t1, t2, new boolean[] { true, true, true });
				// double score = share * 1.0 / t1.size();
				dw.write(l[0], l[2], share);
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void step2_share_instance() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_show_group);
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_sharepair_nellrel_fbrel);
			String[] l;
			while ((l = dr.read()) != null) {
				dw.write(l[0], l[2], l[3]);
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void step3_seednegativepairs() {

		HashMap<String, String> nellstr2mid = new HashMap<String, String>();
		HashMap<String, Integer> mid2gnid = new HashMap<String, Integer>();
		NellOntology no;
		// for one pair of entity of the negative seeds
		HashMap<String, Set<String>> seed_negative = new HashMap<String, Set<String>>();
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_seednegativepair_nellrel_fbrel);
			no = new NellOntology();
			{
				List<String[]> nellstrMidPred = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_cleanno1))
						.readAll();
				for (String[] a : nellstrMidPred) {
					nellstr2mid.put(a[3], a[1]);
				}
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
				String[] l;
				while ((l = dr.read()) != null) {
					int gnid = Integer.parseInt(l[0]);
					String mid = l[1];
					mid2gnid.put(mid, gnid);
				}
				dr.close();
			}
			List<String> relationnames = new ArrayList<String>();
			for (NellRelation nr : no.nellRelationList) {
				relationnames.add(nr.relation_name);
			}
			for (NellRelation nr : no.nellRelationList) {
				for (String[] a : nr.known_negatives) {
					try {
						String mid1 = nellstr2mid.get(a[0]);
						String mid2 = nellstr2mid.get(a[1]);
						int gnid1 = mid2gnid.get(mid1);
						int gnid2 = mid2gnid.get(mid2);
						String key = gnid1 + "\t" + gnid2;
						if (!seed_negative.containsKey(key)) {
							seed_negative.put(key, new HashSet<String>());
						}
						seed_negative.get(key).add(nr.relation_name);
						// D.p(nr.relation_name, gnid1, gnid2);
					} catch (Exception e) {

					}
				}
			}
			List<String[]> list_negative = new ArrayList<String[]>();
			{
				/** check all queryresult.name */

				DelimitedReader dr = new DelimitedReader(Main.file_queryresult_name);
				String[] l;
				while ((l = dr.read()) != null) {
					int startId = Integer.parseInt(l[0]);
					int endId = Integer.parseInt(l[1]);
					String possible_nellrel = l[2].replace("_inverse", "");
					// String fbrel = l[4];
					String keytolook1 = startId + "\t" + endId;
					String keytolook2 = endId + "\t" + startId;
					Set<String> seednegrel1 = seed_negative.get(keytolook1);
					Set<String> seednegrel2 = seed_negative.get(keytolook2);
					if (seednegrel1 != null && seednegrel1.contains(possible_nellrel)
							|| seednegrel2 != null && seednegrel2.contains(possible_nellrel)) {
						list_negative.add(new String[] { l[0], l[1], l[2], l[4], l[5], l[6] });
					}
				}
				dr.close();
			}
			{
				StringTable.sortByColumn(list_negative, new int[] { 2, 3, 0 });
				HashCount<String> hcseed = new HashCount<String>();
				for (String[] a : list_negative) {
					String key = a[2] + "\t" + a[3];
					hcseed.add(key);
				}
				Iterator<Entry<String, Integer>> itseed = hcseed.iterator();
				while (itseed.hasNext()) {
					Entry<String, Integer> e = itseed.next();
					String[] ab = e.getKey().split("\t");
					dw.write(ab[0], ab[1], e.getValue());
				}
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void step3_defaultnegativepairs() {

		HashMap<String, String> nellstr2mid = new HashMap<String, String>();
		HashMap<String, Integer> mid2gnid = new HashMap<String, Integer>();
		NellOntology no;
		HashMap<String, Set<String>> default_negative = new HashMap<String, Set<String>>();
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_defaultnegativepair_nellrel_fbrel);
			no = new NellOntology();
			{
				List<String[]> nellstrMidPred = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_cleanno1))
						.readAll();
				for (String[] a : nellstrMidPred) {
					nellstr2mid.put(a[3], a[1]);
				}
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
				String[] l;
				while ((l = dr.read()) != null) {
					int gnid = Integer.parseInt(l[0]);
					String mid = l[1];
					mid2gnid.put(mid, gnid);
				}
				dr.close();
			}
			List<String> relationnames = new ArrayList<String>();
			for (NellRelation nr : no.nellRelationList) {
				relationnames.add(nr.relation_name);
			}
			for (NellRelation nr : no.nellRelationList) {
				for (String[] a : nr.seedInstances) {
					try {
						String mid1 = nellstr2mid.get(a[0]);
						String mid2 = nellstr2mid.get(a[1]);
						int gnid1 = mid2gnid.get(mid1);
						int gnid2 = mid2gnid.get(mid2);

						String key = gnid1 + "\t" + gnid2;
						if (!default_negative.containsKey(key)) {
							default_negative.put(key, new HashSet<String>());
						}
						for (String r : relationnames) {
							if (!r.equals(nr.relation_name)) {
								default_negative.get(key).add(r);
							}
						}
					} catch (Exception e) {
					}
				}
			}
			List<String[]> list_negative = new ArrayList<String[]>();
			{
				/** check all queryresult.name */
				DelimitedReader dr = new DelimitedReader(Main.file_queryresult_name);
				String[] l;
				while ((l = dr.read()) != null) {
					int startId = Integer.parseInt(l[0]);
					int endId = Integer.parseInt(l[1]);
					String possible_nellrel = l[2].replace("_inverse", "");
					// String fbrel = l[4];
					String keytolook1 = startId + "\t" + endId;
					String keytolook2 = endId + "\t" + startId;
					Set<String> defaultnegrel1 = default_negative.get(keytolook1);
					Set<String> defaultnegrel2 = default_negative.get(keytolook2);
					if (defaultnegrel1 != null && defaultnegrel1.contains(possible_nellrel) ||
							defaultnegrel2 != null && defaultnegrel2.contains(possible_nellrel)) {
						list_negative.add(new String[] { l[0], l[1], l[2], l[4], l[5], l[6] });
						// dw.write("default", l[0], l[1], l[2], l[4], l[5],
						// l[6]);
					}
				}
				dr.close();
			}
			{
				StringTable.sortByColumn(list_negative, new int[] { 2, 3, 0 });
				HashCount<String> hcdefault = new HashCount<String>();
				for (String[] a : list_negative) {
					String key = a[2] + "\t" + a[3];
					hcdefault.add(key);
				}
				Iterator<Entry<String, Integer>> it = hcdefault.iterator();
				while (it.hasNext()) {
					Entry<String, Integer> e = it.next();
					String[] ab = e.getKey().split("\t");
					dw.write(ab[0], ab[1], e.getValue());
				}

			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void step4_typesatisfying() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_queryresult_typesatisfy);
			DelimitedWriter dw = new DelimitedWriter(Main.file_weight_typesatifying);
			String[] l;
			HashCount<String>hc_whole = new HashCount<String>();
			HashCount<String>hc_true = new HashCount<String>();
			while ((l = dr.read()) != null) {
				String key = l[4]+"\t"+l[6];
				if(l[0].equals("true") && l[1].equals("true")){
					hc_true.add(key);
				}
				hc_whole.add(key);
			}
			dr.close();
			//iterator over hc_whole
			Iterator<Entry<String,Integer>> it = hc_whole.iterator();
			while(it.hasNext()){
				Entry<String,Integer>e = it.next();
				String key = e.getKey();
				int whole = e.getValue();
				int trueval = hc_true.see(key);
				String []ab = key.split("\t");
				dw.write(trueval+";;"+whole,ab[0],ab[1]);
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/** surface string similarity */
		// step1_surfacestring_similarity();

		/** number of instances p=<a1,a2> satisfying I(p, fr) & I(p, nr) */
		// step2_share_instance();

		/**
		 * negative evidence: seed: negative seed instances; default: pair
		 * belonging to R\r relation is the default negative pair for relation r
		 * p=<a1,a2>, I know NI(p,nr) AND I(p,fr) --> \neg M(fr,nr)
		 */
		{
//			step3_seednegativepairs();
//			step3_defaultnegativepairs();
		}

		/**
		 * Type satisfying weight
		 * */
		step4_typesatisfying();
	}

}
