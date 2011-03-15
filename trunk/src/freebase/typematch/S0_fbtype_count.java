package freebase.typematch;

import java.util.List;

import javatools.filehandlers.DelimitedReader;

import percept.util.delimited.DelimitedWriter;

import multir.util.HashCount;

public class S0_fbtype_count {
	static HashCount<String> hc = new HashCount<String>();

	public static void main(String[] args) {
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
}
