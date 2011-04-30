package freebase.jointmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.Hash2Value;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S2_variable_nelltype_fbtype_count {

	static HashMap<String, String> mid2ftype = new HashMap<String, String>();

	private static void loadMid2Type(String file) {
		//		try {
		//			DelimitedReader dr = new DelimitedReader(file);
		//			String[] line;
		//			while ((line = dr.read()) != null) {
		//
		//				// filter the type
		//				String type0 = line[1];
		//				if (type0.startsWith("/m/") || type0.startsWith("/common/") || type0.startsWith("/book/book_subject")) {
		//					continue;
		//				}
		//
		//				if (!mid2ftype.containsKey(line[0])) {
		//					mid2ftype.put(line[0], new ArrayList<String>());
		//				}
		//				mid2ftype.get(line[0]).add(line[1]);
		//			}
		//			dr.close();
		//
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
	}

	private static void loadMid2NotableType() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2ftype.put(l[0], l[2]);
			}
			dr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getWeight_typesharingentity(String input_enid_mid_wid_argname_otherarg_relation_label,
			String output_weight_type_shareentity) throws Exception {
		DelimitedWriter dwtemp = new DelimitedWriter(output_weight_type_shareentity + ".temp");
		DelimitedWriter dw = new DelimitedWriter(output_weight_type_shareentity);
		//loadMid2Type(Main.fout_freebase_type_sortMid_subset);

		DelimitedReader dr = new DelimitedReader(input_enid_mid_wid_argname_otherarg_relation_label);
		String[] line;

		HashSet<String> intresting = new HashSet<String>();

		while ((line = dr.read()) != null) {
			String mid = line[1];
			String fbtype = mid2ftype.get(mid);

			String argname = line[3];
			String label = line[6];
			if (label.equals("-1"))
				continue;

			HashSet<String> nellclass = Main.no.entity2class.get(argname);

			if (fbtype == null) {
				D.p("fb type is null:", mid, line[0]);
				continue;
			}
			if (nellclass == null) {
				D.p("nell class type is null:", argname);
				continue;
			}

			for (String nt : nellclass) {
				//					if (nt.equals("sportsTeam") && fbtype.equals("/business/employer")) {
				//						D.p("sportsteam");
				//					}
				intresting.add(nt + "\t" + fbtype + "\t" + argname + "\t" + mid);
			}

		}
		// output
		HashSet<String> appearedNellType = new HashSet<String>();

		HashMap<String, HashSet<String>> nelltype_object_count = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> variable_count = new HashMap<String, HashSet<String>>();

		ArrayList<String> interestingList = new ArrayList<String>();
		interestingList.addAll(intresting);
		Collections.sort(interestingList);

		for (String a : interestingList) {
			String[] x = a.split("\t");
			dwtemp.write(x);
			appearedNellType.add(x[0]);

			// add to nelltype_object_count
			String nelltype = x[0];
			if (!nelltype_object_count.containsKey(nelltype)) {
				nelltype_object_count.put(nelltype, new HashSet<String>());
			}
			nelltype_object_count.get(nelltype).add(x[2]);

			// add to variable
			String typematchingvariable = x[0] + "\t" + x[1];
			if (!variable_count.containsKey(typematchingvariable)) {
				variable_count.put(typematchingvariable, new HashSet<String>());
			}
			variable_count.get(typematchingvariable).add(x[2]);
		}

		dr.close();

		List<String[]> dbtemp = new ArrayList<String[]>();
		for (Entry<String, HashSet<String>> e : variable_count.entrySet()) {
			String[] ab = e.getKey().split("\t");
			// dwcount.write(ab[0],ab[1],e.getValue().size());
			dbtemp.add(new String[] { ab[0], ab[1], e.getValue().size() + "" });
		}
		Collections.sort(dbtemp, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				int v1 = o1[0].compareTo(o2[0]);
				if (v1 != 0) {
					return v1;
				} else {
					int a = Integer.parseInt(o1[2]);
					int b = Integer.parseInt(o2[2]);
					return b - a;
				}
			}
		});

		for (String[] a : dbtemp) {
			String nelltype = a[0];
			dw.write(nelltype, a[1], a[2], nelltype_object_count.get(nelltype).size());
		}
		dw.close();
		dwtemp.close();
		// check if some nell type is missing in this candidate step
		{
			D.p("Missing types in the candidate step");
			for (String cn : Main.no.classNames) {
				if (!appearedNellType.contains(cn)) {
					D.p(cn);
				}
			}
		}
	}

	public static void getCandidate_nelltype_fbtype(String input_weight_type_shareentity,
			String output_candidatemapping_nelltype_fbtype, double candidate_num_nelltype_fbtype) {

		try {
			DelimitedWriter dw0 = new DelimitedWriter(output_candidatemapping_nelltype_fbtype);
			DelimitedReader dr = new DelimitedReader(input_weight_type_shareentity);
			List<String[]> raw = dr.readAll();
			String last = "";
			int lastcount = 0;
			for (String[] l : raw) {
				String nelltype = l[0];
				String fbtype = l[1];
				//double score = Integer.parseInt(l[2]) * 1.0 / Integer.parseInt(l[3]);
				String key = nelltype;
				if (key.equals(last) && lastcount < candidate_num_nelltype_fbtype) {
					dw0.write(nelltype, fbtype);
					lastcount++;
				}
				if (!key.equals(last)) {
					last = key;
					dw0.write(nelltype, fbtype);
					lastcount = 1;
				}
			}
			dr.close();
			dw0.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getWeight_negative(String input_enid_mid_wid_argname_otherarg_relation_label,
			String output_weight_type_negative) {
		//loadMid2Type(Main.fout_freebase_type_sortMid_subset);
		try {
			String[] allnellclass = Main.no.classNames;
			List<String[]> neg_evidence = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(input_enid_mid_wid_argname_otherarg_relation_label);
			DelimitedWriter dw = new DelimitedWriter(output_weight_type_negative);
			String[] l;
			while ((l = dr.read()) != null) {
				String label = l[6];
				if (label.equals("-1"))
					continue;
				String mid = l[1];
				String fbtype = mid2ftype.get(mid);
				String argname = l[3];
				HashSet<String> nellclass = Main.no.entity2class.get(argname);

				/**Notice many argname will be corresponding to more than one class, for example, 
				 * Boston will be both a stateOrProvince & city*/

				if (fbtype == null) {
					D.p("fb type is null:", mid, l[0]);
					continue;
				}
				if (nellclass == null) {
					D.p("nell class type is null:", argname);
					continue;
				}

				for (String nt : allnellclass) {
					if (!nellclass.contains(nt) && nt != null) {
						neg_evidence.add(new String[] { nt, fbtype, argname, mid });
					}
				}

			}

			dr.close();
			StringTable.sortByColumn(neg_evidence, new int[] { 2 });
			//normalize: every argument has at all weight 1 !!!
			Hash2Value<String> hv = new Hash2Value<String>();
			List<List<String[]>> blocks = StringTable.toblock(neg_evidence, 2);
			for (List<String[]> b : blocks) {
				double w = 2.0 / b.size();
				for (String[] line : b) {
					//nt, ft, argname
					hv.add(line[0] + "\t" + line[1], w);
				}
			}
			//			List<String[]>neg_evidence_squeeze = StringTable.squeeze(neg_evidence, new int[]{0,1});
			//			StringTable.sortByColumn(neg_evidence_squeeze, new int[]{1,2});
			Iterator<Entry<String, Double>> it = hv.iterator();
			while (it.hasNext()) {
				Entry<String, Double> e = it.next();
				String[] ab = e.getKey().split("\t");
				double v = e.getValue();
				dw.write(v, ab[0], ab[1]);
			}
			//			for(String []a:neg_evidence_squeeze){
			//				dw.write(a);
			//			}
			//D.p("Negative evidence ",neg_evidence.size());
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		/**These functions run almost instantly*/
		//loadMid2Type(Main.file_freebase_type_sortMid_subset);
		loadMid2NotableType();
		getWeight_typesharingentity(Main.file_enid_mid_wid_argname_otherarg_relation_label,
				Main.file_weight_type_shareentity);
		getCandidate_nelltype_fbtype(Main.file_weight_type_shareentity, Main.file_candidatemapping_nelltype_fbtype,
				Main.candidate_num_nelltype_fbtype);

		/**the negative evidence, for example, if Abraham Lincoln is an positive evidence for "people", it should also be an negative 
		 * evidence for "actor" */
		getWeight_negative(Main.file_enid_mid_wid_argname_otherarg_relation_label, Main.file_weight_type_negative);
	}

}
