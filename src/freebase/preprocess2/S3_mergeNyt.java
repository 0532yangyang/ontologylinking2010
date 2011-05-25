package freebase.preprocess2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S3_mergeNyt {
	public static void mergeOneKind(String suffix, String outputFile) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(outputFile + suffix);
		for (int i = 0; i < Main.datas.length; i++) {
			String input = Main.file_nytsplit_template.replace("$DNUM$", Main.datas[i]) + i;
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

	public static void main(String[] args) throws IOException {

		//		String inputFile = args[0];
		//		int numChunks = 100;
		previousLargestId = new int[Main.datas.length];
		getPreviousLargetsId();
		mergeAlignID(".tokens");
		mergeAlignID(".pos");
		mergeAlignID(".deps");
		mergeNer();
	}

	private static void getPreviousLargetsId() throws IOException {
		int previousSentenceId = 0;
		for (int i = 0; i < Main.datas.length; i++) {
			previousLargestId[i] = previousSentenceId + 1;
			String fname = Main.file_nytsplit_template.replace("$DNUM$", Main.datas[i]) + i;
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
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.file_nyt_sentences
				+ suffix), "utf-8"));
		for (int i = 0; i < Main.datas.length; i++) {
			String fname = Main.file_nytsplit_template.replace("$DNUM$", Main.datas[i]) + i;
			DelimitedReader r1 = new DelimitedReader(fname + ".ids");
			BufferedReader r2 = new BufferedReader(new InputStreamReader(new FileInputStream(fname + suffix), "utf-8"));
			String[] l1 = r1.read();
			String line2 = r2.readLine();
			while (l1 != null && line2 != null) {
				int tempSentenceId = Integer.parseInt(l1[0]);
				int sentenceId = previousLargestId[i] + tempSentenceId;
				int articleId = Integer.parseInt(l1[1]);
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
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.file_nyt_sentences
				+ ".ner"), "utf-8"));
		for (int i = 0; i < Main.datas.length; i++) {
			String fname = Main.file_nytsplit_template.replace("$DNUM$", Main.datas[i]) + i;
			//BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fname + ".ner"), "utf-8"));
			DelimitedReader dr = new DelimitedReader(fname + ".ner");
			String[] l;
			while ((l = dr.read()) != null) {
				if (l.length == 7) {
					int tempsid = Integer.parseInt(l[0]);
					int sentenceId = tempsid + previousLargestId[i];
					w.write(sentenceId + "\t" + l[1] + "\t" + l[2] + "\t" + l[3] + "\t" + l[4] + "\t" + l[5] + "\t"
							+ l[6] + "\n");
				}
			}
			dr.close();
		}
		w.close();

	}
}
