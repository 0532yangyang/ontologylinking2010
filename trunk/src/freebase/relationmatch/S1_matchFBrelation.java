package freebase.relationmatch;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S1_matchFBrelation {
	public static void subsetFBgivenCandidate(){
		try {
			List<String[]>all = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean)).readAll();
			DelimitedWriter dw = new DelimitedWriter(Main.file_fbdump_subset);
			HashSet<String>used = new HashSet<String>();
			for(String []a: all){
				used.add(a[1]);
			}
			D.p(all);
			
			DelimitedReader dr = new DelimitedReader(Main.fbdump);
			String []l;
			while((l = dr.read())!=null){
				String mid = l[0];
				if(used.contains(mid)){
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
	
	public static void main(String []args){
		subsetFBgivenCandidate();
	}
}
