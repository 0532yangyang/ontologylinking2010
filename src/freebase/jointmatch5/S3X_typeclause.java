package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.ml.weightedmaxsat.WeightedClauses;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S3X_typeclause {

	public static String getVariableNameEntity(String nellstring, String mid) {
		String n = convertNellstring(nellstring);
		String m = mid;
		return "VE::" + n + "::" + m;
	}

	static private String convertNellstring(String nellstring) {
		return nellstring.replace(" ", "_");
	}

	static public String getVariableNameType(String nelltype, String fbtype) {

		return "VT::" + nelltype + "::" + fbtype;
	}

	/**
	 * Use similarity score as the weight of the positive literal; the score of
	 * its negative literal??? leave
	 */

	static void getSimilarityClause4Entity() throws IOException {
		entityvar = new HashSet<String>();
		HashMap<String, Double> variable_sim = new HashMap<String, Double>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nellstring_mid);
			List<String[]> raw = dr.readAll();
			for (String[] l : raw) {
				String v = getVariableNameEntity(l[1], l[0]);
				entityvar.add(v);
				variable_sim.put(getVariableNameEntity(l[1], l[0]), 0.0);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_weight_nellstring_mid_cosine);
			List<String[]> allweight = dr.readAll();
			for (String[] l : allweight) {
				String variable = getVariableNameEntity(l[1], l[0]);
				if (variable_sim.containsKey(variable)) {
					variable_sim.put(variable, Double.parseDouble(l[2]));
				}
			}
			dr.close();
		}
		{
			int num = 0;
			for (Entry<String, Double> e : variable_sim.entrySet()) {
				num++;
				dwclauseentity.write(e.getValue() + 0.1, e.getKey(), "similarity");

			}
			D.p("Number of entity similarity clauses: " + num);
		}
	}

	static void getCanonical_entity() {
		try {
			List<String[]> list_nellstr_mid = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nellstring_mid);
			String[] l;
			while ((l = dr.read()) != null) {
				String n = convertNellstring(l[1]);
				String m = l[0];
				list_nellstr_mid.add(new String[] { n, m });
			}
			StringTable.sortUniq(list_nellstr_mid);
			canonialoneside_entity(0, list_nellstr_mid);
			//canonialoneside_entity(1, list_nellstr_mid);

			D.p("abc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void canonialoneside_entity(final int key, List<String[]> list_nellstr_mid) {
		int num = 0;
		Collections.sort(list_nellstr_mid, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[key].compareTo(o2[key]);
			}
		});

		// same nellstr cannot map to two nell entity

		List<String[]> block = new ArrayList<String[]>();
		block.add(list_nellstr_mid.get(0));
		for (int i = 0; i < list_nellstr_mid.size(); i++) {
			if (!list_nellstr_mid.get(i)[key].equals(block.get(0)[key])) {
				num += help_canonical_entity(block);
				block.clear();
			}
			block.add(list_nellstr_mid.get(i));
		}
		num += help_canonical_entity(block);
		D.p("Canonical entity key " + key + " clauses number " + num);
	}

	private static int help_canonical_entity(List<String[]> block) {
		int num = 0;
		for (int i = 0; i < block.size(); i++) {
			String[] b1 = block.get(i);
			for (int j = 0; j < block.size(); j++) {
				String[] b2 = block.get(j);
				try {
					if (b1[0].equals(b2[0]) && b1[1].equals(b2[1]))
						continue;
					StringBuilder sb = new StringBuilder();
					sb.append("$neg$").append(getVariableNameEntity(b1[0], b1[1])).append(" ").append("$neg$")
							.append(getVariableNameEntity(b2[0], b2[1]));
					dwclauseentity.write(Main.WEIGHT_CANONICAL, sb.toString(), "canonical");
					num++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return num;
	}

	static void getCanonical_type() {
		try {
			List<String[]> list_ntype_fbtype = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nelltype_fbtype);
			String[] l;
			while ((l = dr.read()) != null) {
				String n = l[0];
				String m = l[1];
				list_ntype_fbtype.add(new String[] { n, m });
			}
			StringTable.sortUniq(list_ntype_fbtype);
			canonialoneside_type(0, list_ntype_fbtype);
			canonialoneside_type(1, list_ntype_fbtype);

			D.p("abc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void canonialoneside_type(final int key, List<String[]> list_ntype_fbtype) {
		int num = 0;
		Collections.sort(list_ntype_fbtype, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[key].compareTo(o2[key]);
			}
		});

		// same nellstr cannot map to two nell entity

		List<String[]> block = new ArrayList<String[]>();
		block.add(list_ntype_fbtype.get(0));
		for (int i = 0; i < list_ntype_fbtype.size(); i++) {
			if (!list_ntype_fbtype.get(i)[key].equals(block.get(0)[key])) {
				num += help_canonical_type(block);
				block.clear();
			}
			block.add(list_ntype_fbtype.get(i));
		}
		num += help_canonical_type(block);
		D.p("Canonical type key " + key + " clauses number " + num);
	}

	private static int help_canonical_type(List<String[]> block) {
		int num = 0;
		for (int i = 0; i < block.size(); i++) {
			String[] b1 = block.get(i);
			for (int j = 0; j < block.size(); j++) {
				String[] b2 = block.get(j);
				try {
					if (b1[0].equals(b2[0]) && b1[1].equals(b2[1]))
						continue;
					StringBuilder sb = new StringBuilder();
					sb.append("$neg$").append(getVariableNameType(b1[0], b1[1])).append(" ").append("$neg$")
							.append(getVariableNameType(b2[0], b2[1]));
					dwclausetype.write(Main.WEIGHT_CANONICAL, sb.toString(), "canonical");
					num++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return num;
	}

	/**
	 * For sampled FB entity e_f if, M(t_n, t_f) & Class(e_f, t_n), the weight
	 * should be negative
	 * */
	//	public static void getFBInstanceClause(String input_candidatemapping_nellstring_mid) {
	//		try {
	//			List<String[]> candidatemapping = (new DelimitedReader(input_candidatemapping_nellstring_mid))
	//					.readAll();
	//			DelimitedReader dr = new DelimitedReader(Main.fout_testing_pred);
	//			HashMap<String, List<String[]>> classifierPred = new HashMap<String, List<String[]>>();
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				if (!classifierPred.containsKey(l[0])) {
	//					classifierPred.put(l[0], new ArrayList<String[]>());
	//				}
	//				classifierPred.get(l[0]).add(l);
	//			}
	//			int iter = 0;
	//			for (String[] c : candidatemapping) {
	//				String nelltype = c[0];
	//				String fbtype = c[1];
	//				List<String[]> pool = classifierPred.get(fbtype);
	//				int good = 0, bad = 0;
	//				if (pool == null || pool.size() == 0) {
	//					bad = 1;
	//				} else {
	//					for (String[] a : pool) {
	//						if (a[1].equals(nelltype)) {
	//							good++;
	//						} else if (!a[1].equals("NA")) {
	//							bad++;
	//						}
	//					}
	//				}
	//				double negweight = -1.0 * bad / (bad + good);
	//
	//				dwclause.write(negweight, getVariableNameType(nelltype, fbtype));
	//			}
	//			//D.p(candidatemapping);
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//	}

	//	private static void getNegativeTypeClause(String input_candidatemapping_nelltype_fbtype,
	//			String input_weight_type_negative) {
	//		// TODO Auto-generated method stub
	//		try {
	//			HashMap<String, Double> variable_sim = new HashMap<String, Double>();
	//			{
	//				DelimitedReader dr = new DelimitedReader(input_candidatemapping_nelltype_fbtype);
	//				List<String[]> raw = dr.readAll();
	//				for (String[] l : raw) {
	//					variable_sim.put(getVariableNameType(l[0], l[1]), 0.0);
	//				}
	//				dr.close();
	//			}
	//			List<String[]> all = (new DelimitedReader(input_weight_type_negative)).readAll();
	//			{
	//				for (String[] a : all) {
	//					String nt = a[1], ft = a[2];
	//					String var = getVariableNameType(nt, ft);
	//					if (variable_sim.containsKey(var)) {
	//						variable_sim.put(var, Double.parseDouble(a[0]));
	//					}
	//				}
	//			}
	//			{
	//				Iterator<Entry<String, Double>> it = variable_sim.entrySet().iterator();
	//				while (it.hasNext()) {
	//					Entry<String, Double> e = it.next();
	//					dwclause.write(-1 * e.getValue(), e.getKey(), "negativetype");
	//				}
	//			}
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//	}
	//
	//	static void getEntityTypeJoint_old() throws IOException {
	//		//load mid 2 fbtype
	//		HashMap<String, List<String>> mid2types = new HashMap<String, List<String>>();
	//		HashSet<String> set_var_type = new HashSet<String>();
	//		HashCount<String> type_freq = new HashCount<String>();
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				String mid = l[0];
	//				String fbtype = l[2];
	//				if (!mid2types.containsKey(mid)) {
	//					mid2types.put(mid, new ArrayList<String>());
	//				}
	//				mid2types.get(mid).add(fbtype);
	//				type_freq.add(fbtype);
	//			}
	//		}
	//
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nelltype_fbtype);
	//			List<String[]> raw = dr.readAll();
	//			for (String[] l : raw) {
	//				set_var_type.add(getVariableNameType(l[0], l[1]));
	//			}
	//			dr.close();
	//		}
	//		//iterator over entity match
	//		{
	//			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nellstring_mid);
	//			String[] l;
	//			while ((l = dr.read()) != null) {
	//				String nellstr = l[1];
	//				HashSet<String> nelltype = Main.no.entitylower2class.get(nellstr);
	//				String mid = l[0];
	//				String varent = getVariableNameEntity(nellstr, mid);
	//				StringBuilder sb = new StringBuilder();
	//				sb.append("$neg$").append(varent);
	//				List<String> fbtypes = mid2types.get(mid);
	//				if (fbtypes == null) {
	//					continue;
	//				}
	//				//pick one fbtype
	//				int min = Integer.MAX_VALUE;
	//				String specific_fbt = "";
	//				for (String fbt : fbtypes) {
	//					int freq = type_freq.see(fbt);
	//					if (freq < min) {
	//						freq = min;
	//						specific_fbt = fbt;
	//					}
	//				}
	//				boolean anyInCandidate = false;
	//				for (String nt : nelltype) {
	//					String vartyp = getVariableNameType(nt, specific_fbt);
	//					if (set_var_type.contains(vartyp)) {
	//						sb.append(" ").append(vartyp);
	//						anyInCandidate = true;
	//					}
	//				}
	//				//				if (mid.equals("/m/01b8jj")) {
	//				//					D.p("nelltype", nelltype);
	//				//					D.p("varent", varent);
	//				//					D.p("fbtypes", fbtypes);
	//				//					D.p("sb", sb.toString());
	//				//				}
	//				//when anyInCandidate is false, the mid is completely irrelevant to types of that nell object
	//				if (anyInCandidate) {
	//					dwclause.write(1.0, sb.toString(), "typejoint");
	//				}
	//			}
	//		}
	//	}

	public static void getCandidateTypeMatching() throws IOException {
		List<String[]> evidence = new ArrayList<String[]>();
		DelimitedWriter dw = new DelimitedWriter(Main.file_candidatemapping_nelltype_fbtype);
		DelimitedWriter dw2 = new DelimitedWriter(Main.file_nelltype_fbtype_evidenceentity_mid);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_seedluceneclean);
			String[] l;
			while ((l = dr.read()) != null) {
				String rel = l[0];
				String[] domain_range = Main.no.relname2DomainRange.get(rel);
				evidence.add(new String[] { domain_range[0], l[8], l[2], l[4] });
				evidence.add(new String[] { domain_range[1], l[9], l[3], l[5] });
			}
			dr.close();
		}
		StringTable.sortUniq(evidence);
		for (String[] w : evidence) {
			dw2.write(w);
		}
		dw2.close();
		List<String[]> evidence2 = StringTable.squeeze(evidence, new int[] { 0, 1 });
		HashCount<String> writehasNellstr = new HashCount<String>();
		List<String[]> towrite = new ArrayList<String[]>();
		for (String[] w : evidence2) {
			if (writehasNellstr.see(w[1]) < 10) {
				towrite.add(new String[] { w[1], w[2] });
				writehasNellstr.add(w[1]);
			}
		}
		StringTable.sortByColumn(towrite, new int[] { 0, 1 });
		for (String[] w : towrite) {
			dw.write(w);
		}
		dw.close();
	}

	public static void getSimilarityClause4Type() throws IOException {
		{
			typevar = new HashSet<String>();
			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nelltype_fbtype);
			String[] l;
			while ((l = dr.read()) != null) {
				String nelltype = l[0];
				String fbtype = l[1];
				String var = getVariableNameType(nelltype, fbtype);
				int words = StringUtil.numOfShareWords(nelltype, fbtype, new char[] { '/', '_', ' ' });
				if (!typevar.contains(var)) {
					dwclausetype.write(words + 0.1, var, "similarity");
					typevar.add(var);

				}
			}
		}
	}

	public static void getEntityTypeJoint() throws IOException {
		{
			HashSet<String> appear = new HashSet<String>();
			DelimitedReader dr = new DelimitedReader(Main.file_nelltype_fbtype_evidenceentity_mid);
			String[] l;
			while ((l = dr.read()) != null) {
				String nelltype = l[0];
				String fbtype = l[1];
				String nellstr = l[2];
				String mid = l[3];
				String vart = getVariableNameType(nelltype, fbtype);
				String vare = getVariableNameEntity(nellstr, mid);

				String w = "$neg$" + vare + " " + vart;
				if (typevar.contains(vart) && entityvar.contains(vare) && !appear.contains(w)) {
					dwclausetype.write(1, w, "joint");
					appear.add(w);
				}
			}
		}
	}

	static DelimitedWriter dwclausetype;
	static DelimitedWriter dwclauseentity;
	static HashSet<String> typevar;
	static HashSet<String> entityvar;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		dwclausetype = new DelimitedWriter(Main.file_clausetype);
		dwclauseentity = new DelimitedWriter(Main.file_clauseentity);
		getCandidateTypeMatching();
		getSimilarityClause4Entity();
		getSimilarityClause4Type();
		getEntityTypeJoint();
		getCanonical_entity();
		getCanonical_type();
		dwclausetype.close();
		dwclauseentity.close();
		{
			List<String[]> clauses = new ArrayList<String[]>();
			{
				List<String[]> all1 = (new DelimitedReader(Main.file_clausetype)).readAll();
				List<String[]> all2 = (new DelimitedReader(Main.file_clauseentity)).readAll();
				for (String[] a : all1) {
					clauses.add(a);
				}
				for (String[] b : all2) {
					clauses.add(b);
				}
			}
			Collections.shuffle(clauses);
			WeightedClauses wc = new WeightedClauses(clauses);
			wc.update();
			wc.printFinalResult(Main.file_predict_typeonly);
		}
		//		getSimilarityClause4Entity(Main.file_candidatemapping_nellstring_mid, Main.file_weight_nellstring_mid_cosine);
		//
		//		getSimilarityClause4Type(Main.file_candidatemapping_nelltype_fbtype, Main.file_weight_type_shareentity);
		//
		//		//getNegativeTypeClause(Main.file_candidatemapping_nelltype_fbtype, Main.file_weight_type_negative);
		//		/**
		//		 * Every nell string can only map to one fb entity that is,
		//		 * nellstr::fbA -> neg_nellstr::fbB EQUAL: neg_nellstr::fbA OR
		//		 * neg_nellstr::fbB
		//		 * 
		//		 * Meanwhile, every fb entity can only be one nellstring that is,
		//		 * nellstr1::A neg_nellstr2::A EQUAL: neg_nellstr1::A ->
		//		 * neg_nellstr2::A
		//		 */
		//		getCanonical_entity(Main.file_candidatemapping_nellstring_mid);
		//
		//		/**
		//		 * questionable assumption here... Every fb type can only be mapped
		//		 * to one nell typ. THIS IS WRONG!!!
		//		 * 
		//		 * */
		//		getCanonical_type(Main.file_candidatemapping_nelltype_fbtype);
		//
		//		getEntityTypeJoint();

	}

}
