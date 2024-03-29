package freebase.jointmatch2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;

import freebase.typematch.RecordWpSenToken;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.mydb.StringTable;

public class S02_ComputeMatches {

	public static void main(String[] args) throws IOException {

		//String dir = Main.dir_wikidatadump;
		// process freebase
		//ExtractNamedEntitiesFromFreebase.extract(Main.file_freebasedump, dir + "/freebase-names", dir);

		// for easier processing, we first reduce the number of Freebase 
		// entity names to those we discovered in the text
		//		createFreebaseEntitiesInText(dir + "/sentences.ner", dir + "/freebase-names", dir + "/freebase-names-text", dir);
		//
		//		// we then check, which relation instances of freebase could
		//		// be potentially matches
		//		createFreebaseRelationInstancesInText(Main.file_freebasedump, dir + "/freebase-names-text", dir
		//				+ "/freebase-relationInstances-text");
		//
		//		// each relevant Freebase relation type relation type we give a unique
		//		// integer ID
		//		createRelationIDs(dir + "/freebase-relationInstances-text", dir + "/freebase-relationIDs");

		//do nothing but add NA as the relation to uniqpairs
		//		zcl_createUniquePairsInTextFreebaseRelations(Main.dir_wikidatadump + "/matches-uniquepairs",
		//				Main.dir_wikidatadump + "/matches-uniquepairs-relationIDs", Main.dir_wikidatadump);

		// instead of unique pairs of entities, we now look at each mention of
		// a pair of entities and write its potential matches		
		zcl_createPairMentionInTextFreebaseRelations(Main.dir_wikidatadump + "/matches-uniquepairs-relationIDs",
				Main.dir_wikidatadump + "/sentences.ner", Main.dir_wikidatadump + "/matches-pairmentions-relationIDs",
				Main.dir_wikidatadump);
	}

