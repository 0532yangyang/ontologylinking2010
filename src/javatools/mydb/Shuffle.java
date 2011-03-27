package javatools.mydb;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class Shuffle {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void shuffle(String input, String output, String tempdir) throws IOException {
		String file_shuffletemp = tempdir + "/shuffletemp_" + (new Date()).getTime();
		{
			DelimitedReader dr = new DelimitedReader(input);
			DelimitedWriter dw = new DelimitedWriter(file_shuffletemp);
			Random r = new Random();
			String[] l;
			while ((l = dr.read()) != null) {
				String[] s = new String[l.length + 1];
				s[0] = r.nextInt() + "";
				System.arraycopy(l, 0, s, 1, l.length);
				dw.write(s);
			}
			dw.close();
			dr.close();
		}
		{
			Sort.sort(file_shuffletemp, file_shuffletemp + "_2", tempdir, new Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					int a = Integer.parseInt(o1[0]);
					int b = Integer.parseInt(o2[0]);
					return a - b;
				}
			});
		}
		{
			DelimitedReader dr = new DelimitedReader(file_shuffletemp + "_2");
			DelimitedWriter dw = new DelimitedWriter(output);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] s = new String[l.length - 1];
				System.arraycopy(l, 1, s, 0, s.length);
				dw.write(s);
			}
			dw.close();
			dr.close();
		}
		(new File(file_shuffletemp)).delete();
		(new File(file_shuffletemp + "_2")).delete();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
