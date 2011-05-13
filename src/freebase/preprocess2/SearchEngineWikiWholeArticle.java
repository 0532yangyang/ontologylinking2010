package freebase.preprocess2;

import java.io.IOException;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.webapi.LuceneIndexFiles;

public class SearchEngineWikiWholeArticle {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void step1() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_delimited_sections);
			DelimitedWriter dw = new DelimitedWriter(Main.file_wid_wholearticle);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				int id = Integer.parseInt(b.get(0)[0]);
				String title = b.get(0)[2];
				StringBuilder sb = new StringBuilder();
				for (String[] l : b) {
					sb.append(l[3]);
				}
				dw.write(id, title, sb.toString());
			}
			dr.close();
			dw.close();
		}
	}

	public static void step2() throws IOException {
		LuceneIndexFiles.indexDelimitedFile(Main.file_wid_wholearticle, 2, new int[] { 0, 1 },
				Main.file_wholewikiarticle_luceneindex);
	}

	public static void main(String[] args) throws IOException {
		//step1();
		step2();
	}

}
