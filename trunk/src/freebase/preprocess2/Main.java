package freebase.preprocess2;

import java.io.File;

public class Main {
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/wikidump";
	static String fout_stanfordtext_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/wex_stanfordtext.part";
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			pdir = "o:/unix" + pdir;
			fout_stanfordtext_template = "o:/unix" + fout_stanfordtext_template;
		}
	}
	//static String file_wp_stanford = pdir + "/stanfordwiki_senid_artid_secid_token_pos_ner.sortbyArtid";
	static String file_delimited_sections = dir + "/wexsections_wid_sectionid_title_section";
	static String file_wid_wholearticle = dir + "/wid_wholearticle";
	static String file_wholewikiarticle_luceneindex = dir + "/wholewikiarticle_luceneindex";
	//static String file_parsed_wikisections = dir + "/sentences";
	//static String fout_wexsplit_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/wex_article.part";

	//	static {
	//		if (!(new File(fout_wexsplit_template+"0")).exists()) {
	//			fout_wexsplit_template = "o:/unix"+fout_wexsplit_template;
	//		}
	//	}
//	static String[] datas = new String[] { "01", "01", "01", "01", //p01
//			"02", "02", "02", "02", //p02
//			"08", "08", "08", "08", //p03
//			//"04", "04", "04","04", //p04
//			//"12","12","12","12", //p05
//			//"09","09","09","09", //p06
//			"13", "13", "13", "13", "13", "13", "13", //p07
//			"15", "15", "15", "15", "15", "15", "15" //p08
//	};

	static String file_freebasedump = pdir + "/freebase-datadump-quadruples.tsv";
	
	//for nyt
	static String file_nyt_paragraph = "/projects/pardosa/data15/raphaelh/data/paragraphs";
	static String dir_nytdump = "/projects/pardosa/s5/clzhang/ontologylink/nytdump";
	static String file_nyt_widsecidparagraph = dir_nytdump + "/wid_sectionid_paragraph";
	static String file_nytsplit_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/nyt.part";
	static String file_nyt_sentences = dir_nytdump+"/sentences";
	static String[] datas = new String[] { "13", "13", "13", "13", "13", "13", "13", "13", //p07
			"15", "15", "15", "15", "15", "15", "15"//p08
	};
}
