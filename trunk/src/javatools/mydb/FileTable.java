package javatools.mydb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javatools.filehandlers.DelimitedReader;

public class FileTable implements Iterable<String[]>, Iterator<String[]> {
	public static void main(String[] args) {

	}

	DelimitedReader dr;
	String[] l;
	String[] current;
	int[] c;

	public FileTable(String file) throws IOException {
		dr = new DelimitedReader(file);
	}

	public FileTable(String file, int[] c) throws IOException {
		dr = new DelimitedReader(file);
		this.c = c;
	}

	public static List<String[]> takeColumns(String file, int[] columns) throws IOException {
		List<String[]> strcols = new ArrayList<String[]>();
		DelimitedReader dr = new DelimitedReader(file);
		String[] l;
		while ((l = dr.read()) != null) {
			String[] nl = new String[columns.length];
			for (int i = 0; i < columns.length; i++) {
				nl[i] = l[columns[i]];
			}
			strcols.add(nl);
		}
		return strcols;
	}

	@Override
	public boolean hasNext() {
		try {
			l = dr.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub\
		if (l == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String[] next() {
		// TODO Auto-generated method stub
		if (c == null) {
			return l;
		} else {
			current = new String[c.length];
			for (int i = 0; i < c.length; i++) {
				current[i] = l[c[i]];
			}
			return current;
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<String[]> iterator() {
		// TODO Auto-generated method stub
		return this;
	}
}