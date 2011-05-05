package multir.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Eval {

	static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_X";
	//static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_owndataX";

	//static String results1File = "/projects/pardosa/data14/raphaelh/t/newexp-neg5-min5/results";
	//static String results1File = "/projects/pardosa/data14/raphaelh/t/newexp-neg10-min10/results";
	//static String results1File = "/projects/pardosa/data14/raphaelh/t/newexp-neg10-min5/results";
	//static String results1File = "/projects/pardosa/data14/raphaelh/t/newexp.min5.05-06.10/results";
	//static String results1File = "/projects/pardosa/data14/raphaelh/t/newexp.min10.87-06.1/results";
	//static String results1File = "/projects/pardosa/data14/raphaelh/t/results";
	//static String results1File = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/results2";
	static String results1File = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/results";
	//static String results1File = "/projects/pardosa/data16/raphaelh/camera/congle_sen2";

	static boolean indirect = true;

	public static void main(String[] args) throws IOException {

		
		// put results into map
		// guid1, guid2, mtnID -> rel
		Map<String,String> results = new HashMap<String,String>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(results1File), "utf-8"));
			r.readLine();
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
				String guid1 = c[0];
				String guid2 = c[1];
				int mntID = Integer.parseInt(c[2]);
				
				String rel = c[3];
				results.put(guid1 + "\t" + guid2 + "\t" + mntID, rel);
			}
			r.close();
		}
		
		// read labels
		// relation --> (guid1, guid2, mntID -> y/n/indirect)
		Map<String,Map<String,String>> labels = new HashMap<String,Map<String,String>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(labelsFile), "utf-8"));
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
				String relation = c[5];
				String guid1 = c[6];
				String guid2 = c[7];
				int mntID = Integer.parseInt(c[0]);
				String label = c[2];
				
				if (relation.equals("/location/country/administrative_divisions") || 
						relation.equals("/location/administrative_division/country") ||
						relation.equals("/film/film/featured_film_locations")) 
					; //continue;
				
				// only measure performance on location/contains
				//if (!relation.equals("/location/location/contains")) continue;

				
				Map<String,String> m = labels.get(relation);
				if (m == null) {
					m = new HashMap<String,String>();
					labels.put(relation, m);
				}
				m.put(guid1 + "\t" + guid2 + "\t" + mntID, label);
			}
			r.close();
		}
		
		// sort by their number of true labels
		List<Map.Entry<String,Map<String,String>>> l = new
			ArrayList<Map.Entry<String,Map<String,String>>>();
		l.addAll(labels.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String,Map<String,String>>>() {
			public int compare(Map.Entry<String,Map<String,String>> e1, Map.Entry<String,Map<String,String>> e2) {
				int n1 = 0;
				for (Map.Entry<String,String> e : e1.getValue().entrySet())
					if (e.getValue().equals("y") || (indirect && e.getValue().equals("indirect"))) n1++;
				
				int n2 = 0;
				for (Map.Entry<String,String> e : e2.getValue().entrySet())
					if (e.getValue().equals("y") || (indirect && e.getValue().equals("indirect"))) n2++;
				return n2 - n1;
			}
		});
		
		
		// evaluate
		for (Map.Entry<String,Map<String,String>> e : l) { //labels.entrySet()) {
			System.out.println(e.getKey());
			String fbRel = e.getKey();
			
			int TP = 0, FP = 0, TN = 0, FN = 0;
			
			int numFbIsTrue = 0;
			
			Map<String,String> m = e.getValue();
			for (Map.Entry<String,String> c : m.entrySet()) {
				//System.out.println("get result for key " + c.getKey());
				String prRel = results.get(c.getKey());
				boolean fbIsTrue = c.getValue().equals("y") || (indirect && c.getValue().equals("indirect"));
				if (fbRel.equals(prRel) && fbIsTrue) TP++;
				else if (fbRel.equals(prRel) && !fbIsTrue) FP++;
				else if (!fbRel.equals(prRel) && fbIsTrue) FN++;
				else if (!fbRel.equals(prRel) && !fbIsTrue) TN++;
				if (fbIsTrue) numFbIsTrue++;
			}
			if (TP + FP == 0)
				System.out.println("  precision\tNA (no positive predictions)");
			else
				System.out.println("  precision\t" + (double)TP / (double)(TP + FP));

			if (TP + FN == 0) 
				System.out.println("  recall\tNA (no positive labels in test data)");
			else
				System.out.println("  recall\t" + (double)TP / (double)(TP + FN));
			System.out.println("  # fb annot \t" + m.size() + " (fb annotation precision " + (numFbIsTrue / (double)m.size()) + ")");
		}
	}
}
