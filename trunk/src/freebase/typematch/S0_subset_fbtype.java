package freebase.typematch;

import java.util.HashSet;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class S0_subset_fbtype {
	public static void subsetfin_freebase_type_sortMid() throws Exception {
		DelimitedReader dr = new DelimitedReader(Main.fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid);
		DelimitedWriter dw = new DelimitedWriter(Main.fout_freebase_type_sortMid_subset);
		String[] line;
		HashSet<String> usedMid = new HashSet<String>();
		while ((line = dr.read()) != null) {
			String mid = line[1];
			usedMid.add(mid);
		}
		dr.close();

		dr = new DelimitedReader(Main.fin_freebase_type_sortMid);
		while ((line = dr.read()) != null) {
			String mid = line[0];
			if (usedMid.contains(mid)) {
				dw.write(line);
			}
		}
		dr.close();
		dw.close();
	}
	
	public static void main(String []args)throws Exception{
		subsetfin_freebase_type_sortMid();
	}
}
