package javatools.stanford;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javatools.administrative.D;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagger {

	static String modelFile = "/projects/pardosa/s1/clzhang/workspace/OntoLink/model/bidirectional-distsim-wsj-0-18.tagger";
	MaxentTagger tagger;

	public PosTagger() throws IOException, ClassNotFoundException {
		tagger = new MaxentTagger(modelFile);

	}

	/**
	 * @return List of sentence tagging result
	 *	String []: length 3 string, storing the result of one word
	 * */
	public List<List<String[]>> posText(String str) throws IOException {
		List<List<String[]>> result= new ArrayList<List<String[]>>();
		// convert String into InputStream
		InputStream is = new ByteArrayInputStream(str.getBytes());
		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<ArrayList<? extends HasWord>> sentences = tagger.tokenizeText(br);
		for (ArrayList<? extends HasWord> sentence : sentences) {
			List<String[]>onesen = new ArrayList<String[]>();
			ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
			for (TaggedWord tw : tSentence) {
				
				D.p(tw.toString());
			}
			result.add(onesen);
		}
		br.close();
		return result;
	}

	/**
	 * @param args
	 */
	public static void main2(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: java TaggerDemo modelFile fileToTag");
			return;
		}
		MaxentTagger tagger = new MaxentTagger(args[0]);
		@SuppressWarnings("unchecked")
		List<ArrayList<? extends HasWord>> sentences = tagger.tokenizeText(new BufferedReader(new FileReader(args[1])));
		for (ArrayList<? extends HasWord> sentence : sentences) {
			ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
			for (TaggedWord tw : tSentence) {
				D.p(tw.toString());
			}
			// System.out.println(Sentence.listToString(tSentence, false));
		}
	}
	public static void main(String []args)throws Exception{
		PosTagger postagger = new PosTagger();
		postagger.posText("He visited the University of Washington last week");
	}

}
