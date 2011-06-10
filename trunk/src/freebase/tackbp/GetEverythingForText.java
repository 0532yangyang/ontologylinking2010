package freebase.tackbp;

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
import java.util.HashMap;
import java.util.List;

import multir.util.FileOperations;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.parser.ensemble.maltparser.MaltConsoleEngine;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class GetEverythingForText {

	static void split() throws IOException {
		HashMap<Integer, Integer> sid2paragraph = new HashMap<Integer, Integer>();
		{
			//load meta data
			DelimitedReader dr = new DelimitedReader(Main.file_tacmeta);
			String[] l;
			while ((l = dr.read()) != null) {
				int a = Integer.parseInt(l[0]);
				int b = Integer.parseInt(l[1]);
				sid2paragraph.put(a, b);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_tactext);
			DelimitedWriter dw[] = new DelimitedWriter[Main.datas.length];
			for (int i = 0; i < dw.length; i++) {
				dw[i] = new DelimitedWriter(Main.file_tacsplit_template.replace("$DNUM$", Main.datas[i]) + i);
			}
			String[] l;
			int ln = 0;
			while ((l = dr.read()) != null) {
				//if(++ln > 100000)break;
				int sid = Integer.parseInt(l[0]);
				int pid = sid2paragraph.get(sid);
				int partid = sid % Main.datas.length;
				dw[partid].write(sid, pid, l[1]);
			}
			dr.close();
			for (DelimitedWriter dw0 : dw) {
				dw0.close();
			}
		}
	}

	static String opennlpTokenizerModel = "models/en-token.bin";
	static String opennlpPOSModel = "models/en-pos-maxent.bin";
	static String maltparserModelDir = "models";
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-3-all2008.ser.gz";
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-4-conll.ser.gz";
	static String stanfordNerModel = "models/ner-eng-ie.crf-4-conll-distsim.ser.gz";

	public static void parseChunk(int partId) throws IOException {

		//int partId = Integer.parseInt(args[0]);

		String inputFile = Main.file_tacsplit_template.replace("$DNUM$", Main.datas[partId]) + partId;
		//String inputFile = "/projects/pardosa/data01/clzhang/tmp/wp/testfile";

		/** split into ids and noids files*/
		{
			InputStream modelIn = new FileInputStream("models/en-sent.bin");
			SentenceModel model = null;
			try {
				model = new SentenceModel(modelIn);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (modelIn != null) {
					try {
						modelIn.close();
					} catch (IOException e) {
					}
				}
			}
			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
			DelimitedReader dr = new DelimitedReader(inputFile);
			BufferedWriter w1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".ids"),
					"utf-8"));
			BufferedWriter w2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".noids"),
					"utf-8"));
			String[] l;
			int tempsentenceid = 0;
			while ((l = dr.read()) != null) {
				String sentences = l[2];
				String[] all = sentenceDetector.sentDetect(sentences);
				for (String a : all) {
					w1.write(tempsentenceid + "\t" + l[0] + "\t" + l[1] + "\n");
					w2.write(a + "\n");
					tempsentenceid++;
				}
				//				if (count++ > 100) {
				//					break;
				//				}
			}
			dr.close();
			w1.close();
			w2.close();
		}
		//
		{
			InputStream modelIn = new BufferedInputStream(new FileInputStream(opennlpTokenizerModel));
			TokenizerModel model = new TokenizerModel(modelIn);
			Tokenizer tokenizer = new TokenizerME(model);
			{
				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".noids"),
						"utf-8"));
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(inputFile + ".tokens"), "utf-8"));
				String line = null;
				while ((line = r.readLine()) != null) {
					String[] tokens = tokenizer.tokenize(line);
					for (int i = 0; i < tokens.length; i++) {
						if (i > 0)
							w.write(" ");
						w.write(tokens[i]);
					}
					w.write("\n");
				}
				r.close();
				w.close();
			}
		}
		//		{
		//			InputStream modelIn = new BufferedInputStream(new FileInputStream(opennlpPOSModel));
		//			POSModel model = new POSModel(modelIn);
		//			POSTagger postagger = new POSTaggerME(model);
		//			{
		//				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".tokens"),
		//						"utf-8"));
		//				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".pos"),
		//						"utf-8"));
		//				String line = null;
		//				while ((line = r.readLine()) != null) {
		//					String[] toks = line.split(" ");
		//					String[] tags = postagger.tag(toks);
		//					for (int i = 0; i < tags.length; i++) {
		//						if (i > 0)
		//							w.write(" ");
		//						w.write(tags[i]);
		//					}
		//					w.write("\n");
		//				}
		//				r.close();
		//				w.close();
		//			}
		//		}
		//		{
		//			// create working directory
		//			File workingDir = new File(inputFile + ".wd");
		//			File modelDir = new File(maltparserModelDir);
		//
		//			workingDir.mkdir();
		//
		//			// convert to maltin format
		//			{
		//				BufferedReader r1 = new BufferedReader(new InputStreamReader(
		//						new FileInputStream(inputFile + ".tokens"), "utf-8"));
		//				BufferedReader r2 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".pos"),
		//						"utf-8"));
		//				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
		//						new FileOutputStream(inputFile + ".maltin"), "utf-8"));
		//				String line = null;
		//				while ((line = r1.readLine()) != null) {
		//					String[] tokens = line.split(" ");
		//					String[] postags = r2.readLine().split(" ");
		//					for (int i = 0; i < tokens.length; i++) {
		//						w.write((i + 1) + "\t" + tokens[i] + "\t_\t" + postags[i] + "\t" + postags[i] + "\t_\n");
		//					}
		//					w.write("\n");
		//				}
		//				r1.close();
		//				r2.close();
		//				w.close();
		//			}
		//
		//			String[] parameters = { "-m", "parse", "-c", "conll08-nivreeager-ltr", "-l", "liblinear", "-llv", "error",
		//					"-v", "info", "-w", workingDir.getAbsolutePath(), "-md", modelDir.getAbsolutePath(), "-i",
		//					inputFile + ".maltin", "-o", inputFile + ".maltout" };
		//
		//			MaltConsoleEngine engine = new MaltConsoleEngine();
		//			engine.startEngine(parameters);
		//
		//			// remove working directory
		//			FileOperations.rmr(workingDir.getAbsolutePath());
		//		}
		//		{
		//			// convert maltout back to tsv
		//			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".maltout"),
		//					"utf-8"));
		//			DelimitedWriter w = new DelimitedWriter(inputFile + ".deps");
		//
		//			String line = null;
		//			List<String[]> l = new ArrayList<String[]>();
		//
		//			int count = 0;
		//			while (true) {
		//
		//				// read dependencies
		//				l.clear();
		//				while ((line = r.readLine()) != null && line.length() > 0) {
		//					String[] t = line.split("\t");
		//					l.add(t);
		//				}
		//
		//				if (l.size() > 0) {
		//					StringBuilder sbRelType = new StringBuilder();
		//					StringBuilder sbParent = new StringBuilder();
		//					StringBuilder sbPos = new StringBuilder();
		//
		//					for (int i = 0; i < l.size(); i++) {
		//						String[] t = l.get(i);
		//						if (i > 0) {
		//							sbRelType.append(" ");
		//							sbParent.append(" ");
		//							sbPos.append(" ");
		//						}
		//						sbRelType.append(t[7].toUpperCase());
		//						sbParent.append(Integer.parseInt(t[6]) - 1);
		//						sbPos.append(t[3]);
		//					}
		//					w.write(sbRelType.toString(), sbParent.toString(), sbPos.toString());
		//					count++;
		//				}
		//
		//				if (line == null)
		//					break;
		//			}
		//			r.close();
		//			w.close();
		//		}
		{
			// run Jenny's NER system
			AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(stanfordNerModel);

			BufferedReader r1 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".tokens"),
					"utf-8"));
			//			BufferedReader r2 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".ids"),
			//					"utf-8"));
			DelimitedReader dr2 = new DelimitedReader(inputFile + ".ids");
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".ner"),
					"utf-8"));

			String line = null;
			while ((line = r1.readLine()) != null) {
				//int sentenceID = Integer.parseInt(r2.readLine());
				String[] ids = dr2.read();
				String[] tokens = line.split(" ");

				// convert sentence tokens
				List<Word> sentence = new ArrayList<Word>();
				for (int i = 0; i < tokens.length; i++)
					sentence.add(new Word(tokens[i]));

				List<CoreLabel> c = classifier.classifySentence(sentence);

				String[] labels = new String[tokens.length];
				for (int i = 0; i < labels.length; i++) {
					labels[i] = c.get(i).get(AnswerAnnotation.class);
				}

				// find consecutive labels
				int start = -1;
				for (int i = 0; i < labels.length; i++) {
					if (start == -1) {
						if (!labels[i].equals("O"))
							start = i;
					} else {
						if (!labels[i].equals(labels[start])) {
							if (!labels[start].equals("O")) {
								//System.out.println(t[0] + " " + start + " " + i + labels[start] + substring(tokens, start, i));
								w.write(ids[0] + "\t" + ids[1] + "\t" + ids[2] + "\t" + start + "\t" + i + "\t"
										+ substring(tokens, start, i) + "\t" + labels[start] + "\n");
								//w.flush();
							}
							start = i;
						}
					}
				}
				if (start != -1 && !labels[start].equals("O"))
					w.write(ids[0] + "\t" + ids[1] + "\t" + ids[2] + "\t" + start + "\t" + tokens.length + "\t"
							+ substring(tokens, start, tokens.length) + "\t" + labels[start] + "\n");
			}
			w.close();
			r1.close();
			dr2.close();
		}
	}

	private static String substring(String[] tokens, int start, int end) {
		String s = "";
		for (int i = start; i < end; i++) {
			if (i > start)
				s += " ";
			s += tokens[i];
		}
		return s;
	}

	public static void mergeOneKind(String suffix, String outputFile) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(outputFile + suffix);
		for (int i = 0; i < Main.datas.length; i++) {
			String input = Main.fout_wexsplit_template.replace("$DNUM$", Main.datas[i]) + i;
			DelimitedReader dr = new DelimitedReader(input + suffix);
			String[] l;
			while ((l = dr.read()) != null) {
				dw.write(l);
			}
			dr.close();
		}
		dw.close();
	}

	//	public static void main(String[] args) throws IOException {
	//		mergeOneKind(".deps", Main.file_parsed_wikisections);
	//		mergeOneKind(".ner", Main.file_parsed_wikisections);
	//		mergeOneKind(".tokens", Main.file_parsed_wikisections);
	//		mergeOneKind("", Main.file_parsed_wikisections);
	//	}
	static int[] previousLargestId;

	public static void mainX(String[] args) throws IOException {

		//		String inputFile = args[0];
		//		int numChunks = 100;
		previousLargestId = new int[Main.datas.length];
		getPreviousLargetsId();
		mergeAlignID(".tokens");
		//		mergeAlignID(".pos");
		//		mergeAlignID(".deps");
		mergeNer();
	}

	private static void getPreviousLargetsId() throws IOException {
		int previousSentenceId = 0;
		for (int i = 0; i < Main.datas.length; i++) {
			previousLargestId[i] = previousSentenceId + 1;
			String fname = Main.file_tacsplit_template.replace("$DNUM$", Main.datas[i]) + i;
			DelimitedReader r1 = new DelimitedReader(fname + ".ids");
			String[] l1 = r1.read();
			while ((l1 = r1.read()) != null) {
				int sentenceId = previousLargestId[i] + Integer.parseInt(l1[0]);
				previousSentenceId = sentenceId;
			}
		}
		for (int i = 0; i < previousLargestId.length; i++) {
			D.p(previousLargestId[i]);
		}
	}

	private static void mergeAlignID(String suffix) throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Main.file_parsedtac + suffix), "utf-8"));
		for (int i = 0; i < Main.datas.length; i++) {
			String fname = Main.file_tacsplit_template.replace("$DNUM$", Main.datas[i]) + i;
			DelimitedReader r1 = new DelimitedReader(fname + ".ids");
			BufferedReader r2 = new BufferedReader(new InputStreamReader(new FileInputStream(fname + suffix), "utf-8"));
			String[] l1 = r1.read();
			String line2 = r2.readLine();
			while (l1 != null && line2 != null) {
				int sentenceId = Integer.parseInt(l1[1]);
				int articleId = 0;
				int sectionId = Integer.parseInt(l1[2]);
				w.write(sentenceId + "\t" + articleId + "\t" + sectionId + "\t" + line2 + "\n");
				l1 = r1.read();
				line2 = r2.readLine();
			}
			if (l1 != null || line2 != null)
				System.out.println(fname + suffix + " has incorrect number of lines");
			r1.close();
			r2.close();

		}
		w.close();
	}

	private static void mergeNer() throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.file_parsedtac
				+ ".ner"), "utf-8"));
		for (int i = 0; i < Main.datas.length; i++) {
			String fname = Main.file_tacsplit_template.replace("$DNUM$", Main.datas[i]) + i;
			//BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fname + ".ner"), "utf-8"));
			DelimitedReader dr = new DelimitedReader(fname + ".ner");
			String[] l;
			while ((l = dr.read()) != null) {
				if (l.length == 7) {
					int sentenceId = Integer.parseInt(l[1]);
					w.write(sentenceId + "\t" + "0" + "\t" + l[2] + "\t" + l[3] + "\t" + l[4] + "\t" + l[5] + "\t"
							+ l[6] + "\n");
				}
			}
			dr.close();
		}
		w.close();

	}

	public static void main(String[] args) throws IOException {
		//giveParagraphId();
		//split();
		parseChunk(Integer.parseInt(args[0]));

	}

}
