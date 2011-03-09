package freebase.typematch;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

import percept.util.delimited.Sort;

import javatools.administrative.D;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class S0_join_MidWikiurl_wpArticleidTitle {
	static HashMap<String,String> wptitle2mid = new HashMap<String,String>();
	static void doit(){
		
		try {
			DelimitedReader dr = new DelimitedReader("/projects/pardosa/s5/clzhang/ontologylink/mid2Wikiurl");
			String []line;
			while((line = dr.read())!=null){
				if(wptitle2mid.containsKey(line[1]))D.p(line[1]);
				
				wptitle2mid.put(line[1],line[0]);
			}
			dr.close();
			
			dr = new DelimitedReader("/projects/pardosa/s5/clzhang/tmp/wp/enwiki_title");
			DelimitedWriter dw = new DelimitedWriter(Main.fout_mid_artid);
			while((line = dr.read())!=null){
				String title = line[1].trim().replace(" ", "_");
				if(wptitle2mid.containsKey(title)){
					dw.write(wptitle2mid.get(title),line[0],title);
				}
			}
			dw.close();
			dr.close();
			Sort.sort(Main.fout_mid_artid, Main.fout_mid_artid_sbmid,Main.dir,new Comparator<String[]>(){

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
	public static void main(String []args){
		doit();
	}
}
