package stanfordCoreWrapper;

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

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;

import multir.util.FileOperations;
import multir.util.delimited.DelimitedWriter;
import edu.stanford.nlp.parser.ensemble.maltparser.MaltConsoleEngine;

import javatools.filehandlers.DelimitedReader;

public class MaltiDep {
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-4-conll-distsim.ser.gz";
	static String opennlpTokenizerModel = "models/en-token.bin";
	static String opennlpPOSModel = "models/en-pos-maxent.bin";
	static String maltparserModelDir = "models";
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-3-all2008.ser.gz";
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-4-conll.ser.gz";
	static String stanfordNerModel = "models/ner-eng-ie.crf-4-conll-distsim.ser.gz";

	public static void removeDep2(String inputFile, int tokenIndex) throws IOException {
		{
			File workingDir = new File(inputFile + ".wd");
			// remove working directory
			FileOperations.rmr(workingDir.getAbsolutePath());
		}
		{
			(new File(inputFile + ".deps")).deleteOnExit();
			(new File(inputFile + ".maltout")).deleteOnExit();
			(new File(inputFile + ".maltin")).deleteOnExit();
		}
	}

	public static void getDep(String inputFile, int tokenIndex) throws IOException {
		//		{
		//			InputStream modelIn = new BufferedInputStream(new FileInputStream(opennlpPOSModel));
		//			POSModel model = new POSModel(modelIn);
		//			POSTagger postagger = new POSTaggerME(model);
		//			{
		//				//BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
		//				DelimitedReader dr = new DelimitedReader(inputFile);
		//				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".pos"),
		//						"utf-8"));
		//				String[] l;
		//				int count = 0;
		//				while ((l = dr.read()) != null) {
		//					//					if (count++ > 100)
		//					//						break;
		//					String[] toks = l[tokenIndex].split(" ");
		//					String[] tags = postagger.tag(toks);
		//					for (int i = 0; i < tags.length; i++) {
		//						if (i > 0)
		//							w.write(" ");
		//						w.write(tags[i]);
		//					}
		//					w.write("\n");
		//				}
		//				dr.close();
		//				w.close();
		//			}
		//		}
		{
			// create working directory
			File workingDir = new File(inputFile + ".wd");
			File modelDir = new File(maltparserModelDir);

			workingDir.mkdir();

			// convert to maltin format
			{
				DelimitedReader dr = new DelimitedReader(inputFile);
				BufferedReader r2 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".pos"),
						"utf-8"));
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(inputFile + ".maltin"), "utf-8"));
				//String line = null;
				String[] l;
				int count = 0;
				while ((l = dr.read()) != null) {
					//					if (count++ > 100)
					//						break;
					String[] tokens = l[tokenIndex].split(" ");
					String[] postags = r2.readLine().split(" ");
					for (int i = 0; i < tokens.length; i++) {
						w.write((i + 1) + "\t" + tokens[i] + "\t_\t" + postags[i] + "\t" + postags[i] + "\t_\n");
					}
					w.write("\n");
				}
				dr.close();
				r2.close();
				w.close();
			}

			String[] parameters = { "-m", "parse", "-c", "conll08-nivreeager-ltr", "-l", "liblinear", "-llv", "error",
					"-v", "info", "-w", workingDir.getAbsolutePath(), "-md", modelDir.getAbsolutePath(), "-i",
					inputFile + ".maltin", "-o", inputFile + ".maltout" };

			MaltConsoleEngine engine = new MaltConsoleEngine();
			engine.startEngine(parameters);

			// remove working directory
			FileOperations.rmr(workingDir.getAbsolutePath());
		}
		{
			// convert maltout back to tsv
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".maltout"),
					"utf-8"));
			DelimitedWriter w = new DelimitedWriter(inputFile + ".deps");

			String line = null;
			List<String[]> l = new ArrayList<String[]>();

			int count = 0;
			while (true) {

				// read dependencies
				l.clear();
				while ((line = r.readLine()) != null && line.length() > 0) {
					String[] t = line.split("\t");
					l.add(t);
				}

				if (l.size() > 0) {
					StringBuilder sbRelType = new StringBuilder();
					StringBuilder sbParent = new StringBuilder();
					StringBuilder sbPos = new StringBuilder();

					for (int i = 0; i < l.size(); i++) {
						String[] t = l.get(i);
						if (i > 0) {
							sbRelType.append(" ");
							sbParent.append(" ");
							sbPos.append(" ");
						}
						sbRelType.append(t[7].toUpperCase());
						sbParent.append(Integer.parseInt(t[6]) - 1);
						sbPos.append(t[3]);
					}
					w.write(sbRelType.toString(), sbParent.toString(), sbPos.toString());
					count++;
				}

				if (line == null)
					break;
			}
			r.close();
			w.close();
		}
	}

	public static void getDep_old(String inputFile, int tokenIndex) throws IOException {

		{
			DelimitedReader dr = new DelimitedReader(inputFile);
			String[] l;
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".maltin"),
					"utf-8"));
			File workingDir = new File(inputFile + ".wd");
			File modelDir = new File(maltparserModelDir);
			int count = 0;
			while ((l = dr.read()) != null) {
				count++;
				//				if (count > 100)
				//					break;
				//l[tokenIndex] = l[tokenIndex].replaceAll("POSTAG", "");
				String[] tokens = l[tokenIndex].split(" ");
				String[] postags = l[tokenIndex + 1].split(" ");
				for (int i = 0; i < tokens.length; i++) {
					w.write((i + 1) + "\t" + tokens[i] + "\t_\t" + postags[i] + "\t" + postags[i] + "\t_\n");
				}
				w.write("\n");
			}
			w.close();
			String[] parameters = { "-m", "parse", "-c", "conll08-nivreeager-ltr", "-l", "liblinear", "-llv", "error",
					"-v", "info", "-w", workingDir.getAbsolutePath(), "-md", modelDir.getAbsolutePath(), "-i",
					inputFile + ".maltin", "-o", inputFile + ".maltout" };

			MaltConsoleEngine engine = new MaltConsoleEngine();
			engine.startEngine(parameters);
			// remove working directory
			FileOperations.rmr(workingDir.getAbsolutePath());
		}
		{
			// convert maltout back to tsv
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".maltout"),
					"utf-8"));
			DelimitedWriter w = new DelimitedWriter(inputFile + ".deps");

			String line = null;
			List<String[]> l = new ArrayList<String[]>();

			int count = 0;
			while (true) {

				// read dependencies
				l.clear();
				while ((line = r.readLine()) != null && line.length() > 0) {
					String[] t = line.split("\t");
					l.add(t);
				}

				if (l.size() > 0) {
					StringBuilder sbRelType = new StringBuilder();
					StringBuilder sbParent = new StringBuilder();
					StringBuilder sbPos = new StringBuilder();

					for (int i = 0; i < l.size(); i++) {
						String[] t = l.get(i);
						if (i > 0) {
							sbRelType.append(" ");
							sbParent.append(" ");
							sbPos.append(" ");
						}
						sbRelType.append(t[7].toUpperCase());
						sbParent.append(Integer.parseInt(t[6]) - 1);
						sbPos.append(t[3]);
					}
					w.write(sbRelType.toString(), sbParent.toString(), sbPos.toString());
					count++;
				}

				if (line == null)
					break;
			}
			r.close();
			w.close();
		}
		//		{
		//			DelimitedReader dr = new DelimitedReader(inputFile);
		//			DelimitedReader dr2 = new DelimitedReader(inputFile + ".deps");
		//			DelimitedWriter dw = new DelimitedWriter(inputFile + ".withdeps");
		//			String[] l;
		//			String[] l2;
		//			while ((l = dr.read()) != null) {
		//				l2 = dr2.read();
		//				if (l2 != null) {
		//					String[] w = new String[l.length + l2.length];
		//					System.arraycopy(l, 0, w, 0, l.length);
		//					System.arraycopy(l2, 0, w, l.length, l2.length);
		//					dw.write(w);
		//				} else {
		//					break;
		//				}
		//			}
		//			dw.close();
		//		}
		{
			//(new File(inputFile + ".deps")).deleteOnExit();
			(new File(inputFile + ".maltout")).deleteOnExit();
			(new File(inputFile + ".maltin")).deleteOnExit();
		}
	}

	public static void main(String[] args) throws IOException {
		String inputFile = "/projects/pardosa/data14/clzhang/MaltiDep/a";
		getDep(inputFile, 0);
	}

	public static void main2(String[] args) throws IOException {
		String inputFile = "/projects/pardosa/data01/clzhang/tmp/wp/wex_stanfordtext.part0";
		{
			DelimitedReader dr = new DelimitedReader(inputFile);
			String[] l;
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile + ".maltin"),
					"utf-8"));
			File workingDir = new File(inputFile + ".wd");
			File modelDir = new File(maltparserModelDir);
			int count = 0;
			while ((l = dr.read()) != null) {
				count++;
				//				if (count > 100)
				//					break;
				int wid = Integer.parseInt(l[0]);
				String[] tokens = l[2].split(" ");
				String[] postags = l[3].split(" ");
				for (int i = 0; i < tokens.length; i++) {
					w.write((i + 1) + "\t" + tokens[i] + "\t_\t" + postags[i] + "\t" + postags[i] + "\t_\n");
				}
				w.write("\n");
			}
			w.close();
			String[] parameters = { "-m", "parse", "-c", "conll08-nivreeager-ltr", "-l", "liblinear", "-llv", "error",
					"-v", "info", "-w", workingDir.getAbsolutePath(), "-md", modelDir.getAbsolutePath(), "-i",
					inputFile + ".maltin", "-o", inputFile + ".maltout" };

			MaltConsoleEngine engine = new MaltConsoleEngine();
			engine.startEngine(parameters);
			// remove working directory
			FileOperations.rmr(workingDir.getAbsolutePath());
		}
		{
			// convert maltout back to tsv
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".maltout"),
					"utf-8"));
			DelimitedWriter w = new DelimitedWriter(inputFile + ".deps");

			String line = null;
			List<String[]> l = new ArrayList<String[]>();

			int count = 0;
			while (true) {

				// read dependencies
				l.clear();
				while ((line = r.readLine()) != null && line.length() > 0) {
					String[] t = line.split("\t");
					l.add(t);
				}

				if (l.size() > 0) {
					StringBuilder sbRelType = new StringBuilder();
					StringBuilder sbParent = new StringBuilder();
					StringBuilder sbPos = new StringBuilder();

					for (int i = 0; i < l.size(); i++) {
						String[] t = l.get(i);
						if (i > 0) {
							sbRelType.append(" ");
							sbParent.append(" ");
							sbPos.append(" ");
						}
						sbRelType.append(t[7].toUpperCase());
						sbParent.append(Integer.parseInt(t[6]) - 1);
						sbPos.append(t[3]);
					}
					w.write(sbRelType.toString(), sbParent.toString(), sbPos.toString());
					count++;
				}

				if (line == null)
					break;
			}
			r.close();
			w.close();
		}
	}
}
