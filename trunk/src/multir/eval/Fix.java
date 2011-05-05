package multir.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import multir.preprocess.Mappings;

public class Fix {

	public static void main(String[] args) throws IOException {
		
		String dir = "/projects/pardosa/data14/raphaelh/t/newexp.min5.05-06.10";
		
		// read relID to rel mapping
		Map<Integer,String> relID2rel = new HashMap<Integer,String>();
		Mappings mapping = new Mappings();
		mapping.read(dir + "/mapping");		
		for (Map.Entry<String,Integer> e : mapping.getRel2RelID().entrySet())
			relID2rel.put(e.getValue(), e.getKey());

		StringBuilder sb1 = new StringBuilder();
		for (int i=0; i < mapping.numRelations(); i++)
			sb1.append(relID2rel.get(i) + " ");
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + "/results2"), "utf-8"));
		w.write(sb1.toString() + "\n");
		
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(dir + "/results"), "utf-8"));
		String l = null;
		while ((l = r.readLine())!= null) {
			String[] c= l.split("\t");
			
			String arg1 = c[0];
			String arg2 = c[1];
			String m = c[2];
			String scores = c[4];
			
			double[] d = toDouble(scores);
			int max = 0;
			for (int i=1; i < d.length; i++)
				if (d[i] > d[max]) max = i;
			
			String rel = relID2rel.get(max);
			String relScore = d[max] + "";
			
			
			w.write(arg1 + "\t" + arg2 + "\t" + m + "\t" + rel + "\t" + relScore + "\t" + scores + "\n");
		}
		w.close();
		r.close();
	}
	
	static double[] toDouble(String scores) {
		String[] c= scores.split(" ");
		double[] d = new double[c.length];
		for (int i=0; i < d.length; i++)
			d[i] = Double.parseDouble(c[i]);
		return d;
	}
}
