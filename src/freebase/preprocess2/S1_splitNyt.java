package freebase.preprocess2;

import java.io.IOException;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S1_splitNyt {
	public static void giveParagraphId() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_nyt_paragraph);
			DelimitedWriter dw = new DelimitedWriter(Main.file_nyt_widsecidparagraph);
			String[] l;
			int sectionId = 1;

			while ((l = dr.read()) != null) {
				int articleId = Integer.parseInt(l[0]);
				dw.write(articleId, sectionId, l[1]);
				sectionId++;
				//if(sectionId>10000)break;
			}
			dr.close();
			dw.close();
		}
	}

	static void split() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_nyt_widsecidparagraph);
			DelimitedWriter dw[] = new DelimitedWriter[Main.datas.length];
			for (int i = 0; i < dw.length; i++) {
				dw[i] = new DelimitedWriter(Main.file_nytsplit_template.replace("$DNUM$", Main.datas[i]) + i);
			}
			String[] l;
			int ln = 0;
			while ((l = dr.read()) != null) {
				//if(++ln > 100000)break;
				int wid = Integer.parseInt(l[0]);
				int partid = wid % Main.datas.length;
				//String rawtext = l[4].replaceAll("[^\\p{ASCII}]", " ");
				dw[partid].write(l);
			}
			dr.close();
			for (DelimitedWriter dw0 : dw) {
				dw0.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		//giveParagraphId();
		split();
	}
}
