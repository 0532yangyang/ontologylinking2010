package freebase.jointmatch13icsemi;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;

public class Evaluation {

	static void calc_groud_truth() {
		int x = 0;
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] a : nr.seedInstances) {
				D.p(x, nr.relation_name, a[0], a[1]);
				x++;
			}
		}
	}

	static void calc_groud_truth2() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_jointclause + ".1");
		List<String[]>all = dr.readAll();
		{
			Set<String> s = new HashSet<String>();
			for(String []l:all){
				if (l[0].equals("relation_instance")) {
					String key = l[6].split("::")[0] + "\t" + l[7].split("::")[0] + "\t" + l[8].split("::")[0];
					s.add(key);
				}
			}
			D.p("true relation instances", s.size());
		}
		{
			Set<String> s = new HashSet<String>();
			for(String []l:all){
				if (l[0].equals("static_type")) {
					String key = l[4].split("::")[0];
					s.add(key);
				}
			}
			D.p("types", s.size());
		}
		
		{
			Set<String> s = new HashSet<String>();
			for(String []l:all){
				if (l[0].equals("static_entity")) {
					String key = l[4].split("::")[0];
					s.add(key);
				}
			}
			D.p("entity", s.size());
		}
		
		{
			Set<String> s = new HashSet<String>();
			for(String []l:all){
				if (l[0].equals("type_instance")) {
					String key = l[5].split("::")[0] + "\t" + l[6].split("::")[0];
					s.add(key);
				}
			}
			D.p("type instance", s.size());
		}
	}

	public static void main(String[] args) throws IOException {
		calc_groud_truth2();
	}
}
