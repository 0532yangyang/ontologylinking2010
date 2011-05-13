package freebase.jointmatch3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.ml.weightedmaxsat.WeightedClauses;
import javatools.mydb.StringTable;

public class S8_inference {

	public static void mergeClauses() throws Exception {
		List<String[]> all1 = (new DelimitedReader(Main.file_typeclause)).readAll();
		List<String[]> all2 = (new DelimitedReader(Main.file_relationrelclause)).readAll();

		List<String[]> towrite = new ArrayList<String[]>();
		for (String[] a : all1) {
			towrite.add(a);
			//dw.write(a);
		}
		for (String[] b : all2) {
			towrite.add(b);
			//dw.write(b);
		}
		DelimitedWriter dw = new DelimitedWriter(Main.file_jointlclause);
		Collections.shuffle(towrite);
		for (String[] w : towrite) {
			dw.write(w);
		}
		dw.close();
		//		Sort.sort(Main.file_jointlclause + ".tmp", Main.file_jointlclause, Main.dir, new Comparator<String[]>() {
		//
		//			@Override
		//			public int compare(String[] arg0, String[] arg1) {
		//				// TODO Auto-generated method stub
		//				return arg0[1].compareTo(arg1[1]);
		//			}
		//
		//		});

	}

	public static void votepred(int COPYK) throws IOException {
		//		if (!(new File(Main.dir_votepred)).exists()) {
		//			(new File(Main.dir_votepred)).mkdir();
		//		}
		//DelimitedWriter dw = new DelimitedWriter(Main.file_votepred);
		if(!(new File(Main.dir_predict)).exists()){
			(new File(Main.dir_predict)).mkdir();
		}
		List<String[]> clauses = new ArrayList<String[]>();
		{
			List<String[]> all1 = (new DelimitedReader(Main.file_typeclause)).readAll();
			List<String[]> all2 = (new DelimitedReader(Main.file_relationrelclause)).readAll();
			for (String[] a : all1) {
				clauses.add(a);
			}
			for (String[] b : all2) {
				clauses.add(b);
			}
		}
		HashCount<String> hc = new HashCount<String>();
		for (int i = 0; i < COPYK; i++) {
			D.p("iter", i);
			Collections.shuffle(clauses);
			WeightedClauses wc = new WeightedClauses(clauses);
			wc.update();
			List<String> vars = wc.printFinalResult();
			for (String v : vars) {
				hc.add(v);
			}
		}
		hc.printAll(Main.file_predict_vote);
		//dw.close();

	}

	private static void parseVotePred() throws IOException {
		//file_votepred is already sorted according to the weight
		String file_predict_vote = Main.file_predict_vote;
		String file_predict_vote_type = file_predict_vote + ".type";
		String file_predict_vote_entity = file_predict_vote + ".entity";
		String file_predict_vote_relation = file_predict_vote + ".relation";
		String file_predict_vote_newontology = Main.file_predict_vote_newontology;
		List<String[]> all = (new DelimitedReader(file_predict_vote)).readAll();
		List<String[]> table = new ArrayList<String[]>();
		DelimitedWriter dw_type = new DelimitedWriter(file_predict_vote_type);
		DelimitedWriter dw_entity = new DelimitedWriter(file_predict_vote_entity);
		DelimitedWriter dw_rel = new DelimitedWriter(file_predict_vote_relation);
		HashSet<String> exist = new HashSet<String>();
		for (String[] a : all) {
			String[] xyz = a[0].split("::");
			table.add(new String[] { xyz[0], xyz[1], xyz[2], a[1] });
		}
		StringTable.sortByColumn(table, new int[] { 0, 1, 3 }, new boolean[] { false, false, true });
		//List<List<String[]>>blocks = StringTable.toblock(table, 1);
		for (String[] t : table) {
			if (t[0].equals("VT")) {
				dw_type.write(t);
			}
			if (t[0].equals("VR")) {
				dw_rel.write(t);
			}
			if (t[0].equals("VE")) {
				dw_entity.write(t);
			}
		}
		dw_type.close();
		dw_entity.close();
		dw_rel.close();
		theNewOntology(file_predict_vote_type, file_predict_vote_relation, file_predict_vote_newontology);
	}

