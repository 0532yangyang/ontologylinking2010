package faust.subclass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import multir.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeRead;
import javatools.filehandlers.MergeReadRes;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.StringTable;
import javatools.webapi.BingApi;

public class JudeThreeArgument {
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
	static class ParaThree {
		String visible_subset;
		String output;
		String arg1rel; //e.g. "/business/job_title/people_with_this_title|/business/employment_tenure/person"
		String arg2rel; //e.g. "/business/job_title/people_with_this_title|/business/employment_tenure/company"
		String[] arg0filter;//e.g.  "CEO", "Chairman"; if arg0filter == null; there is no filter
		String[] triggerwords; //e.g. "executive", "chairman", "president"
	}

	public static void run(ParaThree pt) throws Exception {
		step1_sort(pt.visible_subset, pt.visible_subset + ".sblinkobj");
		step2_template2delimited(pt.visible_subset + ".sblinkobj", new String[] { pt.arg1rel, pt.arg2rel }, pt.output
				+ ".3mids");
		step3MID2Name(pt.output + ".3mids", pt.output + ".3names");
		step4SplitByKeywords(pt.arg0filter, pt.output + ".3names", pt.output + ".tuple");
		step4PersonOrganizationMatchKey(pt.output + ".tuple", pt.output + ".gen");
		step5_heuristicmatch_alg2(pt.output + ".tuple", pt.output + ".mtch");
		step6_subset(pt.output + ".mtch", pt.output);
		step6_givexiao(pt.output);
	}

	public static void run_afterxiao(ParaThree pt) throws Exception {
		takeXiaoParsing(pt.output);
		labelTriggerWords(pt.output, pt.triggerwords);
	}

	public static void main(String[] args) throws Exception {
		{
			//head of company
			ParaThree pt = new ParaThree();
			pt.visible_subset = Main.dir + "/headOfCompany/visible_cmp";
			pt.output = Main.dir + "/headOfCompany/headOfCompany";
			pt.arg1rel = "/business/job_title/people_with_this_title|/business/employment_tenure/person";
			pt.arg2rel = "/business/job_title/people_with_this_title|/business/employment_tenure/company";
			pt.arg0filter = new String[] { "CEO", "Chairman", "President", "Principal" };
			pt.triggerwords = new String[] { "executive", "chairman", "president", "principal" };
			if (args[0].contains("-1")) {
				//run(pt);
			}
			if (args[0].contains("-2")) {
				//run_afterxiao(pt);
			}
		}
		{
			//head of company
			ParaThree pt = new ParaThree();
			pt.visible_subset = Main.dir + "/headOfNationState/visible_gov";
			pt.output = Main.dir + "/headOfNationState/headOfNationState";
			pt.arg1rel = "/government/government_office_or_title/office_holders|/government/government_position_held/office_holder";
			pt.arg2rel = "/government/government_office_or_title/office_holders|/government/government_position_held/jurisdiction_of_office";
			pt.arg0filter = new String[] { "President", "Senator", "Prime Minister", "Dictator", "Premier", "King",
					"Queen", "Monarch", "Emir" };
			pt.triggerwords = new String[] { "President", "Senator", "minister", "Dictator", "Premier", "King",
					"Queen", "Monarch", "Emir", "leader" };
			if (args[0].contains("-1")) {
				run(pt);
			}
			if (args[0].contains("-2")) {
				run_afterxiao(pt);
			}
		}

	}

