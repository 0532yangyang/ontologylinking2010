package multir.preprocess.nell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import multir.util.HashCount;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

class HashIndex {
	HashMap<String, Set<Integer>> word_mappedid = new HashMap<String, Set<Integer>>();

	public HashIndex() {

	}

	public void indexWord(String word, int id) {
		String nword = word.toLowerCase().trim();
		if (nword.length() < 3)
			return;
		if (!word_mappedid.containsKey(nword)) {
			word_mappedid.put(nword, new HashSet<Integer>());
		}
		word_mappedid.get(nword).add(id);
	}

	public void indexWord(String[] words, int id) {
		for (String w : words) {
			indexWord(w, id);
		}
	}

	public void queryWord(List<String> words, List<Integer> dids, List<Integer> counts) {
		HashCount<Integer> hc = new HashCount<Integer>();
		for (String w : words) {
			w = w.toLowerCase();
			Set<Integer> docids = word_mappedid.get(w);
			if(docids== null)continue;
			for (int a : docids) {
				hc.add(a);
			}
		}
		hc.getAll(dids, counts);
	}

}

public class ManualMatch {

	static String[] relation_name = new String[4000];
	static String[][] relation_words = new String[4000][];
	static HashIndex hi = new HashIndex();
	static List<String[]>all = new ArrayList<String[]>();
	public static void loadRelationNames() throws IOException {
		String relationfile = "/projects/pardosa/data14/raphaelh/t/raw/freebase-relationIDs";
		DelimitedReader dr = new DelimitedReader(relationfile);
		String[] line;
		while ((line = dr.read()) != null) {
			int x = Integer.parseInt(line[0]);
			String y = line[1];
			relation_name[x] = y;
			relation_words[x] = relationName2Words(y);
		}
		dr.close();
	}

	private static String[] relationName2Words(String name) {
		String[] words = name.split("/|_");
		return words;
	}

	public static void loadPairs() throws IOException {
		loadRelationNames();
		String pairfile = "/projects/pardosa/data14/raphaelh/t/raw/matches-uniquepairs-relationIDs.sortedByArgsRel";
		DelimitedReader dr = new DelimitedReader(pairfile);
		String[] line;
		int lid = 0;
		
		while ((line = dr.read()) != null) {
			all.add(line);
			String w1 = line[0];
			String w2 = line[1];
			int rid = Integer.parseInt(line[4]);
			String rname = relation_name[rid];
			String []rwords = relation_words[rid];
			hi.indexWord(w1, lid);
			hi.indexWord(w2,lid);
			hi.indexWord(rwords, lid);
			lid++;
		}
		dr.close();
	}

	/**
	 * @param args
	 */
	public static List<String> splitStringByCapital(String a){
		List<String>result = new ArrayList<String>();
		char[]ary = a.toCharArray();
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<ary.length;i++){
			if(ary[i] >='A' && ary[i]<='Z'){
				String w = sb.toString();
				if(w.length()>0)result.add(a);
				sb = new StringBuilder();
			}
			sb.append(ary[i]);
		}
		return result;
		
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String outputfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/manualmatch_help";
		DelimitedWriter dw = new DelimitedWriter(outputfile);
		loadPairs();
		NellOntology no = new NellOntology();
		for(NellRelation nr: no.nellRelationList){
			if(nr.seedInstances.size()>0){
				List<String>rnamewords = splitStringByCapital(nr.relation_name);
				for(String []seed: nr.seedInstances){
					List<String>query = new ArrayList<String>();
					query.addAll(rnamewords);
					query.add(seed[0]);
					query.add(seed[1]);
					List<Integer>ids = new ArrayList<Integer>();
					List<Integer>counts = new ArrayList<Integer>();

					hi.queryWord(query, ids, counts);
					dw.write("=====",nr.relation_name,seed[0],seed[1],"=====");
					System.out.println(nr.relation_name+"\t<"+seed[0]+", "+seed[1]+">");
					for(int i=0;i<ids.size();i++){
						int did = ids.get(i);
						int count = counts.get(i);
						String []m = all.get(did);
						int mrel = Integer.parseInt(m[4]);
						if(count>=2){
							dw.write(count,m[0],m[1],relation_name[mrel]);
						}
						//System.out.println(count+"\t"+m[0]+"\t"+m[1]+"\t"+relation_name[mrel]);
					}
				}
			}
		}
		dw.close();
	}
	
	

}
