package multir.eval.congle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class PrepareForSentenceLevelAnnotationsAddXiaoResult {

	static String input1 = "/projects/pardosa/data16/raphaelh/riedel0506/test-all-newlabel-Multiple.pb";
	static String input2 = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/resultsTestX2";
	static String input3 = "/projects/pardosa/data16/raphaelh/tmp/seb/exp/resultsTestMultiple.pairs";
	static String input4 = Main.dir + "/guid2name.all";

	static String input5 = Main.dir + "/umassresultTestMultiple";
	static String input6 = Main.dir + "/umassresultTestMultiple.pair";

	//static String alreadyLabeledfile = "/projects/pardosa/s5/clzhang/tmp/seb/labelversion3_550sentence";
	static String alreadyLabeledfile = Main.dir + "/labelversion5_600sentence";
	// static String output =
	// "/projects/pardosa/data14/raphaelh/tmp/seb/sample_sentences";
	static String output = Main.dir + "/overallcompare1";

	static HashMap<String, String> mylabel_sen_label = new HashMap<String, String>();
	static HashSet<String> duplicated = new HashSet<String>();

	public static void loadAlreadyLabel() throws IOException {
		List<String[]> all = (new DelimitedReader(alreadyLabeledfile))
				.readAll();
		for (String[] l : all) {
			mylabel_sen_label.put(l[5], l[6]);
		}
	}

	public static HashMap<String, String[]> turnToHashPair(String file)
			throws IOException {
		List<String[]> pairlist = (new DelimitedReader(file)).readAll();
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		for (String[] l : pairlist) {
			map.put(l[0] + "\t" + l[1], l);
		}
		return map;
	}

	public static HashMap<String, String[]> turnToHashSentence(String file)
			throws IOException {
		List<String[]> pairlist = (new DelimitedReader(file)).readAll();
		Collections.shuffle(pairlist);
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		for (String[] l : pairlist) {
			String key = l[0] + "\t" + l[1] + "\t" + l[5];
			if (!map.containsKey(key)) {
				map.put(key, l);
			} else {
				duplicated.add(key);
			}
		}
		return map;
	}

	public static void main(String[] args) throws IOException {
		// read guid2name
		loadAlreadyLabel();
		HashMap<String, String> guid2name = new HashMap<String, String>();
		{
			DelimitedReader r = new DelimitedReader(input4);
			String[] t = null;
			while ((t = r.read()) != null)
				guid2name.put(t[0], t[1]);
			r.close();
		}

		// read our labels
		List<EntityPairMention> set = new ArrayList<EntityPairMention>();

		HashMap<String, String[]> r1list = turnToHashSentence(input2);
		HashMap<String, String[]> r2list = turnToHashPair(input3);
		HashMap<String, String[]> r1xlist = turnToHashSentence(input5);
		HashMap<String, String[]> r2xlist = turnToHashPair(input6);

		InputStream is = new FileInputStream(input1);
		Relation r = null;

		int posPairs = 0;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			String name1 = guid2name.get(r.getSourceGuid());
			String name2 = guid2name.get(r.getDestGuid());
			if (name1 == null)
				name1 = "";
			if (name2 == null)
				name2 = "";

			String[] p = r2list.get(r.getSourceGuid() + "\t" + r.getDestGuid());
			String[] px = r2xlist.get(r.getSourceGuid() + "\t"
					+ r.getDestGuid());
			// sanity check
			if (!r.getSourceGuid().equals(p[0])
					|| !r.getDestGuid().equals(p[1]))
				System.out.println("error: files not aligned");

			if (!p[2].equals("NA") || !r.getRelType().equals("NA"))
				posPairs++;

			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);

				// our prediction
				if (r.getSourceGuid().equals(
						"/guid/9202a8c04000641f8000000000023ede")
						&& r.getDestGuid().equals(
								"/guid/9202a8c04000641f800000000004aa1a")
						&& i == 16) {
					System.out.println("abc");
				}
				if (rmf.getSentence().contains(
						"About 4,300 job cuts are planned in France")) {
					System.out.println();
				}
				String key = r.getSourceGuid() + "\t" + r.getDestGuid() + "\t"
						+ rmf.getSentence();
				// if (duplicated.contains(key))
				// continue;

				String[] t = r1list.get(key);
				String[] tx = r1xlist.get(key);
				if (t == null || tx == null)
					continue;
				// sanity check
				if (!r.getSourceGuid().equals(t[0])
						|| !r.getDestGuid().equals(t[1]))
					System.out.println("error: files not aligned");

				if (!p[2].equals("NA") || !r.getRelType().equals("NA")
						|| !px[2].equals("NA")) {
					EntityPairMention epm = new EntityPairMention(r
							.getSourceGuid(), r.getDestGuid(), name1, name2, i,
							rmf.getSentence());
					epm.fbRelations = r.getRelType();
					epm.ourPredictions = t[3];
					epm.umassPredictions = tx[3];
					epm.ourscore = Double.parseDouble(t[4]);
					epm.theirscore = Double.parseDouble(tx[4]);
					set.add(epm);
				}
				// if (p[2].equals("NA") && r.getRelType().equals("NA")
				// && !px[2].equals("NA")) {
				// EntityPairMention epm = new EntityPairMention(r
				// .getSourceGuid(), r.getDestGuid(), name1, name2, i,
				// rmf.getSentence());
				// epm.fbRelations = r.getRelType();
				// epm.ourPredictions = t[3];
				// epm.umassPredictions = tx[3];
				// epm.ourscore = Double.parseDouble(t[4]);
				// epm.theirscore = Double.parseDouble(tx[4]);
				// setUmass.add(epm);
				// }
			}
		}
		is.close();

		System.out.println(posPairs);

		System.out.println(set.size());
		Collections.shuffle(set);
		int SAMPLE_SIZE = 600;
		HashSet<String> usedSen = new HashSet<String>();
		DelimitedWriter w = new DelimitedWriter(output);
		for (EntityPairMention epm : set) {
			String putusedsen = epm.sentence;
			if (!usedSen.contains(putusedsen) && mylabel_sen_label.containsKey(epm.sentence)) {
				String label = mylabel_sen_label.get(epm.sentence);
				w.write(epm.guid1, epm.guid2, epm.mention, epm.name1,
						epm.name2, epm.sentence, epm.fbRelations,
						epm.ourPredictions, epm.ourscore, epm.umassPredictions,
						epm.theirscore, label);
				usedSen.add(putusedsen);
			}
		}

		//		for (EntityPairMention epm : set) {
		//			if (usedSen.size() > SAMPLE_SIZE)
		//				break;
		//			if (!usedSen.contains(epm.sentence)) {
		//				w.write(epm.guid1, epm.guid2, epm.mention, epm.name1,
		//						epm.name2, epm.sentence, epm.fbRelations,
		//						epm.ourPredictions, epm.ourscore, epm.umassPredictions,
		//						epm.theirscore, "UNKNOWN");
		//				usedSen.add(epm.sentence);
		//			}
		//		}
		w.close();
		// sample, while preserving order
		// List<Integer> ids = new ArrayList<Integer>(set.size());
		// for (int i = 0; i < set.size(); i++)
		// ids.add(i);
		// int SAMPLE_SIZE = 500;
		// Collections.shuffle(ids);
		// List<Integer> sam = ids.subList(0, SAMPLE_SIZE);
		// Collections.sort(sam);
		//
		// // write sample to file
		// DelimitedWriter w = new DelimitedWriter(output);
		// for (int i = 0; i < sam.size(); i++) {
		// EntityPairMention epm = set.get(sam.get(i));
		// w.write(epm.guid1, epm.guid2, epm.mention, epm.name1, epm.name2,
		// epm.sentence, epm.fbRelations, epm.ourPredictions,
		// epm.umassPredictions);
		// }
		// w.close();

		// distribution
		// HashMap<String, Integer> m = new HashMap<String, Integer>();
		// {
		// for (int i = 0; i < sam.size(); i++) {
		// EntityPairMention epm = set.get(sam.get(i));
		// // String[] fbr = epm.fbRelations.split(",");
		// // for (String fb : fbr)
		// // increment(m, fb);
		//
		// increment(m, epm.ourPredictions);
		// }
		// }

		// for (Map.Entry<String, Integer> e : m.entrySet())
		// System.out.println(e.getValue() + "\t" + e.getKey());

	}

	static void raphaelSample(List<EntityPairMention> set) throws IOException {
		// sample, while preserving order
		List<Integer> ids = new ArrayList<Integer>(set.size());
		for (int i = 0; i < set.size(); i++)
			ids.add(i);
		int SAMPLE_SIZE = 450;
		Collections.shuffle(ids);
		List<Integer> sam = ids.subList(0, SAMPLE_SIZE);
		Collections.sort(sam);

		// write sample to file
		DelimitedWriter w = new DelimitedWriter(output);
		for (int i = 0; i < sam.size(); i++) {
			EntityPairMention epm = set.get(sam.get(i));
			w.write(epm.guid1, epm.guid2, epm.mention, epm.name1, epm.name2,
					epm.sentence, epm.fbRelations, epm.ourPredictions,
					epm.umassPredictions);
		}
		w.close();

		// distribution
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		{
			for (int i = 0; i < sam.size(); i++) {
				EntityPairMention epm = set.get(sam.get(i));
				// String[] fbr = epm.fbRelations.split(",");
				// for (String fb : fbr)
				// increment(m, fb);

				increment(m, epm.ourPredictions);
			}
		}

		for (Map.Entry<String, Integer> e : m.entrySet())
			System.out.println(e.getValue() + "\t" + e.getKey());
	}

	static void increment(HashMap<String, Integer> m, String k) {
		Integer i = m.get(k);
		if (i == null)
			i = 0;
		i++;
		m.put(k, i);
	}

	static class EntityPairMention {
		String guid1, guid2;
		String name1, name2;
		int mention;
		String sentence;
		String fbRelations;
		String ourPredictions;
		String umassPredictions;
		double ourscore;
		double theirscore;
		String truth;

		public EntityPairMention(String guid1, String guid2, String name1,
				String name2, int mention, String sentence) {
			this.guid1 = guid1;
			this.guid2 = guid2;
			this.name1 = name1;
			this.name2 = name2;
			this.mention = mention;
			this.sentence = sentence;
		}
	}

}
