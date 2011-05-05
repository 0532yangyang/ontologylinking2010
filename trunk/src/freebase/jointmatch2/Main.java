package freebase.jointmatch2;

import java.io.File;
import java.util.Date;

import javatools.administrative.D;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/jointmatch2";
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";

	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			pdir = "o:/unix" + pdir;
		}
	}
	/**outside input*/
	static String file_ontology = dir + "/nellontology";
	static NellOntology no = new NellOntology(Main.file_ontology);
	public static String file_wp_stanford = pdir + "/stanfordwiki_senid_artid_secid_token_pos_ner.sortbyArtid";
	static String file_gnid_mid_wid_title = pdir + "/gnid_mid_wid_title";
	//static String fin_freebase_type_sortMid = pdir + "/mid_type_argname.sbmid";
	//static String fin_mid_type = pdir + "/mid_type_argname.clean.sbmid";
	//static String file_wid_type_mid = pdir + "/wid_type_mid";
	//static String file_fbdumpclean = dir+"/fbdump.clean.sort";
	//static String file_fbgraph = dir+"/fbGraph";
	public static String file_fbgraph_clean = pdir + "/fbGraph_clean";
	public static String file_fbedge = pdir + "/fbedge";
	//static String file_fbnode = dir+"/fbnode";
	static String file_fbnode_sbmid = pdir + "/fbnode.sbmid";

	/**S0*/
	static String file_mid2enurl = pdir + "/mid2enurl";
	static String file_gnid_mid_enurl_wid_title = pdir + "/gnid_mid_enurl_wid_title";
	static String file_notable_for_raw = pdir + "/fb_notable_for_raw";
	static String file_notablefor_mid_wid_type = pdir + "/fb_notablefor_mid_wid_type";
	static String file_wikipediapagelink = pdir + "/wikipedia_pagelink";
	static String file_wex_title = pdir + "/wex_title";
	/**S1*/
	static String file_ontologyentity2fbentity_byfbsearch = dir
			+ "/fbsearch_label_arg1_arg2_relation_mid1list_mid2list";
	static String file_enid_mid_wid_argname_otherarg_relation_label = dir
			+ "/enid_mid_wid_argname_otherarg_relation_label";
	static String file_enid_mid_wid_argname_otherarg_relation_label_top1 = dir
			+ "/enid_mid_wid_argname_otherarg_relation_label_top1";
	static String file_wp_stanford_subset = dir + "/stanfordwiki_senid_artid_secid_token_pos_ner.subset.sbaid";
	static String file_candidatemapping_nellstring_mid = dir + "/candidatemapping_nellstring_mid";
	static String file_weight_nellstring_mid_cosine = dir + "/weight_nellstring_mid_cosine";
	//static String file_freebase_type_sortMid_subset = dir + "/freebase_type_sortMid_subset";
	static double string_sim_smooth = 0.01;
	/**S2*/
	static String file_weight_type_shareentity = dir + "/weight_typeshareentity";
	static String file_weight_type_negative = dir + "/weight_negative_shareentity";
	static int candidate_num_nelltype_fbtype = 10;
	static String file_candidatemapping_nelltype_fbtype = dir + "/candidatemapping_nelltype_fbtype";
	/**S3*/
	static final int WEIGHT_CANONICAL = 1000;
	static final String file_typeclause = dir + "/clause_type";

	/**S4*/
	static final int MaxDep = 3;
	static final int MaxNodeId = 45000000;
	static final String file_arg2gnid = dir + "/arg2gnid_arg12_mid12_gnid12";
	static String file_fbgraph_subset = dir + "/fbGraph_subset";
	static final String file_seedbfspath_raw = dir + "/seedbfspathes_raw";
	static final String file_seedbfspath_filteredby_wikiarticle = dir + "/seedbfspathes_filterby_wiki";
	static final String file_seedbfspath_show = dir + "/seedbfspathes_show";
	static final String file_seedbfspath_show_group = dir + "/seedbfspathes_show.group";
	static final String file_sql2instance = dir + "/sql2instance";
	static final String file_sql2instance_shuffle = Main.file_sql2instance + ".shuffle";

	/**S5*/
	static final String file_weight_similarity_nellrel_fbrel = dir + "/weight_similarity_nellrel_fbrel";
	static final String file_weight_sharepair_nellrel_fbrel = dir + "/weight_sharepair_nellrel_fbrel";
	static final String file_weight_seednegativepair_nellrel_fbrel = dir + "/weight_seednegativepair_nellrel_fbrel";
	static final String file_weight_defaultnegativepair_nellrel_fbrel = dir
			+ "/weight_defaultnegativepair_nellrel_fbrel";
	static final String file_weight_typesatifying = dir + "/weight_typesatisfying_nellrel_fbrel";
	static final String file_weight_wikipediapageline = dir + "/weight_pagelink"; //move to S7X because it relies on S72's result
	/**S6,S7,S8*/
	static final String file_relationcandidate = dir + "/candidate_rel";
	static final String file_relationrelclause = dir + "/clause_rel";
	static final String file_jointlclause = dir + "/clause_joint";
	public static final String file_predict_relonly = dir + "/predict_relation";
	public static final String file_predict_typeonly = dir + "/predict_type";
	static String file_predict_vote = dir + "/predict/predict_vote";
	static String file_predict_vote_newontology = dir + "/predict/predict_vote.newontology";
	public static final String file_predict_joint = dir + "/predict_joint";
	static String file_relation_typesign_arg1 = dir + "/relation_typesign_arg1";
	static String file_relation_typesign_arg2 = dir + "/relation_typesign_arg2";
	static String file_relation_typesign_debug = dir + "/relation_typesign_debug";
	static int PAR_NUM_CANDIDATE = 10;
	static int PAR_WEIGHT_CANONICAL = 1000;
	static double PAR_WEIGHT_SEED_NEG = 10;
	static double PAR_WEIGHT_Default_NEG = 10;
	static double PAR_PAGELINK_WEIGHT_THRESH = 0.3;

	/**Pattern*/
	static String file_background_pattern_raw = pdir + "/background_pattern.raw";
	static String file_background_pattern_sort = pdir + "/background_pattern.sort";
	static String file_background_pattern_uniqc = pdir + "/background_pattern.uniqc";
	public static String file_background_pattern_uniqc_10 = pdir + "/background_pattern.uniqc.10";
	/**S71,S72*/
	static String file_patternize_seedpair = dir + "/patternize_seedpair";
	static String file_patternize_sql2pair = dir + "/patternize_sql2pair";
	//static String file_patternize_seedpair_train = dir + "/patternize/seedpair_train";
	static String file_sql2instance_patternvariable = Main.file_sql2instance + ".sample4pattern";
	static String file_stanford_subsetforpattern = dir + "/stanford_subsetforpattern";
	static int SAMPLE_NUM = 100;
	static int throwaway_threshold = 10;
	static String file_widpair_patterns = dir + "/widpair_patterns";
	/**S9 */
	public static final String file_extendedwidpairs = dir + "/extendpairs_wid12_nellrel_fbrel";

	/**S10*/
	static String file_nyt05pb = dir + "/nyt/subset05.100.pb.gz";
	static String file_nyt07pb = dir + "/nyt/subset07.100.pb.gz";
	static String file_nyt07pbrelabel = dir + "/nyt/relabel.subset07.100.pb.gz";
	static String file_nyt05pbrelabel = dir + "/nyt/relabel.subset05.100.pb.gz";
	static String file_nyt05pbrelabelreduceneg = dir + "/nyt/relabel.reduceneg.subset05.100.pb.gz";
	static String file_nyt05countfeatures = dir + "/nyt/fts.relabel.subset05.100.pb.gz";
	static String file_nyt05countfeatures_count = file_nyt05countfeatures + ".counts";
	static int MAP_SIZE = 2000000;
	static int MIN_FTS = 1;
	static String expdir0 = dir + "/nyt/exp0";
	static String file_nytsentence = "/projects/pardosa/data14/raphaelh/t/raw/sentences";
	static String file_byrelation_raw = dir + "/nyt/byrelation_raw";
	/**give nyt relations my id, replace "Type" with the id*/
	static String file_myidnyt05pb = dir + "/nyt/myid.subset05.100.pb.gz";
	static String file_myidnyt07pb = dir + "/nyt/myid.subset07.100.pb.gz";
	static String file_myidnyt05pbwidpair = file_myidnyt05pb + ".widpair";
	static String file_myidnyt07pbwidpair = file_myidnyt07pb + ".widpair";
	//	static String file_myidnyt05pbwidpairnellrelation = file_myidnyt05pbwidpair+".nellrelation";
	//	static String file_myidnyt07pbwidpairnellrelation = file_myidnyt07pbwidpair+".nellrelation";
	static String file_allentitypair = dir + "/subset05.allentitypair";
	static String exp_nellonly_relabel_nyt05pb = dir + "/exp0_nellonly_relabel_nyt05";
	static String exp_mylabel0 = dir + "/exp_mylabel0";
	static String exp_mylabel1 = dir + "/exp_mylabel1";

	/**S11: bing api*/
	static String dir_bing = dir + "/bing";
	static String file_seed2bing = dir_bing + "/seedpairs2bing";
	static String file_extendedpair2bing = dir_bing + "/extendedpair2bing";
	static String file_seed2bing_pbgz = file_seed2bing + ".pb.gz";
	static String file_extendedpair2bing_pbgz = file_extendedpair2bing + ".pb.gz";
	//static String file_extendedpair2bing_byrelationraw = file_extendedpair2bing + ".byrelationraw";
	//static String expbingdir0 = dir_bing + "/exp0";
	static int PAR_NUM_PAIRS_PER_RELATION = 200;

	public static void main(String[] args) throws Exception {
		//		S1_variable_nellent_fbmid.main(null);
		//		S2_variable_nelltype_fbtype_count.main(null);
		S3_typeclause.main(null);
		//S4_variable_relationmatch.main(null);
		//S6_weightrelation.main(null);
		//		S61_typerelationjoint.main(null);
		//S73_getRidofNonSenseRelation.main(null);
		S7X_clauserelation.main(null);
		S8_inference.main(null);
		S9_extend2newinstances.main(null);
		D.p((new Date()).toString());
	}
}