	public static void createFreebaseEntitiesInText(String textEntitiesFile, String freebaseEntitiesFile,
			String freebaseEntitiesInTextFile, // output
			String tmpDir) throws IOException {

		// sort entities in text by name
		Sort.sort(textEntitiesFile, textEntitiesFile + ".sortedByName", tmpDir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				return t1[3].compareTo(t2[3]);
			}
		});

		// both input files are sorted by name now, find matches
		DelimitedWriter w = new DelimitedWriter(freebaseEntitiesInTextFile);
		DelimitedReader r1 = new DelimitedReader(freebaseEntitiesFile);
		DelimitedReader r2 = new DelimitedReader(textEntitiesFile + ".sortedByName");
		String[] t1 = r1.read(), t2 = r2.read();
		while (t1 != null && t2 != null) {
			int c = t1[1].compareTo(t2[3]);
			if (c < 0) {
				t1 = r1.read();
			} else if (c > 0) {
				t2 = r2.read();
			} else {
				String v = t1[1];
				while (t1 != null && t1[1].equals(v)) {
					w.write(t1[0], t1[1]);
					t1 = r1.read();
				}
				t2 = r2.read();
			}
		}
		r1.close();
		r2.close();
		w.close();
	}

	public static void createFreebaseRelationInstancesInText(String freebaseDumpFile,
			String freebaseEntitiesInTextFile, String freebaseRelationInstancesInTextFile // output
	) throws IOException {
		HashSet<String> hs = new HashSet<String>();
		{
			DelimitedReader r = new DelimitedReader(freebaseEntitiesInTextFile);
			String[] t = null;
			while ((t = r.read()) != null)
				hs.add(t[0]);
			r.close();
		}
		DelimitedWriter w = new DelimitedWriter(freebaseRelationInstancesInTextFile);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(freebaseDumpFile), "utf-8"));
		String line = null;
		while ((line = r.readLine()) != null) {
			String[] c = line.split("\t");
			if (c.length == 3 && hs.contains(c[0]) && hs.contains(c[2]))
				w.write(c);
		}
		r.close();
		w.close();
	}

	public static void createRelationIDs(String freebaseRelationInstancesInTextFile, String freebaseRelationIDs // output
	) throws IOException {

		HashMap<String, Integer> rel2relId = new HashMap<String, Integer>();
		DelimitedWriter w1 = new DelimitedWriter(freebaseRelationIDs);
		DelimitedReader r = new DelimitedReader(freebaseRelationInstancesInTextFile);
		String[] t = null;
		while ((t = r.read()) != null) {
			String rel = t[1];
			Integer relId = rel2relId.get(rel);
			if (relId == null) {
				relId = rel2relId.size();
				rel2relId.put(rel, relId);
				w1.write(relId, rel);
			}
		}
		w1.close();
		r.close();
	}

	static void zcl_createUniquePairsInText(String matchesUniquePairs) throws IOException {
		// load tuples into index
		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				String[] names = l[3].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		HashSet<Long> wikilinked = new HashSet<Long>();
		{
			D.p("load wikipedia page link");
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			String[] l;
			while ((l = dr.read()) != null) {
				wikilinked.add(StringTable.intPair2Long(l[0], l[1]));
			}
			dr.close();
		}
		// iterate over mention pairs, lookup possible fb entities, possible rels
		{
			D.p("get unique pairs");
			HashSet<String> uniquePairs = new HashSet<String>();
			//read all ners 
			//1	14	16	Bad Samaritans	MISC;
			//senid, start, end, name, ner;
			DelimitedReader r = new DelimitedReader(Main.dir_wikidatadump + "/sentences.ner");
			String[] t = null;
			List<String[]> mnt = new ArrayList<String[]>();
			int count = 0;
			while ((t = r.read()) != null) {
				String n = t[3].toLowerCase().trim().replaceAll(" ", "_");
				if (++count % 100000 == 0)
					System.out.println(count + "\t" + uniquePairs.size());
				if (mnt.isEmpty()) {
					//get the corresponding wid of the name
					if (name2wid.containsKey(n)) {
						mnt.add(t);
					}
				} else if (mnt.get(0)[0].equals(t[0])) {
					if (name2wid.containsKey(n)) {
						mnt.add(t);
					}
				} else {
					if (mnt.size() > 1) {
						for (int i = 0; i < mnt.size(); i++)
							for (int j = 0; j < mnt.size(); j++) {
								if (i == j)
									continue;
								String arg1 = mnt.get(i)[3];
								String arg2 = mnt.get(j)[3];
								String arg1check = arg1.toLowerCase().trim().replaceAll(" ", "_");
								String arg2check = arg2.toLowerCase().trim().replaceAll(" ", "_");
								int wid1 = name2wid.get(arg1check);
								int wid2 = name2wid.get(arg2check);
								if (wikilinked.contains(StringTable.intPair2Long(wid1, wid2)) && !arg1.equals(arg2)) {
									uniquePairs.add(arg1 + "\t" + arg2);
								}
								//								if (arg1.equals(arg2))
								//									continue;
								//								uniquePairs.add(arg1 + "\t" + arg2);
							}
					}
					mnt.clear();
					if (name2wid.containsKey(n)) {
						mnt.add(t);
					}
				}
			}
			//			if (mnt.size() > 1) {
			//				for (int i = 0; i < mnt.size(); i++)
			//					for (int j = 0; j < mnt.size(); j++) {
			//						if (i == j)
			//							continue;
			//						String arg1 = mnt.get(i)[3];
			//						String arg2 = mnt.get(j)[3];
			//						if (arg1.equals(arg2))
			//							continue;
			//						uniquePairs.add(arg1 + "\t" + arg2);
			//					}
			//			}
			r.close();

			DelimitedWriter w = new DelimitedWriter(matchesUniquePairs);
			for (String s : uniquePairs) {
				String[] p = s.split("\t");
				w.write(p[0], p[1]);
			}
			w.close();
		}
	}

	public static void zcl_createUniquePairsInTextFreebaseRelations(String matchesUniquePairs,
			String matchesUniquePairsWithRelationIDs, // output
			String tmpDir) throws IOException {

		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				String[] names = l[3].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}

		// enumerate fb surface forms and look for matches		
		{
			DelimitedWriter w = new DelimitedWriter(matchesUniquePairsWithRelationIDs);
			DelimitedReader r = new DelimitedReader(matchesUniquePairs);
			String[] t = null;
			int count = 0;
			while ((t = r.read()) != null) {
				if (++count % 100000 == 0)
					System.out.println(count);
				String arg1check = t[0].toLowerCase().trim().replaceAll(" ", "_");
				String arg2check = t[1].toLowerCase().trim().replaceAll(" ", "_");
				int wid1 = name2wid.get(arg1check);
				int wid2 = name2wid.get(arg2check);
				w.write(t[0], t[1], wid1, wid2, "NA");

			}
			r.close();
			w.close();
		}

		// sort by arg1,arg2,relationId
		Sort.sort(matchesUniquePairsWithRelationIDs, matchesUniquePairsWithRelationIDs + ".sortedByArgsRel", tmpDir,
				new Comparator<String[]>() {
					public int compare(String[] t1, String[] t2) {
						int c1 = t1[0].compareTo(t2[0]);
						if (c1 != 0)
							return c1;
						int c2 = t1[1].compareTo(t2[1]);
						if (c2 != 0)
							return c2;
						int s1 = Integer.parseInt(t1[4]);
						int s2 = Integer.parseInt(t2[4]);
						return s1 - s2;
					}
				});
	}

	// output7: /uniquepairs_fb_rels
	// for all entity pairs appearing in text, see if there is a freebase relation
	public static void createUniquePairsInTextFreebaseRelations(String freebaseEntitiesInTextFile,
			String freebaseRelationInstancesInTextFile, String freebaseRelationIDs, String matchesUniquePairs,
			String matchesUniquePairsWithRelationIDs, // output
			String tmpDir) throws IOException {

		HashMap<String, String[]> refs = readEntityName2FreebaseIdsIndex(freebaseEntitiesInTextFile);
		HashMap<String, Integer> args2relId = readArgs2relIDIndex(freebaseRelationInstancesInTextFile,
				freebaseRelationIDs);

		// enumerate fb surface forms and look for matches		
		{
			DelimitedWriter w = new DelimitedWriter(matchesUniquePairsWithRelationIDs);
			DelimitedReader r = new DelimitedReader(matchesUniquePairs);
			String[] t = null;
			int count = 0;
			while ((t = r.read()) != null) {
				if (++count % 100000 == 0)
					System.out.println(count);
				String[] t1 = refs.get(t[0]);
				String[] t2 = refs.get(t[1]);
				for (int i = 0; i < t1.length; i++)
					for (int j = 0; j < t2.length; j++) {
						String key1 = t1[i].substring(3) + " " + t2[j].substring(3);
						Integer rel1 = args2relId.get(key1);
						if (rel1 != null)
							w.write(t[0], t[1], t1[i], t2[j], rel1);
					}
			}
			r.close();
			w.close();
		}

		// sort by arg1,arg2,relationId
		Sort.sort(matchesUniquePairsWithRelationIDs, matchesUniquePairsWithRelationIDs + ".sortedByArgsRel", tmpDir,
				new Comparator<String[]>() {
					public int compare(String[] t1, String[] t2) {
						int c1 = t1[0].compareTo(t2[0]);
						if (c1 != 0)
							return c1;
						int c2 = t1[1].compareTo(t2[1]);
						if (c2 != 0)
							return c2;
						int s1 = Integer.parseInt(t1[4]);
						int s2 = Integer.parseInt(t2[4]);
						return s1 - s2;
					}
				});
	}

	static HashMap<String, String[]> readEntityName2FreebaseIdsIndex(String freebaseEntitiesInTextFile)
			throws IOException {
		// load index of Entity --> refs
		//#; m/013m_nh, /m/01cgxw8, /m/01dpy5s
		HashMap<String, String[]> refs = new HashMap<String, String[]>();
		{
			DelimitedReader r = new DelimitedReader(freebaseEntitiesInTextFile);
			String[] t = null;
			String centity = null;
			List<String> crefs = new ArrayList<String>();
			while ((t = r.read()) != null) {
				if (centity == null) {
					centity = t[1];
					crefs.add(t[0]);
				} else if (centity.equals(t[1])) {
					crefs.add(t[0]);
				} else {
					// do something
					refs.put(centity, crefs.toArray(new String[0]));
					crefs.clear();
					centity = t[1];
					crefs.add(t[0]);
				}
			}
			r.close();
		}
		return refs;
	}

	static HashMap<String, Integer> readArgs2relIDIndex(String freebaseRelationInstancesInTextFile,
			String freebaseRelationIDs) throws IOException {
		HashMap<String, Integer> rel2relID = readRel2relIDIndex(freebaseRelationIDs);
		HashMap<String, Integer> args2relID = new HashMap<String, Integer>();
		DelimitedReader r = new DelimitedReader(freebaseRelationInstancesInTextFile);
		String[] t = null;
		while ((t = r.read()) != null) {
			String arg1 = t[0].substring(3);
			String arg2 = t[2].substring(3);
			String rel = t[1];
			Integer relID = rel2relID.get(rel);
			args2relID.put(arg1 + " " + arg2, relID);
		}
		r.close();
		return args2relID;
	}

	static HashMap<String, Integer> readRel2relIDIndex(String output4) throws IOException {
		HashMap<String, Integer> rel2relId = new HashMap<String, Integer>();
		DelimitedReader r = new DelimitedReader(output4);
		String[] t = null;
		while ((t = r.read()) != null) {
			rel2relId.put(t[1], Integer.parseInt(t[0]));
		}
		r.close();
		return rel2relId;
	}

	static HashMap<String, List<String[]>> readEntityPairFreebaseRelationsIndex(String output7) throws IOException {

		// now: need to go through text again, and add matches
		// iterate over mention pairs, lookup possible fb entities, possible rels
		HashMap<String, List<String[]>> index = new HashMap<String, List<String[]>>();
		{
			DelimitedReader r = new DelimitedReader(output7);
			String[] t = null;
			while ((t = r.read()) != null) {
				String key = t[0] + "\t" + t[1];
				List<String[]> l = index.get(key);
				if (l == null) {
					l = new ArrayList<String[]>();
					index.put(key, l);
				}
				// entity1, entity2, relId
				l.add(new String[] { t[2], t[3], t[4] });
			}
			r.close();
		}
		return index;
	}

	// output9: fb_matches_any
	// pairs of entities with at least one freebase relation
	static void zcl_createPairMentionInTextFreebaseRelations(String matchesUniquePairsWithRelationIDs,
			String textEntitiesFile, String matchesPairMentionsWithRelationIDs, // output
			String tmpDir) throws IOException {

		HashMap<String, Integer> name2wid = new HashMap<String, Integer>();
		{
			D.p("load name2wid");
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				String[] names = l[3].split(" ");
				for (String n : names) {
					name2wid.put(n.toLowerCase(), wid);
				}
			}
		}
		HashMap<String, List<String[]>> index = readEntityPairFreebaseRelationsIndex(matchesUniquePairsWithRelationIDs);
		{
			DelimitedWriter w = new DelimitedWriter(matchesPairMentionsWithRelationIDs);
			DelimitedReader r = new DelimitedReader(textEntitiesFile);
			String[] t = null;
			List<String[]> mnt = new ArrayList<String[]>();
			int count = 0;
			while ((t = r.read()) != null) {
				if (t.length < 3)
					System.out.println(count + " " + t[0]);
				count++;
				String n = t[3].toLowerCase().trim().replaceAll(" ", "_");
				if (mnt.isEmpty()) {
					if (name2wid.containsKey(n)) {
						mnt.add(t);
					}
				} else if (mnt.get(0)[0].equals(t[0])) {
					if (name2wid.containsKey(n)) {
						mnt.add(t);
					}
				} else {
					if (mnt.size() > 1) {
						int[] oneFbArg = new int[mnt.size()];
						for (int i = 0; i < mnt.size(); i++) {
							String x = mnt.get(i)[3].toLowerCase().trim().replaceAll(" ", "_");
							oneFbArg[i] = name2wid.containsKey(x) ? 1 : 0;
						}
						for (int i = 0; i < mnt.size(); i++) {
							for (int j = 0; j < mnt.size(); j++) {
								if (i == j)
									continue;

								String arg1 = mnt.get(i)[3];
								String arg2 = mnt.get(j)[3];
								String key = arg1 + "\t" + arg2;

								// there exists at least one
								// sentenceId, arg1, arg2, start1, end1, start2, end2,
								//   arg1OneFb, arg2OneFb, relOneFb
								if (index.get(key) != null) {
									w.write(mnt.get(0)[0], arg1, arg2, mnt.get(i)[1], mnt.get(i)[2], mnt.get(j)[1],
											mnt.get(j)[2], oneFbArg[i], oneFbArg[j], index.get(key) != null ? 1 : 0);
								}
							}
						}
					}
					mnt.clear();
					if (name2wid.containsKey(n)) {
						mnt.add(t);
					}
				}

			}
			if (mnt.size() > 1) {
				int[] oneFbArg = new int[mnt.size()];
				for (int i = 0; i < mnt.size(); i++) {
					String x = mnt.get(i)[3].toLowerCase().trim().replaceAll(" ", "_");
					oneFbArg[i] = name2wid.containsKey(x) ? 1 : 0;
				}
				for (int i = 0; i < mnt.size(); i++) {
					for (int j = 0; j < mnt.size(); j++) {
						if (i == j)
							continue;

						String arg1 = mnt.get(i)[3];
						String arg2 = mnt.get(j)[3];
						String key = arg1 + "\t" + arg2;

						// there exists at least one
						// sentenceId, arg1, arg2, start1, end1, start2, end2,
						//   arg1OneFb, arg2OneFb, relOneFb
						if (index.get(key) != null) {
							w.write(mnt.get(0)[0], arg1, arg2, mnt.get(i)[1], mnt.get(i)[2], mnt.get(j)[1],
									mnt.get(j)[2], oneFbArg[i], oneFbArg[j], index.get(key) != null ? 1 : 0);
						}
					}
				}
			}
			r.close();
			w.close();
		}

		// schema of input:
		// sentenceId, arg1, arg2, start1, end1, start2, end2, arg1OneFb, arg2OneFb, relOneFb
		// sort by arg1,arg2,sentenceId, note: arg1 < arg2 already
		Sort.sort(matchesPairMentionsWithRelationIDs, matchesPairMentionsWithRelationIDs
				+ ".sortedByArg1Arg2SentenceID", tmpDir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int c1 = t1[1].compareTo(t2[1]);
				if (c1 != 0)
					return c1;
				int c2 = t1[2].compareTo(t2[2]);
				if (c2 != 0)
					return c2;
				int s1 = Integer.parseInt(t1[0]);
				int s2 = Integer.parseInt(t2[0]);
				return s1 - s2;
			}
		});
	}
}
