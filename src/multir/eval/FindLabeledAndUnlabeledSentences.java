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

public class FindLabeledAndUnlabeledSentences {

	
	static String in1 = "/projects/pardosa/data16/raphaelh/camera/bypredictedrelation_baseline";
	static String in2 = "/projects/pardosa/data16/raphaelh/camera/bypredictedrelation_X2";
	
	static String out1 = "/projects/pardosa/data16/raphaelh/camera/bypredictedrelation_baseline2";
	
	public static void main(String[] args) throws IOException {
		
		Map<String, String> m = new HashMap<String,String>();
		{
			BufferedReader r = new BufferedReader(
					new InputStreamReader(new FileInputStream(in2), "utf-8"));
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
				String key = c[0] + "\t" + c[5] + "\t" + c[6] + "\t" + c[7];
				m.put(key, c[2]);
			}
			r.close();
		}
		
		{
			BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(out1), "utf-8"));
			BufferedReader r = new BufferedReader(
					new InputStreamReader(new FileInputStream(in1), "utf-8"));
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
				String key = c[0] + "\t" + c[5] + "\t" + c[6] + "\t" + c[7];
				String v= m.get(key);
				if (v != null) c[2] = v;
				w.write(c[0] + "\t" + c[1] + "\t" + c[2] + "\t" + c[3] + "\t" + c[4]
                     +"\t" + c[5] + "\t" + c[6] + "\t" + c[7] + "\t" + c[8] + "\t" + c[9] + "\n");
			}
			r.close();
			w.close();
		}
	}
}
