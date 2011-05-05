package multir.preprocess;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import multir.util.FileOperations;
import multir.util.delimited.DelimitedWriter;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.ensemble.maltparser.MaltConsoleEngine;

// /projects/pardosa/s2/raphaelh/cj/reranking-parser

public class PreprocessChunkCharniak {

	static String opennlpTokenizerModel = "../models/en-token.bin";
	static String opennlpPOSModel = "../models/en-pos-maxent.bin";
	static String maltparserModelDir = "../models";
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-3-all2008.ser.gz";
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-4-conll.ser.gz";
	static String stanfordNerModel = "../models/ner-eng-ie.crf-4-conll-distsim.ser.gz";

	public static void main(String[] args) throws IOException {

		String inputFile = args[0];
		
		// convert input file (ID\tsentence) to input for charniak-johnson parser
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(inputFile), "utf-8"));
			BufferedWriter w1 = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(inputFile + ".cjin"), "utf-8"));
			String line = null;
			while ((line = r.readLine())!= null) {
				int pos = line.indexOf("\t");
				String v = line.substring(pos+1);
				if (v.indexOf("</s>") >= 0)
					System.out.println("ERROR: " + line);				
				w1.write("<s> " + v + " </s>\n");
			}
			w1.close();
			r.close();
		}

		System.out.println("(cat " + inputFile + ".cjin | ./parse.sh > " + inputFile + ".cjout) >& " + inputFile + ".cjerr &");
		
	}
}
