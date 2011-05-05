package multir.tmp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import multir.eval.PrecisionRecallCurve;
import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.CollinsTraining;
import multir.learning.algorithm.MILModel;
import multir.learning.data.Dataset;
import multir.learning.data.MemoryDataset;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.CreateSubsetDataset;
import multir.preprocess.Mappings;
import multir.preprocess.WordCount;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class NewFeaturesExp {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	static String input1 = "/projects/pardosa/data16/raphaelh/tmp/ecmlOrigRelationsFull";
	static String input2 = dir + "/raw/freebase-relationIDs";
	static String input3 = dir + "/raw/matches-uniquepairs-relationIDs.sortedByArgsRel";
	/*
	static String input4 = dir + "/raw/matches-pairmentions-relationIDs";
	static String input5 = dir + "/raw/sentences.tokens";
	static String input6 = dir + "/raw/sentences.pos";
	static String input7 = dir + "/raw/sentences.deps";
	static String input8 = dir + "/raw/sentences.ner";
	
	static String output1 = dir + "/data/featurizedData";
	static String output2 = dir + "/data/featurizedData.pb";	
	*/
	static int start2005 = 40962186;
	static int start2006 = 43425267;
	static int start2007 = 45961658;
	static int start2005_2 = 42193727;

	static String exp = "/ftexp";
	
	public static void main(String[] args) throws IOException {
		exp = args[0];
		String name = args[1];
		// step1: create smaller input files (spanning only 05-2, and 06), so that
		// we can iterate more efficiently
		/*
		{
			reduce(dir + "/raw/matches-pairmentions-relationIDs", dir + exp + "/matches-pairmentions-relationIDs");
			reduce(dir + "/raw/sentences.tokens", dir + exp + "/sentences.tokens");
			reduce(dir + "/raw/sentences.pos", dir + exp + "/sentences.pos");
			reduce(dir + "/raw/sentences.deps", dir + exp + "/sentences.deps");
			reduce(dir + "/raw/sentences.ner", dir + exp + "/sentences.ner");
		}
		*/
		/*
		// step2: create new featurized data
		{
			NewFeatures featurizer = new NewFeatures();
			//RelationECML featurizer = new RelationECML();
			
			DelimitedReader isTokens = new DelimitedReader(dir + exp + "/sentences.tokens");
			DelimitedReader isPos = new DelimitedReader(dir + exp + "/sentences.pos"); 
			DelimitedReader isDeps = new DelimitedReader(dir + exp + "/sentences.deps");
			DelimitedReader isNer = new DelimitedReader(dir + exp + "/sentences.ner");

			String[] tTokens = isTokens.read();
			String[] tPos = isPos.read();
			String[] tDeps = isDeps.read();
			String[] tNer = isNer.read();
			
			DelimitedWriter w = new DelimitedWriter(dir + exp + "/featurizedData");
			DelimitedReader r = new DelimitedReader(dir + exp + "/matches-pairmentions-relationIDs");
			String[] t = null;
			int count = 0;
			
			HashMap<String,String> nerm = new HashMap<String,String>();
			int nermSentenceId = -1;
			
			while ((t = r.read())!= null) {				
				int sentenceId = Integer.parseInt(t[0]);
				if (++count % 1000000 == 0) System.out.println(count + "\t" + sentenceId);
				
				//if (sentenceId < startSentenceId ||
				//	sentenceId >= endSentenceId) continue;

				// both arguments must be in Freebase
				if (!t[7].equals("1") || !t[8].equals("1")) continue;
				
				
				// align tokens, postags, deps
				while (tTokens != null && Integer.parseInt(tTokens[0]) < sentenceId) tTokens = isTokens.read();
				while (tPos != null && Integer.parseInt(tPos[0]) < sentenceId) tPos = isPos.read();
				while (tDeps != null && Integer.parseInt(tDeps[0]) < sentenceId) tDeps = isDeps.read();
				
				// check if all iterators are aligned
				if (!(Integer.parseInt(tTokens[0]) == sentenceId &&
					  Integer.parseInt(tPos[0]) == sentenceId &&
					  Integer.parseInt(tDeps[0]) == sentenceId)) {
					System.out.println("iterators not aligned: shouldn't happen!");
					System.exit(-1);
				}

				// align ner
				if (nermSentenceId < sentenceId) {
					nerm.clear();
					
					// ner				
					while (tNer != null && Integer.parseInt(tNer[0]) < sentenceId)
						tNer = isNer.read();
					
					while (tNer != null && Integer.parseInt(tNer[0]) == sentenceId) {
						nerm.put(tNer[1] + " " + tNer[2], tNer[4]);
						tNer = isNer.read();
					}
					nermSentenceId = sentenceId;
				}
				
				// create features
				int[] arg1Pos = new int[] { Integer.parseInt(t[3]), Integer.parseInt(t[4]) };
				int[] arg2Pos = new int[] { Integer.parseInt(t[5]), Integer.parseInt(t[6]) };

				if (nerm.get(arg1Pos[0] + " " + arg1Pos[1]) == null)
					System.out.println("error: " + sentenceId + " " + 
							arg1Pos[0] + " " + arg1Pos[1] + " ");				
				
				// convert to arrays
				String[] tokens = tTokens[1].split(" ");
				String[] pos = tPos[1].split(" ");
				String[] depParentsStr = tDeps[2].split(" ");
				String[] depTypes = tDeps[1].split(" ");
				
				int[] depParents = new int[depParentsStr.length];
				for (int i=0; i < depParents.length; i++)
					depParents[i] = Integer.parseInt(depParentsStr[i]);
				
				//System.out.println(iTokenized.tokens.length + " " + iPostags.postags.length + " ");
				if (tokens.length != pos.length) {
					System.out.println(tTokens[1]);
					//System.out.println(iDeps.deps);
					//System.exit(0);
					System.out.println("IGNORING SENTENCE " + sentenceId);
					continue;
				}
				
				List<String> fts = featurizer.getFeatures(sentenceId, 
						tokens, pos,
						depParents, depTypes,
						arg1Pos, arg2Pos, 
						nerm.get(arg1Pos[0] + " " + arg1Pos[1]),
						nerm.get(arg2Pos[0] + " " + arg2Pos[1]));
				StringBuilder sb = new StringBuilder();
				for (String ft : fts)
					sb.append(ft + "\n");
				
				// arg1, arg2, 
				w.write(t[1], t[2], sentenceId, sb.toString());
			}
			r.close();
			w.close();
			
			isTokens.close();
			isPos.close();
			isDeps.close();
			isNer.close();			
		}
		
		// sort by arg1, arg2
		Sort.sort(dir + exp + "/featurizedData", dir + exp + "/featurizedData" + ".sorted", dir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				return NewFeaturesExp.compare(t1, t2);
			}
		});
		
		// first identify relations we care about
		HashSet<String> relsWeCare = new HashSet<String>();
		{
			DelimitedReader r = new DelimitedReader(input1);
			String[] t = null;
			while ((t = r.read())!= null)
				relsWeCare.add(t[0]);
			r.close();
		}
		
		// read mapping rels to ids
		HashMap<Integer,String> relIdsToRel = new HashMap<Integer,String>();
		HashSet<Integer> relIdsWeCare = new HashSet<Integer>();
		{
			DelimitedReader r = new DelimitedReader(input2);
			String[] t = null;
			while ((t = r.read())!= null) {
				String rel = t[1];
				Integer relId = Integer.parseInt(t[0]);
				if (relsWeCare.contains(rel)) {
					relIdsWeCare.add(relId);
					relIdsToRel.put(relId, rel);
				}
			}
			r.close();
		}

		// keep only arg pairs of manually crafted ontology
		HashMap<String,String> positive = new HashMap<String,String>();
		
		{
			String lastKey = "";
			HashSet<Integer> seen = new HashSet<Integer>();

			DelimitedReader r = new DelimitedReader(input3);				
			String[] t = null;
			while ((t = r.read()) != null) {
				int relId = Integer.parseInt(t[4]);
				if (relIdsWeCare.contains(relId)) {
					String key = t[0] + "\t" + t[1];
					if (key.equals(lastKey)) {
						// don't write if already inside seen
						if (seen.contains(relId)) continue;
					} else {
						lastKey = key;
						seen.clear();
					}
					seen.add(relId);
					String s = positive.get(key);
					if (s == null) {
						s = relIdsToRel.get(relId);
					} else {
						s += "," + relIdsToRel.get(relId);
					}
					positive.put(key, s);
				}
			}
			r.close();
		}
		
		// convert to protobuf
		{
			OutputStream os = new GZIPOutputStream(new BufferedOutputStream
				(new FileOutputStream(dir + exp + "/featurizedData.pb.gz")));

			DelimitedReader r = new DelimitedReader(dir + exp + "/featurizedData" + ".sorted");
			Builder relBuilder = null;
			String[] t = null;
			while ((t = r.read()) != null) {				
				if (relBuilder == null || !relBuilder.getSourceGuid().equals(t[0]) ||
						!relBuilder.getDestGuid().equals(t[1])) {
					if (relBuilder != null) {
						// save old relbuilder
						String rel = positive.get(relBuilder.getSourceGuid() + "\t" + relBuilder.getDestGuid());
						if (rel == null) rel = "NA";
						relBuilder.setRelType(rel);
						Relation nr = relBuilder.build();
						nr.writeDelimitedTo(os);
						//if (!rel.equals("NA"))
						//System.out.println("written " + nr.getSourceGuid() + "\t" + nr.getDestGuid() + "\t" + nr.getMentionCount() + "\t" + nr.getRelType());
					}
					relBuilder = Relation.newBuilder();
					relBuilder.setSourceGuid(t[0]);
					relBuilder.setDestGuid(t[1]);
				}
				//if (relBuilder.getMentionCount() < 100)
				relBuilder.addMention(createMention(t));
			}
			if (relBuilder != null) {
				String rel = positive.get(relBuilder.getSourceGuid() + "\t" + relBuilder.getDestGuid());
				if (rel == null) rel = "NA";
				relBuilder.setRelType(rel);
				Relation nr = relBuilder.build();
				nr.writeDelimitedTo(os);
			}
			r.close();
			os.close();
		}

		// subsets		
		CreateSubsetDataset.createSubset(dir + exp + "/featurizedData.pb.gz", 
										dir + exp + "/train.pb.gz", 0, start2006);
		//dir + exp + "/train.pb.gz", start2005_2, start2006);
		CreateSubsetDataset.createSubset(dir + exp + "/featurizedData.pb.gz", 
				dir + exp + "/test.pb.gz", start2006, start2007);

		/*
		// convert to binary representation
		{
			String mappingFile = dir + exp + "/mapping";
			String modelFile = dir + exp + "/model";
			
			{
				String output1 = dir + exp + "/train";
				ConvertProtobufToMILDocument.convert(dir + exp + "/train.pb.gz", output1, mappingFile, true, true); //was true
			}
			
			{
				String output2 = dir + "/ftexp" + "/test";
				ConvertProtobufToMILDocument.convert(dir + exp + "/test.pb.gz", output2, mappingFile, false, false);
			}
			
			{
				MILModel m = new MILModel();
				Mappings mappings = new Mappings();
				mappings.read(mappingFile);
				m.numRelations = mappings.numRelations();
				m.numFeaturesPerRelation = new int[m.numRelations];
				for (int i=0; i < m.numRelations; i++)
					m.numFeaturesPerRelation[i] = mappings.numFeatures();
				m.write(modelFile);
			}
		}
		
		// learning
		{
			Random random = new Random(7);
			
			MILModel model = new MILModel();
			model.read(dir + exp + "/model");
			
			CollinsTraining ct = new CollinsTraining(model, random);
			//ct.topFalse = new TopFalse(dir + exp + "/topFalse");
			
			Dataset train = new MemoryDataset(random, dir + exp + "/train");
			Dataset test = new MemoryDataset(random, dir + exp + "/test");
	
			System.out.println("starting training");
			CRFParameters params = ct.train(train);
			
			PrintStream ps = new PrintStream(dir + exp + "/results");		
			PrecisionRecallCurve.eval(test, params, ps);
		}
		*/
		/*
		{
			DelimitedWriter w = new DelimitedWriter(dir + exp + "/fts1.gz", "utf-8", true);
			
		    InputStream is = new GZIPInputStream(new BufferedInputStream
		    		(new FileInputStream(dir + exp + "/train.pb.gz")));
		    Relation r = null;
		    
		    int count = 0;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
		    	if (++count % 10000 == 0) System.out.println(count);
		    	
		    	for (int m = 0; m < r.getMentionCount(); m++) {
		    		RelationMentionRef rmf = r.getMention(m);
		    		for (int f = 0; f < rmf.getFeatureCount(); f++) {
		    			//w.write(rmf.getFeature(f));
		    			//w.write('\n');
		    			w.write(rmf.getFeature(f));
		    		}
		    	}
		    }
		    w.close();
		    is.close();
		}
		
		WordCount.count(dir + exp + "/fts1.gz", dir + exp + "/fts1.counts" );
		*/
		/*
		{
			String mappingFile = dir + exp + "/mapping";
			String modelFile = dir + exp + "/model";
			
			{
				Mappings m = new Mappings();
				DelimitedReader r = new DelimitedReader(dir + exp + "/fts1.counts");
				String[] t = null;
				while ((t = r.read())!= null) {
					if (Integer.parseInt(t[1]) >= 10)  // minimum feature count 3
					m.getFeatureID(t[0], true);
				}
				r.close();
				m.write(mappingFile);
			}
			
			{
				String output1 = dir + exp + "/train";
				ConvertProtobufToMILDocument.convert(dir + exp + "/train.pb.gz", output1, mappingFile, false, true); //was true
			}
			
			{
				String output2 = dir + exp + "/test";
				ConvertProtobufToMILDocument.convert(dir + exp + "/test.pb.gz", output2, mappingFile, false, false);
			}
			
			{
				MILModel m = new MILModel();
				Mappings mappings = new Mappings();
				mappings.read(mappingFile);
				m.numRelations = mappings.numRelations();
				m.numFeaturesPerRelation = new int[m.numRelations];
				for (int i=0; i < m.numRelations; i++)
					m.numFeaturesPerRelation[i] = mappings.numFeatures();
				m.write(modelFile);
			}
		}
		*/
		// learning
		{
			Random random = new Random(7);
			
			MILModel model = new MILModel();
			model.read(dir + exp + "/model");
			
			CollinsTraining ct = new CollinsTraining(model, random);
			ct.topFalse = new TopFalse(dir + exp + "/topFalseK1000");
			
			Dataset train = new MemoryDataset(random, dir + exp + "/train");
			Dataset test = new MemoryDataset(random, dir + exp + "/test");
	
			System.gc();
			
			System.out.println("starting training");
			CRFParameters params = ct.train(train);
			
			PrintStream ps = new PrintStream(dir + exp + "/results_25it_update7_K1000");
			PrecisionRecallCurve.eval(test, params, ps);
		}

		System.out.println(name + " " + exp);
		
	}

	private static RelationMentionRef createMention(String[] t) {
		RelationMentionRef.Builder mntBuilder = RelationMentionRef.newBuilder();
		mntBuilder.setFilename("" + t[2]);
		mntBuilder.setSourceId(0);
		mntBuilder.setDestId(0);
		String[] fts = t[3].split("\n");
		for (String ft : fts) mntBuilder.addFeature(ft);
		return mntBuilder.build();
	}

	private static void reduce(String in, String out) throws IOException {
		DelimitedReader r = new DelimitedReader(in);
		DelimitedWriter w = new DelimitedWriter(out);
		String[] t = null;
		while ((t = r.read())!= null) {
			int sentenceID = Integer.parseInt(t[0]);
			if (sentenceID >= start2005_2 && sentenceID < start2007) w.write(t); 
		}
		r.close();
		w.close();		
	}

	private static int compare(String a1, String a2, String b1, String b2) {
		int c1 = a1.compareTo(b1);
		if (c1 != 0) return c1;
		int c2 = a2.compareTo(b2);
		return c2;
	}
	
	private static int compare(String[] a, String[] b) {
		return compare(a[0], a[1], b[0], b[1]);
	}

}
