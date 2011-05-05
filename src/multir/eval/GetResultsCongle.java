package multir.eval;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class GetResultsCongle {

	static String input0 = "/projects/pardosa/s5/clzhang/tmp/seb/acl_predictions.txt";
	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/testPositive.pb";
	static String input2 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/testNegative.pb";
	static String input3 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";

	static String output1 = "/projects/pardosa/data16/raphaelh/camera/congle_agg";
	static String output2 = "/projects/pardosa/data16/raphaelh/camera/congle_sen";
	static String output3 = "/projects/pardosa/data16/raphaelh/camera/congle_sen2";
	
	public static void main(String[] args) throws IOException {

		List<Relation> data = new ArrayList<Relation>();
		{
			InputStream is1 = new BufferedInputStream(new FileInputStream(input1));
			Relation r1 = null;
		    while ((r1 = Relation.parseDelimitedFrom(is1))!=null) data.add(r1);
		    is1.close();
			InputStream is2 = new BufferedInputStream(new FileInputStream(input2));
			Relation r2 = null;
		    while ((r2 = Relation.parseDelimitedFrom(is2))!=null) data.add(r2);
		    is2.close();
		}

		List<Prediction> preds = new ArrayList<Prediction>(); 
		{
			BufferedReader r = new BufferedReader(new FileReader(input0));
			String l = null;
			Prediction pred = null;
			while ((l = r.readLine()) != null) {
				if (!l.startsWith("==")) {
					// start of a relation
					if (pred != null) { preds.add(pred); }
					pred = new Prediction();
					StringTokenizer st = new StringTokenizer(l);
					if (st.countTokens() >= 5) {
						pred.srcId = st.nextToken();
						pred.dstId = st.nextToken();
					}
					pred.pred = st.nextToken();
					pred.truth = st.nextToken();
					pred.score = Double.parseDouble(st.nextToken());
				} else {
					StringTokenizer st = new StringTokenizer(l);
					MentionPrediction mp = new MentionPrediction();
					if (l.charAt(2) == '\t') {
						st.nextToken();
						mp.pred = st.nextToken().equals("Yes") ? pred.pred
								: "NA";
						mp.score = Double.parseDouble(st.nextToken());
					} else {
						mp.pred = st.nextToken().equals("==Yes:") ? pred.pred
								: "NA";
					}
					pred.mentions.add(mp);
				}
			}
			preds.add(pred);
			r.close();
		}
		
		/*
		// output Raphael style
		DelimitedWriter dw1 = new DelimitedWriter(output1);
		DelimitedWriter dw2 = new DelimitedWriter(output2);
		Iterator<Relation> it = data.iterator();
		for (Prediction p : preds) {
			Relation r = it.next();

			dw1.write(p.srcId, p.dstId, p.pred, p.score);
			System.out.println(p.score);
			boolean threshold = p.score > 20;
			for (int i = 0; i < p.mentions.size(); i++) {
				String sen = r.getMention(i).getSentence();
				String fts = featuresAsString(r.getMention(i));
				MentionPrediction mp = p.mentions.get(i);
				dw2.write(p.srcId, p.dstId, i, mp.pred, p.score, sen, fts);
			}
		}
		dw2.close();
		dw1.close();
		*/
		
		
		
		Map<String,MentionPrediction> map = new HashMap<String,MentionPrediction>();
		Iterator<Relation> it = data.iterator();
		for (Prediction p : preds) {
			Relation r = it.next();
			for (int i = 0; i < p.mentions.size(); i++) {
				//String sen = r.getMention(i).getSentence();
				String fts = featuresAsString(r.getMention(i));
				MentionPrediction mp = p.mentions.get(i);
				mp.p = p;
				String key = p.srcId + "\t" + p.dstId + "\t" + fts;
				map.put(key, mp);
				//dw2.write(p.srcId, p.dstId, i, mp.pred, p.score, sen, fts);
			}
		}
		
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output3)));
			InputStream is = new BufferedInputStream(new FileInputStream(input3));
			Relation r = null;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	
		    	for (int m = 0; m < r.getMentionCount(); m++) {
		    		RelationMentionRef rmf = r.getMention(m);
		    		String fts = featuresAsString(rmf);		    		
		    		String key = r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + fts;
		    		MentionPrediction mp = map.get(key);
		    		if (mp == null) {
		    			System.out.println("not found");
		    		} else {
		    			w.write(r.getSourceGuid() + "\t" + r.getDestGuid() + "\t" + m + "\t" + mp.pred + "\t" + mp.p.score + "\n");
		    		}
		    	}
		    }
		    is.close();
		    w.close();
		}
	}
	
	static String featuresAsString(RelationMentionRef rmf) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < rmf.getFeatureCount(); i++)
			sb.append(rmf.getFeature(i) + " ");
		return sb.toString();
	}
	
		
	static class Prediction {
		public String dstId, srcId, pred, truth;
		public double score = 0;
		public ArrayList<MentionPrediction> mentions = new ArrayList<MentionPrediction>();
	}

	static class MentionPrediction {
		public String pred = null;
		public double score = 0;
		public Prediction p;
	}
}
