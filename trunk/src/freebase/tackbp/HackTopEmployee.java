package freebase.tackbp;

import java.io.IOException;
import java.util.HashSet;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class HackTopEmployee {

	public static void main(String[] args) throws IOException {
		HashSet<String> use = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.dir + "/employerHasTitle.2");
			String[] l;
			while ((l = dr.read()) != null) {
				use.add(l[0]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.dir + "/hasEmployer.2");
			DelimitedWriter dw = new DelimitedWriter(Main.dir + "/hasTopMemberOrEmployee");
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				if (use.contains(mid)) {
					dw.write(l);
				}
			}
			dw.close();
		}
	}
}
