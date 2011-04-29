package freebase.ienyt;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

import percept.util.delimited.Sort;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S0_rewrite_queryresult_inwid {

	public static void main(String[] args) throws Exception {
		//rewrite();
		sortbyarg();
	}

	/**
	 * @param args
	 */
	/**re-write queryresult in wid*/
	public static void rewrite() throws Exception {
		// TODO Auto-generated method stub

		HashMap<Integer, Integer> gnid2wid = new HashMap<Integer, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int guid = Integer.parseInt(l[0]);
				int wid = Integer.parseInt(l[2]);
				gnid2wid.put(guid, wid);
			}
			dr.close();
		}

		DelimitedWriter dw = new DelimitedWriter(freebase.relmatch.Main.file_queryresult_wid);
		DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_queryresult_name);
		String[] l;
		while ((l = dr.read()) != null) {
			if (!l[0].equals(l[1])) {
				int guid1 = Integer.parseInt(l[0]);
				int guid2 = Integer.parseInt(l[1]);
				if (gnid2wid.containsKey(guid1) && gnid2wid.containsKey(guid2)) {
					int wid1 = gnid2wid.get(guid1);
					int wid2 = gnid2wid.get(guid2);
					dw.write(wid1, wid2, l[2], l[3], l[4], l[5], l[6]);
				}
			}
			//dw.write(l[0],l[1],l[4],"fbcand_"+l[2]);
		}
		dr.close();
		dw.close();
	}

	static void sortbyarg() throws IOException {
		Sort.sort(freebase.relmatch.Main.file_queryresult_wid, freebase.relmatch.Main.file_queryresult_wid + ".sbarg",
				Main.dir, new Comparator<String[]>() {
					@Override
					public int compare(String[] arg0, String[] arg1) {
						// TODO Auto-generated method stub
						String k1 = arg0[0]+" "+arg0[1];
						String k2 = arg1[0]+" "+arg1[1];
						return k1.compareTo(k2);
					}
				});
	}
}
