package javatools.string;


import edu.mit.jwi.IDictionary;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.*;
import edu.mit.*;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class WordNetJwi {
	String wnhome;
	IDictionary dict;

	// wnhome: the position where WordNet 2.1 is, in 128.208.3.111, it is in
	// C:\\Program Files\\WordNet\\2.1
	public WordNetJwi(String wnhome) throws MalformedURLException {
		this.wnhome = wnhome;
		String path = wnhome + File.separator + "dict";
		URL url = new URL("file", null, path);

		// construct the dictionary object and open it
		this.dict = new Dictionary(url);
		this.dict.open();
	}
	
	public WordNetJwi() throws MalformedURLException {
		this.wnhome = "/scratch/Downloads/WordNet-3.0/";
		String path = wnhome + File.separator + "dict";
		URL url = new URL("file", null, path);

		// construct the dictionary object and open it
		this.dict = new Dictionary(url);
		this.dict.open();
	}

	// only the 1st meaning of the word, its hypernyms
	public HashSet<String> getHypernyms(String wordStr) {
		HashSet<String> result = new HashSet<String>();
		// get the synset
		IIndexWord idxWord = getNounIIndexWord(wordStr);
		if(idxWord == null)return result;
		
		for (int myi = 0; myi < idxWord.getWordIDs().size(); myi++) {
			IWordID wordID = idxWord.getWordIDs().get(myi);
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();

			// iterate over words associated with the synset
			for (IWord w : synset.getWords()) {
				result.add(w.getLemma());
			}
			// get the hypernyms

			List<ISynsetID> hypernyms = synset
					.getRelatedSynsets(Pointer.HYPERNYM);

			// print out each hypernym�s id and synonyms
			List<IWord> words;
			for (ISynsetID sid : hypernyms) {
				words = dict.getSynset(sid).getWords();

				for (Iterator<IWord> i = words.iterator(); i.hasNext();) {

					result.add(i.next().getLemma());
				}
			}
		}

		return result;
	}

	public boolean isDictWord(String wordStr){
		boolean res= true;
		IIndexWord idxWord = dict.getIndexWord(wordStr, POS.NOUN);
		if(idxWord==null){
			idxWord = dict.getIndexWord(wordStr, POS.VERB);
		}
		if(idxWord==null){
			idxWord = dict.getIndexWord(wordStr, POS.ADJECTIVE);
		}
		if(idxWord==null){
			idxWord = dict.getIndexWord(wordStr, POS.ADVERB);
		}
		if(idxWord==null){
			res = false;
		}
		return res; 
		
	}
	private IIndexWord getNounIIndexWord(String wordStr){
		
		IIndexWord idxWord = dict.getIndexWord(wordStr, POS.NOUN);
		if (idxWord == null && wordStr.length() > 2) {
			wordStr = wordStr.substring(0, wordStr.length() - 1);
			idxWord = dict.getIndexWord(wordStr, POS.NOUN);
		}
		if (idxWord == null && wordStr.length() > 2) {
			wordStr = wordStr.substring(0, wordStr.length() - 1);
			idxWord = dict.getIndexWord(wordStr, POS.NOUN);
		}
		return idxWord;
	}
	
	
	public HashSet<String> getSynonyms(String wordstr) {
		HashSet<String>result = new HashSet<String>();
		// look up first sense of the word "dog"
		IIndexWord idxWord = getNounIIndexWord(wordstr);
		if(idxWord == null)return result;
		for(int myi = 0; myi<idxWord.getWordIDs().size();myi++){
			IWordID wordID = idxWord.getWordIDs().get(myi); // 1st meaning
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();
			for (IWord w : synset.getWords()) {
				result.add(w.getLemma());
			}
		}
		return result;
	}
	
	

	public void getholonym(String wordStr) {

		// get the synset
		IIndexWord idxWord = dict.getIndexWord(wordStr, POS.NOUN);

		IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning

		IWord word = dict.getWord(wordID);
		ISynset synset = word.getSynset();

		// get the hypernyms

		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPONYM);

		// print out each hypernym�s id and synonyms
		List<IWord> words;
		for (ISynsetID sid : hypernyms) {
			words = dict.getSynset(sid).getWords();
			System.out.print(sid + " {");
			for (Iterator<IWord> i = words.iterator(); i.hasNext();) {
				System.out.print(i.next().getLemma());
				if (i.hasNext())
					System.out.print(", ");
			}
			System.out.println("}");
		}
	}

	/**As a phrase, check if A is the synonymns of B
	 * For example: A= place of birth; B=birthplace */
	public double AisBSynonymsWhole(String a,String b){
		HashSet<String>set_a = this.getSynonyms(a);
		HashSet<String>set_b = this.getSynonyms(b);
		String bb = b.replace("\\s", "_");
		String aa = a.replace("\\s", "_");
		if(set_a.contains(bb) || set_b.contains(aa)){
			return 1;
		}else{
			return 0;
		}
	}
	
	public double AisBHyernymsWhole(String a,String b){
		HashSet<String>set_a = this.getHypernyms(a);
		HashSet<String>set_b = this.getHypernyms(b);
		String bb = b.replace("\\s", "_");
		String aa = a.replace("\\s", "_");
		if(set_a.contains(bb) || set_b.contains(aa)){
			return 1;
		}else{
			return 0;
		}
	}
	
	public int AisBSynonymsPart(String []a0,String[] b0){
		int s = 0;
		for(String a:a0){
			for(String b:b0){
				HashSet<String>set_a = this.getSynonyms(a);
				HashSet<String>set_b = this.getSynonyms(b);
				String bb = b.replace("\\s", "_");
				String aa = a.replace("\\s", "_");
				if(set_a.contains(bb) || set_b.contains(aa)){
					s++;
				}
			}
		}
		return s;
	}
	
	public int AisBHyernymsPart(String []a0, String[] b0){
		int s = 0;
		for(String a:a0){
			for(String b:b0){
				HashSet<String>set_a = this.getHypernyms(a);
				HashSet<String>set_b = this.getHypernyms(b);
				String bb = b.replace("\\s", "_");
				String aa = a.replace("\\s", "_");
				if(set_a.contains(bb) || set_b.contains(aa)){
					s++;
				}
			}
		}
		return s;
	}
	/**In order to get the From word net, extend a0[], b0[] into two big sets, and compare them*/
	public int AandBSetCompare(String []a0,String []b0){
		int res = 0;
		List<String> al = new ArrayList<String>();
		List<String> bl = new ArrayList<String>();
		
		for(String a: a0){
			HashSet<String>set_a_syn = this.getSynonyms(a);
			HashSet<String>set_a_hyp = this.getHypernyms(a);
			al.addAll(set_a_syn);
			al.addAll(set_a_hyp);
		}
		for(String b:b0){
			HashSet<String>set_b_syn = this.getSynonyms(b);
			HashSet<String>set_b_hyp = this.getHypernyms(b);
			bl.addAll(set_b_syn);
			bl.addAll(set_b_hyp);
		}
		Collections.sort(al);
		Collections.sort(bl);
		int i=0,j=0;
		while(i<al.size()&& j<bl.size()){
			String x = al.get(i);
			String y = bl.get(j);
			if(x.equals(y)){
				res++;
				i++;j++;
			}else if(x.compareTo(y)>0){
				j++;
			}else{
				i++;
			}
		}
		return res;
	}
	public void getSynsets()throws Exception{
		IIndexWord iiw = dict.getIndexWord("acquiring", POS.NOUN);
		IWordID wordID = iiw.getWordIDs().get(0);
		IWord word = dict.getWord(wordID);
		System.out.println("Id = " + wordID);
		System.out.println("Lemma = " + word.getLemma());
		System.out.println("Gloss = " + word.getSynset().getGloss());

	}
	public static void main(String[] args) throws Exception {
		//WordNetJwi wnjwi = new WordNetJwi(args[0]);
		WordNetJwi wnjwi = new WordNetJwi();
		wnjwi.getSynsets();
		
		HashSet<String> syns = wnjwi.getSynonyms("nameofcity");
		//HashSet<String> syns = wnjwi.getSynonyms("");
		HashSet<String>hypers = wnjwi.getHypernyms("them");
		double a = wnjwi.AisBHyernymsWhole("consort", "spouse s");
		System.out.println(hypers);
		a = wnjwi.AandBSetCompare("consort partner".split("\\s"),"spouse s".split("\\s"));
		System.out.println(a);
		// wnjwi.getSynonyms("races");
		// wnjwi.getholonym("consort");
	}
}
