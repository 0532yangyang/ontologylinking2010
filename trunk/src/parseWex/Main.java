package parseWex;

import java.io.File;

public class Main {

	static String dir = "/projects/pardosa/s5/clzhang/tmp/wex";
	static String fin_wex_articles = dir + "/freebase-wex-2011-02-19-articles.tsv";
	static String fout_wex_title = dir + "/wex_title";
	static String output = "/projects/pardosa/s5/clzhang/tmp/wp/stanfordwiki_senid_artid_secid_token_pos_ner";
	static String fout_wexsplit_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/wex_article.part";
	static String fout_stanfordtext_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/wex_stanfordtext.part";
	static String[] datas = new String[] { "01", "01", "01", "01", //p01
			"02", "02", "02", "02", //p02
			"08", "08", "08", "08", //p03
			//"04", "04", "04","04", //p04
			//"12","12","12","12", //p05
			//"09","09","09","09", //p06
			"13", "13", "13", "13", "13", "13", "13", //p07
			"15", "15", "15", "15", "15", "15", "15" //p08
	};
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			output = "o:/unix" + output;
			fout_wexsplit_template = "o:/unix" + fout_wexsplit_template;
			fout_stanfordtext_template = "o:/unix" + fout_stanfordtext_template;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
