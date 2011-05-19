package freebase.jointmatch6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import percept.util.delimited.DelimitedWriter;

import javatools.filehandlers.DelimitedReader;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S5 {
	/**Score string alignment given relation matching
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		String file_raw = Main.file_fbsearch2;
		String file_afterrelmatch_typeconstrain = Main.file_afterrelmatch_typeconstrain;

		String file_weight_fbrank = Main.dir + "/weight_str_fbrank";
		String file_weight_typeconstrain = Main.dir + "/weight_str_typeconstrain";
		String file_weight_shareword = Main.dir + "/weight_str_shareword";
		String file_weight_appearinlink = Main.dir + "/weight_str_appearinlink";
		String file_weight_appearintext = Main.dir + "/weight_str_appearintext";

		a1_fbrank(file_raw, file_weight_fbrank);
		a2_typeconstrain(file_raw, file_afterrelmatch_typeconstrain, file_weight_typeconstrain);
		a3_shareword(file_raw, file_weight_shareword);
		//		a4_otherarg_appearinlink(file_raw);
		//		a5_otherarg_appearintext(file_raw);
		//		
		//		b1_inference();
	}

	public static void a1_fbrank(String file_raw, String file_weight_fbrank) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_fbrank);
		DelimitedReader dr = new DelimitedReader(file_raw);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			int score = 0;
			for (String[] l : b) {
				String mid = l[1];
				dw.write(argname, mid, score);
				score++;
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
			String nelltype = arg2nelltype.get(argname);
			HashMap<String, Double> fbtype_dist = nelltype_fbtypedist.get(nelltype);

			for (String[] l : b) {
				String mid = l[1];
				String midfbtype = l[3];
				if (fbtype_dist == null) {
					dw.write(argname, mid, "-1");//error
				} else {
					double score = fbtype_dist.containsKey(midfbtype) ? fbtype_dist.get(midfbtype) : 0;
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
	//	private static void helpAdd(HashMap<String, Set<String>> map, String key, String value) {
	//		if (!map.containsKey(key)) {
	//			map.put(key, new HashSet<String>());
	//		}
	//		map.get(key).add(value);
	//	}
}
