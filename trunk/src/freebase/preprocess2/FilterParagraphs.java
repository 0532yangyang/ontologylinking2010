package freebase.preprocess2;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class FilterParagraphs {

	static String dir = "/projects/pardosa/data15/raphaelh/data";

	static String in = "/projects/pardosa/s2/raphaelh/tmp/parc/articles";
	static String out1 = dir + "/articles.sorted";
	static String out2 = dir + "/paragraphs";

	public static void main(String[] args) throws IOException {
		/*
		Sort.sort(input, dir + "/test2/articles.sorted", dir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int i1 = Integer.parseInt(t1[0]);
				int i2 = Integer.parseInt(t2[0]);
				return i1 - i2;
			}
		});
		*/

		// remove junk paragraphs
		DelimitedReader r = new DelimitedReader(out1);
		DelimitedWriter w = new DelimitedWriter(out2);
		DelimitedWriter w1 = new DelimitedWriter(dir + "/discarded1");
		DelimitedWriter w2 = new DelimitedWriter(dir + "/discarded2");
		DelimitedWriter w3 = new DelimitedWriter(dir + "/discarded3");
		DelimitedWriter w4 = new DelimitedWriter(dir + "/discarded4");
		DelimitedWriter w5 = new DelimitedWriter(dir + "/discarded5");
		DelimitedWriter w6 = new DelimitedWriter(dir + "/discarded6");
		DelimitedWriter w7 = new DelimitedWriter(dir + "/discarded7");
		String[] t = null;
		articles: while ((t = r.read()) != null) {
			// some simple normalizations:
			t[2] = t[2].replace(". . . . ", ".\n");
			t[2] = t[2].replace(" . . . ", " ");

			String[] s = t[2].split("\n");

			// if one of the sentences starts with "*** COMPANY REPORTS
			for (int i = 0; i < s.length; i++)
				if (s[i].startsWith("*3*** COMPANY RE")) {
					w1.write(t[0], t[2]);
					continue articles;
				}

			// paragraphs
			for (int i = 0; i < s.length; i++) {
				if (s[i].startsWith("PICTURE"))
					w2.write(t[0], s[i]);
				else if (s[i].toUpperCase().equals(s[i]))
					w3.write(t[0], s[i]);
				else if (s[i].startsWith("LEAD:") || s[i].startsWith("LEAD "))
					w4.write(t[0], s[i]);
				else if (s[i].split(" ").length < 4)
					w5.write(t[0], s[i]);
				else {
					w.write(t[0], s[i]);
				}
			}
		}

		// now do tokenization and sentence splitting
		// for that, run
		// cj.preprocess.rank.Tokenize in project multir

		// do sentence parse
		// make sure that last sentence of paragraph ends in sentenceEndPunct
		//String sentences[] = sentenceDetector.sentDetect(s[i]);

		// create tokenization and sentence detection models

		
		InputStream modelIn = new FileInputStream("../models/opennlp/en-sent.bin");

		SentenceModel model = null;
		try {
		  model = new SentenceModel(modelIn);
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
		
		int sentenceID = 0;
		DelimitedWriter w = new DelimitedWriter(dir + "/test/sentences.delim");
		DelimitedWriter w1 = new DelimitedWriter(dir + "/test/discarded1");
		DelimitedWriter w2 = new DelimitedWriter(dir + "/test/discarded2");
		DelimitedWriter w3 = new DelimitedWriter(dir + "/test/discarded3");
		DelimitedWriter w4 = new DelimitedWriter(dir + "/test/discarded4");
		DelimitedWriter w5 = new DelimitedWriter(dir + "/test/discarded5");
		DelimitedWriter w6 = new DelimitedWriter(dir + "/test/discarded6");
		DelimitedWriter w7 = new DelimitedWriter(dir + "/test/discarded7");
		DelimitedReader r = new DelimitedReader(dir + "/test/articles.sorted");
		String[] t = null;
		articles:
		while ((t = r.read())!= null) {
			// some simple normalizations:
			t[2] = t[2].replace(". . . . ", ".\n");
			t[2] = t[2].replace(" . . . ", " ");			
			
			String[] s = t[2].split("\n");
			
			// if one of the sentences starts with "*** COMPANY REPORTS
			for (int i=0; i < s.length; i++)
				if (s[i].startsWith("*3*** COMPANY RE")) {
					w1.write(t[0], t[2]);
					continue articles;
				}
			
			// paragraphs
			for (int i=0; i < s.length; i++) {
				if (s[i].startsWith("PICTURE"))
					w2.write(t[0], s[i]);
				else if (s[i].toUpperCase().equals(s[i]))
					w3.write(t[0], s[i]);
				else if (s[i].startsWith("LEAD:") || s[i].startsWith("LEAD "))
					w4.write(t[0], s[i]);
				else if (s[i].split(" ").length < 4)
					w5.write(t[0], s[i]);
				else {
					// do sentence parse
					// make sure that last sentence of paragraph ends in sentenceEndPunct
					String sentences[] = sentenceDetector.sentDetect(s[i]);
					for (int j=0; j < sentences.length; j++) {
						if (//j == sentences.length -1 && i == s.length-1 &&
								!sentenceEndPunct(sentences[j])) {
								w6.write(t[0], sentences[j]);
								continue;
						}
						if (sentences[j].split(" ").length > 200) {
							w7.write(t[0], sentences[j]);
							continue;
						}
						
						w.write("" + sentenceID++, t[0], sentences[j]);
					}
				}
			}
		}
		r.close();
		w.close();
		w1.close();
		w2.close();
		w3.close();
		w4.close();
		w5.close();
		w6.close();
		w7.close();
		*/
		/*
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + "/test/sentences"),  "utf-8"));
		DelimitedReader r= new DelimitedReader(dir + "/test/sentences.delim");
		String[] t = null;
		while ((t = r.read())!= null) {
			t[2] = t[2].replace("\t", " ");
			t[2] = t[2].replace("\n", " ");
			w.write(t[0] + '\t' + t[2] + '\n');
		}
		r.close();
		w.close();
		*/
		/*
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + "/test/sentences.article"),  "utf-8"));
		DelimitedReader r= new DelimitedReader(dir + "/test/sentences.delim");
		String[] t = null;
		while ((t = r.read())!= null) {
		t[2] = t[2].replace("\t", " ");
		t[2] = t[2].replace("\n", " ");
		w.write(t[0] + '\t' + t[1] + "\t" + t[2] + '\n');
		}
		r.close();
		w.close();*/
	}

	private static boolean sentenceEndPunct(String s) {
		return s.endsWith(".") || s.endsWith(":") || s.endsWith(";") || s.endsWith("!") || s.endsWith("?")
				|| s.endsWith(".\'\'") || s.endsWith("!\'\'") || s.endsWith("?\'\'") || s.endsWith(".\"")
				|| s.endsWith("!\"") || s.endsWith("?\"") || s.endsWith(".)") || s.endsWith("!)") || s.endsWith("?)")
				|| s.endsWith(".'") || s.endsWith("!'") || s.endsWith("?'") || s.endsWith(".' ''")
				|| s.endsWith(".'')") || s.endsWith("?'") || s.endsWith("?' ''");
	}
}
