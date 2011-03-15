package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.mydb.StringTable;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class S0_wikipedia {
	static HashMap<String, String> wptitle2mid = new HashMap<String, String>();

	static void mid_wid() {
		try {

			DelimitedReader dr = new DelimitedReader(Main.fin_freebasedump_wikipedia_enid);
			String[] l;
			List<String[]> all = new ArrayList<String[]>();
			while ((l = dr.read()) != null) {
				int id = -1;
				try {
					id = Integer.parseInt(l[3]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (l[0].startsWith("/m") && id >= 0) {
					all.add(new String[] { l[0], l[3] });
				}
			}

			{
				StringTable.sortByColumn(all, new int[] { 1 }, new boolean[] { true }, new boolean[] { true });
				DelimitedWriter dw = new DelimitedWriter(Main.fout_mid_artid);
				for (String[] a : all) {
					dw.write(a);
				}
				dw.close();
			}
			{
				StringTable.sortByColumn(all, new int[] { 0 });
				DelimitedWriter dw = new DelimitedWriter(Main.fout_mid_artid_sbmid);
				for (String[] a : all) {
					dw.write(a);
				}
				dw.close();
			}

			dr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void mid_artid_fake() {

		try {
			DelimitedWriter dw = new DelimitedWriter(Main.fout_mid_artid);
			DelimitedReader dr = new DelimitedReader("/projects/pardosa/s5/clzhang/ontologylink/mid2Wikiurl");
			String[] line;
			while ((line = dr.read()) != null) {
				if (wptitle2mid.containsKey(line[1]))
					D.p(line[1]);

				wptitle2mid.put(line[1], line[0]);
			}
			dr.close();

			dr = new DelimitedReader("/projects/pardosa/s5/clzhang/tmp/wp/enwiki_title");

			while ((line = dr.read()) != null) {
				String title = line[1].trim().replace(" ", "_");
				if (wptitle2mid.containsKey(title)) {
					dw.write(wptitle2mid.get(title), line[0], title);
				}
			}
			dw.close();
			dr.close();
			Sort.sort(Main.fout_mid_artid, Main.fout_mid_artid_sbmid, Main.dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[0].compareTo(o2[0]);
				}

			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	static void mainwid() {
//		try {
//			DelimitedReader dr = new DelimitedReader(Main.fout_mid_artid);
//			DelimitedReader dr0 = new DelimitedReader(Main.fin_wp_stanford);
//			DelimitedWriter dw = new DelimitedWriter(Main.fout_mid_mainwid);
//			List<RecordWpSenToken> list_record = RecordWpSenToken.readByArticleId(dr0);
//
//			String[] l;
//			while ((l = dr.read()) != null) {
//				int wid = Integer.parseInt(l[1]);
//				while (list_record.get(0).articleId < wid
//						&& (list_record = RecordWpSenToken.readByArticleId(dr0)) != null) {
//					// do nothing... wait
//				}
//				if (list_record.get(0).articleId == wid) {
//					dw.write(l);
//				}
//			}
//
//			dr.close();
//			dr0.close();
//			dw.close();
//
//			{
//				Sort.sort(Main.fout_mid_mainwid, Main.fout_mid_mainwid_sbmid, Main.pdir, new Comparator<String[]>() {
//					@Override
//					public int compare(String[] o1, String[] o2) {
//						// TODO Auto-generated method stub
//						return o1[0].compareTo(o2[0]);
//					}
//				});
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	static void wid2category() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.fout_mid_categorywiki + ".temp");
			DelimitedReader dr = new DelimitedReader(Main.fin_wex_category);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[0]);
				String c = l[1].replaceAll(" ", "_");
				dw.write(wid, c);
			}
			dw.close();

			{
				Sort.sort(Main.fout_mid_categorywiki + ".temp", Main.fout_mid_categorywiki, Main.pdir,
						new Comparator<String[]>() {
							@Override
							public int compare(String[] o1, String[] o2) {
								int a = Integer.parseInt(o1[0]);
								int b = Integer.parseInt(o2[0]);
								return a - b;
							}
						});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		/**
		 * join_mid wikiurl and wpArticleidTitle to get file: mid_wid
		 */
		mid_wid();

		/**
		 * dealing with Main.fout_mid_artid, getting those mid_wid pairs that
		 * wid has the documents
		 */
		//mainwid();

		/** get the category information for all wiki articles */
		// wid2category();
	}
}
