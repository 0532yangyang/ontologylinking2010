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
	
	/**S0_fb*/
	static String file_fbdump_subset = dir+"/fbdump_subset.sbmid";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/**It is a failure*/
		//S0_wex.main(null);
		//S1_infoboxrelation.main(null);
	}

}
