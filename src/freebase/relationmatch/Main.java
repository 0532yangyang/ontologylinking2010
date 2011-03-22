package freebase.relationmatch;

public class Main {

	/**
	 * @param args
	 */
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/relationmatch";
	static String pdir = freebase.typematch.Main.pdir;
	
	static String fbdump = pdir+"/freebasedump.sort";
	
	/**S0*/
	static String file_wex_infobox_call  = "/projects/pardosa/s5/clzhang/tmp/wex/a/template_call.sbcid";
	static String file_wex_infobox_value = "/projects/pardosa/s5/clzhang/tmp/wex/a/template_values.sbcid";
	static String file_wex_template_clean = dir+"/template_wid_sid_cid_ibname_relation_value_description";
	static String file_infobox_clean = dir+"/infobox_wid_sid_cid_ibname_relation_value_description";
	static String file_infobox_clean_subset_sbwid = dir+"/infobox_wid_sid_cid_ibname_relation_value_description.subset.sbwid";
	static String file_argBmatchIBValue = dir+"/argBmatchIBvalue_wid_argA_argB_relation_arg1or2_ibname_ibrelation_value";
	/**S1*/
	static boolean s0_cleanWexInfobox = false;
	
	/**S1_matchFBrelation*/
	static String file_fbdump_subset_len0 = dir+"/fbdumpsubset_len0.sbmid";
	static String file_relation_len0 = dir+"/len0_mid_rel_value";
	static String file_relation_len1 = dir+"/len1_mid_rel_mid_wid";
	static String file_relation_len3= dir+"/len3_mid_rel_mid_wid";
	static String file_relation_len2 = dir+"/len2_mid_rel_value";
	static String file_merge_relation_len1len2 = dir+"/merge_len12_mid_rel_mid_wid";
	static String file_fbdump_subset_relation_len2_prepare = dir+"/fbdumpsubset_mid_rel_lang_len2_prepare_mid";
	
	static String file_match_len0 = dir+"/matchlen0_argAurl_argAmid_wid_argA_nr_arg12_fr_argB_fbvalue";
	static String file_match_len2 = dir+"/matchlen2_argAurl_argAmid_wid_argA_nr_arg12_fr_argB_fbvalue";
	static String file_match_len1 = dir+"/matchlen1_argAurl_argAmid_wid_argA_nr_arg12_fr_argB_fbmid_wid_value";
	static String file_match_len3 = dir+"/matchlen3_argAurl_argAmid_wid_argA_nr_arg12_fr_argB_fbmid_wid_value";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/**It is a failure*/
		//S0_wex.main(null);
		//S1_infoboxrelation.main(null);
	}

}
