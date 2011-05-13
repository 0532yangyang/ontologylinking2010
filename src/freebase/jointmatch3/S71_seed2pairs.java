package freebase.jointmatch3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import percept.main.LogLinear;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S71_seed2pairs {
	public static void seedpair2patterns() throws IOException {
		HashMap<String, Integer> nellstr2wid = new HashMap<String, Integer>();
		List<String[]> nellstrMidPred = (new DelimitedReader(
				Main.file_enid_mid_wid_argname_otherarg_relation_label_top1)).readAll();
		for (String[] a : nellstrMidPred) {
			nellstr2wid.put(a[3], Integer.parseInt(a[2]));
		}
		D.p(nellstr2wid.size());
		List<String[]> widpair = new ArrayList<String[]>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					String arg1 = s[0];
					String arg2 = s[1];
					if (nellstr2wid.containsKey(arg1) && nellstr2wid.containsKey(arg2)) {
						int wid1 = nellstr2wid.get(arg1);
						int wid2 = nellstr2wid.get(arg2);
						widpair.add(new String[] { "" + wid1, "" + wid2, nr.relation_name });
						//D.p(wid1, wid2, nr.relation_name);
					}
				}
			}
		}
		Pattern.widpair2feature(widpair, Main.file_patternize_seedpair);
	}

	public static void patterns2feature2train() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_patternize_seedpair);
			List<String[]> towrite = new ArrayList<String[]>();

			String[] l;
			String lastmark = "";
			while ((l = dr.read()) != null) {
				String mark = l[0] + "_" + l[1];
				if (!mark.equals(lastmark)) {
					String clas = l[2];
					String fts = l[5];
					lastmark = mark;
					towrite.add(new String[] { mark, clas, fts });
					//	dw.write(mark, clas, fts);
				}
			}
			DelimitedWriter dw1 = new DelimitedWriter(Main.file_patternize_seedpair + ".train");
			DelimitedWriter dw2 = new DelimitedWriter(Main.file_patternize_seedpair + ".test");
			Random r = new Random();
			HashSet<String> appear = new HashSet<String>();
			for (String[] w : towrite) {
				double r0 = r.nextDouble();
				if (r0 > 0.1 || !appear.contains(w[1])) {
					dw1.write(w);
					appear.add(w[1]);
				} else {
					dw2.write(w);
				}
			}
			dw1.close();
			dw2.close();
		}
		LogLinear ll = new LogLinear(Main.dir, Main.file_patternize_seedpair + ".train");
		ll.predict(Main.file_patternize_seedpair + ".test", Main.file_patternize_seedpair + ".test" + ".debug");
		{
			ll.printDebug(Main.file_patternize_seedpair + ".test" + ".debug2");
			//			DelimitedWriter dw = new DelimitedWriter(Main.file_patternize_seedpair_train + ".debug2");
			//			dw.write(ll.params.toString());
			//			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		seedpair2patterns();
		patterns2feature2train();

		//		Pattern.widpair2feature(pairs, output);
	}
}
