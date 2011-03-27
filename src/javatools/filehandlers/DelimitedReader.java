package javatools.filehandlers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DelimitedReader {

	private int BUFFER_SIZE = 8 * 1024 * 1024;
	private BufferedReader br;
	public boolean EOF = false;
	int count = 0;

	public DelimitedReader(String filename) throws IOException {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"), BUFFER_SIZE);
	}

	public DelimitedReader(String filename, int bufferSize) throws IOException {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"), bufferSize);
	}

	public DelimitedReader(String filename, String charset) throws IOException {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset), BUFFER_SIZE);
	}

	public DelimitedReader(String filename, String charset, boolean gzip) throws IOException {
		if (!gzip)
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset), BUFFER_SIZE);
		else
			br = new BufferedReader(new InputStreamReader(new java.util.zip.GZIPInputStream((new FileInputStream(
					filename))), charset), BUFFER_SIZE);
	}

	public String[] read() throws IOException {
		count++;
		if (count % 100000 == 0) {
			System.out.print(count + "\r");
			System.out.flush();
		}
		String line = br.readLine();
		if (line == null){
			this.EOF = true;
			return null;
		}
		/*
		 * String[] cols = line.split("\t"); for (int i=0; i < cols.length; i++)
		 * cols[i] = Util.unescape(cols[i]); return cols;
		 */
		return toCols(line);
	}

	public static String[] toCols(String line) {
		String[] buffer = new String[32];
		int numBuffer = 0;
		int start = 0;
		int end;

		while ((end = line.indexOf('\t', start)) != -1) {
			if (numBuffer >= buffer.length)
				buffer = extendBuffer(buffer);
			buffer[numBuffer++] = DelimitedEscape.unescape(line.substring(start, end));
			start = end + 1;
		}
		if (numBuffer >= buffer.length)
			buffer = extendBuffer(buffer);
		buffer[numBuffer++] = DelimitedEscape.unescape(line.substring(start));

		String[] returnValue = new String[numBuffer];
		System.arraycopy(buffer, 0, returnValue, 0, numBuffer);
		return returnValue;
	}

	private static String[] extendBuffer(String[] buffer) {
		String[] newBuffer = new String[2 * buffer.length];
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		return newBuffer;
	}

	public List<String[]> readAll() throws IOException {
		List<String[]> all = new ArrayList<String[]>();
		String[] line;
		while ((line = read()) != null) {
			all.add(line);
		}
		return all;
	}
	public List<String[]> readAll(int MAX) throws IOException {
		List<String[]> all = new ArrayList<String[]>(MAX);
		String[] line;
		while ((line = read()) != null) {
			all.add(line);
		}
		return all;
	}
	public HashMap<String,String> readAll2Hash(int keyId, int valueId) throws IOException{
		HashMap<String,String>all = new HashMap<String,String>();
		String []l;
		while((l = read())!=null){
			all.put(l[keyId], l[valueId]);
		}
		return all;
	}

	private String[] blockbuffer;
	public List<String[]> readBlock(int key) throws IOException{
		if(blockbuffer == null){
			blockbuffer = this.read();
		}
		if(this.EOF || blockbuffer == null)
			return null;
		List<String[]> block = new ArrayList<String[]>();
		block.add(blockbuffer);
		String []l;
		while((l = this.read())!=null && l[key].equals(block.get(0)[key])){
			block.add(l);
		}
		blockbuffer = l;
		return block;
	}
	public void close() throws IOException {
		br.close();
	}
	
}
