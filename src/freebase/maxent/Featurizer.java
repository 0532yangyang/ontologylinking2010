package freebase.maxent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.database.WordNet;
import javatools.string.Stemmer;
import javatools.string.StringUtil;

public class Featurizer {

	static class FeaturizerConfig {
		static int windowssize = 10;
		static int bagofwords = 1;
		static Stemmer s = new Stemmer();
	}

	static WordNet wn;

	public static boolean featurizeRelation(RecordRelMatch rmr) throws Exception {
		if (wn == null) {
			wn = new WordNet();
		}

		HashSet<Integer> hs = new HashSet<Integer>();
		String nellr = rmr.nellRelation.replace("_inverse", "");
		String fbr = rmr.fbJoinRelation;

		if (nellr.equals("cityLocatedInState") && fbr.contains("film_location")) {
			int x = 0;
		}
		List<String> nellrSplit = StringUtil.tokenize(nellr, new char[] {});
		List<String> fbrSplit = StringUtil.tokenize(fbr, new char[] { '/', '|', '_' });
		StringUtil.sortAndRemoveDuplicate(nellrSplit);
		StringUtil.sortAndRemoveDuplicate(fbrSplit);
		int wordmatch = 0, synmatch = 0, synAllmatch = 0;

		{
			// Feature.NAME_MATCH
			// StringUtil.sortAndRemoveDuplicate(nellrSplit);
			// StringUtil.sortAndRemoveDuplicate(fbrSplit);
			wordmatch = StringUtil.numOfShareWords(nellrSplit, fbrSplit);
			// D.p("wordmatch",nellrSplit,fbrSplit,numMatch);
		}
		{
			// Feature.REL_WORD_SYNSET_MATCH
			HashSet<Integer> nellrIdsSet = new HashSet<Integer>();
			HashSet<Integer> fbrIdsSet = new HashSet<Integer>();
			List<Integer> nellrIds = new ArrayList<Integer>();
			List<Integer> fbrIds = new ArrayList<Integer>();
			for (String ns : nellrSplit) {
				int nsid = wn.getStemSynIdTop(ns, false);
				if (nsid > 0)
					nellrIdsSet.add(nsid);
			}
			for (String fs : fbrSplit) {
				int fsid = wn.getStemSynIdTop(fs, false);
				if (fsid > 0)
					fbrIdsSet.add(fsid);
			}
			nellrIds.addAll(nellrIdsSet);
			fbrIds.addAll(fbrIdsSet);
			Collections.sort(nellrIds);
			Collections.sort(fbrIds);
			synmatch = StringUtil.numOfShareInteger(nellrIds, fbrIds);
			// D.p("synmatch",nellrSplit,fbrSplit,numMatch);
		}
		{
			// Feature.REL_WORD_SYNSET_MATCH
			HashSet<Integer> nellrIdsSet = new HashSet<Integer>();
			HashSet<Integer> fbrIdsSet = new HashSet<Integer>();
			List<Integer> nellrIds = new ArrayList<Integer>();
			List<Integer> fbrIds = new ArrayList<Integer>();
			for (String ns : nellrSplit) {
				List<Integer> nsidlist = wn.getStemSynId(ns, false);
				for (int nsid : nsidlist)
					nellrIdsSet.add(nsid);
			}
			for (String fs : fbrSplit) {
				List<Integer> fsidlist = wn.getStemSynId(fs, false);
				for (int fsid : fsidlist)
					fbrIdsSet.add(fsid);
			}
			nellrIds.addAll(nellrIdsSet);
			fbrIds.addAll(fbrIdsSet);
			Collections.sort(nellrIds);
			Collections.sort(fbrIds);
			synAllmatch = StringUtil.numOfShareInteger(nellrIds, fbrIds);
			// D.p("synmatch",nellrSplit,fbrSplit,numMatch);
		}
		D.p(nellr, fbr, wordmatch, synmatch, synAllmatch);
		return false;
	}

