package multir.util.delimited;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DelimitedReader {

	private int BUFFER_SIZE = 8*1024*1024;
	private BufferedReader br;
	
	public DelimitedReader(String filename) 
	throws IOException {
		br = new BufferedReader(new InputStreamReader
				(new FileInputStream(filename), "utf-8"), BUFFER_SIZE);		
	}

	public DelimitedReader(String filename, int bufferSize) 
	throws IOException {
		br = new BufferedReader(new InputStreamReader
				(new FileInputStream(filename), "utf-8"), bufferSize);		
	}
	
	public DelimitedReader(String filename, String charset) 
	throws IOException {
		br = new BufferedReader(new InputStreamReader
				(new FileInputStream(filename), charset), BUFFER_SIZE);		
	}

	public DelimitedReader(String filename, String charset, boolean gzip) 
	throws IOException {
		if (!gzip)
			br = new BufferedReader(new InputStreamReader
					(new FileInputStream(filename), charset), BUFFER_SIZE);		
		else
			br = new BufferedReader(new InputStreamReader(
					new java.util.zip.GZIPInputStream(
							(new FileInputStream(filename))), charset), BUFFER_SIZE);		
	}

	public String[] read()
	throws IOException {
		String line = br.readLine();
		if (line == null) return null;
		/*
		String[] cols = line.split("\t");
		for (int i=0; i < cols.length; i++)
			cols[i] = Util.unescape(cols[i]);
		return cols;
		
		*/
		return toCols(line);
	}
	
	public static String[] toCols(String line) {
		String[] buffer = new String[32];
		int numBuffer = 0;
		int start = 0;
		int end;
		
		while ((end = line.indexOf('\t', start)) != -1) {
			if (numBuffer >= buffer.length) buffer = extendBuffer(buffer);
			buffer[numBuffer++] = DelimitedEscape.unescape(line.substring(start, end));
			start = end + 1;
		}
		if (numBuffer >= buffer.length) buffer = extendBuffer(buffer);
		buffer[numBuffer++] = DelimitedEscape.unescape(line.substring(start)); 
		
		String[] returnValue = new String[numBuffer];
		System.arraycopy(buffer, 0, returnValue, 0, numBuffer);
		return returnValue;		
	}
	
	private static String[] extendBuffer(String[] buffer) {
		String[] newBuffer = new String[2*buffer.length];
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		return newBuffer;
	}
	
	public void close()
	throws IOException {
		br.close();
	}
}
