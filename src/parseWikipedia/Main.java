package parseWikipedia;

import java.io.File;

import java.util.List;

import stanfordCoreWrapper.CoreNlpPipeline;
import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.parsers.Char;

public class Main {
	static String input1 = "/projects/pardosa/s5/clzhang/tmp/wp/enwiki_rawtext";
	static String output = "/projects/pardosa/s5/clzhang/tmp/wp/stanfordwiki_senid_artid_secid_token_pos_ner";
	static String file_blikitext_all = "/projects/pardosa/s5/clzhang/tmp/wp/enwiki_parsedtext";
	static String file_blikitext_all_clean = "/projects/pardosa/s5/clzhang/tmp/wp/enwiki_parsedtext_clean";
	static String file_blikitext_all_wipedaway = "/projects/pardosa/s5/clzhang/tmp/wp/enwiki_parsedtext_wipedaway";
	static String dir_blikitext_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/";
	static String file_blikitext_template = "enwiki_blikitext.part";
	static String file_stanfordtext_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/enwiki_stanfordtext.part";
	static String[] datas = new String[] { "01", "01", "01","01", //p01
		"02", "02", "02","02", //p02
		"08", "08", "08","08", //p03
		"04", "04", "04","04", //p04
		"12","12","12","12", //p05
		"09","09","09","09", //p06
		"13", "13", "13","13","13","13" //p07
		};

	public static void main(String[] args) throws Exception {
		/**
		 * file_blikitext_all: comes from the result of info.bliki.wiki*
		 
		  
		/**Step 1: clean the data
		 */
		Step1_CleanBlikiText.main(null);

		/**Step 2: split the data*/
		
		/*** Step2 : call stanford */
		Step3_CallStanford.main(null);

	}
}
