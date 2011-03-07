package multir.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MergeChunks {

	public static void main(String[] args) throws IOException {

		String inputFile = args[0];
		int numChunks = 100;
		
		mergeAlignID(inputFile, ".tokens", numChunks);
		mergeAlignID(inputFile, ".pos", numChunks);
		mergeAlignID(inputFile, ".deps", numChunks);
		merge(inputFile, ".ner", numChunks);
	}
	
	private static void mergeAlignID(String inputFile, String suffix, int numChunks) 
		throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(inputFile + suffix), "utf-8"));
		for (int i=0; i < 100; i++) {
			BufferedReader r1 = new BufferedReader(new InputStreamReader
					(new FileInputStream(inputFile + ".chunk" + i + ".ids"), "utf-8"));
			BufferedReader r2 = new BufferedReader(new InputStreamReader
					(new FileInputStream(inputFile + ".chunk" + i + suffix), "utf-8"));
			String line1 = r1.readLine(), line2 = r2.readLine();
			while (line1 != null && line2 != null) {
				int sentenceID = Integer.parseInt(line1);
				w.write(sentenceID + "\t" + line2 + "\n");
				line1 = r1.readLine();
				line2 = r2.readLine();
			}
			if (line1 != null || line2 != null)
				System.out.println(inputFile + ".chunk" + i + suffix + " has incorrect number of lines");
			r1.close();
			r2.close();
		}
		w.close();		
	}
	
	private static void merge(String inputFile, String suffix, int numChunks)
		throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(inputFile + suffix), "utf-8"));
		for (int i=0; i < 100; i++) {
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(inputFile + ".chunk" + i + suffix), "utf-8"));
			String line = null;
			while ((line = r.readLine())!= null) {
				w.write(line + "\n");
			}
			r.close();
		}
		w.close();		
		
	}
}
