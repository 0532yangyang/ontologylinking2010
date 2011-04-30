package freebase.jointmatch;

import java.io.IOException;
import java.util.HashMap;

import javatools.administrative.D;
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

	public static void main(String[] args) throws IOException {
		//get_gnid_mid_wid_title_enid();
		//get_wid_fbtype();
		get_notable_type();
	}
}
