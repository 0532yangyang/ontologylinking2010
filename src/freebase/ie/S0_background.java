package freebase.ie;

import java.util.ArrayList;
import java.util.List;

import freebase.typematch.RecordWpSenToken;

import javatools.filehandlers.DelimitedReader;
import javatools.string.RemoveStopwords;

public class S0_background {

	public static void patternize(String[] token, String[] pos, String[] ner, List<String> patterns, int WINSIZE) {
		List<String> l = new ArrayList<String>();
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
			if (!ner[i].equals("O")) {
				s = ner[i];
			}
			l.add(s);
		}

		for (int k = 1; k <= WINSIZE; k++) {
			for (int i = 0; i < l.size() - WINSIZE; i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = i; j < i + k; j++) {
					sb.append(l.get(i) + "_");
				}
				patterns.add(sb.toString());
			}
		}
		l = null;
	}

	public static void doit() {
		try {
			 List<String> patterns = new ArrayList<String>();
			RecordWpSenToken rwst = new RecordWpSenToken();
			DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_wp_stanford);
			while ((rwst = RecordWpSenToken.read(dr)) != null) {
				patternize(rwst.token,rwst.pos,rwst.ner,patterns,3);
				patterns.clear();
			}
			
		} catch (Exception e) {

		}
	}

	/**I want a background frequence of the pattern in WK articles*/
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		doit();
	}

}
