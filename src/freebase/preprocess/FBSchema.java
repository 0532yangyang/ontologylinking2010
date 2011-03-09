package freebase.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javatools.administrative.D;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;

public class FBSchema {

	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink/";
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/typematch/";
	static String file_schemaincluderaw = dir + "/freebaseschema_includetype_raw";
	static String fin_quad = pdir + "/freebasedump.sort";
	static String fout_filterfromquad = dir + "/freebaseschema_include_temp1";

	/** step2 */
	static String dir_fbschema = pdir + "/freebaseschema";
	static String fout_fbnicelookingtype = dir + "/freebaseschema_niceLookingTypes";
	static String fout_fbtype = dir+"/fb_nice_types";
	static String fout_fbmidtypeargname = dir+"/fb_mid_type_argname";
	
	public static void step1() {

		HashSet<String> possibleTypeMid = new HashSet<String>();
		// String file_midname =
		// "/projects/pardosa/s5/clzhang/ontologylink/fbnamealias";
		try {
			DelimitedReader dr = new DelimitedReader(file_schemaincluderaw);
			String[] line;
			while ((line = dr.read()) != null) {
				possibleTypeMid.add(line[0]);
				possibleTypeMid.add(line[2]);
			}
			dr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			DelimitedReader dr = new DelimitedReader(fin_quad);
			DelimitedWriter dw = new DelimitedWriter(fout_filterfromquad);
			String[] line;
			while ((line = dr.read()) != null) {
				if (possibleTypeMid.contains(line[0]) && line[1].equals("/type/object/key")) {
					dw.write(line);
				}
			}
			dr.close();
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void step2() {
		try {
			DelimitedWriter dw_fbtype = new DelimitedWriter(fout_fbtype);
			DelimitedWriter dw_fbmidtype = new DelimitedWriter(fout_fbmidtypeargname+".temp");
			File DIR_fbschema = new File(dir_fbschema);
			String []domains = DIR_fbschema.list();
			D.p(domains.length);
			for(int i = 0;i<domains.length;i++){
				String d = domains[i];
				File DIR_domain = new File(dir_fbschema+"/"+d);
				String types[] = DIR_domain.list();
				for(String t:types){
					//D.p(d+"/"+t);
					if(!d.equals("base")){
						String type_file = "/"+d+"/"+t;
						String typename = "";
						if(type_file.endsWith(".tsv")){
							typename = type_file.replace(".tsv","");
							{
								DelimitedReader dr = new DelimitedReader(dir_fbschema+"/"+type_file);
								String []line = dr.read();
								while((line = dr.read())!=null){
									if(line.length<2)continue;
									String argname = line[0];
									String mid = line[1];
									dw_fbmidtype.write(mid,typename,argname);
								}
								dr.close();
							}
						}
					}
				}
				
			}
			dw_fbtype.close();
			dw_fbmidtype.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Sort.sort(fout_fbmidtypeargname+".temp",fout_fbmidtypeargname,dir, new Comparator<String[]>(){

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[0].compareTo(o2[0]);
				}
				
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// static HashMap<String,String>mid2name = new
	// HashMap<String,String>(10000000);
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/**
		 * Freebase quad dump has type structure in
		 * /freebase/type_hints/included_types Use unix command grep to get them
		 * out, we get /projects/pardosa/s5/clzhang/ontologylink/typematch/
		 * freebaseschema_includetype_raw The mid1 and mid2 of these tuples are
		 * unknown, so we filter quad dump with these mid and get
		 * freebaseschema_includetype_temp1
		 */
		//step1();

		/**
		 * freebaseschema contains all NICE LOOKING types that we need, Get them
		 * out!!! put them in freebaseschema_nicelooking_types
		 * */
		step2();

	}

}
