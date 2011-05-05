package multir.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class EvalPredictions {

	static String input = "/projects/pardosa/data16/raphaelh/camera/bypredictedrelation_X";
	//static String input = "/projects/pardosa/data16/raphaelh/camera/bypredictedrelation_baseline2";

	public static void main(String[] args) throws IOException {
		
		Map<String,int[]> m = new HashMap<String,int[]>();
		
		BufferedReader r = new BufferedReader(new InputStreamReader
				(new FileInputStream(input), "utf-8"));
		String l = null;
		while ((l = r.readLine())!= null) {
			String[] c = l.split("\t");
			String rel = c[5];
			boolean tf = c[2].equals("y");
			int[] i = m.get(rel);
			if (i == null) {
				i = new int[] { 0, 0 };
				m.put(rel, i);
			}
			if (tf) i[0]++;
			i[1]++;
		}
		r.close();
		
		for (Map.Entry<String,int[]> e : m.entrySet()) {
			System.out.println(e.getKey());
			
			int[] i = e.getValue();
			System.out.println("  " + (i[0]/(double)i[1]));
			System.out.println("  " + i[1]);
		}
	}
}
