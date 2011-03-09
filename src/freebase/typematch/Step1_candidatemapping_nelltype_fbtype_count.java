package freebase.typematch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javatools.administrative.D;
import multir.util.HashCount;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class Step1_candidatemapping_nelltype_fbtype_count {

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

	static HashMap<String, List<String>> mid2ftype = new HashMap<String, List<String>>();

	private static void loadMid2Type(String file) {
		try {
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			while ((line = dr.read()) != null) {

				// filter the type
				String type0 = line[1];
				if (type0.startsWith("/m/") || type0.startsWith("/common/") || type0.startsWith("/book/book_subject")) {
					continue;
				}

				if (!mid2ftype.containsKey(line[0])) {
					mid2ftype.put(line[0], new ArrayList<String>());
				}
				mid2ftype.get(line[0]).add(line[1]);
			}
			dr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mapping() throws Exception {
		loadMid2Type(Main.fout_freebase_type_sortMid_subset);

		DelimitedReader dr = new DelimitedReader(Main.fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid);
		String[] line;

		HashSet<String> intresting = new HashSet<String>();
		while ((line = dr.read()) != null) {
			String mid = line[1];
			List<String> fbtypes = mid2ftype.get(mid);

			String argname = line[3];
			String label = line[6];
			if(label.equals("-1"))continue;
			HashSet<String> nellclass = Main.nellontology.entity2class.get(argname);

			if (fbtypes == null) {
				D.p("fb type is null:", mid, line[0]);
				continue;
			}
			if (nellclass == null) {
				D.p("nell class type is null:", argname);
				continue;
			}
			for (String ft : fbtypes) {
				for (String nt : nellclass) {
					intresting.add(nt + "\t" + ft + "\t" + argname + "\t" + mid);
				}
			}
		}
		// output
		HashSet<String> appearedNellType = new HashSet<String>();
		DelimitedWriter dw = new DelimitedWriter(Main.fout_candidatemapping_nelltype_fbtype_argname_mid);
		
		HashMap<String,HashSet<String>> nelltype_object_count = new HashMap<String,HashSet<String>>();
		HashMap<String,HashSet<String>> variable_count = new HashMap<String,HashSet<String>>();
		
		ArrayList<String> interestingList = new ArrayList<String>();
		interestingList.addAll(intresting);
		Collections.sort(interestingList);
		
		for (String a : interestingList) {
			String[] x = a.split("\t");
			dw.write(x);
			appearedNellType.add(x[0]);
			
			//add to nelltype_object_count
			String nelltype = x[0];
			if(!nelltype_object_count.containsKey(nelltype)){
				nelltype_object_count.put(nelltype,new HashSet<String>());
			}
			nelltype_object_count.get(nelltype).add(x[2]);
			
			//add to variable
			String typematchingvariable = x[0]+"\t"+x[1];
			if(!variable_count.containsKey(typematchingvariable)){
				variable_count.put(typematchingvariable, new HashSet<String>());
			}
			variable_count.get(typematchingvariable).add(x[2]);
		}
		dw.close();
		dr.close();
		
		
		List<String[]>dbtemp = new ArrayList<String[]>();
		for(Entry<String,HashSet<String>>e: variable_count.entrySet()){
			String []ab = e.getKey().split("\t");
			//dwcount.write(ab[0],ab[1],e.getValue().size());
			dbtemp.add(new String[]{ab[0],ab[1],e.getValue().size()+""});
		}
		Collections.sort(dbtemp,new Comparator<String[]>(){
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				int v1 = o1[0].compareTo(o2[0]);
				if(v1!=0){
					return v1;
				}else{
					int a = Integer.parseInt(o1[2]);
					int b = Integer.parseInt(o2[2]);
					return b-a;
				}
			}
		});
		DelimitedWriter dwcount = new DelimitedWriter(Main.fout_candidatemapping_nelltype_fbtype_count);
		for(String []a:dbtemp){
			String nelltype = a[0];
			dwcount.write(nelltype,a[1],a[2],nelltype_object_count.get(nelltype).size());
		}
		dwcount.close();

		// check if some nell type is missing in this candidate step
		{
			D.p("Missing types in the candidate step");
			for (String cn : Main.nellontology.classNames) {
				if (!appearedNellType.contains(cn)) {
					D.p(cn);
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// subsetfin_freebase_type_sortMid();
		mapping();
		// D.p("joke");

		// how many classes there are ?

	}

}
