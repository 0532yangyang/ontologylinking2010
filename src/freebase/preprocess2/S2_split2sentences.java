package freebase.preprocess2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javatools.administrative.D;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class S2_split2sentences {
	public static void main(String[] args) throws FileNotFoundException {
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
		String[] mysen = sentenceDetector.sentDetect("This is a joke. I like joke");
		for (int i = 0; i < mysen.length; i++) {
			D.p(mysen[i]);
		}

	}
}
