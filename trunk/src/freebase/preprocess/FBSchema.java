package freebase.preprocess;

import java.util.HashMap;
import java.util.HashSet;

import javatools.administrative.D;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;



public class FBSchema {

	/**
	 * @param args
	 */
//	static HashMap<String,String>mid2name = new HashMap<String,String>(10000000);
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String file_schemaincluderaw = "/projects/pardosa/s5/clzhang/ontologylink/freebaseschema_includetype_raw";
		HashSet<String> possibleTypeMid = new HashSet<String>();
		//String file_midname = "/projects/pardosa/s5/clzhang/ontologylink/fbnamealias";
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
		
		String fin_quad = "/projects/pardosa/s5/clzhang/ontologylink/freebasedump.sort";
		String fout_schemainclude = "/projects/pardosa/s5/clzhang/ontologylink/freebaseschema_includetype";
		try {
			DelimitedReader dr = new DelimitedReader(fin_quad);
			DelimitedWriter dw =new DelimitedWriter(fout_schemainclude);
			String[] line;
			while ((line = dr.read())	 != null) {
				if(possibleTypeMid.contains(line[0]) && line[1].equals("/type/object/key")){
					dw.write(line);
				}
			}
			dr.close();
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
