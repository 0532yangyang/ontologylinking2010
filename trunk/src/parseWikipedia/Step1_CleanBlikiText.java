package parseWikipedia;

import java.io.IOException;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class Step1_CleanBlikiText {

	/**
	 * @param args
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DelimitedReader dr = new DelimitedReader(Main.file_blikitext_all);
		DelimitedWriter dw = new DelimitedWriter(Main.file_blikitext_all_clean);
		DelimitedWriter dw_away = new DelimitedWriter(Main.file_blikitext_all_wipedaway);
		String[] line;
		int sectionId = 1;
		while ((line = dr.read()) != null) {
			//if(sectionId>1000) break;
			int articleId = Integer.parseInt(line[0]);
			String text = line[1];
			if(isValid(text)){
				dw.write(articleId,sectionId,line[1]);
				sectionId++;
			}
			else{
				dw_away.write(articleId,line[1]);
			}
		}
		dr.close();
		dw.close();
		dw_away.close();
	}

	static boolean isValid(String text) {
		char[] c = text.trim().toCharArray();
		if (c.length == 0)
			return false;
//		if (!Character.isUpperCase(c[0])) {
//			return false;
//		}
		{
			boolean containSentenceEnd = false;
			for (int i = 0; i < c.length; i++) {
				if (c[i] == '.' || c[i] == '?' || c[i] == '!') {
					containSentenceEnd = true;
				}
			}
			if(!containSentenceEnd)return false;
		}
		return true;
	}
}
