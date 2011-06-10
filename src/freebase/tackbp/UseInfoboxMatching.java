package freebase.tackbp;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import multir.util.delimited.Sort;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class UseInfoboxMatching {

	public static void getTrainingTupes() throws IOException {
		HashMap<String, List<String>> ibrel2kbprel = new HashMap<String, List<String>>();
		{
			List<String[]> all = (new DelimitedReader(Main.file_mapping2ib)).readAll();
			StringTable.sortByColumn(all, new int[] { 1 });
			D.p(all);
			for (String[] a : all) {
				StringTable.mapKey2SetAdd(ibrel2kbprel, a[0], a[1], true);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_ibdump);
			DelimitedWriter dw = new DelimitedWriter(Main.file_ibtuples);
			String[] l;
			while ((l = dr.read()) != null) {
				String ibname = l[1];
				String relname = l[2];
				String key = ibname + ":" + relname;
				if (ibrel2kbprel.containsKey(key)) {
					List<String> kbprel = ibrel2kbprel.get(key);
					for (String r : kbprel) {
						dw.write(r, l[0], l[1]+":"+l[2], l[3]);
						//dw.write(r,l[0],l[1], [2],l[3]);
					}
				}
			}
			dw.close();
		}
		Sort.sort(Main.file_ibtuples, Main.file_ibtuples + ".sort", Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] arg0, String[] arg1) {
				// TODO Auto-generated method stub
				return arg0[0].compareTo(arg1[0]);
			}
		});
	}

	public static void main(String[] args) throws IOException {
		getTrainingTupes();
	}
}
