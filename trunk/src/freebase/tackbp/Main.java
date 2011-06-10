package freebase.tackbp;

import java.io.File;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/tackbp/ontosmooth1";
	static String dirfb = "/projects/pardosa/s5/clzhang/freebase";
	static String dirwikidump = "/projects/pardosa/s5/clzhang/ontologylink/wikidump";
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
	
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			dirfb = "o:/unix" + dirfb;
			dirwikidump = "o:/unix" + dirwikidump;
			pdir = "o:/unix" + pdir;
		}
	}
	static String dir_nytdump = pdir + "/nytdump";
	static String file_gnid_mid_enurl_wid_title_type_clean = pdir + "/gnid_mid_enurl_wid_title_type.clean";
	static String file_ibdump = dirfb + "/ibdump";
	static String file_fbdump = dirfb + "/freebase-datadump-quadruples.tsv";
	static String file_fulltypeinfo = dirfb + "/full_type_infomation";
	static String file_fbvisibledump = dirfb + "/visible.filter.sbmid";
	static String file_mid2typename = dirfb + "/mid_wid_type_name_alias";

	static String file_citylist = dir + "/listcity";
	static String file_provincelist = dir + "/listprovince";
	static String file_countrylist = dir + "/listcountry";
	static String file_containedby = dir + "/containedby";

	static String file_mapping2ib = dir + "/mapping2ib";
	static String file_ibtuples = dir + "/ibtuples";
	static String file_mid_wid_type_name_alias = "/projects/pardosa/s5/clzhang/freebase/mid_wid_type_name_alias";

	static String file_tactext = "/projects/pardosa/s5/raphaelh/tac/data/sentences.text";
	static String file_tacmeta = "/projects/pardosa/s5/raphaelh/tac/data/sentences.meta";
	static String file_tacsplit_template = "/projects/pardosa/data$DNUM$/clzhang/tmp/wp/tac.part";
	static String file_parsedtac = "/projects/pardosa/s5/clzhang/tackbp/tacdump/sentences";
	static String[] datas = new String[] { "13", "13", "13", "13", "13", "13", "13", "13", //p07
		"15", "15", "15", "15", "15", "15", "15"//p08
	};
}
