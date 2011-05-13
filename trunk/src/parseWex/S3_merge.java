package parseWex;

import java.io.IOException;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import stanfordCoreWrapper.MaltiDep;

public class S3_merge {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.output);
		DelimitedWriter wner = new DelimitedWriter(Main.output + ".ner");
		int senid = 1;
		for (int i = 0; i < Main.datas.length; i++) {
			String file = Main.fout_stanfordtext_template.replace("$DNUM$", Main.datas[i]) + i;
			{
				DelimitedReader dr = new DelimitedReader(file);
				String[] l;
				while ((l = dr.read()) != null) {
					dw.write(senid, l[0], 0, l[2], l[3], l[4]);
					String[] ner = l[4].split(" ");
					String[] tokens = l[2].split(" ");
					findConsecutive(senid, l[0], ner, tokens, wner);
					senid++;
				}
			}
		}
		dw.close();
		wner.close();
	}

	static void findConsecutive(int senid, String wid_str, String[] labels, String[] tokens, DelimitedWriter w)
			throws IOException {
		int start = -1;
		for (int i = 0; i < labels.length; i++) {
			if (start == -1) {
				if (!labels[i].equals("O"))
					start = i;
			} else {
				if (!labels[i].equals(labels[start])) {
					if (!labels[start].equals("O")) {
						w.write(senid, wid_str, start, i, substring(tokens, start, i), labels[start]);
					}
					start = i;
				}
			}
		}
		if (start != -1 && !labels[start].equals("O"))
			w.write(senid, wid_str, start, tokens.length, substring(tokens, start, tokens.length), labels[start]);
	}

	private static String substring(String[] tokens, int start, int end) {
		String s = "";
		for (int i = start; i < end; i++) {
			if (i > start)
				s += " ";
			s += tokens[i];
		}
		return s;
	}
}
