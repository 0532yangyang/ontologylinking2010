package faust.subclass;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class GetCTO {
	public static void main(String[] args) throws IOException {
		String file_raw = Main.dir + "/cto_raw";
		String file_pair_in_mid = Main.dir + "/cto_midpair";
		String file_pair_in_mid_name = Main.dir + "/cto_midnamepair";
		String file_heuristicmatchpairswiki = Main.dir + "/cto_heursticmatchpairs_wiki";
		String file_heuristicmatchpairsnyt = Main.dir + "/cto_heursticmatchpairs_nyt";
		String subdir_wiki = Main.dir + "/wikisentence";
		String subdir_nyt = Main.dir + "/nytsentence";
		//		step1GrabVisible("/people/person/employment_history|/business/employment_tenure/title", "/m/02211by", file_raw);
		//		step2GrabABPair(file_raw, "/people/person/employment_history|/business/employment_tenure/company",
		//				file_pair_in_mid);
		//step3AssignName4AB(file_pair_in_mid, file_pair_in_mid_name);
		//step4HeuristicMatch("cto", file_pair_in_mid_name, Main.file_wiki_sentence, file_heuristicmatchpairswiki);
		//step4HeuristicMatch("cto", file_pair_in_mid_name, Main.file_nyt_sentence, file_heuristicmatchpairsnyt);
		step5SubsetSentenceTokenDepPos(file_heuristicmatchpairsnyt, Main.file_nyt_sentence, subdir_nyt);
	}

	/**For CTO example,
	 * fbrelname is /people/person/employment_history|/business/employment_tenure/title
	 * hiddenargname is /m/02211by
	 * Then I get a lot of PERSON TEMPMID
	 * Then I try to find PERSON /people/person/employment_history|/business/employment_tenure/company COMPANYMID TEMPMID
	 * @throws IOException 
	 * */
	public static void step1GrabVisible(String fbrelname, String hiddenargname, String output) throws IOException {
		String file_visible = Main.dir_freebase + "/visible";
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(file_visible);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[2].equals(fbrelname) && l[3].equals(hiddenargname)) {
				dw.write(l);
			}
		}
		dr.close();
		dw.close();
	}

	public static void step2GrabABPair(String file_raw, String fbrelname, String file_pair_in_mid) throws IOException {
		HashSet<String> ACpairs = new HashSet<String>();
		DelimitedWriter dw = new DelimitedWriter(file_pair_in_mid);
		{
			DelimitedReader dr = new DelimitedReader(file_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				ACpairs.add(l[1] + "\t" + l[4]);
			}
		}
		String file_visible = Main.dir_freebase + "/visible";
		{
			DelimitedReader dr = new DelimitedReader(file_visible);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[2].equals(fbrelname)) {
					String AC = l[1] + "\t" + l[4];
					if (ACpairs.contains(AC)) {
						dw.write(l[1], l[3]);
					}
				}
			}
		}
		dw.close();
	}

	public static void step3AssignName4AB(String file_pair_in_mid, String file_pair_in_mid_name) throws IOException {
		HashMap<String, Set<String>> mid2names = new HashMap<String, Set<String>>();
		DelimitedWriter dw = new DelimitedWriter(file_pair_in_mid_name);
		{
			//get mids from file_pair_in_mid, put them into hashmap mid2names;
			DelimitedReader dr = new DelimitedReader(file_pair_in_mid);
			String[] l;
			while ((l = dr.read()) != null) {
				if (!mid2names.containsKey(l[0])) {
					mid2names.put(l[0], new HashSet<String>());
				}
				if (!mid2names.containsKey(l[1])) {
					mid2names.put(l[1], new HashSet<String>());
				}
			}
			dr.close();
		}
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
			//check file_pair_in_mid again, extend the mids to names and write them in file_pair_in_mid_name
			DelimitedReader dr = new DelimitedReader(file_pair_in_mid);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid1 = l[0];
				String mid2 = l[1];
				Set<String> namesOfArg1 = mid2names.get(mid1);
				Set<String> namesOfArg2 = mid2names.get(mid2);
				if (namesOfArg1.size() == 0 || namesOfArg2.size() == 0) {
					System.err.println(l);
				}
				for (String n1 : namesOfArg1) {
					for (String n2 : namesOfArg2) {
						dw.write(mid1, mid2, n1, n2);
					}
				}
			}
		}
		dw.close();
	}

	public static void step4HeuristicMatch(String rel, String file_pair_in_mid_name, String file_sentence,
			String output_heuristicmatchpair) throws IOException {
		HashMap<String, String[]> keyToMatch = new HashMap<String, String[]>();
		DelimitedWriter dw = new DelimitedWriter(output_heuristicmatchpair);
		{
			DelimitedReader dr = new DelimitedReader(file_pair_in_mid_name);
			String[] l;
			while ((l = dr.read()) != null) {
				String tomatch = (l[2] + "\t" + l[3]).toLowerCase();
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
									argBEndToken, argAType, argBType, "e");
					}
				}
			}
			dr.close();
		}
		dw.close();
	}

	public static void step5SubsetSentenceTokenDepPos(String file_heuristicmatchpair, String file_sentence,
			String outputdir) throws IOException {
		(new File(outputdir)).mkdir();
		HashSet<Integer> sentenceIdSet = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(file_heuristicmatchpair);
			String[] l;
			while ((l = dr.read()) != null) {
				sentenceIdSet.add(Integer.parseInt(l[0]));
			}
		}
		//get tokens
		step5SubsetSentenceTokenDepPos_help1(sentenceIdSet, "tokens", outputdir, file_sentence);
		step5SubsetSentenceTokenDepPos_help1(sentenceIdSet, "pos", outputdir, file_sentence);
		step5SubsetSentenceTokenDepPos_help1(sentenceIdSet, "ner", outputdir, file_sentence);
		step5SubsetSentenceTokenDepPos_help1(sentenceIdSet, "deps", outputdir, file_sentence);
	}

	private static void step5SubsetSentenceTokenDepPos_help1(HashSet<Integer> sentenceIdSet, String affix,
			String outputdir, String file_sentence) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(outputdir + "/" + affix);
		DelimitedReader dr = new DelimitedReader(file_sentence + "." + affix);
		String[] l;
		while ((l = dr.read()) != null) {
			int sentenceId = Integer.parseInt(l[0]);
			if (sentenceIdSet.contains(sentenceId)) {
				dw.write(l);
			}
		}
		dw.close();
	}
}
