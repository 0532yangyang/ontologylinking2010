package multir.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multir.eval.GenerateSententialPrecisionRecallCurve3.Example;
import multir.eval.GenerateSententialPrecisionRecallCurve3.Label;

public class GenerateSententialPrecisionRecallCurve {

	//static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_X";
	static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_owndataX";

	static String results1File = "/projects/pardosa/data14/raphaelh/t/newexp-neg5-min5/results";
	static String results2File = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/results";
	static String results3File = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/results";

	static String curveFile = "/projects/pardosa/data16/raphaelh/camera/curve1";
	static String sentencesFile = "/projects/pardosa/data16/raphaelh/camera/curve1_sentences";
	
	static boolean indirect = true;

	static class Example {
		String arg1;
		String arg2;
		int mentionID;
		String predRelation;
		double predScore;
		boolean correct = false;
	}
	
	static class Label {
		String relation;
		boolean tf;
		String name1;
		String name2;
		String sentence;
	}
	
	public static void main(String[] args) throws IOException {

		// put results into map
		// guid1, guid2, mtnID -> ex
		//Map<String,Example> predictions = new HashMap<String,Example>();
		List<Example> predictions = new ArrayList<Example>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(results1File), "utf-8"));
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");

				Example e = new Example();
				e.arg1 = c[0];
				e.arg2 = c[1];
				e.mentionID = Integer.parseInt(c[2]);
				e.predRelation = c[3];
				e.predScore = Double.parseDouble(c[4]);
				//predictions.put(e.arg1 + "\t" + e.arg2 + "\t" + e.mentionID, e);
				predictions.add(e);
			}
			r.close();
		}
		
		//List<Example> labeled = new ArrayList<Example>();
		Map<String,List<Label>> labels = new HashMap<String,List<Label>>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(labelsFile), "utf-8"));
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");
				String key = c[6] + "\t" + c[7] + "\t" + c[0]; // arg1, arg2, mentionID
				Label label = new Label();
				label.relation = c[5]; 
				if (label.relation.equals("/location/country/administrative_divisions") || 
						label.relation.equals("/location/administrative_division/country")) 
					continue;
				label.name1 = c[3];
				label.name2 = c[4];
				label.sentence = c[8];

				label.tf = c[2].equals("y") || c[2].equals("indirect");
				
				List<Label> ll = labels.get(key);
				if (ll == null) {
					ll = new ArrayList<Label>();
					labels.put(key, ll);
				}
				ll.add(label);
			}
			r.close();
		}

		// sort predictions by decreasing score
		Collections.sort(predictions, new Comparator<Example>() {
			public int compare(Example e1, Example e2) {
				if (e1.predScore > e2.predScore) return -1; else return 1;
			}
		});

		// max recall
		int MAX_TP = 0;
		for (List<Label> ll : labels.values()) {
			for (Label l : ll)
				if (!l.relation.equals("NA") && l.tf) MAX_TP++;
		}
		
		
		List<double[]> curve = new ArrayList<double[]>();
		
		int TP = 0, FP = 0, FN = 0;
		for (Example e : predictions) {
			String key = e.arg1 + "\t" + e.arg2 + "\t" + e.mentionID;
			List<Label> ll = labels.get(key);
			if (ll != null) {
				for (Label l : ll) {
					if (l.relation.equals(e.predRelation)) {
						if (l.tf) TP++;
						else FP++;
					} else {
						if (l.tf) FN++; // && e.predRelation.equals("NA")) FN++;
						//else TN++;
					}
				}
				double precision = TP / (double)(TP + FP);
				double recall = TP / (double)(MAX_TP);
				curve.add(new double[] { precision, recall } );				
			}
		}
		
		
		// print to file
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(curveFile)));
			
			for (double[] d : curve) {
				w.write(d[1] + "\t" + d[0] + "\n");
			}
			w.close();
		}
		
		// print the most confident predictions
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sentencesFile), "utf-8"));
			for (Example e : predictions) {
				String key = e.arg1 + "\t" + e.arg2 + "\t" + e.mentionID;
				List<Label> ll = labels.get(key);
				if (ll != null) {
					StringBuilder sb = new StringBuilder();
					for (Label l : ll) {
						sb.append(l.tf + ":" + l.relation + ", ");
					}
					Label l1 = ll.get(0);
					w.write(l1.name1 + "\t" + l1.name2 + "\t" + e.predRelation + "\t" + sb.toString() + "\t" + l1.sentence + "\t" + e.predScore + "\n");
				}
			}
			w.close();
		}

	}
}
