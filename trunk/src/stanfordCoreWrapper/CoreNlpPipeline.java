package stanfordCoreWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreNlpPipeline {
	Properties props;
	StanfordCoreNLP pipeline;
	public CoreNlpPipeline(){
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		// +", parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}
	
	public List<List<String[]>> parse(String rawText){
		// create an empty Annotation just with the given text
		List<List<String[]>> result = new ArrayList<List<String[]>>(); 
		Annotation document = new Annotation(rawText);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			List<String[]> parsed_sentence = new ArrayList<String[]>();
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);
				String []r = new String[]{word,pos,ne};
				parsed_sentence.add(r);
				//System.out.println(word + "\t" + pos + "\t" + ne);
			}
			result.add(parsed_sentence);
		}
		return result;
		
	}
	
	public List<String[]> parse2lines(String rawtext){
		List<String[]>result = new ArrayList<String[]>();
		List<List<String[]>> parsed = parse(rawtext);
		for (List<String[]> s : parsed) {
			String towrite[] = new String[5];

			StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder(), sb3 = new StringBuilder();
			for (String[] w : s) {
				// if(w[0].contains(" ") || w[1].contains(" ")||
				// w[2].contains(" ")){
				// System.err.println("blank happens!!!");
				// }
				w[0] = w[0].replaceAll(" ", "_");
				w[1] = w[1].replaceAll(" ", "_");
				w[2] = w[2].replaceAll(" ", "_");
				sb1.append(w[0] + " ");
				sb2.append(w[1] + " ");
				sb3.append(w[2] + " ");
			}
			int id = 0;
			towrite[0] = sb1.toString();
			towrite[1] = sb2.toString();
			towrite[2] = sb3.toString();
			result.add(towrite);
		}
		return result;
	}
	
	
	
}
