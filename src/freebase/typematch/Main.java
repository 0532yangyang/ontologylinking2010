package freebase.typematch;

import nell.preprocess.NellOntology;

public class Main {

	static NellOntology nellontology = new NellOntology();

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/typematch";

	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink/";
	
	/**This file is created by freebase.preprocess.NellSeedSearchInFBEngineClean*/
	static String fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid = dir
			+ "/enid_mid_wid_argname_otherarg_relation_label.sortbyWid";

	/**Input: freebase entity 2 type*/
	static String fin_freebase_type_sortMid = pdir+"/freebase_type.sbmid";
	static String fout_freebase_type_sortMid_subset = dir+"/freebase_type.sbmid.nellsubset";
	
	/**output the candidate type mappings, just by entity mapping result*/
	static String fout_candidatemapping_nelltype_fbtype_count = dir+"/candidatemapping_nelltype_fbtype_count";
	
	
	
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		/**Generate candidate Nell type vs FB Type*/
		Step1_candidatemapping_nelltype_fbtype_count.main(null);
		
		/***/
		
	}

}