package freebase.typematch;

import nell.preprocess.NellOntology;

public class Main {

	static NellOntology nellontology = new NellOntology();

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/typematch";

	public static String pdir = "/projects/pardosa/s5/clzhang/ontologylink/";

	/** nice looking types, we only consider these types */
	static String fin_fbtype_nicelooking = dir + "/fb_nice_types";

	/**
	 * This file is created by freebase.preprocess.NellSeedSearchInFBEngineClean
	 */
//	static String fin_enid_mid_wid_argname_otherarg_relation_label_sortbywid = dir
//			+ "/enid_mid_wid_argname_otherarg_relation_label.sbwid";

	/** Input: freebase entity 2 type */
	static String fin_freebase_type_sortMid = pdir + "/mid_type_argname.sbmid";
	static String fin_freebase_type_clean_sortMid = pdir + "/mid_type_argname.clean.sbmid";
	static String fin_freebase_type_clean_sample = dir + "/mid_type_argname.clean.sample";
	static String fin_freebase_type_sortType = pdir + "/mid_type_argname.sbtype";
	static String fout_freebase_type_sortMid_subset = dir + "/mid_type_argname.subset";
	static String fout_fbtype_count = pdir + "/fbtype_count";

	/** output the candidate type mappings, just by entity mapping result */
	static final int CANDIDATE_NUM = 10;
	static String fout_candidatemapping_nelltype_fbtype = dir + "/candidatemapping_nelltype_fbtype";
	static String fout_weight_type_shareentity = dir + "/weight_typeshareentity";
	static String fout_weight_type_shareentity_tfidf = dir + "/weight_typeshareentity_tfidf";
	// static String fout_candidatemapping_nelltype_fbtype_count =
	// dir+"/candidatemapping_nelltype_fbtype_count";
	// static String fout_candidatemapping_nelltype_fbtype_argname_mid =
	// dir+"/candidatemapping_nelltype_fbtype_argname_mid";

	/**
	 * output the Freebase searching result of the nell entities in surface
	 * string
	 */
	static String fout_fbsearchresult_raw = dir + "/fbsearch_label_arg1_arg2_relation_mid1list_mid2list";
	public static String fout_fbsearchresult_clean = dir + "/enid_mid_wid_argname_otherarg_relation_label.sbwid";

	/** Mid 2 Wikipedia Id */
	static String fin_wex_category = "/projects/pardosa/s5/clzhang/tmp/wex/a/wex-category.tsv";
	static String fin_freebasedump_wikipedia_enid = pdir + "/fbdump_wikipedia_enid.temp";
	public static String fout_mid_artid = pdir + "/mid_wid_title.sbwid";
	static String fout_mid_artid_sbmid = pdir + "/mid_wid_title.sbmid";
	// static String fout_mid_mainwid = pdir+"/mid_mainwid.sbwid";
	// static String fout_mid_mainwid_sbmid = pdir+"/mid_mainwid.sbmid";
	static String fout_wid_categorywiki = pdir + "/wid_catwiki.sbwid";
	/** mid enurl */
	static String fin_mid_enurl = pdir + "/mid2enurl";

	/** wikipedia stanford parsed result */
	static String fin_wp_stanford = "/projects/pardosa/s5/clzhang/tmp/wp/stanfordwiki_senid_artid_secid_token_pos_ner.sortbyArtid";
	static String fout_wp_mainidlist = dir + "/mainwidlist";
	static String fout_wp_stanford_subset = dir + "/stanfordwiki_senid_artid_secid_token_pos_ner.subset.sbaid";
	static String fout_wp_stanford_s4subset = dir + "/stanfordwiki_senid_artid_secid_token_pos_ner.s4subset.sbwid";
	/** candidate nell string 2 mid & score */
	public static String fout_candidatemapping_nellstring_mid = dir + "/candidatemapping_nellstring_mid";
	static String fout_weight_nellstring_mid_cosine = dir + "/weight_entityname_cosine";
	static String fout_temp_mid_wid_argA_argB_appearInWiki = dir + "/temp_mid_wid_argA_argB_appearInWiki";

	/** fbname and alias */
	static String fin_fbnamealias_unsorted = pdir + "/fbnamealias";
	static String fin_fbnamealias = pdir + "/mid_fbnamealias.sbmid";
	static String fin_fbnamealias_subset = dir + "/mid_fbnamealias.sbmid.subset";

	/** nellclass classifier */
	static String fout_training_nelltype_mid_mainwid = dir + "/training_nelltype_mid_mainwid";
	static String fout_testing_nelltype_fbtype_fbtypeinstance_mid_wid = dir + "/testing_nt_ft_fte_mid_wid";
	static String fout_nelltypeNA = dir + "/nelltypeNA_nullmid_wid";
	static String dir_nellclassifier = dir + "/nelltypeclassifier";
	static String fout_training_featurized = dir_nellclassifier + "/trainingfeatures";
	static String fout_testing_featurized = dir_nellclassifier + "/testingfeatures";
	static String fout_testing_pred = dir_nellclassifier + "/testingpredict";
	// static String fout_nelltype_binary = dir_nellclassifier+"/binary";
	static String fout_wid_categorywiki_subset = dir + "/wid_wikicat_subset";
	static final int TOPKSentenceInWkarticle = 5;
	static final double NATakeRatio = 0.3;
	static final int MIN_TRAINING_INSTANCE = 0;
	static final int SAMPLPE_SIZE_PER_FBTYPE = 100;

	/** weighted disjunctive clauses */
	static String fout_clauses = dir + "/clauses";
	static String fout_predict1 = dir + "/predict1";

	/** canonical rule weight */
	static final int WEIGHT_CANONICAL = 1000;

	static boolean s0_fb_cleanFBType = false;
	static boolean s0_sample1000EveryType = false;
	static boolean s0_sortByType = false;
	//static boolean s0_fb_cleanFBType = true;
	// static boolean s0_sample1000EveryType = true;
	// static boolean s0_sortByType =true;
	

	public static void main(String[] args) throws Exception {

		/**
		 * Get mid to wikipedia article id,with the help of mid_wikiurl file
		 * wikipedia article id VS title
		 */
		//S0_fb.main(null);

		/** Sort freebase name file, by mid */
		// S0_sort_fbnamealias.main(null);

		/** Dealing with wikipedia */
		//S0_wikipedia.main(null);

		/**
		 * Generate candidate nell entity vs fb entity (VERY BIG java file)
		 */
		S1_variable_nellent_fbmid.main(null);

		/** Generate candidate Nell type vs FB Type */
		S2_variable_nelltype_fbtype_count.main(null);

		/** Train the classifier for fb entity */
		S4_nellclass_classifier.main(null);

		/** Generate clauses */
		S5_clause.main(null);

		/** Weigthed maximum sat */
		SX_weightedmaxsat.main(null);
	}

}
