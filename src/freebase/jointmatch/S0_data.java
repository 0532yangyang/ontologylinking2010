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

	public static void get_wid_fbtype() throws IOException {
		HashMap<String, Integer> map_mid2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[1];
				if(mid.equals("/m/0dbp00")){
					D.p(l);
				}
				int wid = Integer.parseInt(l[2]);
				map_mid2wid.put(mid, wid);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.fin_mid_type);
			DelimitedWriter dw = new DelimitedWriter(Main.file_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				if(mid.equals("/m/0dbp00")){
					D.p(l);
				}
				if (map_mid2wid.containsKey(mid)) {
					int wid = map_mid2wid.get(mid);
					dw.write(wid, l[1]);
				}
			}
			dw.close();
			dr.close();
		}
	}

	public static void main(String[] args) throws IOException {
		//get_gnid_mid_wid_title_enid();
		get_wid_fbtype();
	}
}
