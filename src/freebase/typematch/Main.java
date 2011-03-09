package freebase.typematch;

import nell.preprocess.NellOntology;

public class Main {

	static NellOntology nellontology = new NellOntology();

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/typematch";

	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink/";
	
	/**nice looking types, we only consider these types*/
	static String fin_fbtype_nicelooking = dir+"/fb_nice_types";
	
	/**This file is created by freebase.preprocess.NellSeedSearchInFBEngineClean*/
	static String fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid = dir
			+ "/enid_mid_wid_argname_otherarg_relation_label.sortbyWid";

	/**Input: freebase entity 2 type*/
	static String fin_freebase_type_sortMid = dir+"/fb_mid_type_argname.temp";
	static String fout_freebase_type_sortMid_subset = dir+"/fb_mid_type_argname.subset";
	
	/**output the candidate type mappings, just by entity mapping result*/
	static String fout_candidatemapping_nelltype_fbtype_count = dir+"/candidatemapping_nelltype_fbtype_count";
	static String fout_candidatemapping_nelltype_fbtype_argname_mid = dir+"/candidatemapping_nelltype_fbtype_argname_mid";
	
	
	
	public static void main(String[] args) throws Exception{

		/**Get a subset of fbtype infomation, the whole set is in
		 * /projects/pardosa/s5/clzhang/ontologylink/fb_mid_type_argname
		 * */
		S0_subset_fbtype.main(null);
		
		/**Generate candidate Nell type vs FB Type*/
		S1_variable_nelltype_fbtype_count.main(null);
		
		/***/
		
	}

}
