package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S61_typerelationjoint {

	public static void getTypeSignatureOfCandidateRelations_step1() throws IOException {
		//load types of wid entites
		HashMap<Integer, String> map_wid2type = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[1]);
				map_wid2type.put(wid, l[2]);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance);
			DelimitedWriter dw0 = new DelimitedWriter(Main.file_relation_typesign_debug);
			String[] l;
			HashSet<Integer> missing = new HashSet<Integer>();
			HashCount<String> hc1 = new HashCount<String>();
			HashCount<String> hc2 = new HashCount<String>();
			while ((l = dr.read()) != null) {
				int wid1 = Integer.parseInt(l[0]);
				int wid2 = Integer.parseInt(l[1]);
				String fbr = l[3];
				//				List<String> arg1types = map_wid2type.get(wid1);
				//				List<String> arg2types = map_wid2type.get(wid2);
				String arg1type = map_wid2type.get(wid1);
				String arg2type = map_wid2type.get(wid2);
				{
					/**there are at all 616 wid missing type information*/
					if (arg1type == null) {
						missing.add(wid1);
					}
					if (arg2type == null) {
						missing.add(wid2);
					}
				}
				if (arg1type != null) {

					hc1.add(fbr + "\t" + arg1type);

					hc1.add(fbr + "\tAny");
				}
				if (arg2type != null) {

					hc2.add(fbr + "\t" + arg2type);

					hc2.add(fbr + "\tAny");
				}
			}
			hc1.printAll(Main.file_relation_typesign_arg1 + ".temp");
			hc2.printAll(Main.file_relation_typesign_arg2 + ".temp");
			D.p("");
			D.p("Missing", missing.size());
			dr.close();
			dw0.close();
		}
	}

	static void getTypeSignatureOfCandidateRelations_step2(String file) throws IOException {
		List<String[]> all = (new DelimitedReader(file + ".temp")).readAll();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String[] l : all) {
			map.put(l[0], Integer.parseInt(l[1]));
		}
		List<String[]> towrite = new ArrayList<String[]>();
		Iterator<Entry<String, Integer>> it = map.entrySet().iterator();
		DelimitedWriter dw = new DelimitedWriter(file);
		while ((it.hasNext())) {
			Entry<String, Integer> e = it.next();
			String key = e.getKey();
			if (!key.contains("\tAny")) {
				String[] ab = key.split("\\t");
				String fbr = ab[0];
				String argtype = ab[1];
				String look = fbr + "\tAny";
				int total = map.get(look);
				int value = e.getValue();
				double ratio = value * 1.0 / total;
				if (ratio > 0.1) {
					//dw.write(fbr, argtype, ratio);
					towrite.add(new String[] { fbr, argtype, ratio + "" });
				}
			}
		}
		StringTable.sortByColumn(towrite, new int[] { 0, 2, 1 }, new boolean[] { false, true, false });
		for (String[] a : towrite) {
			dw.write(a);
		}
		dw.close();
	}

	public static void main(String[] args) throws IOException {

		/**Get type signature of the candidate relations;
		 * for example, /organization/organization/companies_acquired|/business/acquisition/company_acquired| --> company & company*/
		{
			getTypeSignatureOfCandidateRelations_step1();
			getTypeSignatureOfCandidateRelations_step2(Main.file_relation_typesign_arg1);
			getTypeSignatureOfCandidateRelations_step2(Main.file_relation_typesign_arg2);
		}

	}
}
