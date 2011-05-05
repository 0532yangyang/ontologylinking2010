package multir.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelDistribution {

	static String labels1File = "/projects/pardosa/data16/raphaelh/camera/byrelation_X";
	static String labels2File = "/projects/pardosa/data16/raphaelh/camera/byrelation_owndataX";
	
	public static void main(String[] args) throws IOException {
		
		final HashMap<String, int[]> m1 = new HashMap<String,int[]>();
		final HashMap<String, int[]> m2 = new HashMap<String,int[]>();
		
		populate(labels1File, m1);
		populate(labels2File, m2);
		
		int totalPos1 = 0, totalPos2 = 0;
		int totalAll1 = 0, totalAll2 = 0;
		for (Map.Entry<String,int[]> e : m1.entrySet()) {
			totalPos1 += e.getValue()[0];
			totalAll1 += e.getValue()[1];
		}
		for (Map.Entry<String,int[]> e : m2.entrySet()) {
			totalPos2 += e.getValue()[0];
			totalAll2 += e.getValue()[1];
		}
		
		Set<String> rels = new HashSet<String>();
		rels.addAll(m1.keySet());
		rels.addAll(m2.keySet());
		List<String> l = new ArrayList<String>(rels);
		Collections.sort(l, new Comparator<String>() {
			public int compare(String s1, String s2) {
				int[] i1 = m1.get(s1);
				int[] i2 = m1.get(s2);
				if (i1 == null) return 1;
				else if (i2 == null) return -1;
				else if (i1 != null && i2 != null)
					return i2[0] - i1[0];
				return 1;
			}
		});
		
		for (String relation : l) {
			System.out.println(relation);
			
			int[] i1 = m1.get(relation);
			int[] i2 = m2.get(relation);
						
			System.out.println("  umass  " + ((i1 == null)? "0" : (i1[0] / (double)totalPos1)) + "\t" +
					((i1 == null)? "0" : (i1[1])));
			System.out.println("  own    " + ((i2 == null)? "0" : (i2[0] / (double)totalPos2)) + "\t" + 
					((i2 == null)? "0" : (i2[1])));
		}
		
	}
	
	static void populate(String labelsFile, Map<String,int[]> m) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader
				(new FileInputStream(labelsFile), "utf-8"));
		String l = null;
		while ((l = r.readLine())!= null) {
			String[] c = l.split("\t");
			String relation = c[5]; 
			if (relation.equals("/location/country/administrative_divisions") || 
					relation.equals("/location/administrative_division/country") ||
					relation.equals("/film/film/featured_film_locations")) 
				; //continue;
			boolean tf = c[2].equals("y") || c[2].equals("indirect");
			
			int[] i = m.get(relation);
			if (i == null) {
				i = new int[2];
				m.put(relation, i);
			}
			if (tf) i[0]++;
			i[1]++;
		}
		r.close();
	}
	
}
