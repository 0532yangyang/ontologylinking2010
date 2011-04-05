package freebase.ie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import percept.main.LogLinear;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

public class s2_pipeline_om4re {

	/**
	 * @param args
	 */
	static void experiment1(int K, double smooth) {
		try {
			String ndir = Main.dir + "/exp";
			String sortfile = ndir + "/sort";
			String trainfile = ndir + "/train";
			String testfile = ndir + "/test";
			String debugfile = ndir + "/debug";
			{
				Sort.sort(Main.file_instance_featurize, sortfile, ndir, new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						int wida1 = Integer.parseInt(o1[0]);
						int widb1 = Integer.parseInt(o1[1]);
						int sid1 = Integer.parseInt(o1[5]);
						int wida2 = Integer.parseInt(o2[0]);
						int widb2 = Integer.parseInt(o2[1]);
						int sid2 = Integer.parseInt(o2[5]);
						if (wida1 != wida2) {
							return wida1 - wida2;
						} else if (widb1 != widb2) {
							return widb1 - widb2;
						} else {
							return sid1 - sid2;
						}
					}
				});
			}
			{
				DelimitedReader dr = new DelimitedReader(sortfile);
				DelimitedWriter dw_train = new DelimitedWriter(trainfile);
				DelimitedWriter dw_test = new DelimitedWriter(testfile);
				List<String[]> totest = new ArrayList<String[]>();
				HashSet<String> set_label = new HashSet<String>();
				String[] l;
				List<String[]> buffer = new ArrayList<String[]>();
				buffer.add(dr.read());
				while ((l = dr.read()) != null) {
					if (l[0].equals(buffer.get(0)[0]) && l[1].equals(buffer.get(0)[1])) {
						buffer.add(l);
					} else {
						//do something about the buffer
						help_experiment1(buffer, K, dw_train, set_label, totest);
						buffer.clear();
						buffer.add(l);
					}
				}
				help_experiment1(buffer, K, dw_train, set_label, totest);
				dr.close();

				Random r = new Random();
				for (String[] a : totest) {
					if (set_label.contains(a[1])) {
						double x = r.nextDouble();
						if (x < smooth) {
							dw_train.write(a);
						} else {
							dw_test.write(a);
						}
					}
				}
				dw_train.close();
				dw_test.close();
			}
			{
				LogLinear ll = new LogLinear(Main.dir, trainfile);
				List<String> pred = ll.predict(testfile, debugfile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void help_experiment1(List<String[]> buffer, int K,
			DelimitedWriter dw_train, HashSet<String> set_label, List<String[]> totest) throws IOException {
		String[] first = buffer.get(0);
		HashSet<String> f = new HashSet<String>();
		for (int k = 0; k < K && k < buffer.size(); k++) {
			String[] s = buffer.get(k)[6].split(" ");
			for (String s0 : s)
				f.add(s0);
		}
		StringBuilder sb = new StringBuilder();
		for (String f0 : f) {
			sb.append(f0 + " ");
		}
		if (first[3].equals("nell_pos")) {
			String label = first[2];
			dw_train.write(first[0] + "_" + first[1], label, sb.toString());
			set_label.add(label);
		} else {
			String label = first[3].split("_")[1];
			totest.add(new String[] { first[0] + "_" + first[1], label, sb.toString() });
			//			dw_test.write(first[0] + "_" + first[1], label, sb.toString());
		}
	}

	/**Prove ontology mapping can help relation extraction even in pipeline way*/

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//s2_pipeline_om4re
		for (double a = 0.1; a < 1; a += 0.1) {
			D.p("Experiment with smooth", a);
			experiment1(3, a);
		}
	}

}
