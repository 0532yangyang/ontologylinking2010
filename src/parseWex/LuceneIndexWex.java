package parseWex;

import java.io.IOException;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class LuceneIndexWex {
	public static void main(String[] args) throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.fin_wex_articles);
		DelimitedWriter dw = new DelimitedWriter(Main.fin_delimited_sections);
		String[] l;
		int sectionid = 1;
		int count = 0;
		while ((l = dr.read()) != null) {
			//			if (count++ > 10000)
			//				break;
			try {
				String[] sections = l[4].split("\n\n");
				for (String s : sections) {
					dw.write(l[0], sectionid++, l[1], s);
				}
			} catch (Exception e) {
				System.err.println("Wrong at: " + l[0] + "\t" + l[1]);
			}
		}
		dw.close();
	}
}
