package parseWex;

import java.io.File;

public class Main {

	static String dir = "/projects/pardosa/s5/clzhang/tmp/wex";
	static String pdir = "/projects/pardosa/";
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			pdir = "o:/unix" + pdir;
		}
	}
	static String fin_wex_articles = dir + "/freebase-wex-2011-02-19-articles.tsv";
	static String fin_delimited_sections = dir + "/wexsections_wid_sectionid_title_section";
	static String fout_wex_title = dir + "/wex_title";
	static String output = dir + "/wexsentences";//used to be in wp
	static String fout_wexsplit_template = pdir + "/data$DNUM$/clzhang/tmp/wp/wex_article.part";
	static String fout_stanfordtext_template = pdir + "/data$DNUM$/clzhang/tmp/wp/wex_stanfordtext.part";
	static String[] datas = new String[] { "01", "01", "01", "01", //p01
			"02", "02", "02", "02", //p02
			"08", "08", "08", "08", //p03
			//"04", "04", "04","04", //p04
			//"12","12","12","12", //p05
			//"09","09","09","09", //p06
			"13", "13", "13", "13", "13", "13", "13", //p07
			"15", "15", "15", "15", "15", "15", "15" //p08
	};

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
