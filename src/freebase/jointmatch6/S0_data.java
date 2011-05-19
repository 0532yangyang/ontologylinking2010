package freebase.jointmatch6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S0_data {

	public static void get_gnid_mid_wid_title_enid() throws IOException {
		HashMap<String, String> map_mid2enurl = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_mid2enurl);
			String[] l;
			while ((l = dr.read()) != null) {
				map_mid2enurl.put(l[0], l[1]);
			}
			dr.close();
		}
		{
			//926     /m/01003_       135776  Krum Krum,_Texas Denton_County_/_Krum_city
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			//926     /m/01003_       135776  ??? Krum Krum,_Texas Denton_County_/_Krum_city
			DelimitedWriter dw = new DelimitedWriter(Main.file_gnid_mid_enurl_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				if (map_mid2enurl.containsKey(l[1])) {
					String enurl = map_mid2enurl.get(l[1]);
					dw.write(l[0], l[1], l[2], enurl, l[3]);
				}
			}
			dr.close();
			dw.close();
		}

	}

	/**not using type information anymore, use notable_for*/
	public static void giveup_get_wid_fbtype() throws IOException {
		//		HashMap<String, Integer> map_mid2wid = new HashMap<String, Integer>();
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				String mid = l[1];
		//				if (mid.equals("/m/0dbp00")) {
		//					D.p(l);
		//				}
		//				int wid = Integer.parseInt(l[2]);
		//				map_mid2wid.put(mid, wid);
		//			}
		//			dr.close();
		//		}
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.fin_mid_type);
		//			DelimitedWriter dw = new DelimitedWriter(Main.file_wid_type_mid);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				String mid = l[0];
		//				if (mid.equals("/m/0dbp00")) {
		//					D.p(l);
		//				}
		//				if (map_mid2wid.containsKey(mid)) {
		//					int wid = map_mid2wid.get(mid);
		//					dw.write(wid, l[1], mid);
		//				}
		//			}
		//			dw.close();
		//			dr.close();
		//		}
	}

	public static void get_notable_type() throws IOException {
		HashMap<String, Integer> mid2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2wid.put(l[1], Integer.parseInt(l[2]));
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notable_for_raw);
			DelimitedWriter dw = new DelimitedWriter(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				String[] ab = l[3].split(" ");
				try {
					String x = ab[1].split(":")[1].replace("\"", "").replace(",", "");
					if (mid2wid.containsKey(mid)) {
						int wid = mid2wid.get(mid);
						dw.write(mid, wid, x);
					}
				} catch (Exception e) {

				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void get_notable_type2() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notable_for_raw);
			DelimitedWriter dw = new DelimitedWriter(Main.file_notable_for_raw2);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				String[] ab = l[3].split(" ");
				try {
					String x = ab[1].split(":")[1].replace("\"", "").replace(",", "");
					dw.write(mid, x);
				} catch (Exception e) {

				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void get_wiki_pagelink() throws IOException {
		HashSet<Integer> usedwid = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				usedwid.add(Integer.parseInt(l[2]));
			}
			dr.close();
		}
		String raw = "/projects/pardosa/s5/clzhang/tmp/wp/wikipagelinks.raw";
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_wikipediapagelink + ".raw");
			BufferedReader br = new BufferedReader(new FileReader(raw));
			String l;
			while ((l = br.readLine()) != null) {
				if (l.contains("INSERT INTO"))
					break;
			}
			while ((l = br.readLine()) != null) {
				try {
					if (l.startsWith("INSERT INTO `pagelinks` VALUES")) {
						l = l.replace("INSERT INTO `pagelinks` VALUES (", "");
					}
					char[] cs = l.toCharArray();
					int stop[] = new int[2];
					int stopid = 0;
					for (int i = 0; i < cs.length; i++) {
						if (cs[i] == ',') {
							stop[stopid] = i;
							stopid++;
							if (stopid == 2)
								break;
						}
					}
					int id = Integer.parseInt(l.substring(0, stop[0]));
					String linkto = l.substring(stop[1] + 2, l.length() - 1);
					if (usedwid.contains(id)) {
						dw.write(id, linkto);
					}
				} catch (Exception e) {
					System.err.println(l);
				}
			}
			br.close();
			dw.close();
		}
	}

	public static void get_wiki_pagelink_step2() throws IOException {
		HashMap<String, List<Integer>> map_name2wid = new HashMap<String, List<Integer>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikititles + ".filteredbyfb");
			String[] l;
			while ((l = dr.read()) != null) {
				String name = l[1];
				int id = Integer.parseInt(l[0]);
				if (!map_name2wid.containsKey(name)) {
					map_name2wid.put(name, new ArrayList<Integer>());
				}
				map_name2wid.get(name).add(id);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink + ".raw");
			DelimitedWriter dw = new DelimitedWriter(Main.file_wikipediapagelink);
			int match = 0, missing = 0;
			String[] l;
			int count = 0;
			while ((l = dr.read()) != null) {
				count++;
				if (count % 1000000 == 0) {
					D.p("match", "missing", match, missing);
				}
				try {
					int wid1 = Integer.parseInt(l[0]);
					String name = l[1].trim().replace(" ", "_");
					if (map_name2wid.containsKey(name)) {
						List<Integer> ids = map_name2wid.get(name);
						for (int x : ids) {
							dw.write(wid1, x);
						}
						match++;
					} else {
						missing++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			D.p("missing", missing);
			dr.close();
			dw.close();
		}
	}

	public static void indexFBEntityNames() throws IOException {
		String tempfile = Main.file_gnid_mid_wid_title + ".temp";
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			DelimitedWriter dw = new DelimitedWriter(tempfile);
			String[] l;
			while ((l = dr.read()) != null) {
				dw.write(l[1], l[2], l[3].replace("_", " "));
			}
			dw.close();
		}
		javatools.webapi.LuceneIndexFiles.indexDelimitedFile(tempfile, 2, new int[] { 0, 1 },
				Main.dir_gnid_mid_wid_title_luceneindex);
	}

	public static void getIndegreeOfWikiArticle() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			String[] l;
			HashCount<Integer> hc = new HashCount<Integer>();
			while ((l = dr.read()) != null) {
				int id1 = Integer.parseInt(l[0]);
				int id2 = Integer.parseInt(l[1]);
				hc.add(id2);
			}
			dr.close();
			DelimitedWriter dw = new DelimitedWriter(Main.file_wikipediapagelink_indegree);
			Iterator<Map.Entry<Integer, Integer>> it = hc.iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, Integer> e = it.next();
				dw.write(e.getKey(), e.getValue());
			}
			dw.close();
		}
	}

	public static void getWikiTitles() throws IOException {
		HashSet<Integer> usedwid = new HashSet<Integer>();
		String raw = "/projects/pardosa/s5/clzhang/tmp/wp/pagetitles.raw";
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_wikititles);
			BufferedReader br = new BufferedReader(new FileReader(raw));
			String l;
			while ((l = br.readLine()) != null) {
				if (l.contains("INSERT INTO"))
					break;
			}
			while ((l = br.readLine()) != null) {
				try {
					if (l.startsWith("INSERT INTO `page` VALUES (")) {
						l = l.replace("INSERT INTO `page` VALUES (", "");
					}
					char[] cs = l.toCharArray();
					int stop[] = new int[2];
					int stopid = 0;
					for (int i = 0; i < cs.length; i++) {
						if (cs[i] == '\'') {
							stop[stopid] = i;
							stopid++;
							if (stopid == 2) {
								break;
							}
						}
					}
					String name = l.substring(stop[0] + 1, stop[1]);
					String[] abc = l.split(",");
					int wid = Integer.parseInt(abc[0]);
					dw.write(wid, name);
				} catch (Exception e) {
					System.err.println(l);
				}
			}
			br.close();
			dw.close();
		}
	}

	public static void getWikiTitles_filterbyfb() throws IOException {
		HashSet<Integer> usedwid = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				usedwid.add(wid);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikititles);
			DelimitedWriter dw = new DelimitedWriter(Main.file_wikititles + ".filteredbyfb");
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[0]);
				if (usedwid.contains(wid)) {
					dw.write(l);
				}
			}
			dw.close();
		}
	}

	public static void getOutdegreeArticle() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink + ".raw");
		String[] l;
		HashCount<Integer> hc = new HashCount<Integer>();
		while ((l = dr.read()) != null) {
			int id1 = Integer.parseInt(l[0]);
			hc.add(id1);
		}
		dr.close();
		DelimitedWriter dw = new DelimitedWriter(Main.file_wikipediapagelink_outdegree);
		Iterator<Map.Entry<Integer, Integer>> it = hc.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> e = it.next();
			dw.write(e.getKey(), e.getValue());
		}
		dw.close();
	}

	public static void get_wiki_pagelink_step2_gnid() throws IOException {
		HashMap<String, List<Integer>> map_name2wid = new HashMap<String, List<Integer>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] titles = l[3].split(" ");
				int wid = Integer.parseInt(l[2]);
				for (String t : titles) {
					if (!map_name2wid.containsKey(t)) {
						map_name2wid.put(t, new ArrayList<Integer>());
					}
					map_name2wid.get(t).add(wid);
				}
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink + ".raw");
			DelimitedWriter dw = new DelimitedWriter(Main.file_wikipediapagelink);
			int match = 0, missing = 0;
			String[] l;
			while ((l = dr.read()) != null) {
				try {
					int wid1 = Integer.parseInt(l[0]);
					String name = l[1];

					if (map_name2wid.containsKey(name)) {
						if (map_name2wid.containsKey(name)) {
							List<Integer> ids = map_name2wid.get(name);
							for (int x : ids) {
								dw.write(wid1, x);
							}
						}
						match++;
					} else {
						missing++;
					}
				} catch (Exception e) {

				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void create_gnid_wid_enurl_wid_title() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_gnid_mid_enurl_wid_title);
		HashMap<String, String[]> mid2others = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.pdir + "/mid_wid");
			String[] l;
			int error = 0;
			while ((l = dr.read()) != null) {
				//gnid, mid, enurl,wid, title;
				if (mid2others.containsKey(l[0])) {
					//System.err.println("error:\t" + l[0] + "\t" + l[1]);
					//error++;
				}
				String[] s = new String[5];
				s[0] = "";
				s[1] = l[0];
				s[2] = "";
				s[3] = l[1];
				s[4] = "";
				mid2others.put(l[0], s);
			}
			D.p(error);
		}
		{
			D.p("gnid");
			DelimitedReader dr = new DelimitedReader(Main.pdir + "/fbnode.sbmid");
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				String gnidstr = l[1];
				String[] x = mid2others.get(mid);
				if (x != null) {
					x[0] = gnidstr;
				}
			}
		}
		{
			D.p("enurl");
			DelimitedReader dr = new DelimitedReader(Main.pdir + "/mid2enurl");
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				String enurl = l[1];
				String[] x = mid2others.get(mid);
				if (x != null) {
					x[2] = enurl;
				}
			}
		}
		{
			D.p("name");
			DelimitedReader dr = new DelimitedReader(Main.pdir + "/fbnamealias");
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				String name = l[1];
				String[] x = mid2others.get(mid);
				if (x != null) {
					name = name.replace(" ", "_");
					x[4] += name + " ";
				}
			}
		}
		for (Entry<String, String[]> e : mid2others.entrySet()) {
			String[] l = e.getValue();
			dw.write(l);
		}
		dw.close();
	}

	public static void create_gnid_wid_enurl_wid_title_type() throws IOException {
		HashMap<String, String> mid2type = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notable_for_raw2);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				String type = l[1];
				mid2type.put(mid, type);
			}
			dr.close();
		}
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_gnid_mid_enurl_wid_title_type);
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title);
			String[] l;
			int missing = 0;
			while ((l = dr.read()) != null) {
				if (l.length != 5) {
					D.p(l);
				}
				String mid = l[1];
				String type = "NA";

				if (mid2type.containsKey(mid)) {
					type = mid2type.get(mid);
				} else {
					//D.p("Type Missing for", mid, l[2]);
					missing++;
				}
				String[] s = new String[l.length + 1];
				System.arraycopy(l, 0, s, 0, l.length);
				s[l.length] = type;
				dw.write(s);

			}
			D.p("total missing", missing);
			dw.close();
		}
	}

	private static void test_gnid_wid_enurl_wid_title() throws IOException {
		String file = Main.dir + "/mycreateontology/manual_entity_mid.v2";
		String output = Main.dir + "/mycreateontology/manual_entity_mid";
		HashMap<String, String[]> mid2others = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2others.put(l[1], l);
			}
		}
		{
			int nummissing = 0;
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[1].trim();
				String[] s = mid2others.get(mid);
				if (s == null) {
					dw.write(l[0], l[1], "MISSING");
					nummissing++;
				} else {
					dw.write(l[0], l[1], s[3]);
				}
			}
			dw.close();
			D.p("Too bad!!! missing", nummissing);
		}
	}

	private static void clean_gnid2others() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type);
		DelimitedWriter dw = new DelimitedWriter(Main.file_gnid_mid_enurl_wid_title_type + ".clean");
		String[] l;
		while ((l = dr.read()) != null) {
			try {
				int gnid = Integer.parseInt(l[0]);
				String mid = l[1];
				String enurl = l[2];
				int wid = Integer.parseInt(l[3]);
				String title = l[4];
				String type = l[5];
				dw.write(gnid, mid, enurl, wid, title, type);
			} catch (Exception e) {
				D.p(l);
			}
		}
		dw.close();
		dr.close();
	}

	public static void main(String[] args) throws IOException {
		//get_gnid_mid_wid_title_enid();
		//get_wid_fbtype();
		//get_notable_type();
		//getWikiTitles();
		//getWikiTitles_filterbyfb();
		//get_wiki_pagelink();
		//get_wiki_pagelink_step2();
		//indexFBEntityNames();
		//getIndegreeOfWikiArticle();
		//getOutdegreeArticle();
		{
			//create_gnid_wid_enurl_wid_title();

		}
		//get_notable_type2();
		//create_gnid_wid_enurl_wid_title_type();
		//test_gnid_wid_enurl_wid_title();
		clean_gnid2others();
	}
}