	public static void main2(String[] args) throws Exception {
		if (args[0].contains("1")) {
			step1_sort(Main.dir + "/visible_cmp", Main.dir + "/visible_cmp.sblinkobj");
			step1_sort(Main.dir + "/visible_gov", Main.dir + "/visible_gov.sblinkobj");
		}

		if (args[0].contains("2")) {
			step2_template2delimited(Main.dir + "/visible_cmp.sblinkobj", new String[] {
					"/business/job_title/people_with_this_title|/business/employment_tenure/person",
					"/business/job_title/people_with_this_title|/business/employment_tenure/company" }, Main.dir
					+ "/cmptitle_person_org");
			step2_template2delimited(
					Main.dir + "/visible_gov.sblinkobj",
					new String[] {
							"/government/government_office_or_title/office_holders|/government/government_position_held/office_holder",
							"/government/government_office_or_title/office_holders|/government/government_position_held/jurisdiction_of_office" },
					Main.dir + "/govtitle_person_org");
		}
		if (args[0].contains("3")) {
			step3MID2Name(Main.dir + "/cmptitle_person_org", Main.dir + "/cmptitle_person_org_name");
			step3MID2Name(Main.dir + "/govtitle_person_org", Main.dir + "/govtitle_person_org_name");
		}

		if (args[0].contains("4")) {
			step4SplitByKeywords(new String[] { "CEO", "Chairman", "President", "Principal" }, Main.dir
					+ "/cmptitle_person_org_name", Main.dir + "/HeadOfCompany.tuple");
			step4SplitByKeywords(new String[] { "President", "Senator", "Prime Minister", "Dictator", "Premier",
					"King", "Queen", "Monarch", "Emir" }, Main.dir + "/govtitle_person_org_name", Main.dir
					+ "/HeadOfNationState.tuple");
			step4SplitByKeywords(new String[] { "Mayor" }, Main.dir + "/govtitle_person_org_name", Main.dir
					+ "/HeadOfCityTownOrVillage.tuple");
			step4SplitByKeywordsNoPrimeMinister(new String[] { "Minister" }, Main.dir + "/govtitle_person_org_name",
					Main.dir + "/Minister.tuple");
		}
		if (args[0].contains("-5")) {
			//			String in = Main.dir + "/cmptitle_person_org_name";
			//			String out = Main.dir + "/HeadOfCompany";
			step456(Main.dir + "/HeadOfCompany");
			step456(Main.dir + "/HeadOfNationState");
			step456(Main.dir + "/Minister");
			step456(Main.dir + "/HeadOfCityTownOrVillage");
		}
		if (args[0].contains("-6")) {
			step6_givexiao(Main.dir + "/HeadOfCompany");
			step6_givexiao(Main.dir + "/HeadOfNationState");
			step6_givexiao(Main.dir + "/Minister");
			step6_givexiao(Main.dir + "/HeadOfCityTownOrVillage");
		}
		if (args[0].contains("-7")) {
			takeXiaoParsing(Main.dir + "/HeadOfCompany");
			takeXiaoParsing(Main.dir + "/HeadOfNationState");
			takeXiaoParsing(Main.dir + "/Minister");
			takeXiaoParsing(Main.dir + "/HeadOfCityTownOrVillage");
		}
		if (args[0].equals("-8")) {
			labelTriggerWords(Main.dir + "/HeadOfCompany", new String[] { "executive", "chairman", "president",
					"principal" });
			labelTriggerWords(Main.dir + "/HeadOfNationState", new String[] { "President", "Senator", "minister",
					"Dictator", "Premier", "King", "Queen", "Monarch", "Emir", "leader" });
			labelTriggerWords(Main.dir + "/Minister", new String[] { "Minister", "Chancellor" });
			labelTriggerWords(Main.dir + "/HeadOfCityTownOrVillage", new String[] { "Mayor" });
		}
		if (args[0].equals("-j")) {
			//			step6_jude(Main.dir + "/HeadOfCompany.xiaoner.trigger", Main.dir + "/jude/HeadOfCompany");
			//			step6_jude(Main.dir + "/HeadOfNationState.xiaoner.trigger", Main.dir + "/jude/HeadOfNationState");
			//			step6_jude(Main.dir + "/Minister.xiaoner.trigger", Main.dir + "/jude/Minister");
			//			step6_jude(Main.dir + "/HeadOfCityTownOrVillage.xiaoner.trigger", Main.dir
			//					+ "/jude/HeadOfCityTownOrVillage");
			step6_jude_split(Main.dir + "/jude", new String[] { "HeadOfCompany", "HeadOfNationState", "Minister",
					"HeadOfCityTownOrVillage" }, Main.dir + "/split_");

		}
	}

