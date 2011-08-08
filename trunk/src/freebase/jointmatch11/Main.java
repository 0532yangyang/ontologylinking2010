package freebase.jointmatch11;

import java.io.File;
import java.util.Date;

import javatools.administrative.D;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/jointmatch11";
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
	static String dirwikidump = "/projects/pardosa/s5/clzhang/ontologylink/wikidump";
	static String dir_wikisection = Main.dir + "/wikisection";
	static String fbdir = "/projects/pardosa/s5/clzhang/freebase";

	static String file_mid_wid_type_name_alias = "/projects/pardosa/s5/clzhang/freebase/mid_wid_type_name_alias";
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			pdir = "o:/unix" + pdir;
			dir_wikisection = "o:/unix" + dir_wikisection;
			fbdir = "o:/unix" + fbdir;

			file_mid_wid_type_name_alias = "o:/unix" + file_mid_wid_type_name_alias;
		}
	}
	static String file_fbvisible = fbdir + "/visible.filter.sbmid";
	static String file_fbvisible2 = fbdir + "/visible.filter.sbmid2";

	static String file_fbgraph_matrix = fbdir + "/graph.matrix";
	static String file_fbgraph_rel2myid = fbdir + "/graph.rel2myid";
	static String file_fbgraph_mid2myid = fbdir + "/graph.mid2myid";

	/**outside input*/
	static String file_ontology = dir + "/nellontology";
	static NellOntology no = new NellOntology(Main.file_ontology);
	public static String file_wp_stanford = pdir + "/stanfordwiki_senid_artid_secid_token_pos_ner.sortbyArtid";
	//static String file_fbnode = dir+"/fbnode";
	static String file_fbnode_sbmid = pdir + "/fbnode.sbmid";

	/**S0*/
	static String file_mid2enurl = pdir + "/mid2enurl";
	static String file_gnid_mid_enurl_wid_title = pdir + "/gnid_mid_enurl_wid_title";
	static String file_gnid_mid_enurl_wid_title_type = pdir + "/gnid_mid_enurl_wid_title_type";
	static String file_gnid_mid_enurl_wid_title_type_clean = pdir + "/gnid_mid_enurl_wid_title_type.clean";
	static String file_notable_for_raw = pdir + "/fb_notable_for_raw";
	static String file_notable_for_raw2 = pdir + "/fb_notablefor_raw2_mid_type";
	static String file_notablefor_mid_wid_type = pdir + "/fb_notablefor_mid_wid_type";
	static String file_wikipediapagelink = pdir + "/wikipedia_pagelink";
	static String file_wikipediapagelinkraw = pdir + "/wikipedia_pagelink.raw";
	static String file_wikititles = pdir + "/wikipediatitles";
	static String file_wikipediapagelink_indegree = pdir + "/wikipedia_pagelink_indegree";
	static String file_wikipediapagelink_outdegree = pdir + "/wikipedia_pagelink_outdegree";
	static String file_wex_title = pdir + "/wex_title";
	/**EVAL*/
	static String file_eval_fbsearchresult = dir + "/eval/eval_fbsearch";
	/**S1*/
	static String file_fbsearch1 = dir + "/fbsearch.raw1";
	static String file_fbsearch2 = dir + "/fbsearch.raw2";
	static String file_fbsearch3 = dir + "/fbsearch.raw3";
	static String file_fbsearch4 = dir + "/fbsearch.raw4";
	static String file_fbsearchmatch_len1 = dir + "/fbsearch.match.len1";
	static String file_fbsearchmatch_len2 = dir + "/fbsearch.match.len2";

	static String file_relationmatch_candidate = dir + "/candidate.relation";
	static String file_typematch_candidate = dir + "/candidate.type";
	static String file_entitymatch_candidate = dir + "/candidate.entity";

	static String file_entitymatch_staticfeature = dir + "/staticfeature.entity";
	static String file_typematch_staticfeature = dir + "/staticfeature.type";
	static String file_relationmatch_staticfeature = dir + "/staticfeature.relation";

	static String file_jointclause = dir + "/jointclause";

	static String file_fbsearchcandidate = dir + "/fbsearch.candidate";
	//	static String file_fbsearchcandidate_len1 = dir + "/fbsearch.candidate.len1";
	//	static String file_fbsearchcandidate_len2 = dir + "/fbsearch.candidate.len2";
	static String file_fbsql2instances_len1 = dir + "/sql2instance.len1";
	static String file_fbsql2instances_len2 = dir + "/sql2instance.len2";
	static String file_fbsql2instances = dir + "/sql2instance";
	
	static String file_wksensubset = dir + "/wksen_subset";
	static String file_wklinksub = dir + "/wklink_subset";
	static String file_knownnegative = dir + "/knownnegative";
	//	static String file_ontologyentity2fbentity_byfbsearch = dir
	//			+ "/fbsearch_label_arg1_arg2_relation_mid1list_mid2list";
	//	static String file_ontologyentity2fbentity_bylucene = dir + "/lucene_argname_mid_wid_names_sharewords_indegree";
	//	static String file_seedluceneclean = dir + "/seedluceneclean";
	//	static String file_enid_mid_wid_argname_otherarg_relation_label = dir
	//			+ "/enid_mid_wid_argname_otherarg_relation_label";
	//	static String file_enid_mid_wid_argname_otherarg_relation_label_top1 = dir
	//			+ "/enid_mid_wid_argname_otherarg_relation_label_top1";
	//static String file_wp_stanford_subset = dir + "/stanfordwiki_senid_artid_secid_token_pos_ner.subset.sbaid";
	//	static String file_candidatemapping_nellstring_mid = dir + "/candidatemapping_nellstring_mid";
	//	static String file_weight_nellstring_mid_cosine = dir + "/weight_nellstring_mid_cosine";
	//static String file_freebase_type_sortMid_subset = dir + "/freebase_type_sortMid_subset";
	static double string_sim_smooth = 0.01;
	/**S2*/
	static String file_weight_type_shareentity = dir + "/weight_typeshareentity";
	static String file_weight_type_negative = dir + "/weight_negative_shareentity";
	static int candidate_num_nelltype_fbtype = 10;
	static String file_candidatemapping_nelltype_fbtype = dir + "/candidatemapping_nelltype_fbtype";
	static String file_nelltype_fbtype_evidenceentity_mid = dir + "/nelltype_fbtype_evidenceentity_mid";
	/**S3*/
	static final int WEIGHT_CANONICAL = 10000;
	static final String file_clausetype = dir + "/clause_type";
	static final String file_clauseentity = dir + "/clause_entity";


	/**S6*/
	static String file_relabel_sql2instance = dir+"/relabel_sql2instance";
	static String dir_nytdump = pdir+"/nytdump";

	//static int PAR_NUM_PAIRS_PER_RELATION = 200;

	public static void main(String[] args) throws Exception {
		S1_generatecandidate.main(null);
		S2_staticfeature.main(null);
		S3_jointfeature.main(null);
		S7_gibbssampling2.main(null);
	}
}
