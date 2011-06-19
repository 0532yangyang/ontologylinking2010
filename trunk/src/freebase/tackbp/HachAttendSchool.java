package freebase.tackbp;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class HachAttendSchool {

	static String[] mappedrelation = new String[] {
			"/people/person/education|/education/education/institution" };

	static String file_targetrelation = Main.dir + "/attendschool";

	static void step1() throws IOException {
		Set<String> usedrel = new HashSet<String>();
		for (String r : mappedrelation)
			usedrel.add(r);
		DelimitedReader dr = new DelimitedReader(Main.file_fbvisibledump);
		DelimitedWriter dw = new DelimitedWriter(file_targetrelation + ".1");
		String[] l;
		while ((l = dr.read()) != null) {
			if (usedrel.contains(l[2])) {
				dw.write(l);
			}
		}
		dr.close();
		dw.close();
	}

	static void step2() throws IOException {
		{
			HashMap<String, String[]> mid2other = new HashMap<String, String[]>();
			DelimitedWriter dw = new DelimitedWriter(file_targetrelation + ".2");
			{
				DelimitedReader dr = new DelimitedReader(Main.file_mid2typename);
				String[] l;
				while ((l = dr.read()) != null) {
					String mid = l[0];
					mid2other.put(mid, l);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(file_targetrelation + ".1");
				String[] l;
				HashSet<String> avoidduplicate = new HashSet<String>();
				while ((l = dr.read()) != null) {
					String mid1 = l[1];
					String mid2 = l[3];
					String[] info1 = mid2other.get(mid1);
					String[] info2 = mid2other.get(mid2);
					if (!avoidduplicate.contains(mid1 + "\t" + mid2) && info1 != null && info2 != null) {
						dw.write(mid1, mid2, info1[3], info2[3], info1[2], info2[2]);
						avoidduplicate.add(mid1 + "\t" + mid2);
					}
				}
				dr.close();
			}
			dw.close();
		}
		{
			HashSet<String> typeOfArg2 = new HashSet<String>();
			List<String[]> all = (new DelimitedReader(file_targetrelation + ".2")).readAll();
			for (String[] a : all) {
				typeOfArg2.add(a[5]);
			}
			for (String a : typeOfArg2) {
				D.p(a);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		step1();
		step2();
	}

}
