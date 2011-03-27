package freebase.typematch;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

public class S0_fb {
	static HashCount<String> hc = new HashCount<String>();

	public static void sbtype_count() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fin_freebase_type_sortMid);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_fbtype_count);
			String[] line;
			while ((line = dr.read()) != null) {
				hc.add(line[1]);
			}
			dr.close();
			List<String[]> all = hc.getAll();
			for (String[] a : all) {
				dw.write(a[0], a[1]);
			}
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cleanFBType() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fin_freebase_type_sortMid);
			DelimitedWriter dw = new DelimitedWriter(Main.fin_freebase_type_clean_sortMid);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[0].startsWith("/m/") && !l[1].equals("/common/topic")) {
					dw.write(l);
				}
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void sortByType() {
		try {
			D.p("sort by type");
			Sort.sort(Main.fin_freebase_type_sortMid, Main.fin_freebase_type_sortType, Main.dir,
					new Comparator<String[]>() {

						@Override
						public int compare(String[] o1, String[] o2) {
							// TODO Auto-generated method stub
							return o1[1].compareTo(o2[1]);
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sample1000EveryType() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fin_freebase_type_clean_sortMid);
			DelimitedWriter dw = new DelimitedWriter(Main.fin_freebase_type_clean_sample);
			String[] l;
			HashCount<String> hc = new HashCount<String>();
			while ((l = dr.read()) != null) {
				int c = hc.see(l[1]);
				if (c < 1000) {
					dw.write(l);
					hc.add(l[1]);
				}
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		//cleanFBType();

		sample1000EveryType();

		//sortByType();
	}
}
