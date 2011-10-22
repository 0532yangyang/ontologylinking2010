package faust.subclass;

import java.io.File;

public class Main {
	public static String dir = "/projects/pardosa/s5/clzhang/ontologylink/faust_subclass20111021";
	public static String dir_freebase = "/projects/pardosa/s5/clzhang/freebase";
	public static String file_mid2name = dir_freebase + "/mid_wid_type_name_alias";
	public static String file_wiki_sentence = "/projects/pardosa/s5/clzhang/ontologylink/wikidump/sentences";
	public static String file_nyt_sentence = "/projects/pardosa/s5/clzhang/ontologylink/nytdump/sentences";
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			dir_freebase = "o:/unix" + dir_freebase;
			file_wiki_sentence = "o:/unix" + file_wiki_sentence;
			file_nyt_sentence = "o:/unix" + file_nyt_sentence;

		}
	}

	public static String file_mid2namesfullset = freebase.preprocess3.Main.file_mid2namesfullset;

}
