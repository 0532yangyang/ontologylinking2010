package freebase.ie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import freebase.typematch.RecordWpSenToken;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.string.RemoveStopwords;
import javatools.string.Stemmer;

public class S0_background {

	public static void patternize(String[] token, String[] pos, String[] ner, Set<String> patterns, int WINSIZE) {
		List<String> l = new ArrayList<String>();
		List<String> lner = new ArrayList<String>();
		for (int i = 0; i < token.length; i++) {

			String s = token[i].toLowerCase();
			char[] c = pos[i].toCharArray();
			//is not a word!
			if (!Character.isLetter(c[0])) {
				continue;
			}
			//is a stop word!
			if (RemoveStopwords.isStop(s)) {
				continue;
			}

			//stemmer
			s = Stemmer.stem(s);

			if (!ner[i].equals("O")) {
				lner.add(ner[i]);
			} else {
				lner.add(s);
			}
			l.add(s);
		}

		for (int i = 0; i < l.size(); i++) {
			patterns.add(l.get(i));
		}
		for (int k = 2; k <= WINSIZE; k++) {
			for (int i = 0; i < l.size() - WINSIZE; i++) {
				StringBuilder sb = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				for (int j = i; j < i + k; j++) {
					sb.append(l.get(j) + "_");
					sb2.append(lner.get(j) + "_");
				}
				patterns.add(sb.toString());
				patterns.add(sb2.toString());
			}
		}
		l = null;
		lner = null;
	}

	//	public static void doit() {
	//		try {
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern);
	//			HashCount<String> hc = new HashCount<String>();
	//			Set<String> patterns = new HashSet<String>();
	//			long sum = 0;
	//			RecordWpSenToken rwst = new RecordWpSenToken();
	//			DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_wp_stanford);
	//			while ((rwst = RecordWpSenToken.read(dr)) != null) {
	//				patternize(rwst.token, rwst.pos, rwst.ner, patterns, 3);
	//				//D.p(patterns);
	//				for (String p : patterns) {
	//					hc.add(p);
	//					sum++;
	//				}
	//				patterns.clear();
	//			}
	//			D.p(sum);
	//			dw.write("TOTAL",sum);
	//			Iterator<Entry<String, Integer>> it = hc.iterator();
	//			while (it.hasNext()) {
	//				Entry<String, Integer> e = it.next();
	//				String p = e.getKey();
	//				int c = e.getValue();
	//				if (c > 1) {
	//					dw.write(p, c);
	//				}
	//			}
	//			
	//			dw.close();
	//		} catch (Exception e) {
	//
	//		}
	//	}

	public static void doit2() {
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern_raw);
			//HashCount<String> hc = new HashCount<String>();
			Set<String> patterns = new HashSet<String>();
			long sum = 0;
			RecordWpSenToken rwst = new RecordWpSenToken();
			DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_wp_stanford);
			while ((rwst = RecordWpSenToken.read(dr)) != null) {
				patternize(rwst.token, rwst.pos, rwst.ner, patterns, 3);
				//D.p(patterns);
				for (String p : patterns) {
					dw.write(p);
					sum++;
				}
				patterns.clear();
			}
			//			D.p(sum);
			//			dw.write("TOTAL",sum);
			//Iterator<Entry<String, Integer>> it = hc.iterator();
			//			while (it.hasNext()) {
			//				Entry<String, Integer> e = it.next();
			//				String p = e.getKey();
			//				int c = e.getValue();
			//				if (c > 1) {
			//					dw.write(p, c);
			//				}
			//			}
			dw.close();
		} catch (Exception e) {

		}
	}

	static void uniqc() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_background_pattern_sort);
			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern_uniqc);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				String str = b.get(0)[0];
				if (b.size() == 1)
					continue;

				char[] c = str.toCharArray();
				int numLetter = 0;
				for (int i = 0; i < c.length; i++) {
					if (c[i] >= 'a' && c[i] <= 'z' || c[i] >= 'A' && c[i] <= 'Z') {
						numLetter++;
					}
				}
				if (numLetter * 2 < c.length) {
					continue;
				}
				if (str.contains("www.") || str.contains("http:")) {
					continue;
				}
				dw.write(str, b.size());

			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void filterLessThanK(int k) {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_background_pattern_uniqc);
			DelimitedWriter dw = new DelimitedWriter(Main.file_background_pattern_uniqc_10);
			String[] l;
			while ((l = dr.read()) != null) {
				int x = Integer.parseInt(l[1]);
				if(x>10){
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**I want a background frequency of the pattern in WK articles*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//doit2();

		/**sort it*/
		//uniqc();

		filterLessThanK(10);
	}

}
