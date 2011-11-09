package faust.subclass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import multir.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.StringTable;
import javatools.webapi.BingApi;

public class JudeByClass {
	/**
	 *        Dictator
	Emir
	Emperor
	Energy_Minister
	Finance_Minister
	Foreign_Minister
	Justice_Minister
	King
	Mayor
	Premier
	President
	Prime_Minister
	Queen
	Top_Executive
	 * */
	public static void main(String[] args) throws Exception {
		if (args[0].contains("1")) {
			step1_sort(Main.dir + "/visible_cmp", Main.dir + "/visible_cmp.sblinkobj");
		}

		if (args[0].contains("2")) {
			step2_template2delimited(Main.dir + "/visible_cmp.sblinkobj", new String[] {
					"/business/job_title/people_with_this_title|/business/employment_tenure/person",
					"/business/job_title/people_with_this_title|/business/employment_tenure/company" }, Main.dir
					+ "/cmptitle_person_org");
		}
		if (args[0].contains("3")) {
			step3_filterJudeWant(new String[] { "TopExecutive::/m/0dq_5", "Chair::/m/09d6p2" }, Main.dir
					+ "/cmptitle_person_org", Main.dir + "/jude_cmptitle_person_org");
		}
		if (args[0].contains("4")) {
			step1_sort(Main.dir + "/visible_gov", Main.dir + "/visible_gov.sblinkobj");
		}

		if (args[0].contains("5")) {
			step2_template2delimited(
					Main.dir + "/visible_gov.sblinkobj",
					new String[] {
							"/government/government_office_or_title/office_holders|/government/government_position_held/office_holder",
							"/government/government_office_or_title/office_holders|/government/government_position_held/jurisdiction_of_office" },
					Main.dir + "/govtitle_person_org");
		}

		if (args[0].contains("6")) {
			step3_filterJudeWant(new String[] { "Monarch::/m/04syw", "Dictator::/m/02bms", "Mayor::/m/0pqc5",
					"PrimeMinister::/m/060bp", "Premier::/m/0fkx3", "President::/m/060c4", "President::/m/060d2",
					"FinanceMinister::/m/01zq91", "JusticMinister::/m/03k6v6" }, Main.dir + "/govtitle_person_org",
					Main.dir + "/jude_govtitle_person_org");
		}

		if (args[0].contains("7")) {
			String file_pair_in_mid = Main.dir + "/jude_cmptitle_person_org";
			String file_pair_in_mid_name = file_pair_in_mid + "_name";
			String file_matchkey_personorg = file_pair_in_mid + "_matchkey";
			String file_matchsen_personorg = file_pair_in_mid + "_matchsen";
			String file_subsetsen = file_pair_in_mid + ".noids";
			String file_subsetner = file_pair_in_mid + ".ner";
			step3MID2Name(file_pair_in_mid, file_pair_in_mid_name);
			step4PersonOrganizationMatchKey(file_pair_in_mid_name, file_matchkey_personorg);
			step5_heuristicmatch(file_matchkey_personorg, file_matchsen_personorg);
			step6_subset(file_matchsen_personorg, file_subsetsen, file_subsetner);
		}
		if (args[0].contains("8")) {
			String file_pair_in_mid = Main.dir + "/jude_govtitle_person_org";
			String file_pair_in_mid_name = file_pair_in_mid + "_name";
			String file_matchkey_personorg = file_pair_in_mid + "_matchkey";
			String file_matchsen_personorg = file_pair_in_mid + "_matchsen";
			String file_subsetsen = file_pair_in_mid + ".noids";
			String file_subsetner = file_pair_in_mid + ".ner";
			step3MID2Name(file_pair_in_mid, file_pair_in_mid_name);
			step4PersonOrganizationMatchKey(file_pair_in_mid_name, file_matchkey_personorg);
			step5_heuristicmatch(file_matchkey_personorg, file_matchsen_personorg);
			step6_subset(file_matchsen_personorg, file_subsetsen, file_subsetner);
		}
		

	}

