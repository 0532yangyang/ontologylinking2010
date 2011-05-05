package multir.tmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import multir.util.delimited.DelimitedReader;

public class NewFeatures2 {

	//static final int LongPathThresh = 5;

	HashSet<String>[] lex;
	
	static String[] lists = {
		"/location/citytown",
		"/location/country",
		"/location/region",
		"/location/administrative_division",
		"/location/us_state",
		"/architecture/building",  
		"/organization/organization", // new relations
		"/business/business_operation",
		"/business/employer",
		"/people/person"  };
	
	@SuppressWarnings("unchecked")
	public NewFeatures2() {
		lex = (HashSet<String>[])new HashSet[lists.length];
		for (int i=0; i < lex.length; i++)
			lex[i] = new HashSet<String>();
		try {
			DelimitedReader r = new DelimitedReader
				("/projects/pardosa/data14/raphaelh/t/ftexp/freebase-lists-names");
			String[] t = null;
			while ((t = r.read())!= null) {
				for (int i =0; i < lists.length; i++)
					if (t[1].equals(lists[i]))
						lex[i].add(t[0]);
			}
			r.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getString(String[] tokens, int[] pos) {
		if (pos[1]-pos[0] == 1) return tokens[pos[0]];
		StringBuilder sb = new StringBuilder();
		for (int i=pos[0]; i < pos[1]; i++) {
			if (i > pos[0]) sb.append(" ");
			sb.append(tokens[i]);
		}
		return sb.toString();
	}
	
	
	public List<String> getFeatures(int sentenceId, String[] tokens, 
			String[] postags,
			int[] depParents, String[] depTypes,
			int[] arg1Pos, int[] arg2Pos, String arg1ner, String arg2ner) {
		List<String> features = new ArrayList<String>();
		
		arg1ner = "MISC";
		arg2ner = "MISC";

		String arg1 = getString(tokens, arg1Pos);
		String arg2 = getString(tokens, arg2Pos);


		for (int i=0; i < lists.length; i++) {
			if (lex[i].contains(arg1)) {
				arg1ner = lists[i];
				break;
			}
		}

		for (int i=0; i < lists.length; i++) {
			if (lex[i].contains(arg2)) {
				arg2ner = lists[i];
				break;
			}
		}

/*		
		// new lexicon features
		for (int i=0; i < lists.length; i++)
			if (lex[i].contains(arg1))
				features.add("1_" + lists[i]);

		for (int i=0; i < lists.length; i++) 
			if (lex[i].contains(arg2)) 
				features.add("2_" + lists[i]);
*/		
		
		
		

		/** ner feature, such as LOCATION->PERSON */
//		features.add(arg1ner + "->" + arg2ner);
		
		// it's easier to deal with first, second
		int[] first = arg1Pos, second = arg2Pos;
		String firstNer = arg1ner, secondNer = arg2ner;
		if (arg1Pos[0] > arg2Pos[0]) {
			second = arg1Pos; first = arg2Pos;
			firstNer = arg2ner; secondNer = arg1ner;
		}
		
		// define the inverse prefix
		String inv = (arg1Pos[0] > arg2Pos[0])? 
				"inverse_true" : "inverse_false";
		
		// define the middle parts
		StringBuilder middleTokens = new StringBuilder();
		StringBuilder middleTags = new StringBuilder();
		for (int i=first[1]; i < second[0]; i++) {
			if (i > first[1]) {
				middleTokens.append(" ");
				middleTags.append(" ");
			}
			middleTokens.append(tokens[i]);
			middleTags.append(postags[i]);
		}
		
		if (second[0] - first[1] > 10) {
			middleTokens.setLength(0);
			middleTokens.append("*LONG*");
			
			// newly added
			middleTags.setLength(0);
			middleTags.append("*LONG*");
		}
		
		// define the prefixes and suffixes
		String[] prefixTokens = new String[2];
		String[] suffixTokens = new String[2];
		
		for (int i=0; i < 2; i++) {
			int tokIndex = first[0] - i - 1;
			if (tokIndex < 0) prefixTokens[i] = "B_" + tokIndex;
			else prefixTokens[i] = tokens[tokIndex];
		}

		for (int i=0; i < 2; i++) {
			int tokIndex = second[1] + i;
			if (tokIndex >= tokens.length) suffixTokens[i] = "B_" + (tokIndex - tokens.length + 1);
			else suffixTokens[i] = tokens[tokIndex];
		}

		String[] prefixes = new String[3];
		String[] suffixes = new String[3];

		prefixes[0] = suffixes[0] = "";
		prefixes[1] = prefixTokens[0];
		prefixes[2] = prefixTokens[1] + " " + prefixTokens[0];
		suffixes[1] = suffixTokens[0];
		suffixes[2] = suffixTokens[0] + " " + suffixTokens[1];
		
		// generate the features in the same order as in ecml data
		String mto = middleTokens.toString();
		String mta = middleTags.toString();

		features.add(inv + "|" + firstNer + "|" + mto + "|" + secondNer);
		features.add(inv + "|" + prefixes[1] + "|" + firstNer + "|" + mto + "|" + secondNer + "|" + suffixes[1]);
//		features.add(inv + "|" + prefixes[2] + "|" + firstNer + "|" + mto + "|" + secondNer + "|" + suffixes[2]);

//		features.add(inv + "|" + firstNer + "|" + mta + "|" + secondNer);
//		features.add(inv + "|" + prefixes[1] + "|" + firstNer + "|" + mta + "|" + secondNer + "|" + suffixes[1]);
//		features.add(inv + "|" + prefixes[2] + "|" + firstNer + "|" + mta + "|" + secondNer + "|" + suffixes[2]);
		
		// dependency features
		if (depParents == null || depParents.length < tokens.length) return features;
		
		// identify head words of arg1 and arg2
		// (start at end, while inside entity, jump)
		int head1 = arg1Pos[1]-1;
		while (depParents[head1] >= arg1Pos[0] && depParents[head1] < arg1Pos[1]) head1 = depParents[head1];
		int head2 = arg2Pos[1]-1;
		//System.out.println(head1 + " " + head2);
		while (depParents[head2] >= arg2Pos[0] && depParents[head2] < arg2Pos[1]) head2 = depParents[head2];
		
		
		// find path of dependencies from first to second
		int[] path1 = new int[tokens.length];
		for (int i=0; i < path1.length; i++) path1[i] = -1;
		path1[0] = head1; // last token of first argument
		for (int i=1; i < path1.length; i++) {
			path1[i] = depParents[path1[i-1]];
			if (path1[i] == -1) break;
		}	
		int[] path2 = new int[tokens.length];
		for (int i=0; i < path2.length; i++) path2[i] = -1;
		path2[0] = head2; // last token of first argument
		for (int i=1; i < path2.length; i++) {
			path2[i] = depParents[path2[i-1]];
			if (path2[i] == -1) break;
		}
		int lca = -1;
		int lcaUp = 0, lcaDown = 0;
		outer:
		for (int i=0; i < path1.length; i++)
			for (int j=0; j < path2.length; j++) {
				if (path1[i] == -1 || path2[j] == -1) {
					break; // no path
				}
				if (path1[i] == path2[j]) {
					lca = path1[i];
					lcaUp = i;
					lcaDown = j;
					break outer;
				}
			}
		
		if (lca < 0) return features; // no dependency path (shouldn't happen)
		
		String[] dirs = new String[lcaUp + lcaDown];
		String[] strs = new String[lcaUp + lcaDown];
		String[] rels = new String[lcaUp + lcaDown];

		StringBuilder middleDirs = new StringBuilder();
		StringBuilder middleRels = new StringBuilder();
		StringBuilder middleStrs = new StringBuilder();

		if (lcaUp + lcaDown < 12) {
			
			for (int i=0; i < lcaUp; i++) {
				dirs[i] = "->";
				strs[i] = i > 0? tokens[path1[i]] : "";
				rels[i] = depTypes[path1[i]];
				//System.out.println("[" + depTypes[path1[i]] + "]->");
			}
			for (int j=0; j < lcaDown; j++) {
			//for (int j=lcaDown-1; j >= 0; j--) {
				dirs[lcaUp + j] = "<-";
				strs[lcaUp + j] = (lcaUp + j > 0)? tokens[path2[lcaDown-j]] : ""; // word taken from above
				rels[lcaUp + j] = depTypes[path2[lcaDown-j]];
				//System.out.println("[" + depTypes[path2[j]] + "]<-");
			}
			
			for (int i=0; i < dirs.length; i++) {
				middleDirs.append(dirs[i]);
				middleRels.append("[" + rels[i] + "]" + dirs[i]);
				middleStrs.append(strs[i] + "[" + rels[i] + "]" + dirs[i]);
			}
		}
		else {
				middleDirs.append("*LONG-PATH*");
				middleRels.append("*LONG-PATH*");
				middleStrs.append("*LONG-PATH*");
		}
	
		String basicDir = arg1ner + "|" + middleDirs.toString() + "|" + arg2ner;
		String basicDep = arg1ner + "|" + middleRels.toString() + "|" + arg2ner;
		String basicStr = arg1ner + "|" + middleStrs.toString() + "|" + arg2ner;
		

		// new left and right windows: all elements pointing to first arg, but not on path
		//List<Integer> lws = new ArrayList<Integer>();
		//List<Integer> rws = new ArrayList<Integer>();
		
		List<String> arg1dirs = new ArrayList<String>();
		List<String> arg1deps = new ArrayList<String>();
		List<String> arg1strs = new ArrayList<String>();
		List<String> arg2dirs = new ArrayList<String>();
		List<String> arg2deps = new ArrayList<String>();
		List<String> arg2strs = new ArrayList<String>();
		
		// pointing out of argument
		for (int i=0; i < tokens.length; i++) {
			// make sure itself is not either argument
//			if (i >= first[0] && i < first[1]) continue;
//			if (i >= second[0] && i < second[1]) continue;
			if (i == head1) continue;
			if (i == head2) continue;
			
			// make sure i is not on path
			boolean onPath = false;
			for (int j=0; j < lcaUp; j++) if (path1[j] == i) onPath = true;
			for (int j=0; j < lcaDown; j++) if (path2[j] == i) onPath = true;
			if (onPath) continue;
			// make sure i points to first or second arg
			//if (depParents[i] >= first[0] && depParents[i] < first[1]) lws.add(i);
			//if (depParents[i] >= second[0] && depParents[i] < second[1]) rws.add(i);
			if (depParents[i] == head1) {
				//lws.add(i);
				arg1dirs.add("->");				
				arg1deps.add("[" + depTypes[i] + "]->");
				arg1strs.add(tokens[i] + "[" + depTypes[i] + "]->");
			}
			if (depParents[i] == head2) {
				//rws.add(i);			
				arg2dirs.add("->");				
				arg2deps.add("[" + depTypes[i] + "]->");
				arg2strs.add("[" + depTypes[i] + "]->" + tokens[i]);
			}
		}
		
		
		// case 1: pointing into the argument pair structure (always attach to lhs):
		// pointing from arguments
		if (lcaUp == 0 && depParents[head1] != -1 || depParents[head1] == head2) {

			
			arg1dirs.add("<-");				
			arg1deps.add("[" + depTypes[head1] + "]<-");
			arg1strs.add(tokens[head1] + "[" + depTypes[head1] + "]<-");
			
//			if ((depParents[head1] >= first[0] && depParents[head1] < first[1]) ||
//				(depParents[head1] >= second[0] && depParents[head1] < second[1]))
//				;
//			else 
			
			if (depParents[depParents[head1]] != -1) {
				arg1dirs.add("<-");
				arg1deps.add("[" + depTypes[depParents[head1]] + "]<-");
				arg1strs.add(tokens[depParents[head1]] + "[" + depTypes[depParents[head1]] + "]<-");
			}
		}
		// if parent is not on path or if parent is 
		if (lcaDown == 0 && depParents[head2] != -1 || depParents[head2] == head1) { // should this actually attach to rhs???
			arg1dirs.add("<-");
			arg1deps.add("[" + depTypes[head2] + "]<-");
			arg1strs.add(tokens[head2] + "[" + depTypes[head2] + "]<-");

//			if ((depParents[head2] >= first[0] && depParents[head2] < first[1]) ||
//					(depParents[head2] >= second[0] && depParents[head2] < second[1]))
//					;
//				else 

			if (depParents[depParents[head2]] != -1) {
				arg1dirs.add("<-");
				arg1deps.add("[" + depTypes[depParents[head2]] + "]<-");
				arg1strs.add(tokens[depParents[head2]] + "[" + depTypes[depParents[head2]] + "]<-");
			}
		}
		
		// case 2: pointing out of argument
		
		//features.add("dir:" + basicDir);		
		//features.add("dep:" + basicDep);

		/*
		// left and right, including word
		for (String w1 : arg1strs)
			for (String w2 : arg2strs)
				features.add("str:" + w1 + "|" + basicStr + "|" + w2);
		*/
		
		// only left
		for (int i=0; i < arg1strs.size(); i++) {
			features.add("str:" + arg1strs.get(i) + "|" + basicStr);
//			features.add("dep:" + arg1deps.get(i) + "|" + basicDep);
//			features.add("dir:" + arg1dirs.get(i) + "|" + basicDir);
		}
		
		
		// only right
		for (int i=0; i < arg2strs.size(); i++) {
			features.add("str:" + basicStr + "|" + arg2strs.get(i));
//			features.add("dep:" + basicDep + "|" + arg2deps.get(i));
//			features.add("dir:" + basicDir + "|" + arg2dirs.get(i));
		}

		features.add("str:" + basicStr);
	
		
		
		
		
		
		// new features
		
//		features.add(inv + "|" + prefixes[1] + "|" + firstNer + "|" + mto + "|" + secondNer + "|" + suffixes[1]);
		
		return features;
	}
}
