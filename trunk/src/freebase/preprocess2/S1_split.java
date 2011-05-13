package freebase.preprocess2;

import java.io.IOException;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S1_split {
	public static void main(String[] args) {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_delimited_sections);
			DelimitedWriter dw[] = new DelimitedWriter[Main.datas.length];
			for (int i = 0; i < dw.length; i++) {
				dw[i] = new DelimitedWriter(Main.fout_wexsplit_template.replace("$DNUM$", Main.datas[i]) + i);
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
			for (DelimitedWriter dw0 : dw)
				dw0.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