	public static void theNewOntology(String file_predict_vote_type, String file_predict_vote_relation,
			String file_predict_vote_newontology) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_predict_vote_newontology);
		List<String[]> pred_type = (new DelimitedReader(file_predict_vote_type)).readAll();
		List<String[]> pred_relation = (new DelimitedReader(file_predict_vote_relation)).readAll();
		StringTable.sortByColumn(pred_type, new int[] { 1, 3 }, new boolean[] { false, true });
		StringTable.sortByColumn(pred_relation, new int[] { 1, 3 }, new boolean[] { false, true });
		HashMap<String, List<String>> map_nelltype_fbtypes = new HashMap<String, List<String>>();
		{
			List<List<String[]>> blocks = StringTable.toblock(pred_type, 1);
			for (List<String[]> b : blocks) {
				int maxcnt = Integer.parseInt(b.get(0)[3]);
				int threshold = (int) (0.5 * maxcnt);
				map_nelltype_fbtypes.put(b.get(0)[1], new ArrayList<String>());
				for (String[] b0 : b) {
					int x = Integer.parseInt(b0[3]);
					if (x > threshold) {
						map_nelltype_fbtypes.get(b0[1]).add(b0[2]);
					}
				}
			}
		}
		List<String[]> matchingresult = new ArrayList<String[]>();
		{
			List<List<String[]>> blocks = StringTable.toblock(pred_relation, 1);
			for (List<String[]> b : blocks) {
				int maxcnt = Integer.parseInt(b.get(0)[3]);
				int threshold = (int) (0.5 * maxcnt);
				for (String[] b0 : b) {
					int x = Integer.parseInt(b0[3]);
					if (x > threshold) {
						String nellrelation = b0[1];
						String fbrelation = b0[2];
						List<String> arg1types, arg2types;
						boolean arg1arg2same = false;
						if (!nellrelation.contains("_inverse")) {
							String[] ab = Main.no.relname2DomainRange.get(nellrelation);
							if (ab[0].equals(ab[1])) {
								arg1arg2same = true;
							}
							arg1types = map_nelltype_fbtypes.get(ab[0]);
							arg2types = map_nelltype_fbtypes.get(ab[1]);
						} else {
							String[] ab = Main.no.relname2DomainRange.get(nellrelation.replace("_inverse", ""));
							if (ab[0].equals(ab[1])) {
								arg1arg2same = true;
							}
							arg1types = map_nelltype_fbtypes.get(ab[1]);
							arg2types = map_nelltype_fbtypes.get(ab[0]);

						}
						if (arg1types != null && arg2types != null) {
							if (arg1arg2same) {
								for (String t1 : arg1types) {
									matchingresult.add(new String[] { nellrelation, fbrelation, t1, t1 });
								}
							} else {
								for (String t1 : arg1types) {
									for (String t2 : arg2types) {
										matchingresult.add(new String[] { nellrelation, fbrelation, t1, t2 });
									}
								}
							}
						}

					}
				}
			}
		}
		for (String[] l : matchingresult) {
			dw.write(l);
		}
		dw.close();
	}

	public static void predrelonly() {

		WeightedClauses wc = new WeightedClauses(Main.file_relationrelclause);
		wc.update();
		wc.printFinalResult(Main.file_predict_relonly);

	}

	public static void predtypeonly() {

		WeightedClauses wc = new WeightedClauses(Main.file_typeclause);
		wc.update();
		wc.printFinalResult(Main.file_predict_typeonly);

	}

	public static void predjoint() {

		WeightedClauses wc = new WeightedClauses(Main.file_jointlclause);
		wc.update();
		wc.printFinalResult(Main.file_predict_joint);

	}

	private static void sampleSql2isntance4DebugPurpose() throws IOException {

		HashMap<Integer, String> wid2title = new HashMap<Integer, String>();
		{
			//load wid 2 title
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);

			String[] l;
			while ((l = dr.read()) != null) {
				wid2title.put(Integer.parseInt(l[2]), l[3]);
			}
		}
		HashCount<String> hc = new HashCount<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance);
			DelimitedWriter dw = new DelimitedWriter(Main.file_sql2instance + ".sample10");
			String[] l;
			while ((l = dr.read()) != null) {
				String key = l[2] + "\t" + l[3];
				int a = hc.see(key);
				if (a < 10) {
					int wid1 = Integer.parseInt(l[0]);
					int wid2 = Integer.parseInt(l[1]);
					String title1 = wid2title.get(wid1);
					String title2 = wid2title.get(wid2);
					if (title1 != null && title2 != null) {
						dw.write(wid1, wid2, title1, title2, l[2], l[3]);
						hc.add(key);
					}
				}
			}
			dw.close();
			dr.close();
		}
	}

	private static void splitIntoEntityTypeRelationPred() throws IOException {
		{
			List<String[]> all = (new DelimitedReader(Main.file_predict_joint)).readAll();
			StringTable.sortByColumn(all, new int[] { 0 });
			DelimitedWriter dw_type = new DelimitedWriter(Main.file_predict_joint + ".type");
			DelimitedWriter dw_entity = new DelimitedWriter(Main.file_predict_joint + ".entity");
			DelimitedWriter dw_rel = new DelimitedWriter(Main.file_predict_joint + ".relation");
			for (String[] a : all) {
				String[] xyz = a[0].split("::");
				if (xyz[0].equals("VE")) {
					dw_entity.write(xyz[1], xyz[2]);
				}
				if (xyz[0].equals("VT")) {
					dw_type.write(xyz[1], xyz[2]);
				}
				if (xyz[0].equals("VR")) {
					dw_rel.write(xyz[1], xyz[2]);
				}
			}
			dw_type.close();
			dw_entity.close();
			dw_rel.close();

		}
	}

	private static void showDebugInfomation() throws IOException {
		HashMap<String, List<String[]>> data = new HashMap<String, List<String[]>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance + ".sample10");
			String[] l;
			while ((l = dr.read()) != null) {
				String key = l[4] + "\t" + l[5];
				if (!data.containsKey(key)) {
					data.put(key, new ArrayList<String[]>());
				}
				data.get(key).add(l);
			}
		}
		{
			List<String[]> all = (new DelimitedReader(Main.file_predict_joint)).readAll();
			DelimitedWriter dw = new DelimitedWriter(Main.file_predict_joint + ".debug");
			for (String[] a : all) {
				if (a[0].contains("VR::")) {
					String[] xyz = a[0].split("::");
					String nell = xyz[1];
					String fb = xyz[2];
					String key = nell + "\t" + fb;
					List<String[]> examples = data.get(key);
					dw.write(a);
					for (String[] e : examples) {
						dw.write("     ", e[0], e[1], e[2], e[3]);
					}
				}
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws Exception {
		votepred(20);
		parseVotePred();
		//showDebugInfomation();
	}
}
