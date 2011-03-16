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
			+ "/enid_mid_wid_argname_otherarg_relation_label.sbwid";

	/**Input: freebase entity 2 type*/
	static String fin_freebase_type_sortMid = pdir+"/mid_type_argname.sbmid";
	static String fout_freebase_type_sortMid_subset = dir+"/mid_type_argname.subset";
	static String fout_fbtype_count = pdir+"/fbtype_count";
	
	/**output the candidate type mappings, just by entity mapping result*/
	static final int CANDIDATE_NUM= 10;
	static String fout_candidatemapping_nelltype_fbtype = dir+"/candidatemapping_nelltype_fbtype";
	static String fout_weight_type_shareentity = dir+"/weight_typeshareentity";
	static String fout_weight_type_shareentity_tfidf = dir+"/weight_typeshareentity_tfidf";
	//static String fout_candidatemapping_nelltype_fbtype_count = dir+"/candidatemapping_nelltype_fbtype_count";
	//static String fout_candidatemapping_nelltype_fbtype_argname_mid = dir+"/candidatemapping_nelltype_fbtype_argname_mid";
	
	/**output the Freebase searching result of the nell entities in surface string*/
	static String fout_fbsearchresult_raw = dir+"/fbsearch_label_arg1_arg2_relation_mid1list_mid2list";
	static String fout_fbsearchresult_clean = dir+"/enid_mid_wid_argname_otherarg_relation_label.sbwid";
	
	/**Mid 2 Wikipedia Id*/
	static String fin_wex_category = "/projects/pardosa/s5/clzhang/tmp/wex/a/wex-category.tsv";
	static String fin_freebasedump_wikipedia_enid = pdir+"/fbdump_wikipedia_enid.temp";
	static String fout_mid_artid = pdir+"/mid_wid_title.sbwid";
	static String fout_mid_artid_sbmid =  pdir+"/mid_wid_title.sbmid";
	//static String fout_mid_mainwid = pdir+"/mid_mainwid.sbwid";
	//static String fout_mid_mainwid_sbmid = pdir+"/mid_mainwid.sbmid";
	static String fout_wid_categorywiki = pdir+"/wid_catwiki.sbwid";
	/**mid enurl*/
	static String fin_mid_enurl = pdir+"/mid2enurl";
	
	
	/**wikipedia stanford parsed result*/
	static String fin_wp_stanford = "/projects/pardosa/s5/clzhang/tmp/wp/stanfordwiki_senid_artid_secid_token_pos_ner.sortbyArtid";
	static String fout_wp_mainidlist = dir+"/mainwidlist";
	static String fout_wp_stanford_subset = dir+"/stanfordwiki_senid_artid_secid_token_pos_ner.subset.sbaid";
	static String fout_wp_stanford_s4subset = dir+"/stanfordwiki_senid_artid_secid_token_pos_ner.s4subset.sbwid";
	/**candidate nell string 2 mid & score*/
	static String fout_candidatemapping_nellstring_mid = dir+"/candidatemapping_nellstring_mid";
	static String fout_weight_nellstring_mid_cosine = dir+"/weight_entityname_cosine";
	static String fout_temp_mid_wid_argA_argB_appearInWiki = dir+"/temp_mid_wid_argA_argB_appearInWiki";
	
	/**fbname and alias*/
	static String fin_fbnamealias_unsorted = pdir+"/fbnamealias";
	static String fin_fbnamealias = pdir+"/mid_fbnamealias.sbmid";
	static String fin_fbnamealias_subset = dir+"/mid_fbnamealias.sbmid.subset";
	
	/**nellclass classifier*/
	static String fout_nelltype_mid_mainwid = dir+"/nelltype_mid_mainwid";
	static String fout_nelltypeNA = dir+"/nelltypeNA_nullmid_wid";
	static String dir_nellclassifier = dir+"/nelltypeclassifier";
	static String fout_nelltype_featurized= dir_nellclassifier+"/features";
	static String fout_nelltype_binary = dir_nellclassifier+"/binary";
	static String fout_wid_categorywiki_subset = dir+"/wid_wikicat_subset";
	static final int TOPKSentenceInWkarticle = 5;
	static final double NATakeRatio = 0.3;
	static final int MIN_TRAINING_INSTANCE = 10;
	/**weighted disjunctive clauses */
	static String fout_clauses = dir+"/clauses";
	static String fout_predict1 = dir+"/predict1";
	
	
	/**canonical rule weight*/
	static final int WEIGHT_CANONICAL = 1000;
	public static void main(String[] args) throws Exception{



		
		/**Get mid to wikipedia article id,with the help of 
		 * mid_wikiurl file
		 * wikipedia article id VS title*/
		//S0_join_MidWikiurl_wpArticleidTitle.main(null);
		
		/**Sort freebase name file, by mid*/
		//S0_sort_fbnamealias.main(null);
		
		
		/**Generate candidate nell entity vs fb entity
		 * (VERY BIG java file)*/
		S2_variable_nellent_fbmid.main(null);
		
		
		/**Generate candidate Nell type vs FB Type*/
		S1_variable_nelltype_fbtype_count.main(null);
		

		
		/**Generate clauses*/
		S3_clause.main(null);
		
		/**Weigthed maximum sat*/
		SX_weightedmaxsat.main(null);
	}

}
