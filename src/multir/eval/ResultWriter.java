package multir.eval;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.FullInferenceTest;
import multir.learning.algorithm.Parse;
import multir.learning.algorithm.Scorer;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;
import multir.preprocess.Mappings;

public class ResultWriter {

	// force single best only
	static boolean singleBestOnly = false; //false;
	
	
	public static void eval(String mappingFile, Dataset test, CRFParameters params,
			PrintStream ps) throws IOException {
		
		// need mapping from relIDs to rels
		Mappings mapping = new Mappings();
		mapping.read(mappingFile);
		Map<Integer,String> relID2rel = new HashMap<Integer,String>();
		for (Map.Entry<String,Integer> e : mapping.getRel2RelID().entrySet())
			relID2rel.put(e.getValue(), e.getKey());
		
		System.out.println("eval");
		Scorer scorer = new Scorer();

		int multP = 0, multT = 0;

		
		StringBuilder sb1 = new StringBuilder();
		for (int i=0; i < mapping.numRelations(); i++)
			sb1.append(relID2rel.get(i) + " ");
		ps.append(sb1.toString() + "\n");
		
		MILDocument doc = new MILDocument();
		test.reset();		
		while (test.next(doc)) {
			Parse parse = FullInferenceTest.infer(doc, scorer, params);
			int[] Yt = doc.Y;
			int[] Yp = parse.Y;
			
			if (Yp.length > 1 && singleBestOnly) {
				int max = 0;
				for (int i=1; i < Yp.length; i++)
					if (parse.scores[Yp[i]] > parse.scores[Yp[max]]) max = i;
				Yp = new int[] { Yp[max] };
				
				// set sentence-level predictions
				for (int m = 0; m < doc.numMentions; m++) {
					if (parse.Z[m] != 0 && parse.Z[m] != max) {
						if (parse.allScores[m][0] > parse.allScores[m][max]) parse.Z[m] = 0;
						else parse.Z[m] = max;
					}
				}
					
				
			}
			

			if (Yt.length > 1) multT++;
			if (Yp.length > 1) multP++;

			/*
			for (int m = 0; m < doc.numMentions; m++)
				ps.append(doc.arg1 + "\t" + doc.arg2 + "\t" + m + "\t" + relID2rel.get(parse.Z[m]) + "\t" + parse.score + "\n");
				//ps.append(doc.arg1 + "\t" + doc.arg2 + "\t" + doc.mentionIDs[m] + "\t" + parse.Z[m] +"\n");
			*/
			
			for (int m = 0; m < doc.numMentions; m++) {
				
				StringBuilder sb2 = new StringBuilder();
				for (int i=0; i < parse.allScores[m].length; i++)
					sb2.append(parse.allScores[m][i] + " ");
				ps.append(doc.arg1 + "\t" + doc.arg2 + "\t" + m + "\t" + relID2rel.get(parse.Z[m]) + "\t" + parse.score + "\t" + sb2.toString() + "\n");
			}
		}
		
		System.out.println("pred  " + multP);
		System.out.println("true  " + multT);
		System.out.println("total " + test.numDocs());
	}
}
