package stanfordCoreWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import multir.util.FileOperations;
import multir.util.delimited.DelimitedWriter;
import edu.stanford.nlp.parser.ensemble.maltparser.MaltConsoleEngine;

import javatools.filehandlers.DelimitedReader;

public class MaltiDep {
	//static String stanfordNerModel = "../models/ner-eng-ie.crf-4-conll-distsim.ser.gz";
	static String maltparserModelDir = "models";

	public static void getDep(String inputFile, int tokenIndex) throws IOException {
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
		{
			DelimitedReader dr = new DelimitedReader(inputFile);
			DelimitedReader dr2 = new DelimitedReader(inputFile + ".deps");
			DelimitedWriter dw = new DelimitedWriter(inputFile + ".withdeps");
			String[] l;
			String[] l2;
			while ((l = dr.read()) != null) {
				l2 = dr2.read();
				if (l2 != null) {
					String[] w = new String[l.length + l2.length];
					System.arraycopy(l, 0, w, 0, l.length);
					System.arraycopy(l2, 0, w, l.length, l2.length);
					dw.write(w);
				} else {
					break;
				}
			}
			dw.close();
		}
		{
			(new File(inputFile + ".deps")).deleteOnExit();
			(new File(inputFile + ".maltout")).deleteOnExit();
			(new File(inputFile + ".maltin")).deleteOnExit();
		}
	}

	public static void main(String[] args) throws IOException {
		String inputFile = "/projects/pardosa/data01/clzhang/tmp/wp/wex_stanfordtext.part" + args[0];
		getDep(inputFile, 2);
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
				if (count > 100)
					break;
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
