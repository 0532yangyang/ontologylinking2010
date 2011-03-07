package nell.preprocess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import multir.util.delimited.Sort;

public class ComputeMatches {

	static String dir = "/projects/pardosa/data14/raphaelh/t/raw";
	static String outdir = "/projects/pardosa/s5/clzhang/ontologylink/tmp1";
	public static void main(String[] args) throws IOException {

		NellOntology no = new NellOntology();
		createUniquePairsInTextNellRelations(
				no,
				outdir+"/matches-uniquepairs-relationIDs-nell",
				outdir);
		
		createPairMentionInTextNellRelations(
				no,
				dir + "/sentences.ner",
				outdir + "/matches-pairmentions-relationIDs-nell",
				outdir);
	}


	

	public static void createUniquePairsInTextNellRelations(
			NellOntology no,
			String matchesUniquePairsWithRelationIDs, // output
			String tmpDir) throws IOException {

		// enumerate fb surface forms and look for matches		
		{
			DelimitedWriter w = new DelimitedWriter(matchesUniquePairsWithRelationIDs);
			for(NellRelation nr: no.nellRelationList){
				for(String []t: nr.seedInstances){
					String key = t[0]+"\t"+t[1];
					List<Integer[]> cidcidridlist = no.entitypair2eidrid.get(key);
					Integer[] towrt = cidcidridlist.get(0);
					w.write(t[0],t[1],towrt[0],towrt[1],towrt[2]);
				}
			}
			w.close();
		}		
		
		// sort by arg1,arg2,relationId
		Sort.sort(matchesUniquePairsWithRelationIDs, 
				matchesUniquePairsWithRelationIDs + ".sortedByArgsRel", 
				tmpDir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int c1 = t1[0].compareTo(t2[0]);
				if (c1 != 0) return c1;
				int c2 = t1[1].compareTo(t2[1]);
				if (c2 != 0) return c2;
				int s1 = Integer.parseInt(t1[4]);
				int s2 = Integer.parseInt(t2[4]);
				return s1 - s2;
			}});
	}

	static void createPairMentionInTextNellRelations(
			NellOntology no,
			String textEntitiesFile,
			String matchesPairMentionsWithRelationIDs,// output
			String tmpDir
			) throws IOException{
		{
			DelimitedWriter w = new DelimitedWriter(matchesPairMentionsWithRelationIDs);
			DelimitedReader r = new DelimitedReader(textEntitiesFile);
			String[] t = null;		
			List<String[]> mnt = new ArrayList<String[]>();
			int count = 0;
			while ((t = r.read())!= null) {
				if (t.length < 3)
					System.out.println(count + " " +t[0]);
				if(count++%100000==0)System.out.println(count);
//				if(count > 100000)break;
				if (mnt.isEmpty()) { 
					mnt.add(t);
				} else if (mnt.get(0)[0].equals(t[0])) {
					mnt.add(t);
				} else {
					if (mnt.size() > 1) {
						int[] oneNellArg = new int[mnt.size()];
						for (int i=0; i < mnt.size(); i++)
							//oneNellArg : check whether it is an nell seed NNP
							oneNellArg[i] = no.entity2classId.get(mnt.get(i)[3]) != null? 1 : 0;
						
						for (int i=0; i < mnt.size(); i++)
							for (int j=0; j < mnt.size(); j++) {
								if (i==j) continue;
								
								String arg1 = mnt.get(i)[3];
								String arg2 = mnt.get(j)[3];
								String key = arg1 + "\t" + arg2;
								
								// there exists at least one
								// sentenceId, arg1, arg2, start1, end1, start2, end2,
								//   arg1OneFb, arg2OneFb, relOneFb
								if(no.entitypair2eidrid.get(key) != null){
									List<Integer[]> cidcidridlist = no.entitypair2eidrid.get(key);
									Integer[] cidcidrid = cidcidridlist.get(0);
									String relation_name = no.relationNames[cidcidrid[2]];
									w.write(mnt.get(0)[0],
											arg1, arg2, 
											mnt.get(i)[1], mnt.get(i)[2],
											mnt.get(j)[1], mnt.get(j)[2], 
											oneNellArg[i], oneNellArg[j],
											1,
											relation_name);
								}
							}
					}
					mnt.clear();
					Integer rf = no.className2Id.get(t[3]);
					if (rf != null) mnt.add(t);
				}
				
			}
			System.out.println(mnt.size());
			if (mnt.size() > 1) {
				int[] oneNellArg = new int[mnt.size()];
				for (int i=0; i < mnt.size(); i++)
					//oneNellArg : check whether it is an nell seed NNP
					oneNellArg[i] = no.className2Id.get(mnt.get(i)[3]) != null? 1 : 0;
				
				for (int i=0; i < mnt.size(); i++)
					for (int j=0; j < mnt.size(); j++) {
						if (i==j) continue;
						
						String arg1 = mnt.get(i)[3];
						String arg2 = mnt.get(j)[3];
						String key = arg1 + "\t" + arg2;
						
						// there exists at least one
						// sentenceId, arg1, arg2, start1, end1, start2, end2,
						//   arg1OneFb, arg2OneFb, relOneFb
						w.write(mnt.get(0)[0],
							arg1, arg2, 
							mnt.get(i)[1], mnt.get(i)[2],
							mnt.get(j)[1], mnt.get(j)[2], 
							oneNellArg[i], oneNellArg[j],
							no.entitypair2eidrid.get(key) != null? 1 : 0);
					}
			}
			r.close();
			w.close();
		}

		// schema of input:
		// sentenceId, arg1, arg2, start1, end1, start2, end2, arg1OneFb, arg2OneFb, relOneFb
		// sort by arg1,arg2,sentenceId, note: arg1 < arg2 already
		Sort.sort(matchesPairMentionsWithRelationIDs, 
				matchesPairMentionsWithRelationIDs + ".sortedByArg1Arg2SentenceID", 
				tmpDir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int c1 = t1[1].compareTo(t2[1]);
				if (c1 != 0) return c1;
				int c2 = t1[2].compareTo(t2[2]);
				if (c2 != 0) return c2;
				int s1 = Integer.parseInt(t1[0]);
				int s2 = Integer.parseInt(t2[0]);
				return s1 - s2;
			}});
	}
}
