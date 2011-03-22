package freebase.relationmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class Giveup_S1_matchFBrelation {
	public static void subsetFBgivenCandidate() {
		try {
			List<String[]> all = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean)).readAll();
			DelimitedWriter dw = new DelimitedWriter(Main.file_fbdump_subset_len0);
			HashSet<String> used = new HashSet<String>();
			for (String[] a : all) {
				used.add(a[1]);
			}
			D.p(all);

			DelimitedReader dr = new DelimitedReader(Main.fbdump);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				if (used.contains(mid)) {
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void len2step1() {

		try {
			/** if one mid has some wikipedia article */
			HashMap<String, String[]> mid2mainwidtitle = new HashMap<String, String[]>();
			{
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fout_mid_wid_title);
				String[] l;
				while ((l = dr.read()) != null) {
					mid2mainwidtitle.put(l[0], l);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_fbdump_subset_len0);
				DelimitedWriter dwlen0 = new DelimitedWriter(Main.file_relation_len0);
				DelimitedWriter dwlen1 = new DelimitedWriter(Main.file_relation_len1);
				DelimitedWriter dwlen2pre = new DelimitedWriter(Main.file_relation_len2 + ".temp1");
				String[] l;
				while ((l = dr.read()) != null) {
					String argBmid = l[2];
					if (argBmid.startsWith("/m/")) {
						if (mid2mainwidtitle.containsKey(argBmid)) {
							// main wid
							String b[] = mid2mainwidtitle.get(argBmid);
							dwlen1.write(new String[] { l[0], l[1], argBmid, b[1], b[2] });
						} else {
							dwlen2pre.write(new String[] { l[0], l[1], argBmid });
						}
					} else {
						// value already!!!
						dwlen0.write(new String[] { l[0], l[1], l[3] });
					}
				}
				dr.close();
				dwlen0.close();
				dwlen2pre.close();
				dwlen1.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void fbsubsetLen2() {
		try {
			HashSet<String> used = new HashSet<String>();
			final int ArgBmidIndex = 2;
			{
				DelimitedReader dr = new DelimitedReader(Main.file_relation_len2 + ".temp1");
				String[] l;
				while ((l = dr.read()) != null) {
					used.add(l[ArgBmidIndex]);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.fbdump);
				DelimitedWriter dw = new DelimitedWriter(Main.file_fbdump_subset_relation_len2_prepare + ".temp");
				String[] l;
				while ((l = dr.read()) != null) {
					if (used.contains(l[0])) {
						dw.write(l);
					}
				}
				dw.close();
				dr.close();
			}
			{
				// remove all /type/... stuff
				DelimitedReader dr = new DelimitedReader(Main.file_fbdump_subset_relation_len2_prepare + ".temp");
				DelimitedWriter dw = new DelimitedWriter(Main.file_fbdump_subset_relation_len2_prepare);
				String[] l;
				while ((l = dr.read()) != null) {
					if (!l[1].startsWith("/type") && !l[1].startsWith("/common")) {
						dw.write(l);
					}
				}
				dr.close();
				dw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void len2step2() {

		try {
			DelimitedWriter dw3 = new DelimitedWriter(Main.file_relation_len3);
			DelimitedWriter dw2 = new DelimitedWriter(Main.file_relation_len2);
			final int ArgBmidIndex = 2;
			{
				Sort.sort(Main.file_relation_len3 + ".temp1", Main.file_relation_len3 + ".temp2", Main.dir,
						new Comparator<String[]>() {
							public int compare(String[] o1, String[] o2) {
								// TODO Auto-generated method stub
								return o1[ArgBmidIndex].compareTo(o2[ArgBmidIndex]);
							}
						});
			}
			/** if one mid has some wikipedia article */
			HashMap<String, String[]> mid2mainwidtitle = new HashMap<String, String[]>();
			{
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fout_mid_wid_title);
				String[] l;
				while ((l = dr.read()) != null) {
					mid2mainwidtitle.put(l[0], l);
				}
				dr.close();
			}

			DelimitedReader dr = new DelimitedReader(Main.file_relation_len3 + ".temp2");
			DelimitedReader drb = new DelimitedReader(Main.file_fbdump_subset_relation_len2_prepare);
			List<String[]> blocks = drb.readBlock(0);
			String[] l;
			int nullnum = 0;
			// HashSet<String> bad = new HashSet<String>(); 580K
			// HashSet<String> good = new HashSet<String>(); 18K
			while ((l = dr.read()) != null) {
				String argBmid = l[ArgBmidIndex];
				if (drb == null)
					break;
				while (blocks.get(0)[0].compareTo(argBmid) < 0 && (blocks = drb.readBlock(0)) != null) {
					// wait;
				}

				String argAmid = l[0];
				String rel1 = l[1];
				if (blocks.get(0)[0].equals(argBmid)) {
					for (String[] b : blocks) {
						String rel2 = b[1];
						String argBValue = b[2];
						if (argBValue.startsWith("/m/")) {
							// looking for its wid
							String w[] = mid2mainwidtitle.get(argBValue);
							if (w != null) {
								dw3.write(argAmid, rel1 + "|" + rel2, argBValue, w[1], w[2]);
							} else {
								nullnum++;
								// dw.write(argAmid, rel1 + "|" + rel2,
								// argBValue, "null");
							}
						} else {
							dw2.write(argAmid, rel1 + "|" + rel2, b[3]);
						}
					}
				}
			}
			// System.err.println(good.size() + "\t" + bad.size());
			System.err.println("Number of length2 path not leads to wid\t" + nullnum);
			drb.close();
			dw3.close();
			dw2.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void matchValue(String input, String output) {
		try {
			List<String[]> all = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean)).readAll();
			DelimitedWriter dw = new DelimitedWriter(output);
			StringTable.sortByColumn(all, new int[] { 1 });
			DelimitedReader dr = new DelimitedReader(input);
			List<String[]> b = dr.readBlock(0);
			for (String[] a : all) {
				if (a[6].equals("-1"))
					continue;

				String argB = a[4];
				String argA = a[3];
				String widstr = a[2];
				String argAmid = a[1];
				String argAurl = a[0];
				String nellrelation = a[5];
				String arg1or2 = a[7];
				if (b == null)
					break;
				while (b.get(0)[0].compareTo(argAmid) < 0 && (b = dr.readBlock(0)) != null) {

				}
				if (b.get(0)[0].equals(argAmid)) {
					for (String[] b0 : b) {
						String fbrelation = b0[1];
						if (fbrelation.startsWith("/type/object") || fbrelation.startsWith("/user")
								|| fbrelation.startsWith("/base") || fbrelation.startsWith("/m/")) {
							continue;
						}
						String value = b0[2];
						int[] p = new int[2];
						int share = StringUtil.numOfShareWords(argB, value, p);
						if (share > 0 && share == Math.max(p[0], p[1])) {
							dw.write(argAurl, argAmid, widstr, argA, nellrelation, arg1or2, fbrelation, argB, value);
						}
					}
				} else {
					D.p("Missing", argAmid);// good!!! no one is missing!
				}
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Alias
	 * */
	public static void matchTitle(String input, String output) {
		try {
			List<String[]> all = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean)).readAll();
			DelimitedWriter dw = new DelimitedWriter(output);
			StringTable.sortByColumn(all, new int[] { 1 });
			final int blockkey = 0;
			Sort.sort(input, input + ".sbmid", Main.dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[blockkey].compareTo(o2[blockkey]);
				}

			});
			DelimitedReader dr = new DelimitedReader(input + ".sbmid");
			HashSet<String> tocheck = new HashSet<String>();
			List<String[]> b = dr.readBlock(blockkey);
			for (String[] a : all) {
				if (a[6].equals("-1"))
					continue;

				String argB = a[4];
				String argA = a[3];
				String widstr = a[2];
				String argAmid = a[1];
				if (argAmid.equals("/m/01kmd4")) {
					System.out.print("");
				}
				String argAurl = a[0];
				String nellrelation = a[5];
				String arg1or2 = a[7];
				if (b == null)
					break;
				while (b.get(0)[blockkey].compareTo(argAmid) < 0 && (b = dr.readBlock(0)) != null) {

				}
				if (b.get(0)[blockkey].equals(argAmid)) {
					for (String[] b0 : b) {

						String fbrelation = b0[1];
						if (fbrelation.startsWith("/type/object") || fbrelation.startsWith("/user")
								|| fbrelation.startsWith("/base") || fbrelation.startsWith("/m/")) {
							continue;
						}
						String argBmid = b0[2];
						String argBwidstr = b0[3];
						String value = b0[4];
						String[] names = value.split(" ");
						for (String n : names) {
							int[] p = new int[2];
							int share = StringUtil.numOfShareWords(argB, n, p);
							if (share > 0 && share == Math.max(p[0], p[1])) {
								String check = argAurl + argAmid + widstr + argA + nellrelation + arg1or2 + fbrelation
										+ argB + argBmid + argBwidstr + value;
								if (!tocheck.contains(check)) {
									dw.write(argAurl, argAmid, widstr, argA, nellrelation, arg1or2, fbrelation, argB,
											argBmid, argBwidstr, value);
									tocheck.add(check);
								}
								break;
							}
						}
					}
				} else {
					// D.p("Missing", argAmid);// good!!! no one is missing!
				}
			}
			dw.close();

		} catch (Exception e) {

		}
	}

	static void getCandidate(String file){
		HashMap<String,List<String>>relpair_evidence = new HashMap<String,List<String>>();
		try {
			DelimitedReader dr = new DelimitedReader(file);
			DelimitedWriter dw = new DelimitedWriter(file+".tosee");
			String []l;
			while((l = dr.read())!=null){
				String nellrelation = l[4];
				String fbrelation = l[6];
				String pair = nellrelation+"  "+fbrelation;
				if(!relpair_evidence.containsKey(pair)){
					relpair_evidence.put(pair, new ArrayList<String>());
				}
				relpair_evidence.get(pair).add(l[3]+" "+l[7]);
			}
			dr.close();
			
			for(Entry<String,List<String>>e: relpair_evidence.entrySet()){
				dw.write(e.getKey(),e.getValue());
			}
			dw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		/** By argA, filter out FB dump */
		// subsetFBgivenCandidate();

		/**
		 * Given the subset of quad of FB dump, convert the mid arg2 into its
		 * Name, or into a table For example, /m/04mj9l6 into Player: Mauricio
		 * Castro Team: New England Revolution The results are stored in
		 * len0_mid_rel_value len1_mid_rel_mid_wid len2_mid_rel_mid
		 * */
		{
			// len2step1();
			// fbsubsetLen2();
			// len2step2();
		}
		/**
		 * Q: I really doubt if len0 can lead us something? Because most of them
		 * are just /type/object... stuff???
		 */
		/**
		 * A: Yes, it is pretty useful to get alias!!!
		 */
		// matchValue(Main.file_relation_len0, Main.file_match_len0);
		// matchValue(Main.file_relation_len2, Main.file_match_len2);

		/** Need to load the mid_fbnamealias */
		//matchTitle(Main.file_relation_len1, Main.file_match_len1);
		//matchTitle(Main.file_relation_len3, Main.file_match_len3);

		getCandidate(Main.file_match_len3);
	}
}
