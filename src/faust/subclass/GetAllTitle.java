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
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.StringTable;
import javatools.webapi.BingApi;

public class GetAllTitle {

	public static void main(String[] args) throws Exception {
		String file_raw = Main.dir + "/raw";
		String file_pair_in_mid = Main.dir + "/midpair";
		String file_pair_in_mid_name = Main.dir + "/midnamepair";
		String file_matchkey_persontitle = Main.dir + "/matchkey_persontitle";
		String file_matchkey_personorg = Main.dir + "/matchkey_personorg";
		String file_nyt_matchkeys = "/projects/pardosa/s5/clzhang/dumpsentences/nyt/nerpair.sbettpair";
		String file_matchsentence_persontitle = Main.dir + "/matchsentence_persontitle";
		String file_matchsentence_personorg = Main.dir + "/matchsentence_personorg";
		String file_matchsentence_personorg_jude = Main.dir + "/matchsentence_personorg.jude";
		String file_sentencesubset_personorg = Main.dir + "/sentencesubset_personorg";
		step1GrabVisible(file_raw);
		step2GetPersonTitleOrgInMID(file_raw, file_pair_in_mid);
		step3MID2Name(file_pair_in_mid, file_pair_in_mid_name);
		step4PersonTitleMatchKey(file_pair_in_mid_name, file_matchkey_persontitle);
		step4PersonOrganizationMatchKey(file_pair_in_mid_name, file_matchkey_personorg);
		step5_heuristicmatch(file_matchkey_persontitle, file_matchsentence_persontitle);
		step5_heuristicmatch(file_matchkey_personorg, file_matchsentence_personorg);
		step6_subset(file_matchsentence_personorg, file_sentencesubset_personorg);
		//step6_jude(file_matchsentence_personorg, file_pair_in_mid_name, file_matchsentence_personorg_jude);
	}

	/**just grab all /people/person/employment_history|***
	 * j1 /m/0157m	/people/person/employment_history|/business/employment_tenure/company	/m/09c7w0	/m/0481d68
	 * j1 /m/0157m	/people/person/employment_history|/business/employment_tenure/title	/m/060c4	/m/0481d68
	 * And then sort by the fourth column
	 * @throws IOException 
	 * */
	public static void step1GrabVisible(String output) throws IOException {
		String file_visible = Main.dir_freebase + "/visible";
		DelimitedWriter dw = new DelimitedWriter(output + ".notsorted");
		DelimitedReader dr = new DelimitedReader(file_visible);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[2].startsWith("/people/person/employment_history")) {
				dw.write(l);
			}
		}
		dr.close();
		dw.close();
		Sort.sort(output + ".notsorted", output, Main.dir, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[4].compareTo(o2[4]);
			}

		});

	}

	public static void step2GetPersonTitleOrgInMID(String sortedbyhidden, String output) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(sortedbyhidden);
		List<String[]> b;
		while ((b = dr.readBlock(4)) != null) {
			String person = b.get(0)[1];
			String company = null;
			String title = null;
			for (String[] a : b) {
				if (a[2].equals("/people/person/employment_history|/business/employment_tenure/company")) {
					company = a[3];
				}
				if (a[2].equals("/people/person/employment_history|/business/employment_tenure/title")) {
					title = a[3];
				}
			}
			if (company != null && title != null)
				dw.write(person, title, company);
		}
		dw.close();
		dr.close();
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
				Set<String>[] myname = new Set[3];
				boolean success = true;
				for (int i = 0; i < 3; i++) {
					myname[i] = mid2names.get(l[i]);
					if (myname[i] == null || myname[i].size() == 0) {
						success = false;
						break;
					}
				}
				if (success) {
					for (String p : myname[0]) {
						for (String t : myname[1]) {
							for (String o : myname[2]) {
								dw.write(instanceNum, l[0], l[1], l[2], p, t, o);
								instanceNum++;
							}
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

	public static void step4PersonTitleMatchKey(String input, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output + ".temp");
		String[] l;
		List<String[]> table = new ArrayList<String[]>();
		while ((l = dr.read()) != null) {
			table.add(new String[] { l[1], l[2], l[4], l[5] });
		}
		StringTable.sortUniq(table);
		for (String[] t : table) {
			dw.write(step4_help_tokey(t[2], t[3]), t[0], t[1], t[2], t[3]);
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

	public static void step4PersonOrganizationMatchKey(String input, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output + ".temp");
		String[] l;
		List<String[]> table = new ArrayList<String[]>();
		while ((l = dr.read()) != null) {
			table.add(new String[] { l[1], l[3], l[4], l[6] });
		}
		StringTable.sortUniq(table);
		for (String[] t : table) {
			dw.write(step4_help_tokey(t[2], t[3]), t[0], t[1], t[2], t[3]);
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
			String matchKey = l1[0];
			for (String[] l2 : l2list) {
				String senIdStr = l2[1];
				dw.write(senIdStr, mid1, mid2, name1, name2, matchKey);
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

	public static void step6_subset(String input_matchsentence, String output) throws IOException {
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
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(Main.file_nyt_sentence + ".tokens");
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
