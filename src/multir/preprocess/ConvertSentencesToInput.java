package multir.preprocess;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import multir.util.delimited.DelimitedReader;

public class ConvertSentencesToInput {

	// only relevant for us: do not include in distribution
	
	public static void main(String[] args) throws IOException {
		convert(args[0], args[1]);
	}
	
	public static void convert(String input, String output) throws IOException {
		// write sentences into file
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(output), "utf-8"));
			DelimitedReader r = new DelimitedReader(input);
			String[] t = null;
			while ((t = r.read())!= null)
				w.write(t[0] + "\t" + t[3] + "\n");
			r.close();
			w.close();
		}
	}
}
