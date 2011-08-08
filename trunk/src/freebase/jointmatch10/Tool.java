package freebase.jointmatch10;

import java.io.IOException;
import java.util.HashMap;

import javatools.filehandlers.DelimitedReader;

public class Tool {
	public static HashMap<String, String[]> get_enurl2others() throws IOException {
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] enurls = l[2].split(" ");
				for (String enurl : enurls) {
					map.put(enurl, l);
				}
			}
			dr.close();
		}
		return map;
	}

	public static HashMap<String, String[]> get_mid2others() throws IOException {
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l.length == 5) {
					String mid = l[1];
					map.put(mid, l);
				}
			}
			dr.close();
		}
		return map;
	}

	public static HashMap<String, String> get_notabletype() throws IOException {
		HashMap<String, String> notabletype = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_notablefor_mid_wid_type);
			String[] l;
			while ((l = dr.read()) != null) {
				notabletype.put(l[0], l[2]);
			}
		}
		return notabletype;
	}
}