	public static void step456(String out) throws Exception {
		step4PersonOrganizationMatchKey(out + ".tuple", out + ".gen");
		//step5_heuristicmatch(out + ".gen", out + ".mtch");
		step5_heuristicmatch_alg2(out + ".tuple", out + ".mtch");
		step6_subset(out + ".mtch", out);

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
					StringBuilder sb = new StringBuilder();
					for (String t : myname[0])
						sb.append(t + ";;");
					for (String p : myname[1]) {
						for (String o : myname[2]) {
							dw.write(instanceNum, l[0], l[1], l[2], p, o, sb.toString());
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

	public static void step4SplitByKeywords(String[] keytitle2subclass, String input, String output) throws IOException {
		HashSet<String> keytitles = new HashSet<String>();
		HashCount<String> subclassCount = new HashCount<String>();
		for (String s : keytitle2subclass) {
			keytitles.add(s);
		}
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			if (keytitle2subclass.length == 0) {
				dw.write(l[0], l[1], l[2], l[3], l[4], l[5], l[6], "NA");
			}
			for (String t : keytitle2subclass) {
				//keytitle2subclass.length == 0  no filtering at all
				if (l[6].toLowerCase().contains(t.toLowerCase())) {
					dw.write(l[0], l[1], l[2], l[3], l[4], l[5], l[6], t);
					subclassCount.add(t);
					break;
				}
			}

		}
		dr.close();
		dw.close();
		subclassCount.printAll();
	}

	public static void step4SplitByKeywordsNoPrimeMinister(String[] keytitle2subclass, String input, String output)
			throws IOException {
		HashSet<String> keytitles = new HashSet<String>();
		HashCount<String> subclassCount = new HashCount<String>();
		for (String s : keytitle2subclass) {
			keytitles.add(s);
		}
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			for (String t : keytitle2subclass) {
				if (l[6].toLowerCase().contains(t.toLowerCase()) && !l[6].toLowerCase().contains("prime minister")) {
					dw.write(l[0], l[1], l[2], l[3], l[4], l[5], l[6], t);
					subclassCount.add(t);
					break;
				}
			}

		}
		dr.close();
		dw.close();
		subclassCount.printAll();
	}

	public static void step4PersonOrganizationMatchKey(String input, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output + ".temp");
		String[] l;
		List<String[]> table = new ArrayList<String[]>();
		while ((l = dr.read()) != null) {
			table.add(new String[] { l[2], l[3], l[4], l[5], l[6], l[7] });
		}
		StringTable.sortUniq(table);
		for (String[] t : table) {
			dw.write(step4_help_tokey(t[2], t[3]), t[0], t[1], t[2], t[3], t[4], t[5]);
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
			String fbtitle = l1[5];
			String ourtitle = l1[6];
			String matchKey = l1[0];
			//	dw.write(sid, l[2], l[3], l[4], l[5], step4_help_tokey(l[4], l[5]), l[6], l[7],
			//		argAStartToken, argAEndToken, argBStartToken, argBEndToken);
			for (String[] l2 : l2list) {
				String senIdStr = l2[1];
				String arg1Start = l2[2], arg1end = l2[3], arg2start = l2[4], arg2end = l2[5];
				dw.write(senIdStr, mid1, mid2, name1, name2, matchKey, fbtitle, ourtitle, arg1Start, arg1end,
						arg2start, arg2end);
			}
		}
		mr.close();
		dw.close();
	}

