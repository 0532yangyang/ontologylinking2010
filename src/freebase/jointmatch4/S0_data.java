package freebase.jointmatch4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import cc.factorie.protobuf.DocumentProtos.Relation;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.PbReader;
import javatools.mydb.StringTable;

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
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
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
		HashMap<String, Integer> map_name2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] titles = l[3].split(" ");
				int wid = Integer.parseInt(l[2]);
				for (String t : titles) {
					map_name2wid.put(t, wid);
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
						dw.write(wid1, map_name2wid.get(name));
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

	static void convertSeb2Nell() throws IOException {
		HashMap<String, String> guid2name = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_seb_guid2name);
			String[] l;
			while ((l = dr.read()) != null) {
				guid2name.put(l[0], l[1]);
			}
			dr.close();
		}
		DelimitedWriter dw = new DelimitedWriter(Main.file_ontology + ".raw");
		List<String[]> towrite = new ArrayList<String[]>();
		PbReader pr = new PbReader(Main.file_sebtrain);
		Relation r = null;
		while ((r = pr.read()) != null) {
			String rels = r.getRelType();
			if (!rels.equals("NA")) {
				String[] rel = rels.split(",");
				String sguid = r.getSourceGuid();
				String dguid = r.getDestGuid();
				String sname = guid2name.get(sguid);
				String dname = guid2name.get(dguid);
				if (sname != null && dname != null) {
					for (String r0 : rel) {
						towrite.add(new String[] { r0, sname, dname });
					}
				}
			}
		}
		StringTable.sortByColumn(towrite, new int[] { 0 });
		List<List<String[]>> blocks = StringTable.toblock(towrite, 0);
		for (List<String[]> b : blocks) {
			String relationname = b.get(0)[0];
			StringBuilder sb = new StringBuilder();
			int k = 0;
			for (String[] l : b) {
				sb.append("{\"" + l[1] + "\",\"" + l[2] + "\"} ");
				if (k++ > 10)
					break;

			}
			dw.write(relationname, sb.toString());
		}
		dw.close();

	}

	static void convertInfobox2Nell() throws IOException {
		HashMap<String, String[]> relation2everything = new HashMap<String, String[]>();
		{
			List<String[]> all1 = (new DelimitedReader(Main.file_raw_ontology_class)).readAll();
			List<String[]> all2 = (new DelimitedReader(Main.file_raw_ontology_seedinstance)).readAll();
			for (String[] a : all1) {
				relation2everything.put(a[0], new String[] { a[0], a[1], a[2], "" });
			}
			List<List<String[]>> blocks = StringTable.toblock(all2, 0);
			for (List<String[]> b : blocks) {
				String relname = b.get(0)[0];
				StringBuilder sb = new StringBuilder();
				for (String[] l : b) {
					sb.append("{\"" + l[1] + "\",\"" + l[2] + "\"} ");
				}
				relation2everything.get(relname)[3] = sb.toString();
			}
		}
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_ontology);
			for (Entry<String, String[]> e : relation2everything.entrySet()) {
				dw.write(e.getValue());
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		//get_gnid_mid_wid_title_enid();
		//get_wid_fbtype();
		//get_notable_type();
		//get_wiki_pagelink();
		//get_wiki_pagelink_step2();
		//convertSeb2Nell();
		convertInfobox2Nell();
	}
}