	public static void step1_sort(String input, String output) throws Exception {
		Sort.sort(input, output, Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[4].compareTo(o2[4]);
			}

		});
	}

	public static void step2_template2delimited(String input, String[] fields, String output) throws Exception {
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output);
		List<String[]> b;
		while ((b = dr.readBlock(4)) != null) {
			String mainObj = b.get(0)[1];
			String[] fieldsVal = new String[fields.length + 1];
			fieldsVal[0] = mainObj;
			for (String[] l : b) {
				for (int k = 0; k < fields.length; k++) {
					if (l[2].equals(fields[k])) {
						fieldsVal[k + 1] = l[3];
					}
				}
			}
			boolean goodMatch = true;
			for (int k = 0; k < fieldsVal.length; k++) {
				if (fieldsVal[k] == null) {
					goodMatch = false;
					break;
				}
			}
			if (goodMatch) {
				dw.write(fieldsVal);
			}
		}
		dr.close();
		dw.close();
	}

	public static void step3_filterJudeWant(String[] judeTitle2Mid, String input, String output) throws Exception {
		HashMap<String, String> mid2judetitle = new HashMap<String, String>();
		HashCount<String> judetitlecount = new HashCount<String>();
		for (String s : judeTitle2Mid) {
			String[] ab = s.split("::");
			mid2judetitle.put(ab[1], ab[0]);
		}
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			if (mid2judetitle.containsKey(l[0])) {
				String judetitle = mid2judetitle.get(l[0]);
				judetitlecount.add(judetitle);
				dw.write(mid2judetitle.get(l[0]), l[1], l[2]);
			}
		}
		dr.close();
		dw.close();
		judetitlecount.printAll();
	}

	public static void step3MID2Name(String input, String output) throws IOException {
		HashMap<String, Set<String>> mid2names = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2names.put(l[0], new HashSet<String>());
				mid2names.put(l[1], new HashSet<String>());
				mid2names.put(l[2], new HashSet<String>());
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedWriter dwbug = new DelimitedWriter(output + ".debug");
		{
			//get their names by look at Main.file_fbdump_2_len4 
			DelimitedReader dr = new DelimitedReader(Main.file_mid2namesfullset);
			String[] l;
			while ((l = dr.read()) != null) {
				if (mid2names.containsKey(l[0])) {
					mid2names.get(l[0]).add(l[1]);
				}
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			int instanceNum = 1;
			while ((l = dr.read()) != null) {
				Set<String>[] myname = new Set[2];
				boolean success = true;
				for (int i = 0; i < 2; i++) {
					myname[i] = mid2names.get(l[i + 1]);
					if (myname[i] == null || myname[i].size() == 0) {
						success = false;
						break;
					}
				}
				if (success) {
					String t = l[0];
					for (String p : myname[0]) {
						for (String o : myname[1]) {
							dw.write(instanceNum, l[0], l[1], l[2], p, t, o);
							instanceNum++;
						}
					}
				} else {
					dwbug.write(l);
				}
			}
			dr.close();
			dw.close();
			dwbug.close();
		}
	}

	public static void step4PersonOrganizationMatchKey(String input, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output + ".temp");
		String[] l;
		List<String[]> table = new ArrayList<String[]>();
		while ((l = dr.read()) != null) {
			table.add(new String[] { l[2], l[3], l[4], l[6], l[1] });
		}
		StringTable.sortUniq(table);
		for (String[] t : table) {
			dw.write(step4_help_tokey(t[2], t[3]), t[0], t[1], t[2], t[3], t[4]);
		}
		dw.close();
		Sort.sort(output + ".temp", output, Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[0].compareTo(o2[0]);
			}

		});
	}

	public static String step4_help_tokey(String arg1, String arg2) {
		return (arg1 + "$$$" + arg2).replaceAll("\\s", "_").toLowerCase();
	}

	public static void step5_heuristicmatch(String input, String output) throws Exception {

		DelimitedWriter dw = new DelimitedWriter(output);
		MergeReadStr mr = new MergeReadStr(input, Main.file_nyt_matchkeys, 0, 0);
		MergeReadResStr mrrs = null;
		while ((mrrs = mr.read()) != null) {
			ArrayList<String[]> l1list = mrrs.line1_list;
			ArrayList<String[]> l2list = mrrs.line2_list;
			//there should be only one element in l1list, but it doesn't matter too much
			String[] l1 = l1list.get(0);
			String mid1 = l1[1];
			String mid2 = l1[2];
			String name1 = l1[3];
			String name2 = l1[4];
			String title = l1[5];
			String matchKey = l1[0];
			for (String[] l2 : l2list) {
				String senIdStr = l2[1];
				dw.write(senIdStr, mid1, mid2, name1, name2, matchKey, title);
			}
		}
		mr.close();
		dw.close();
	}

	public static void step5_heuristicmatch_alg2(String rel, String file_pair_in_mid_name, String file_sentence,
			String output_heuristicmatchpair) throws IOException {
		HashMap<String, String[]> keyToMatch = new HashMap<String, String[]>();
		DelimitedWriter dw = new DelimitedWriter(output_heuristicmatchpair);
		{
			DelimitedReader dr = new DelimitedReader(file_pair_in_mid_name);
			String[] l;
			while ((l = dr.read()) != null) {
				String tomatch = (l[3] + "\t" + l[4]).toLowerCase();
				keyToMatch.put(tomatch, l);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_sentence + ".ner");
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				int sid = Integer.parseInt(b.get(0)[0]);
				int sectionId = Integer.parseInt(b.get(0)[2]);
				for (int i = 0; i < b.size(); i++) {
					for (int j = i + 1; j < b.size(); j++) {
						String arg1 = b.get(i)[5]; //is it really 4?
						String arg2 = b.get(j)[5];
						boolean getMatch = false;
						String argA = "", argB = "", argAStartToken = "", argAEndToken = "", argBStartToken = "", argBEndToken = "", argAType = "", argBType = "";
						{
							String key = (arg1 + "\t" + arg2).toLowerCase();
							if (keyToMatch.containsKey(key)) {
								argA = arg1;
								argB = arg2;
								argAStartToken = b.get(i)[3];
								argAEndToken = b.get(i)[4];
								argAType = b.get(i)[6];
								argBStartToken = b.get(j)[3];
								argBEndToken = b.get(j)[4];
								argBType = b.get(j)[6];
								getMatch = true;
							}
						}
						{
							String key = (arg2 + "\t" + arg1).toLowerCase();
							if (keyToMatch.containsKey(key)) {
								argA = arg2;
								argB = arg1;
								argAStartToken = b.get(j)[3];
								argAEndToken = b.get(j)[4];
								argAType = b.get(j)[6];
								argBStartToken = b.get(i)[3];
								argBEndToken = b.get(i)[4];
								argBType = b.get(i)[6];
								getMatch = true;
							}
						}
						if (getMatch)
							dw.write(sid, sectionId, rel, argA, argB, argAStartToken, argAEndToken, argBStartToken,
									argBEndToken, argAType, argBType);
					}
				}
			}
			dr.close();
		}
		dw.close();
	}

	public static void step6_subset(String input_matchsentence, String outputnoids, String outputners)
			throws IOException {
		HashSet<Integer> sentenceIdSet = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(input_matchsentence);
			String[] l;
			while ((l = dr.read()) != null) {
				sentenceIdSet.add(Integer.parseInt(l[0]));
			}
			dr.close();
		}
		{
			DelimitedWriter dw = new DelimitedWriter(outputnoids);
			DelimitedReader dr = new DelimitedReader(Main.file_nyt_sentence + ".noids");
			String[] l;
			while ((l = dr.read()) != null) {
				int sid = Integer.parseInt(l[0]);
				if (sentenceIdSet.contains(sid)) {
					dw.write(l[0], l[3]);
				}
			}
			dr.close();
			dw.close();
		}
		{
			DelimitedWriter dw = new DelimitedWriter(outputners);
			DelimitedReader dr = new DelimitedReader(Main.file_nyt_sentence + ".ner");
			String[] l;
			while ((l = dr.read()) != null) {
				int sid = Integer.parseInt(l[0]);
				if (sentenceIdSet.contains(sid)) {
					dw.write(l);
				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void step6_jude(String input_matchsentence, String input_midpair, String judematch)
			throws IOException {
		HashMap<String, Set<String>> perorg2title = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(input_midpair);
			String[] l;
			while ((l = dr.read()) != null) {
				StringTable.mapKey2SetAdd(perorg2title, l[1] + "\t" + l[3], l[5]);
			}
		}
		//filter the sentence set
		{
			List<String[]> table = new ArrayList<String[]>();
			DelimitedReader dr = new DelimitedReader(input_matchsentence);
			DelimitedWriter dw = new DelimitedWriter(judematch);
			String[] l;
			while ((l = dr.read()) != null) {
				String perorg = l[1] + "\t" + l[2];
				Set<String> title = perorg2title.get(perorg);
				StringBuilder sb = new StringBuilder();
				for (String ti : title)
					sb.append(ti + ";;");
				table.add(new String[] { l[0], l[1], l[2], l[3], l[4], sb.toString() });
			}
			dr.close();
			StringTable.sortByColumn(table, new int[] { 0 }, new boolean[] { true }, new boolean[] { true });
			for (String[] t : table) {
				dw.write(t);
			}
			dw.close();
		}

	}

}
