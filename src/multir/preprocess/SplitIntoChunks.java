package multir.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SplitIntoChunks {

	public static void main(String[] args) throws IOException {
		split(args[0], args[1], Integer.parseInt(args[2]));
	}

	public static void split(String input, String chunkPrefix, int numChunks) throws IOException {
		
		System.out.println("Breaking input sentences file into chunks ...");
		{
			// count lines
			int numLines = 0;
			{
				BufferedReader r = new BufferedReader
					(new InputStreamReader(new FileInputStream(input), "utf-8"));
				while (r.readLine() != null) numLines++;
				r.close();
			}
			int numLinesPerChunk = numLines / numChunks + 
				(numLines % numChunks != 0 ? 1 : 0);
			int chunk = 0;
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(input), "utf-8"));
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(chunkPrefix + chunk++), "utf-8"));
			String line = null;
			int numLine = 0;
			while ((line = r.readLine()) != null) {
				if (numLine > 0 && numLine % numLinesPerChunk == 0) {
					w.close();
					w = new BufferedWriter(new OutputStreamWriter
							(new FileOutputStream(chunkPrefix + chunk++), "utf-8"));
				}
				w.write(line);
				w.write('\n');
				numLine++;
			}
			r.close();
			w.close();
		}
	}
}
