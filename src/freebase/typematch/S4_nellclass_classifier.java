package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.mydb.StringTable;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class S4_nellclass_classifier {

	/**
	 * @param args
	 */

	public static void nelltype_nellstring_mid_wid() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.fout_nelltype_mid_mainwid);
			HashMap<String, String> nellstr_mid_sbweight = new HashMap<String, String>();
			HashMap<String, String> map_mid2mainwid = (new DelimitedReader(Main.fout_mid_artid_sbmid)).readAll2Hash(
					0, 1);
			{
				List<String[]> all = (new DelimitedReader(Main.fout_weight_nellstring_mid_cosine)).readAll();
				StringTable.sortByColumn(all, new int[] { 1, 2 }, new boolean[] { false, true });
				for (String[] x : all) {
					if (!nellstr_mid_sbweight.containsKey(x[1])) {
						if (map_mid2mainwid.containsKey(x[0])) {
							nellstr_mid_sbweight.put(x[1], x[0]);
						}
					}
					// nellstr_mid_sbweight.get(x[1]).add(x);
				}
			}

			DelimitedReader dr = new DelimitedReader(Main.fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid);
			List<String[]> interesting = new ArrayList<String[]>();

			String[] line;
			int nellclasstypenull = 0;
			HashSet<String> avoiddup = new HashSet<String>();
			while ((line = dr.read()) != null) {

				String argname = line[3];
				String topmid = nellstr_mid_sbweight.get(argname.toLowerCase());
				String label = line[6];
				if (label.equals("-1"))
					continue;
				if (avoiddup.contains(argname)) {
					continue;
				}
				avoiddup.add(argname);
				HashSet<String> nellclass = Main.nellontology.entity2class.get(argname);
				{
					if (nellclass == null || topmid == null) {
						nellclasstypenull++;
						// D.p("nell class type is null:", argname);
						continue;
					}
				}
				for (String nt : nellclass) {
					interesting.add(new String[] { nt, topmid, argname, "-1" });
				}
				// D.p(argname, topmid, nellclass);
			}
			StringTable.sortUniq(interesting);
			StringTable.sortByColumn(interesting, new int[] { 1 });

			{
				/** Join mid_mainwid with interesting */
				int missingwid = 0;
				DelimitedReader drw = new DelimitedReader(Main.fout_mid_artid_sbmid);
				String[] l = drw.read();
				for (String[] t : interesting) {
					String topmid = t[1];
					while (l[0].compareTo(topmid) < 0 && (l = drw.read()) != null)
						;
					if (l[0].equals(topmid)) {
						t[3] = l[1];
					} else {
						missingwid++;
					}
				}
				D.p("missing wid ", missingwid);
			}
			{
				StringTable.sortByColumn(interesting, new int[] { 3 }, new boolean[] { true }, new boolean[] { true });
				for (String[] t : interesting) {
					dw.write(t);
				}
			}
			D.p("Missing topmid ", nellclasstypenull);
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void subset_stanfordwiki(){
		try {
			HashSet<Integer>usedArtid = new HashSet<Integer>();
			List<String[]> all = (new DelimitedReader(Main.fout_nelltype_mid_mainwid)).readAll();
			for(String []l: all){
				usedArtid.add(Integer.parseInt(l[3]));
			}
			DelimitedReader dr = new DelimitedReader(Main.fin_wp_stanford);
			DelimitedWriter dw = new DelimitedWriter(Main.fout_wp_stanford_s4subset);
			String []l;
			while((l = dr.read())!=null){
				int wid = Integer.parseInt(l[1]);
				if(usedArtid.contains(wid)){
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**Featurize*/
	public static void featurize(){
		try {
			
			DelimitedReader dr = new DelimitedReader(Main.fout_nelltype_mid_mainwid);
			String []l;
			while((l = dr.read())!=null){
				
			}
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/** nelltype_nellstring_mid_wid,
		 * then sample subset of the Wikipedia article */
//		nelltype_nellstring_mid_wid();
		subset_stanfordwiki();
		
		
		
		
	}

}
