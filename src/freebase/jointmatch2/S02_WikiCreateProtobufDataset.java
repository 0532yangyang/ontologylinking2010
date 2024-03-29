package freebase.jointmatch2;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import multir.preprocess.RelationECML;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class S02_WikiCreateProtobufDataset {

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/wikidump";

	//static String input1 = "/projects/pardosa/data16/raphaelh/tmp/ecmlOrigRelationsFull";
	//	static String input2 = dir + "/raw/freebase-relationIDs";
	//	static String input3 = dir + "/raw/matches-uniquepairs-relationIDs.sortedByArgsRel";
	static String input4 = dir + "/matches-pairmentions-relationIDs";

	static String input5 = dir + "/sentences.tokens";
	static String input6 = dir + "/sentences.pos";
	static String input7 = dir + "/sentences.deps";
	static String input8 = dir + "/sentences.ner"; // ner

	static String output1 = dir + "/featurizedData";
	static String output2 = dir + "/featurizedData.pb";

	//static String output3 = dir + ds + "/fb_matches_any2.selection";
	//static String output4 = dir + ds + "/names.selection";

	public static void main(String[] args) throws IOException {
		/*
		reduce(input4, input4 + ".selection");		
		reduce(input5, input5 + ".selection");		
		reduce(input6, input6 + ".selection");		
		reduce(input7, input7 + ".selection");
		reduce(input8, input8 + ".selection");
		*/

		// iterate over data, keep only if in target

		//		{
		//			RelationECML ecml = new RelationECML();
		//
		//			multir.util.delimited.DelimitedReader isTokens = new DelimitedReader(input5);
		//			DelimitedReader isPos = new DelimitedReader(input6);
		//			DelimitedReader isDeps = new DelimitedReader(input7);
		//			DelimitedReader isNer = new DelimitedReader(input8);
		//
		//			String[] tTokens = isTokens.read();
		//			String[] tPos = isPos.read();
		//			String[] tDeps = isDeps.read();
		//			String[] tNer = isNer.read();
		//
		//			DelimitedWriter w = new DelimitedWriter(output1);
		//			//8	Susan Parks	John O'Hara	3	5	19	21	1	1	0
		//			//senid, arg1, arg2, arg1pos[0,1], arg2pos[0,1]
		//			DelimitedReader r = new DelimitedReader(input4);
		//			String[] t = null;
		//			int count = 0;
		//
		//			HashMap<String, String> nerm = new HashMap<String, String>();
		//			int nermSentenceId = -1;
		//
		//			while ((t = r.read()) != null) {
		//				int sentenceId = Integer.parseInt(t[0]);
		//				if (++count % 1000000 == 0)
		//					System.out.println(count + "\t" + sentenceId);
		//
		//				//if (sentenceId < startSentenceId ||
		//				//	sentenceId >= endSentenceId) continue;
		//
		//				// both arguments must be in Freebase
		//				if (!t[7].equals("1") || !t[8].equals("1"))
		//					continue;
		//
		//				// align tokens, postags, deps
		//				while (tTokens != null && Integer.parseInt(tTokens[0]) < sentenceId)
		//					tTokens = isTokens.read();
		//				while (tPos != null && Integer.parseInt(tPos[0]) < sentenceId)
		//					tPos = isPos.read();
		//				while (tDeps != null && Integer.parseInt(tDeps[0]) < sentenceId)
		//					tDeps = isDeps.read();
		//
		//				// check if all iterators are aligned
		//				if (!(Integer.parseInt(tTokens[0]) == sentenceId && Integer.parseInt(tPos[0]) == sentenceId && Integer
		//						.parseInt(tDeps[0]) == sentenceId)) {
		//					System.out.println("iterators not aligned: shouldn't happen!");
		//					System.exit(-1);
		//				}
		//
		//				// align ner
		//				if (nermSentenceId < sentenceId) {
		//					nerm.clear();
		//
		//					// ner				
		//					while (tNer != null && Integer.parseInt(tNer[0]) < sentenceId)
		//						tNer = isNer.read();
		//
		//					while (tNer != null && Integer.parseInt(tNer[0]) == sentenceId) {
		//						nerm.put(tNer[1] + " " + tNer[2], tNer[4]);
		//						tNer = isNer.read();
		//					}
		//					nermSentenceId = sentenceId;
		//				}
		//
		//				// create features
		//				int[] arg1Pos = new int[] { Integer.parseInt(t[3]), Integer.parseInt(t[4]) };
		//				int[] arg2Pos = new int[] { Integer.parseInt(t[5]), Integer.parseInt(t[6]) };
		//
		//				if (nerm.get(arg1Pos[0] + " " + arg1Pos[1]) == null) {
		//					System.out.println("SHOULDN't HAPPEN!!!");
		//					System.out.println(sentenceId + " " + arg1Pos[0] + " " + arg1Pos[1] + " ");
		//				}
		//
		//				// convert to arrays
		//				String[] tokens = tTokens[1].split(" ");
		//				String[] pos = tPos[1].split(" ");
		//				String[] depParentsStr = tDeps[2].split(" ");
		//				String[] depTypes = tDeps[1].split(" ");
		//
		//				int[] depParents = new int[depParentsStr.length];
		//				for (int i = 0; i < depParents.length; i++)
		//					depParents[i] = Integer.parseInt(depParentsStr[i]);
		//
		//				//System.out.println(iTokenized.tokens.length + " " + iPostags.postags.length + " ");
		//				if (tokens.length != pos.length) {
		//					System.out.println(tTokens[1]);
		//					//System.out.println(iDeps.deps);
		//					//System.exit(0);
		//					System.out.println("IGNORING SENTENCE " + sentenceId);
		//					continue;
		//				}
		//
		//				List<String> fts = ecml.getFeatures(sentenceId, tokens, pos, depParents, depTypes, arg1Pos, arg2Pos,
		//						nerm.get(arg1Pos[0] + " " + arg1Pos[1]), nerm.get(arg2Pos[0] + " " + arg2Pos[1]));
		//				StringBuilder sb = new StringBuilder();
		//				for (String ft : fts)
		//					sb.append(ft + "\n");
		//
		//				// arg1, arg2, 
		//				w.write(t[1], t[2], sentenceId, sb.toString());
		//			}
		//			r.close();
		//			w.close();
		//
		//			isTokens.close();
		//			isPos.close();
		//			isDeps.close();
		//			isNer.close();
		//		}
		//
		//		// sort by arg1, arg2
		//		Sort.sort(output1, output1 + ".sorted", dir, new Comparator<String[]>() {
		//			public int compare(String[] t1, String[] t2) {
		//				return S02_WikiCreateProtobufDataset.compare(t1, t2);
		//			}
		//		});

		// first identify relations we care about
		//		HashSet<String> relsWeCare = new HashSet<String>();
		//		{
		//			DelimitedReader r = new DelimitedReader(input1);
		//			String[] t = null;
		//			while ((t = r.read()) != null)
		//				relsWeCare.add(t[0]);
		//			r.close();
		//		}

		// read mapping rels to ids
		//		HashMap<Integer, String> relIdsToRel = new HashMap<Integer, String>();
		//		HashSet<Integer> relIdsWeCare = new HashSet<Integer>();
		//		{
		//			DelimitedReader r = new DelimitedReader(input2);
		//			String[] t = null;
		//			while ((t = r.read()) != null) {
		//				String rel = t[1];
		//				Integer relId = Integer.parseInt(t[0]);
		//				if (relsWeCare.contains(rel)) {
		//					relIdsWeCare.add(relId);
		//					relIdsToRel.put(relId, rel);
		//				}
		//			}
		//			r.close();
		//		}

		// keep only arg pairs of manually crafted ontology
		//		HashMap<String, String> positive = new HashMap<String, String>();
		//
		//		{
		//			String lastKey = "";
		//			HashSet<Integer> seen = new HashSet<Integer>();
		//
		//			DelimitedReader r = new DelimitedReader(input3);
		//			String[] t = null;
		//			while ((t = r.read()) != null) {
		//				int relId = Integer.parseInt(t[4]);
		//				if (relIdsWeCare.contains(relId)) {
		//					String key = t[0] + "\t" + t[1];
		//					if (key.equals(lastKey)) {
		//						// don't write if already inside seen
		//						if (seen.contains(relId))
		//							continue;
		//					} else {
		//						lastKey = key;
		//						seen.clear();
		//					}
		//					seen.add(relId);
		//					String s = positive.get(key);
		//					if (s == null) {
		//						s = relIdsToRel.get(relId);
		//					} else {
		//						s += "," + relIdsToRel.get(relId);
		//					}
		//					positive.put(key, s);
		//				}
		//			}
		//			r.close();
		//		}

		// convert to protobuf
		{
			OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(output2)));

			DelimitedReader r = new DelimitedReader(output1 + ".sorted");
			Builder relBuilder = null;
			String[] t = null;
			while ((t = r.read()) != null) {
				if (relBuilder == null || !relBuilder.getSourceGuid().equals(t[0])
						|| !relBuilder.getDestGuid().equals(t[1])) {
					if (relBuilder != null) {
						// save old relbuilder
						//String rel = positive.get(relBuilder.getSourceGuid() + "\t" + relBuilder.getDestGuid());
						String rel = "NA";
						//						if (rel == null)
						//							rel = "NA";
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
				String rel = "NA";//positive.get(relBuilder.getSourceGuid() + "\t" + relBuilder.getDestGuid());
				//				if (rel == null)
				//					rel = "NA";
				relBuilder.setRelType(rel);
				Relation nr = relBuilder.build();
				nr.writeDelimitedTo(os);
			}
			r.close();
			os.close();
		}

	}

	/*
	private static void reduce(String input, String output) throws IOException {
		DelimitedWriter w = new DelimitedWriter(output);
		DelimitedReader r = new DelimitedReader(input);
		String[] t = null;
		int count = 0;
		while ((t = r.read())!= null){
			int sentenceId = Integer.parseInt(t[0]);
			if (++count % 1000000 == 0) System.out.println(count + "\t" + sentenceId);
			
			if (sentenceId < startSentenceId ||
				sentenceId >= endSentenceId) continue;
			
			w.write(t);
		}
		r.close();
		w.close();
	}
	*/

	private static RelationMentionRef createMention(String[] t) {
		RelationMentionRef.Builder mntBuilder = RelationMentionRef.newBuilder();
		mntBuilder.setFilename("" + t[2]);
		mntBuilder.setSourceId(0);
		mntBuilder.setDestId(0);
		String[] fts = t[3].split("\n");
		for (String ft : fts)
			mntBuilder.addFeature(ft);
		return mntBuilder.build();
	}

	private static int compare(String a1, String a2, String b1, String b2) {
		int c1 = a1.compareTo(b1);
		if (c1 != 0)
			return c1;
		int c2 = a2.compareTo(b2);
		return c2;
	}

	private static int compare(String[] a, String[] b) {
		return compare(a[0], a[1], b[0], b[1]);
	}
}
