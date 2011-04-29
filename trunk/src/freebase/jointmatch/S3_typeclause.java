package freebase.jointmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S3_typeclause {

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
	static void getSimilarityClause4Entity(String input_candidatemapping_nellstring_mid,
			String input_weight_nellstring_mid_cosine) {
		try {
			HashMap<String, Double> variable_sim = new HashMap<String, Double>();
			{
				DelimitedReader dr = new DelimitedReader(input_candidatemapping_nellstring_mid);
				List<String[]> raw = dr.readAll();
				for (String[] l : raw) {
					variable_sim.put(getVariableNameEntity(l[1], l[0]), 0.0);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(input_weight_nellstring_mid_cosine);
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
					dwclause.write(e.getValue(), e.getKey());
				}
				D.p("Number of entity similarity clauses: " + num);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void getSimilarityClause4Type(String input_candidatemapping_nelltype_fbtype,
			String input_file_weight_type_shareentity) {
		try {
			HashMap<String, Double> variable_sim = new HashMap<String, Double>();
			{
				DelimitedReader dr = new DelimitedReader(input_candidatemapping_nelltype_fbtype);
				List<String[]> raw = dr.readAll();
				for (String[] l : raw) {
					variable_sim.put(getVariableNameType(l[0], l[1]), 0.0);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(input_file_weight_type_shareentity);
				List<String[]> allweight = dr.readAll();
				for (String[] l : allweight) {
					String variable = getVariableNameType(l[0], l[1]);
					if (variable_sim.containsKey(variable)) {
						double weight = Integer.parseInt(l[2]) * 1.0 / Integer.parseInt(l[3]);
						variable_sim.put(variable, weight);
					}
				}
				dr.close();
			}
			{
				int num = 0;
				for (Entry<String, Double> e : variable_sim.entrySet()) {
					num++;
					dwclause.write(e.getValue(), e.getKey());
				}
				D.p("Number of type similarity clauses: " + num);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void getCanonical_entity(String input_candidatemapping_nellstring_mid) {
		try {
			List<String[]> list_nellstr_mid = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(input_candidatemapping_nellstring_mid);
			String[] l;
			while ((l = dr.read()) != null) {
				String n = convertNellstring(l[1]);
				String m = l[0];
				list_nellstr_mid.add(new String[] { n, m });
			}
			canonialoneside_entity(0, list_nellstr_mid);
			canonialoneside_entity(1, list_nellstr_mid);

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
					dwclause.write(Main.WEIGHT_CANONICAL, sb.toString());
					num++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return num;
	}

	static void getCanonical_type(String input_candidatemapping_nelltype_fbtype) {
		try {
			List<String[]> list_ntype_fbtype = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(input_candidatemapping_nelltype_fbtype);
			String[] l;
			while ((l = dr.read()) != null) {
				String n = l[0];
				String m = l[1];
				list_ntype_fbtype.add(new String[] { n, m });
			}
			canonialoneside_type(0, list_ntype_fbtype);

			// canonialoneside_type(1, list_ntype_fbtype);

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
					dwclause.write(Main.WEIGHT_CANONICAL, sb.toString());
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

	private static void getNegativeTypeClause(String input_candidatemapping_nelltype_fbtype,
			String input_weight_type_negative) {
		// TODO Auto-generated method stub
		try {
			HashMap<String, Double> variable_sim = new HashMap<String, Double>();
			{
				DelimitedReader dr = new DelimitedReader(input_candidatemapping_nelltype_fbtype);
				List<String[]> raw = dr.readAll();
				for (String[] l : raw) {
					variable_sim.put(getVariableNameType(l[0], l[1]), 0.0);
				}
				dr.close();
			}
			List<String[]> all = (new DelimitedReader(input_weight_type_negative)).readAll();
			{
				for (String[] a : all) {
					String nt = a[1], ft = a[2];
					String var = getVariableNameType(nt, ft);
					if (variable_sim.containsKey(var)) {
						variable_sim.put(var, Double.parseDouble(a[0]));
					}
				}
			}
			{
				Iterator<Entry<String, Double>> it = variable_sim.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Double> e = it.next();
					dwclause.write(-1 * e.getValue(), e.getKey());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static DelimitedWriter dwclause;

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub

		dwclause = new DelimitedWriter(Main.file_typeclause);

		//getFBInstanceClause();

		getSimilarityClause4Entity(Main.file_candidatemapping_nellstring_mid, Main.file_weight_nellstring_mid_cosine);

		getSimilarityClause4Type(Main.file_candidatemapping_nelltype_fbtype,
				Main.file_weight_type_shareentity);

		getNegativeTypeClause(Main.file_candidatemapping_nelltype_fbtype, Main.file_weight_type_negative);
		/**
		 * Every nell string can only map to one fb entity that is,
		 * nellstr::fbA -> neg_nellstr::fbB EQUAL: neg_nellstr::fbA OR
		 * neg_nellstr::fbB
		 * 
		 * Meanwhile, every fb entity can only be one nellstring that is,
		 * nellstr1::A neg_nellstr2::A EQUAL: neg_nellstr1::A ->
		 * neg_nellstr2::A
		 */
		getCanonical_entity(Main.file_candidatemapping_nellstring_mid);

		/**
		 * questionable assumption here... Every fb type can only be mapped
		 * to one nell typ. THIS IS WRONG!!!
		 * 
		 * */
		getCanonical_type(Main.file_candidatemapping_nelltype_fbtype);

		dwclause.close();

	}

}
