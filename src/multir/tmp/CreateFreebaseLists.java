package multir.tmp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class CreateFreebaseLists {

	static String input = "/projects/pardosa/data10/raphaelh/freebase/freebase-datadump-quadruples.tsv";
	
	static String dir = "/projects/pardosa/data14/raphaelh/t/ftexp";
	
	static String[] lists = {
		"/location/citytown",
		"/location/country",
		"/location/region",
		"/location/administrative_division",
		"/location/us_state",
		"/architecture/building",/*  
		"/organization/organization", // new relations
		"/business/business_operation",
		"/business/employer",
		"/people/person" */ };
	
		
	
	public static void main(String[] args) throws IOException {
	
		{
			DelimitedWriter w = new DelimitedWriter(dir + "/freebase-lists2");
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(input), "utf-8"));
			
			String line = null;
			while ((line = r.readLine())!=null) {
				String[] t = line.split("\t");
				if (!t[1].equals("/type/object/type")) continue;
				
				boolean found = false;
				for (int i=0; i < lists.length; i++)
					if (lists[i].equals(t[2])) found = true;
				
				if (!found) continue;
				
				w.write(t[0], t[2]);
			}
			r.close();
			w.close();
		}
		
		HashMap<String,List<String>> m =new HashMap<String,List<String>>();
		{
			DelimitedReader r = new DelimitedReader(dir + "/freebase-lists2");
			String[] t = null;
			while ((t = r.read())!= null) {
				List<String> l = m.get(t[0]);
				if (l == null) {
					l = new ArrayList<String>(1);
					m.put(t[0], l);
				}
				l.add(t[1]);
			}
			r.close();
		}
		
		{
			DelimitedWriter w = new DelimitedWriter(dir + "/freebase-lists-names2");
			DelimitedReader r = new DelimitedReader(dir + "/../raw/freebase-names");
			String[] t = null;
			while ((t = r.read())!= null) {
				List<String> l = m.get(t[0]);
				if (l != null) {
					for (String s : l)
						w.write(t[1], s);
				}
			}
			r.close();
			w.close();
		}
		
	}
	
}
