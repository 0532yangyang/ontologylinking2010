package freebase.typematch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;


public class RecordWpSenToken {

public 	int sentenceId;
	public int sectionId;
	public int articleId;
	public String text;
	public String[] token;
	public String[] pos;
	public String[] ner;

	public static RecordWpSenToken read(DelimitedReader dr) throws IOException {
		RecordWpSenToken rwst = new RecordWpSenToken();
		String[] line = dr.read();
		if (line == null) {
			return null;
		} else {
			rwst.sentenceId = Integer.parseInt(line[0]);
			rwst.articleId = Integer.parseInt(line[1]);
			rwst.sectionId = Integer.parseInt(line[2]);
			StringBuilder sb = new StringBuilder();
			try {
				rwst.token = line[3].split(" ");
				for (String t : rwst.token) {
					sb.append(t).append(" ");
				}
				rwst.pos = line[4].split(" ");
				rwst.ner = line[5].split(" ");
			} catch (Exception e) {
				e.printStackTrace();
			}
			rwst.text = sb.toString();
			return rwst;
		}
	}

	static RecordWpSenToken buffer_rwst = null;

	public static List<RecordWpSenToken> readByArticleId(DelimitedReader dr, boolean isFirst) throws IOException {
		if(isFirst)buffer_rwst = null;
		List<RecordWpSenToken> rwstlist = new ArrayList<RecordWpSenToken>();

		if (buffer_rwst == null) {
			if (!dr.EOF) {
				buffer_rwst = RecordWpSenToken.read(dr);
			} else {
				return null;
			}
		}
		rwstlist.add(buffer_rwst);
		RecordWpSenToken rwst;
		while ((rwst = RecordWpSenToken.read(dr)) != null) {
			if (rwst.articleId == rwstlist.get(0).articleId) {
				rwstlist.add(rwst);
			} else {
				break;
			}
		}
		buffer_rwst = rwst;
		return rwstlist;
	}

	public String toString() {
		return text;
	}

	public static void main(String[] args) {
		try {
			DelimitedReader dr = new DelimitedReader(Main.fout_wp_stanford_subset);
			List<RecordWpSenToken> rl;
			while ((rl = readByArticleId(dr,false)) != null ) {
				D.p(rl.size());
			}
			dr.close();
		} catch (Exception e) {

		}
	}
}
