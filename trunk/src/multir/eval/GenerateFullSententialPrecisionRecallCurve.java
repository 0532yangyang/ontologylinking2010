package multir.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerateFullSententialPrecisionRecallCurve {

	static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_X";
	//static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_owndataX";

	static String resultsFile = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/results";
	//static String resultsFile = "/projects/pardosa/data14/raphaelh/t/newexp-neg10-min1-0506/results";
	static String mappingFile = "/projects/pardosa/data14/raphaelh/t/newexp-neg10-min1-0506/mapping";
	
	static String curveFile = "/projects/pardosa/data16/raphaelh/camera/curveFull";

	static class Example {
		String arg1;
		String arg2;
		int mentionID;
		String predRelation;
		double predScore;
		int rank;
	}
	
	static class Label {
		String relation;
		boolean tf;
		String name1;
		String name2;
		String sentence;
	}

	public static void run(String labelsFile, String exp, String[] targets) throws IOException {
		targetsSet = new HashSet<String>();
		for (String t : targets) targetsSet.add(t);
		GenerateFullSententialPrecisionRecallCurve.labelsFile = labelsFile;
		GenerateFullSententialPrecisionRecallCurve.resultsFile = "/projects/pardosa/data14/raphaelh/t" + exp + "/results";
		GenerateFullSententialPrecisionRecallCurve.mappingFile = "/projects/pardosa/data14/raphaelh/t" + exp + "/mapping";
		GenerateFullSententialPrecisionRecallCurve.curveFile = "/projects/pardosa/data14/raphaelh/t" + exp + "/curveFull";
		main(new String[0]);
	}
	
	static Set<String> targetsSet;
	
	public static void main(String[] args) throws IOException {
		
		Map<Integer,String> relID2rel = new HashMap<Integer,String>();
		/*
		// read relID to rel mapping
		Mappings mapping = new Mappings();
		mapping.read(mappingFile);		
		for (Map.Entry<String,Integer> e : mapping.getRel2RelID().entrySet())
			relID2rel.put(e.getValue(), e.getKey());
		*/
		
		List<Example> predictions = new ArrayList<Example>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(resultsFile), "utf-8"));
			String[] h = r.readLine().split(" ");
			for (int i=0; i < h.length; i++) relID2rel.put(i, h[i]);
			
			String l = null;
			while ((l = r.readLine())!= null) {
				String[] c = l.split("\t");

				//final String[] rels = c[3].split(" ");
				final String[] scores = c[5].split(" ");
				Integer[] pos = new Integer[scores.length];
				for (int i=0; i < pos.length; i++) pos[i] = i;
				Arrays.sort(pos, new Comparator<Integer>() {
					public int compare(Integer i1, Integer i2) {
						double d1 = Double.parseDouble(scores[i1]);
						double d2 = Double.parseDouble(scores[i2]);
						if (d1 > d2) return -1; else return 1;
					}
				});
				
				
				for (int i=0; i < scores.length; i++) {
					Example e = new Example();
					e.arg1 = c[0];
					e.arg2 = c[1];
					e.mentionID = Integer.parseInt(c[2]);
					
					int p = pos[i];
					//e.predRelation = rels[p];
					e.predRelation = relID2rel.get(p);
					//e.predScore = Double.parseDouble(scores[i]) - Double.parseDouble(scores[0]);
					e.predScore = Double.parseDouble(scores[p]);
					e.rank = i;
					
					// HACK; diff rel - NA
					//e.predScore = Double.parseDouble(scores[p]) - Double.parseDouble(scores[0]);
					//e.rank = 0;
					//if (e.rank == 0)
					predictions.add(e);
				}
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
						label.relation.equals("/location/administrative_division/country") ||
						label.relation.equals("/film/film/featured_film_locations")) 
					continue;
				
				// only measure performance on location/contains
				//if (!label.relation.equals("/location/location/contains")) continue;
				/*
				if (!label.relation.equals("/location/location/contains") &&
						!label.relation.equals("/people/person/nationality") &&
						!label.relation.equals("/location/neighborhood/neighborhood_of") &&
						!label.relation.equals("/people/person/children"))
					continue;
				*/
				//if (!targetsSet.contains(label.relation)) continue;
				
				
				label.tf = c[2].equals("y") || c[2].equals("indirect");
				label.name1 = c[3];
				label.name2 = c[4];
				label.sentence = c[8];
				
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
				if (e1.rank < e2.rank) return -1; 
				else if (e1.rank > e2.rank)	return 1;
				else 
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
		double lastPrecision = -1, lastRecall = -1;
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
				if (precision != lastPrecision || recall != lastRecall) {
					curve.add(new double[] { precision, recall } );
					lastPrecision = precision;
					lastRecall = recall;
				}
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
		
	}
}
