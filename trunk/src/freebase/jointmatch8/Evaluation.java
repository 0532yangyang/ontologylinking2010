package freebase.jointmatch8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class Evaluation {
	public static void manual_entity_v2() throws IOException {
		String fbresult = Main.dir + "/fbsearch.raw1";
		String output = Main.dir + "/mycreateontology/manual_entity_mid.v2";
		String mylabelfile = Main.dir + "/mycreateontology/manual_entity_mid.v1";
		HashMap<String, String> name2mid = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(mylabelfile);

			String[] l;
			while ((l = dr.read()) != null) {
				name2mid.put(l[0], l[1]);
			}
		}
		HashMap<String, List<String>> argument2types = new HashMap<String, List<String>>();
		//DelimitedWriter dw = new DelimitedWriter(file_ontologyentity2fbentity_byfbsearch);
		{
			for (NellRelation nr : Main.no.nellRelationList) {

				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					if (!argument2types.containsKey(a[0])) {
						argument2types.put(a[0], new ArrayList<String>());
					}
					argument2types.get(a[0]).add(nr.domain + "_arg1" + "_" + a[1] + "_" + nr.relation_name);
					if (!argument2types.containsKey(a[1])) {
						argument2types.put(a[1], new ArrayList<String>());
					}
					argument2types.get(a[1]).add(nr.range + "_arg2" + "_" + a[0] + "_" + nr.relation_name);
				}
				for (String[] a : nr.known_negatives) {
					if (!argument2types.containsKey(a[0])) {
						argument2types.put(a[0], new ArrayList<String>());
					}
					argument2types.get(a[0]).add(nr.domain + "_neg_arg1" + "_" + a[1] + "_" + nr.relation_name);
					if (!argument2types.containsKey(a[1])) {
						argument2types.put(a[1], new ArrayList<String>());
					}
					argument2types.get(a[1]).add(nr.range + "_neg_arg2" + "_" + a[0] + "_" + nr.relation_name);
				}
			}
		}
		HashMap<String, String> notabletype = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				notabletype.put(l[0], l[2]);
			}
		}
		HashMap<String, String[]> map_enurl2others = Tool.get_enurl2others();
		HashMap<String, String[]> map_mid2others = Tool.get_mid2others();
		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(fbresult);
			String[] l;
			String lastname = "";
			while ((l = dr.read()) != null) {
				try {
					String name = l[0];
					String mylabeledmid = name2mid.get(name);
					String idstr = l[1];
					if (idstr.startsWith("/en/")) {
						idstr = map_enurl2others.get(idstr.replace("/en/", ""))[1];
					} else if (idstr.startsWith("/m/")) {
					} else {
						continue;
					}
					if (idstr != null) {
						String mid = idstr;
						String[] s = map_mid2others.get(mid);
						List<String> arginfo = argument2types.get(name);
						String arginfo2str = "";
						if (arginfo != null) {
							arginfo2str = arginfo.toString();
						}
						boolean yes = mylabeledmid.equals(mid);
						String type = notabletype.get(mid);
						String wid = "";
						String namealias = "missing";
						if (s != null) {
							wid = s[2];
							namealias = s[3];
						}
						if (type == null) {
							type = "null";
						}
						if (!name.equals(lastname)) {
							dw.write(" ");
							lastname = name;
						}
						dw.write(name, yes, type, arginfo2str, mid, wid, namealias);
					}
				} catch (Exception e) {
					System.err.println(l);
				}
			}
			dw.close();
			dr.close();
		}
	}

	public static void manual_entity_v3() throws IOException {
		String fbresult = Main.dir + "/fbsearch.raw2";
		String output = Main.dir + "/mycreateontology/manual_entity_mid.v2";
		String mylabelfile = Main.dir + "/mycreateontology/manual_entity_mid.v1";
		HashMap<String, String> name2mid = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(mylabelfile);

			String[] l;
			while ((l = dr.read()) != null) {
				name2mid.put(l[0], l[1]);
			}
		}
		HashMap<String, List<String>> argument2types = new HashMap<String, List<String>>();
		//DelimitedWriter dw = new DelimitedWriter(file_ontologyentity2fbentity_byfbsearch);
		{
			for (NellRelation nr : Main.no.nellRelationList) {

				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					if (!argument2types.containsKey(a[0])) {
						argument2types.put(a[0], new ArrayList<String>());
					}
					argument2types.get(a[0]).add(nr.domain + "_arg1" + "_" + a[1] + "_" + nr.relation_name);
					if (!argument2types.containsKey(a[1])) {
						argument2types.put(a[1], new ArrayList<String>());
					}
					argument2types.get(a[1]).add(nr.range + "_arg2" + "_" + a[0] + "_" + nr.relation_name);
				}
				for (String[] a : nr.known_negatives) {
					if (!argument2types.containsKey(a[0])) {
						argument2types.put(a[0], new ArrayList<String>());
					}
					argument2types.get(a[0]).add(nr.domain + "_neg_arg1" + "_" + a[1] + "_" + nr.relation_name);
					if (!argument2types.containsKey(a[1])) {
						argument2types.put(a[1], new ArrayList<String>());
					}
					argument2types.get(a[1]).add(nr.range + "_neg_arg2" + "_" + a[0] + "_" + nr.relation_name);
				}
			}
		}

		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(fbresult);
			String[] l;
			String lastname = "";
			while ((l = dr.read()) != null) {
				String name = l[0];
				if (!name.equals(lastname)) {
					dw.write("");
					lastname = name;
				}
				List<String> info = argument2types.get(name);
				String infostr = "";
				if (info != null) {
					infostr = info.toString();
				}
				dw.write(l[0], infostr, l[3], l[1], l[2], l[4]);
			}
			dw.close();
			dr.close();
		}
	}

	public static void fbsearch_entity_topk_acc() throws IOException {
		/**Conclusion by running this code
		 * precision	0	0.6812933025404158
		precision	1	0.03810623556581986
		precision	2	0.009237875288683603
		precision	3	0.008083140877598153
		precision	4	0.003464203233256351
		precision	5	0.0023094688221709007
		precision	6	0.0
		precision	7	0.0011547344110854503
		precision	8	0.0
		precision	9	0.0
		recall	0	0.8885542168674698
		recall	1	0.04969879518072289
		recall	2	0.012048192771084338
		recall	3	0.010542168674698794
		recall	4	0.004518072289156626
		recall	5	0.0030120481927710845
		recall	6	0.0
		recall	7	0.0015060240963855422
		recall	8	0.0
		recall	9	0.0*/
		HashMap<String, String> answer = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_goldentitymapping);
			String[] l;
			while ((l = dr.read()) != null) {
				answer.put(l[0], l[1]);
			}
		}

		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_eval_fbsearchresult);
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
			List<String[]> b;
			int[] correct_at_k_count = new int[10];
			int divideby = 0;
			while ((b = dr.readBlock(0)) != null) {
				String name = b.get(0)[0];
				String mid = answer.get(name);
				if (mid == null) {
					//no correct answer
					dw.write("no answer:", name);
					mid = "";
				}
				for (int k = 0; k < 10 && k < b.size(); k++) {
					String pred = b.get(k)[1];
					if (mid.equals(pred)) {
						correct_at_k_count[k]++;
						break;
					}
				}
				divideby++;
			}
			for (int i = 0; i < 10; i++) {
				dw.write("precision", i, correct_at_k_count[i] * 1.0 / divideby);
			}
			for (int i = 0; i < 10; i++) {
				dw.write("recall", i, correct_at_k_count[i] * 1.0 / answer.size());
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		manual_entity_v3();
		//fbsearch_entity_topk_acc();
	}
}
