package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.string.StringUtil;

public class S0_myfbsearch {
	static HashMap<String, HashSet<Integer>> word2wids = new HashMap<String, HashSet<Integer>>(1000000);

	private static void loadMidTitle2() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fout_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				int wid = Integer.parseInt(l[1]);
				List<String> words = StringUtil.tokenize(l[2], new char[] { '_', ' ', ',', '/' });
				for (String w : words) {
					w = w.toLowerCase();
					if (!word2wids.containsKey(w)) {
						word2wids.put(w, new HashSet<Integer>());
					}
					word2wids.get(w).add(wid);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadMidTitle() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fout_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				int wid = Integer.parseInt(l[1]);
				List<String> words = StringUtil.tokenize(l[2], new char[] { ' '});
				for (String w : words) {
					w = w.toLowerCase().replaceAll("_", " ").replaceAll(",", "").replaceAll("/", "");
					if (!word2wids.containsKey(w)) {
						word2wids.put(w, new HashSet<Integer>());
					}
					word2wids.get(w).add(wid);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<Integer> search(String query) {
		List<Integer> result = new ArrayList<Integer>();
		List<String> words = StringUtil.tokenize(query, new char[] { '_', ' ', ',', '/' });
		HashCount<Integer> c = new HashCount<Integer>();
		for (String w : words) {
			w = w.toLowerCase();
			if (word2wids.containsKey(w)) {
				HashSet<Integer> wids = word2wids.get(w);
				for (int wid : wids) {
					c.add(wid);
				}
			}
		}
		List<Integer> matchwids = new ArrayList<Integer>();
		List<Integer> counts = new ArrayList<Integer>();
		c.getAll(matchwids, counts);
		for (int i = 0; i < matchwids.size(); i++) {
			if (counts.get(i) == counts.get(0)) {
				result.add(matchwids.get(i));
			}
		}
		return result;
	}

	/**Not easy to let it work...*/
	public static void main(String[] args) throws IOException {

		loadMidTitle();
		DelimitedWriter dw = new DelimitedWriter(Main.file_myfbsearch);
		NellOntology no = new NellOntology();
		for (NellRelation nr : no.nellRelationList) {
			List<String[]> seeds = nr.seedInstances;
			for (String[] s : seeds) {
				List<Integer> r1 = search(s[0]);
				List<Integer> r2 = search(s[1]);
				dw.write(s[0], r1);
				dw.write(s[1], r2);
			}
		}
		dw.close();
	}
}