	public static void step5_heuristicmatch_alg2(String input, String output) throws IOException {
		HashMap<String, String[]> keyToMatch = new HashMap<String, String[]>();
		DelimitedWriter dw = new DelimitedWriter(output);
		{
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				String tomatch = (l[4] + "\t" + l[5]).toLowerCase();
				keyToMatch.put(tomatch, l);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_nyt_sentence + ".ner");
			List<String[]> b;
			String[] l = null;
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
								l = keyToMatch.get(key);
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
								l = keyToMatch.get(key);
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
						//dw.write(senIdStr, mid1, mid2, name1, name2, matchKey, fbtitle, arg1Start, arg1end, arg2start, arg2end);
						if (getMatch && l != null) {
							dw.write(sid, l[2], l[3], l[4], l[5], step4_help_tokey(l[4], l[5]), l[6], l[7],
									argAStartToken, argAEndToken, argBStartToken, argBEndToken);
							//							dw.write(sid, sectionId, argA, argB, argAStartToken, argAEndToken, argBStartToken,
							//									argBEndToken, argAType, argBType);
						}

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
			DelimitedWriter dw = new DelimitedWriter(output + ".noids");
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
			DelimitedWriter dw = new DelimitedWriter(output + ".tokens");
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
		{
			DelimitedWriter dw = new DelimitedWriter(output + ".ner");
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
		//		{
		//			DelimitedWriter dw = new DelimitedWriter(output + ".deps");
		//			DelimitedReader dr = new DelimitedReader(Main.file_nyt_sentence + ".deps");
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				int sid = Integer.parseInt(l[0]);
		//				if (sentenceIdSet.contains(sid)) {
		//					dw.write(l);
		//				}
		//			}
		//			dr.close();
		//			dw.close();
		//		}
	}

	public static void step6_givexiao(String in) throws Exception {
		Sort.sort(in + ".mtch", in + ".mtchsbsid", Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] arg0, String[] arg1) {
				// TODO Auto-generated method stub
				return Integer.parseInt(arg0[0]) - Integer.parseInt(arg1[0]);
			}
		});
		DelimitedWriter dw = new DelimitedWriter(in + ".xiao");
		MergeRead mr = new MergeRead(in + ".mtchsbsid", in + ".tokens", 0, 0);
		MergeReadRes mrs;
		while ((mrs = mr.read()) != null) {
			List<String[]> list1 = mrs.line1_list;
			List<String[]> list2 = mrs.line2_list;
			for (String[] l1 : list1)
				for (String[] l2 : list2)
					dw.write(l1[0], l1[3], l1[4], l1[8], l1[9], l1[10], l1[11], l2[1], l1[6], l1[7]);
		}
		dw.close();
		mr.close();
	}

	public static void takeXiaoParsing(String file) throws IOException {
		List<String[]> segments = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(file + ".segment2");
			DelimitedReader dr2 = new DelimitedReader(file + ".predictions2");
			String[] l;
			String[] l2 = null;
			int state = 0; //1: in the middle of the entity; 0: not in the entity
			while ((l = dr.read()) != null) {
				if (l.length < 2)
					continue;
				if (l[2].equals("B-E")) {
					l2 = dr2.read();
					segments.add(new String[] { l[0], l2[2] });
					state = 1;
				} else if (l[2].equals("I-E")) {
					segments.add(new String[] { l[0], l2[2] });
				} else {
					l2 = null;
					segments.add(new String[] { l[0], "O" });
					state = 0;
				}
			}
			dr.close();
			dr2.close();

		}
		{
			DelimitedReader dr = new DelimitedReader(file + ".xiao");
			DelimitedWriter dw = new DelimitedWriter(file + ".xiaoner");
			String[] l;
			int k = 0;
			while ((l = dr.read()) != null) {
				if (l[0].equals("4890180")) {
					D.p("");
				}
				int arg1start = Integer.parseInt(l[3]);
				int arg1end = Integer.parseInt(l[4]);
				int arg2start = Integer.parseInt(l[5]);
				int arg2end = Integer.parseInt(l[6]);
				String[] t = l[7].split(" ");
				List<String> tags = new ArrayList<String>();
				for (int i = 0; i < t.length; i++) {
					String[] ab = segments.get(k++);
					tags.add(ab[1]);
				}
				//get the ner tag of the argument
				String ner1 = takeXiaoParsing_help(tags, arg1start, arg1end);
				String ner2 = takeXiaoParsing_help(tags, arg2start, arg2end);
				dw.write(l[0], l[1], l[2], l[3], l[4], l[5], l[6], ner1, ner2, l[7], l[8], l[9]);
			}
			dw.close();
		}

	}

	public static String takeXiaoParsing_help(List<String> tags, int argstart, int argend) {
		String ner1 = tags.get(argstart);
		boolean ner1consistent = true;
		for (int j = argstart; j < argend; j++) {
			if (!tags.get(j).equals(ner1)) {
				ner1consistent = false;
			}
		}
		if (argstart - 1 > 0 && tags.get(argstart - 1).equals(ner1)) {
			ner1consistent = false;
		}
		if (argend < tags.size() && tags.get(argend).equals(ner1)) {
			ner1consistent = false;
		}
		if (!ner1consistent)
			return "O";
		return ner1;
	}

	public static void labelTriggerWords(String file, String[] keywords) throws IOException {
		HashSet<String> setkw = new HashSet<String>();
		for (String k : keywords)
			setkw.add(k.toLowerCase());
		DelimitedReader dr = new DelimitedReader(file + ".xiaoner");
		DelimitedWriter dw = new DelimitedWriter(file + ".xiaoner.trigger");
		String[] l;
		int numSentenceHasKeyWord = 0, numSentenceAll = 0;
		while ((l = dr.read()) != null) {
			String[] tokens = l[9].split(" ");
			StringBuilder sbpos = new StringBuilder();
			boolean keywordmatch = false;
			for (int ti = 0; ti < tokens.length; ti++) {
				String t = tokens[ti];
				if (setkw.contains(t.toLowerCase())) {
					sbpos.append(ti + ";");
					keywordmatch = true;
				}
			}
			if (keywordmatch) {
				numSentenceHasKeyWord++;
			} else {
				sbpos.append("NA;");
			}
			numSentenceAll++;
			dw.write(l[0], l[1], l[2], l[3], l[4], l[5], l[6], sbpos.toString(), l[7], l[8], l[9], l[10], l[11]);
		}
		dw.close();
		D.p(file, numSentenceHasKeyWord, numSentenceAll);
	}

	public static void step6_jude(String input, String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			dw.write(l);
		}
		dr.close();
		dw.close();
	}

	public static void step6_jude_split(String dir, String[] in, String output) throws IOException {
		List<String[]> all = new ArrayList<String[]>();
		HashMap<String, Integer> outtitle_file = new HashMap<String, Integer>();
		for (String f : in) {
			DelimitedReader dr = new DelimitedReader(dir + "/" + f);
			String[] l;
			while ((l = dr.read()) != null) {
				all.add(l);
				if (!outtitle_file.containsKey(l[12])) {
					outtitle_file.put(l[12], outtitle_file.size());
				}
			}
			dr.close();
		}
		DelimitedWriter[] dw = new DelimitedWriter[outtitle_file.size()];
		for (Entry<String, Integer> e : outtitle_file.entrySet()) {
			dw[e.getValue()] = new DelimitedWriter(output + e.getKey());
		}
		for (String[] l : all) {
			int k = outtitle_file.get(l[12]);
			dw[k].write(l);
		}
		for (int i = 0; i < dw.length; i++) {
			if (dw[i] != null)
				dw[i].close();
		}
	}

}
