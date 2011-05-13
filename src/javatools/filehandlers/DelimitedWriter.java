package javatools.filehandlers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class DelimitedWriter {

	private int BUFFER_SIZE = 8 * 1024 * 1024;
	private BufferedWriter bw;

	public DelimitedWriter(String filename) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"), BUFFER_SIZE);
	}

	public DelimitedWriter(String filename, int bufferSize) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"), bufferSize);
	}

	public DelimitedWriter(String filename, boolean append) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append), "utf-8"), BUFFER_SIZE);
	}

	public DelimitedWriter(String filename, String charset) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), charset), BUFFER_SIZE);
	}

	public DelimitedWriter(String filename, String charset, boolean gzipped) throws IOException {
		if (gzipped)
			bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename)),
					charset), BUFFER_SIZE);
		else
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), charset), BUFFER_SIZE);
	}

	public void write(String... cols) throws IOException {
		if (cols.length == 0)
			return;

		bw.write(DelimitedEscape.escape(cols[0]));
		for (int i = 1; i < cols.length; i++)
			bw.write("\t" + DelimitedEscape.escape(cols[i]));
		bw.write("\n");
	}

	public void write(Object... cols) throws IOException {
		if (cols.length == 0)

			return;
		bw.write(DelimitedEscape.escape(cols[0].toString()));
		for (int i = 1; i < cols.length; i++)
			bw.write("\t" + DelimitedEscape.escape(cols[i].toString()));
		bw.write("\n");
	}

	public void close() throws IOException {
		bw.close();
	}

	public void flush() throws IOException {
		bw.flush();
	}
}
