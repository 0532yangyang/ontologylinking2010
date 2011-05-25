package freebase.jointmatch6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import percept.util.delimited.DelimitedWriter;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S5 {
	/**Score string alignment given relation matching
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		String file_raw = Main.file_fbsearch2;
		String file_gold = Main.dir + "/mycreateontology/manual_entity_mid";
		String file_afterrelmatch_typeconstrain = Main.file_afterrelmatch_typeconstrain;
		String file_wikilink_subset = Main.file_wklinksub;
		String file_wksensubset = Main.file_wksensubset;
		String file_weight_fbrank = Main.dir + "/weight_str_fbrank";
		String file_weight_typeconstrain = Main.dir + "/weight_str_typeconstrain";
		String file_weight_shareword = Main.dir + "/weight_str_shareword";
		String file_weight_appearinlink = Main.dir + "/weight_str_appearinlink";
		String file_weight_appearintext = Main.dir + "/weight_str_appearintext";
		String file_inference_nellstr = Main.dir + "/inference_str";
		String file_newnellstr2mid = Main.dir + "/nellstr2mid.new";
		String file_difference = Main.dir + "/nellstr2mid.difference";
		String file_eval_alignment = Main.dir + "/eval_alignment";
		
		a1_fbrank(file_raw, file_weight_fbrank);
		a2_typeconstrain(file_raw, file_afterrelmatch_typeconstrain, file_weight_typeconstrain);
		a3_shareword(file_raw, file_weight_shareword);
		a4_otherarg_appearinlink(file_raw, file_wikilink_subset, file_weight_appearinlink);
		a5_otherarg_appearintext(file_raw, file_wksensubset, file_weight_appearintext);

		b1_inference(file_raw, new String[] { file_weight_fbrank, file_weight_typeconstrain, file_weight_shareword,
				file_weight_appearinlink, file_weight_appearintext }, new NellstrMidScoreIndex[] {
				NellstrMidScoreIndex.FBRANK, NellstrMidScoreIndex.TYPECONSTRIAN, NellstrMidScoreIndex.SHAREWORD,
				NellstrMidScoreIndex.WIKILINK, NellstrMidScoreIndex.WIKITEXT }, file_inference_nellstr,
				file_newnellstr2mid);
		b2_setGood(file_raw, file_inference_nellstr, file_gold, file_difference);
		string_alignment_accuracy(file_gold, file_newnellstr2mid, file_eval_alignment);
	}

	public static void a1_fbrank(String file_raw, String file_weight_fbrank) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_fbrank);
		DelimitedReader dr = new DelimitedReader(file_raw);
		HashSet<String> appear = new HashSet<String>();
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			int score = 0;
			for (String[] l : b) {
				String mid = l[1];
				if (!appear.contains(argname + "\t" + mid)) {
					dw.write(argname, mid, score);
					appear.add(argname + "\t" + mid);
					score++;
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void a2_typeconstrain(String file_raw, String file_afterrelmatch_typeconstrain,
			String file_weight_typeconstrain) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_typeconstrain);
		HashMap<String, HashMap<String, Double>> nelltype_fbtypedist = new HashMap<String, HashMap<String, Double>>();
		{
			DelimitedReader dr = new DelimitedReader(file_afterrelmatch_typeconstrain);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				String nelltype = b.get(0)[0];
				nelltype_fbtypedist.put(nelltype, new HashMap<String, Double>());
				List<List<String[]>> s = StringTable.toblock(b, 1);
				int sum = 0;
				for (List<String[]> s0 : s) {
					int size = s0.size();
					sum += size;
				}
				for (List<String[]> s0 : s) {
					String fbtype = s0.get(0)[1];
					int size = s0.size();
					nelltype_fbtypedist.get(nelltype).put(fbtype, size * 1.0 / sum);
				}
			}
			dr.close();
		}
		HashMap<String, String> arg2nelltype = new HashMap<String, String>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					arg2nelltype.put(s[0], nr.domain);
					arg2nelltype.put(s[1], nr.range);
				}
				for (String[] s : nr.known_negatives) {
					arg2nelltype.put(s[0], nr.domain);
					arg2nelltype.put(s[1], nr.range);
				}
			}
		}
		DelimitedReader dr = new DelimitedReader(file_raw);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			if (argname.equals("Green Bay")) {
				//	D.p("Green Bay");
			}
			String nelltype = arg2nelltype.get(argname);
			HashMap<String, Double> fbtype_dist = nelltype_fbtypedist.get(nelltype);

			for (String[] l : b) {
				String mid = l[1];
				String midfbtype = l[3];
				if (fbtype_dist == null) {
					dw.write(argname, mid, "1");//error
				} else {
					//double score = fbtype_dist.containsKey(midfbtype) ? fbtype_dist.get(midfbtype) : 0;
					double score = fbtype_dist.containsKey(midfbtype) ? 1 : 0;
					dw.write(argname, mid, score);
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void a3_shareword(String file_raw, String file_weight_shareword) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_shareword);
		DelimitedReader dr = new DelimitedReader(file_raw);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			for (String[] l : b) {
				String mid = l[1];
				String names = l[4];
				String[] split = names.split(" ");
				double highestscore = 0;
				for (String n : split) {
					List<String> t1 = StringUtil.tokenize(n.toLowerCase(), new char[] { '_', ' ' });
					List<String> t2 = StringUtil.tokenize(argname.toLowerCase(), new char[] { '_', ' ' });
					int shareword = StringUtil.numOfShareWords(n.toLowerCase(), argname.toLowerCase(), new char[] {
							'_', ' ' });
					double score = shareword * 1.0 / Math.max(t1.size(), t2.size());
					if (score > highestscore) {
						highestscore = score;
					}
				}
				dw.write(argname, mid, highestscore);
			}
		}
		dr.close();
		dw.close();
	}

	public static void a4_otherarg_appearinlink(String file_raw, String file_wikilink_subset,
			String file_weight_otherarginlink) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_otherarginlink);
		HashMap<String, Set<String>> arg2otherArg = new HashMap<String, Set<String>>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					StringTable.mapKey2SetAdd(arg2otherArg, s[0], s[1]);
					StringTable.mapKey2SetAdd(arg2otherArg, s[1], s[0]);
				}
			}
		}
		List<String[]> allraw = (new DelimitedReader(file_raw)).readAll();
		StringTable.sortByIntColumn(allraw, new int[] { 2 });
		//List<String[]> alllink = (new DelimitedReader(file_wikilink_subset)).readAll();
		{
			DelimitedReader dr = new DelimitedReader(file_wikilink_subset);
			List<String[]> b = dr.readBlock(0);
			for (String[] l : allraw) {
				int wid = Integer.parseInt(l[2]);
				String argname = l[0];
				String mid = l[1];
				Set<String> otherargs = arg2otherArg.get(argname);
				int sum = 0;

				while (b != null && Integer.parseInt(b.get(0)[0]) < wid) {
					b = dr.readBlock(0);
				}
				if (b != null && otherargs != null && Integer.parseInt(b.get(0)[0]) == wid) {
					for (String[] s : b) {
						String link = s[1].replace("_", " ");
						for (String a : otherargs) {
							if (a.equals(link)) {
								sum++;
							}
						}
					}
				}
				if (sum > 0)
					sum = 1;
				dw.write(argname, mid, sum + "");
			}
		}
		dw.close();
	}

	public static void a5_otherarg_appearintext(String file_raw, String file_wksensubset,
			String file_weight_appearintext) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_appearintext);
		HashMap<String, Set<String>> arg2otherArg = new HashMap<String, Set<String>>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					StringTable.mapKey2SetAdd(arg2otherArg, s[0], s[1]);
					StringTable.mapKey2SetAdd(arg2otherArg, s[1], s[0]);
				}
			}
		}
		List<String[]> allraw = (new DelimitedReader(file_raw)).readAll();
		StringTable.sortByIntColumn(allraw, new int[] { 2 });
		//List<String[]> alllink = (new DelimitedReader(file_wikilink_subset)).readAll();
		{
			DelimitedReader dr = new DelimitedReader(file_wksensubset);
			List<String[]> b = dr.readBlock(1);
			for (String[] l : allraw) {
				int wid = Integer.parseInt(l[2]);
				String argname = l[0];
				String mid = l[1];
				Set<String> otherargs = arg2otherArg.get(argname);
				int sum = 0;

				while (b != null && Integer.parseInt(b.get(0)[1]) < wid) {
					b = dr.readBlock(1);
				}
				if (b != null && otherargs != null && Integer.parseInt(b.get(0)[1]) == wid) {
					for (String[] s : b) {
						String text = s[3];
						for (String a : otherargs) {
							if (text.contains(a)) {
								sum++;
							}
						}
					}
				}
				if (sum > 0)
					sum = 1;
				dw.write(argname, mid, sum + "");
			}
		}
		dw.close();
	}

	public static void b1_inference(String file_raw, String[] weightfiles, NellstrMidScoreIndex[] weighttype,//weight files
			String output_inference,//detailed inference result
			String output_newnellstr2mid) throws IOException {
		HashMap<String, NellstrMidPred> map_strmid2pred = new HashMap<String, NellstrMidPred>();
		{
			DelimitedReader dr = new DelimitedReader(file_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellstr = l[0];
				String mid = l[1];
				NellstrMidPred nmp = new NellstrMidPred(nellstr, mid, l[5]);
				map_strmid2pred.put(nellstr + "\t" + mid, nmp);
			}
			dr.close();
		}
		{
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.FBRANK, -1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.TYPECONSTRIAN, 1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.SHAREWORD, 1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.WIKILINK, 1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.WIKITEXT, 1.0);
		}
		for (int i = 0; i < weightfiles.length; i++) {
			String file = weightfiles[i];
			NellstrMidScoreIndex si = weighttype[i];
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {
				NellstrMidPred p = map_strmid2pred.get(l[0] + "\t" + l[1]);
				p.setScore(si, Double.parseDouble(l[2]));
			}
			dr.close();
		}
		List<String[]> towrite = new ArrayList<String[]>();
		{
			/**Inference*/

			for (Entry<String, NellstrMidPred> e : map_strmid2pred.entrySet()) {
				NellstrMidPred p = e.getValue();
				StringBuilder sb = new StringBuilder();
				double sum = 0;
				for (Entry<NellstrMidScoreIndex, Double> ee : p.scores.entrySet()) {
					sum += NellstrMidPred.weightForScore.get(ee.getKey()) * ee.getValue();
					sb.append(ee.getKey() + "=" + ee.getValue() + " ");
				}
				towrite.add(new String[] { p.nellstr, p.mid, p.enurl, sum + "", sb.toString() });
			}
			StringTable.sortByColumn(towrite, new int[] { 0, 3 }, new boolean[] { false, true });
			StringTable.delimitedWrite(towrite, output_inference);
		}
		{
			HashSet<String> result = new HashSet<String>();
			List<List<String[]>> blocks = StringTable.toblock(towrite, 0);
			for (List<String[]> b : blocks) {
				result.add(b.get(0)[0] + "\t" + b.get(0)[1]);
			}
			DelimitedWriter dw = new DelimitedWriter(output_newnellstr2mid);
			DelimitedReader dr = new DelimitedReader(file_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				if (result.contains(l[0] + "\t" + l[1])) {
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		}
	}

	public static void b2_setGood(String file_raw, String file_inference_nellstr, String file_gold, String output)
			throws IOException {
		HashMap<String, String> nellstr2mid = new HashMap<String, String>();
		HashMap<String, String> nellstr2goldmid = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_gold);
			String[] l;
			while ((l = dr.read()) != null) {
				nellstr2goldmid.put(l[0], l[1]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_inference_nellstr);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				nellstr2mid.put(b.get(0)[0], b.get(0)[1]);
			}
		}
		/**check the difference to no1 fbsearch*/
		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(file_raw);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				String nellstr = b.get(0)[0];
				String no1mid = b.get(0)[1];
				String mypredmid = nellstr2mid.get(nellstr);
				String goldmid = nellstr2goldmid.containsKey(nellstr) ? nellstr2goldmid.get(nellstr) : "NA";
				if (!no1mid.equals(mypredmid)) {
					dw.write(nellstr, mypredmid, no1mid, goldmid);
				}
			}
			dw.close();
		}
	}

	public static void string_alignment_accuracy(String file_gold, String file_newnellstr2mid, String output)
			throws IOException {
		HashMap<String, String> answer = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_gold);
			String[] l;
			while ((l = dr.read()) != null) {
				answer.put(l[0], l[1]);
			}
		}

		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(file_newnellstr2mid);
			String[] l;
			int correct = 0, total = 0;
			while ((l = dr.read()) != null) {
				String name = l[0];
				String right_mid = answer.get(name);
				String pred_mid = l[1];
				if (right_mid != null) {
					if (right_mid.equals(pred_mid)) {
						correct++;
					}
					total++;
				}
			}
			dw.write("Accuracy:", correct, total, correct * 1.0 / total);
			D.p("Accuracy:", correct, total, correct * 1.0 / total);
			dw.close();
		}
	}
}

enum NellstrMidScoreIndex {
	FBRANK, //freebase search rank
	TYPECONSTRIAN, //type constrain
	SHAREWORD, //share words
	WIKILINK, //other arg appears in link
	WIKITEXT, //other arg appears in text
}

class NellstrMidPred {
	String nellstr;
	String mid;
	String enurl;
	boolean isGood;
	int pid;

	HashMap<NellstrMidScoreIndex, Double> scores = new HashMap<NellstrMidScoreIndex, Double>();
	static HashMap<NellstrMidScoreIndex, Double> weightForScore = new HashMap<NellstrMidScoreIndex, Double>();

	public NellstrMidPred(String nellstr, String mid, String enurl) {
		this.nellstr = nellstr;
		this.mid = mid;
		this.enurl = enurl;
	}

	public void setScore(NellstrMidScoreIndex si, double value) {
		scores.put(si, value);
	}

	double getWeight(NellstrMidScoreIndex si) {
		return scores.get(si);
	}
}
