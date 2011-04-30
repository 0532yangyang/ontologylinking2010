package freebase.jointmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S7_clauserelation {
	static DelimitedWriter dwclause;

	static private String getVariableRelation(String nellrelation, String fbrelation) {
		String n = nellrelation;
		String m = fbrelation;
		return "VR::" + n + "::" + m;
	}

	static void getRelationPairClause() {
		try {
			List<String[]> all = (new DelimitedReader(Main.file_weight_similarity_nellrel_fbrel)).readAll();
			List<String[]> candidate = StringTable.selectTopKofBlock(all, 0, Main.PAR_NUM_CANDIDATE);

			DelimitedWriter dw = new DelimitedWriter(Main.file_relationcandidate);

			for (String[] c : candidate) {
				double weight = Double.parseDouble(c[2]);
				String var = getVariableRelation(c[0], c[1]);
				dw.write(c[0], c[1]);
				dwclause.write(weight + 0.1, var);
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void getCanonical_relation() {
		try {
			List<String[]> list_nr_fr = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(Main.file_relationcandidate);
			String[] l;
			while ((l = dr.read()) != null) {
				String n = l[0];
				String m = l[1];
				list_nr_fr.add(new String[] { n, m });
			}
			canonialoneside_relation(0, list_nr_fr);
			canonialoneside_relation(1, list_nr_fr);
			D.p("abc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void canonialoneside_relation(final int key, List<String[]> list_nellstr_mid) {
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
				num += help_canonical_relation(block);
				block.clear();
			}
			block.add(list_nellstr_mid.get(i));
		}
		num += help_canonical_relation(block);
		D.p("Canonical entity key " + key + " clauses number " + num);
	}

	private static int help_canonical_relation(List<String[]> block) {
		int num = 0;
		for (int i = 0; i < block.size(); i++) {
			String[] b1 = block.get(i);
			for (int j = 0; j < block.size(); j++) {
				String[] b2 = block.get(j);
				try {
					if (b1[0].equals(b2[0]) && b1[1].equals(b2[1]))
						continue;
					StringBuilder sb = new StringBuilder();
					sb.append("$neg$").append(getVariableRelation(b1[0], b1[1])).append(" ").append("$neg$")
							.append(getVariableRelation(b2[0], b2[1]));
					dwclause.write(Main.PAR_WEIGHT_CANONICAL, sb.toString());
					num++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return num;
	}

	static void getShareInstanceClause() {
		try {
			HashSet<String> candidate = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_relationcandidate);
				String[] l;
				while ((l = dr.read()) != null) {
					String n = l[0];
					String m = l[1];
					candidate.add(getVariableRelation(n, m));
				}
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_weight_sharepair_nellrel_fbrel);
				String[] l;
				int num = 0;
				while ((l = dr.read()) != null) {
					String var = getVariableRelation(l[0], l[1]);
					if (candidate.contains(var)) {
						double val = Double.parseDouble(l[2]);
						dwclause.write(val / 10 + "", var);
						num++;
					}
				}
				D.p("number of sharing entity clauses", num);
				dr.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void getNegInstanceClause() {
		try {
			HashSet<String> candidate = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_relationcandidate);
				String[] l;
				while ((l = dr.read()) != null) {
					String n = l[0];
					String m = l[1];
					candidate.add(getVariableRelation(n, m));
				}
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_weight_seednegativepair_nellrel_fbrel);
				String[] l;
				int num = 0;
				while ((l = dr.read()) != null) {
					String var = getVariableRelation(l[0], l[1]);
					if (candidate.contains(var)) {
						double val = -1 * Main.PAR_WEIGHT_SEED_NEG * Math.min(Double.parseDouble(l[2]) / 10, 1);
						dwclause.write(val, var);
						num++;
					}
				}
				D.p("number of sharing entity clauses", num);
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_weight_defaultnegativepair_nellrel_fbrel);
				String[] l;
				int num = 0;
				while ((l = dr.read()) != null) {
					String var = getVariableRelation(l[0], l[1]);
					if (candidate.contains(var)) {
						double weight = Double.parseDouble(l[2]);
						if (weight > Main.PAR_WEIGHT_Default_NEG) {
							dwclause.write(-1, var);
						}
						//double val = -1 * Main.PAR_WEIGHT_Default_NEG * Math.min(Double.parseDouble(l[2]) / 10, 1);
						num++;
					}
				}
				D.p("number of sharing entity clauses", num);
				dr.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void getTypeSatisfyClause() {
		try {
			HashSet<String> candidate = new HashSet<String>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_relationcandidate);
				String[] l;
				while ((l = dr.read()) != null) {
					String n = l[0];
					String m = l[1];
					candidate.add(getVariableRelation(n, m));
				}
			}

			{
				DelimitedReader dr = new DelimitedReader(Main.file_weight_typesatifying);
				String[] l;
				while ((l = dr.read()) != null) {
					String var = getVariableRelation(l[1], l[2]);
					if (candidate.contains(var)) {
						String[] ab = l[0].split(";;");
						int trueval = Integer.parseInt(ab[0]);
						int wholeval = Integer.parseInt(ab[1]);
						double score = trueval * 1.0 / wholeval;
						dwclause.write(score, var);
					}
				}
				dr.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void getRelationTypeJointClause() throws IOException {
		//HashSet<String> candidate = new HashSet<String>();
		HashSet<String> typematchcandidate = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_candidatemapping_nelltype_fbtype);
			String[] l;
			while ((l = dr.read()) != null) {
				typematchcandidate.add(S3_typeclause.getVariableNameType(l[0], l[1]));
			}
		}
		//load arg1 & arg2
		HashMap<String, List<String[]>> map_fbrarg1typesign = getRelationTypeJointClause_load_fbrel_typesign(Main.file_relation_typesign_arg1);
		HashMap<String, List<String[]>> map_fbrarg2typesign = getRelationTypeJointClause_load_fbrel_typesign(Main.file_relation_typesign_arg2);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_relationcandidate);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellr = l[0];
				String nellr_typesign[] = new String[2];
				if (!nellr.contains("_inverse")) {
					nellr_typesign = Main.no.relname2DomainRange.get(nellr);
				} else {
					String temp[] = Main.no.relname2DomainRange.get(nellr.replace("_inverse", ""));
					nellr_typesign[0] = temp[1];
					nellr_typesign[1] = temp[0];
				}
				String fbr = l[1];
				String var_nrfr = getVariableRelation(nellr, fbr);
				List<String[]> fbr_arg_typesign[] = new List[2];
				fbr_arg_typesign[0] = map_fbrarg1typesign.get(fbr);
				fbr_arg_typesign[1] = map_fbrarg2typesign.get(fbr);
				for (int i : new int[] { 0, 1 }) {
					String nt = nellr_typesign[i];
					if (fbr_arg_typesign[i] != null) {
						StringBuilder sb = new StringBuilder();
						sb.append("$neg$").append(var_nrfr);
						for (String[] ft_l : fbr_arg_typesign[i]) {
							String ft = ft_l[1];
							double confidence = Double.parseDouble(ft_l[2]) / fbr_arg_typesign[i].size();
							String var_ntft = S3_typeclause.getVariableNameType(nt, ft);
							if (typematchcandidate.contains(var_ntft)) {
								sb.append(" ").append(var_ntft);
							}
						}
						dwclause.write(1.0, sb.toString());
					}
				}
				//candidate.add(getVariableRelation(nellr, fbr));
			}
		}
	}

	static String[] fbtype_filterout = new String[] { "/book/book_subject", "/people/person", "/location/location",

	};

	static HashMap<String, List<String[]>> getRelationTypeJointClause_load_fbrel_typesign(String file)
			throws IOException {
		HashMap<String, List<String[]>> res = new HashMap<String, List<String[]>>();
		{
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {
				String fbr = l[0];
				if (!res.containsKey(fbr)) {
					res.put(fbr, new ArrayList<String[]>());
				}
				double val = Double.parseDouble(l[2]);
				if (val > 0.7) {
					res.get(fbr).add(l);
				}
			}
			dr.close();
		}
		return res;
	}

	public static void main(String[] args) throws Exception {
		dwclause = new DelimitedWriter(Main.file_relationrelclause);
		getRelationPairClause();
		getCanonical_relation();
		getShareInstanceClause();
		getNegInstanceClause();
		//getTypeSatisfyClause();
		getRelationTypeJointClause();
		dwclause.close();
	}
}
