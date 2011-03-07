package freebase.maxent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import multir.util.delimited.DelimitedReader;

public class RecordWpSenToken {

	int sentenceId;
	int sectionId;
	int articleId;
	String rawSentence;
	String []token_str;
	int [][]token_pos;

	public static RecordWpSenToken read(DelimitedReader dr) throws IOException {
		RecordWpSenToken rwst = new RecordWpSenToken();
		String[] line = dr.read();
		if (line == null) {
			return null;
		} else {
			rwst.sentenceId = Integer.parseInt(line[0]);
			rwst.sectionId = Integer.parseInt(line[1]);
			rwst.articleId = Integer.parseInt(line[2]);
			rwst.rawSentence = line[3];
			try{
				rwst.token_str = line[4].split("\\t");
				String []temp = line[5].split(" ");
				rwst.token_pos = new int[temp.length][2];
				for(int i = 0;i<temp.length;i++){
					String []ab = temp[i].split(":");
					rwst.token_pos[i][0] = Integer.parseInt(ab[0]);
					rwst.token_pos[i][1] = Integer.parseInt(ab[1]);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return rwst;
		}
	}
	
	static RecordWpSenToken buffer_rwst = null;
	public static List<RecordWpSenToken> readByArticleId(DelimitedReader dr) throws IOException {
		List<RecordWpSenToken>rwstlist = new ArrayList<RecordWpSenToken>();
		if(buffer_rwst == null) buffer_rwst = RecordWpSenToken.read(dr);
		rwstlist.add(buffer_rwst);
		RecordWpSenToken rwst;
		while((rwst = RecordWpSenToken.read(dr))!=null){
			if(rwst.articleId == rwstlist.get(0).articleId){
				rwstlist.add(rwst);
			}else{
				buffer_rwst = rwst;
				break;
			}
		}
		return rwstlist;
	}
	
	public String toString(){
		return rawSentence;
	}
}