	public static List<String> featureInstance(List<RecordWpSenToken> list_wpsen, String argB)
			throws Exception {
		//D.p(list_wpsen);
		//D.p("==================================================================================");
		List<String> features = new ArrayList<String>();
		for(RecordWpSenToken wpsen: list_wpsen){
			List<int[]> list_argBpos = help_wpsenFindArgBPosition(wpsen,argB);
			if(list_argBpos.size()>0){
				features.addAll(f_wpWords(wpsen,list_argBpos));
			}
		}
		return features;
	}

	private static int addNodeFeature(String featureName, int node, Model model, Set<Integer> hs, boolean addNewFeatures) {
		Integer v = model.featureName2Id.get(featureName);
		if (v == null) {
			if (!addNewFeatures)
				return -1;
			v = model.featureId2Name.size();
			model.featureId2Name.add(featureName);
			model.featureName2Id.put(featureName, v);
		}
		hs.add(v);
		return v;
		// Integer v = model.nodeFactors[node].name2featureId.get(featureName);
		// if (v == null) {
		// if (!addNewFeatures) return;
		// v = model.nodeFactors[node].addFeature(featureName);
		// }
	}

	private static List<int[]> help_wpsenFindArgBPosition(RecordWpSenToken wpsen, String argb) {
		List<int[]> result = new ArrayList<int[]>();
		List<String> argbtoken = StringUtil.tokenize(argb, new char[] { ' ' });
		for (int i = 0; i < wpsen.token_str.length; i++) {
			boolean isMatch = true;
			for (int j = 0; j < argbtoken.size(); j++) {
				if (i + j >= wpsen.token_str.length) {
					isMatch = false;
					break;
				}
				String a = wpsen.token_str[i + j].toLowerCase();
				String b = argbtoken.get(j).toLowerCase();
				if (!a.equals(b)) {
					isMatch = false;
					break;
				}
			}
			if (isMatch) {
				int[] r = new int[2];
				r[0] = i;
				r[1] = i + wpsen.token_str.length;
				result.add(r);
			}
		}
		return result;
	}

	private static List<String> f_wpWords(RecordWpSenToken wpsen, List<int[]> argBpos) {
		List<String> features = new ArrayList<String>();
		for (int[] pos : argBpos) {
			for (int i = pos[0]-FeaturizerConfig.windowssize; i < pos[1]+ FeaturizerConfig.windowssize ; i++) {
				if( i< 0 || i>=wpsen.token_str.length)continue;
				features.add(wpsen.token_str[i]);
			}
		}
		return features;
	}
	//one feature wikipedia bag of words
	private static List<String> f_wpBagofWords(RecordWpSenToken wpsen, List<int[]> argBpos) {
		List<String> features = new ArrayList<String>();
		for (int[] pos : argBpos) {
			for (int i = 1; i <= FeaturizerConfig.windowssize; i++) {
				int start = pos[0] - i;
				for (int j = 1; j <= FeaturizerConfig.bagofwords; j++) {
					int end = start + j;
					if(start >= 0 && end <wpsen.token_str.length){
						f_wpBagofWords_help(start,end,wpsen,features);
					}
				}
				start = pos[1];
				for(int j=1;j<=FeaturizerConfig.bagofwords;j++){
					int end = start+j;
					f_wpBagofWords_help(start,end,wpsen,features);
				}
			}
		}
		return features;
	}
	private static void f_wpBagofWords_help(int start,int end, RecordWpSenToken wpsen,List<String> features ){
		if(start >= 0 && end <wpsen.token_str.length){
			StringBuilder sb = new StringBuilder();
			//sb.append("BW_").append(i).append("_").append(j).append("_");
			for(int k=start;k<end;k++){
				String toappend = wpsen.token_str[k];
				{
					//stemmer and lower case;
					toappend = FeaturizerConfig.s.stem(toappend.toLowerCase());
				}
				sb.append(toappend+"_");
			}
			features.add(sb.toString());
		}
	}

}
