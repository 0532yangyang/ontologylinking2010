package freebase.relmatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/relationmatch";
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
//	static {
//		if((new File(dir)).exists()){
//			dir = "";
//		}
//	}
	static String reportdir = "/projects/pardosa/s5/clzhang/ontologylink/relationmatch/report";
	static String file_fbdump = pdir + "/freebasedump.sort";
	static String file_fbdumpclean = dir+"/fbdump.clean.sort";
	static String file_fbgraph = dir+"/fbGraph";
	static String file_fbgraph_clean = dir+"/fbGraph_clean";
	static String file_fbedge = dir+"/fbedge";
	static String file_fbnode = dir+"/fbnode";
	static String file_fbnode_sbmid = dir+"/fbnode.sbmid";
	static String file_fbgraph_subset = dir+"/fbGraph_subset";
	static String file_gnid_mid_wid_title = dir+"/gnid_mid_wid_title";
	
	/**bfs*/
	static final int MaxDep = 3;
	static final int MaxNodeId = 45000000;
	static final String file_arg2gnid = dir+"/arg2gnid_arg12_mid12_gnid12";
	static final String file_seedbfspath_raw = dir+"/seedbfspathes_raw";
	static final String file_seedbfspath_show = dir+"/seedbfspathes_show";
	static final String file_seedbfspath_show_group = dir+"/seedbfspathes_show_group";
	
	/**query graph by FB relation*/
	static final String file_queryresult = dir+"/queryresult";
	static final String file_queryresult_name = dir+"/queryresult_name";
	
	/**S4 similarity weight file*/
	static final String file_weight_similarity_nellrel_fbrel = dir+"/weight_similarity_nellrel_fbrel";
	static final String file_weight_sharepair_nellrel_fbrel = dir+"/weight_sharepair_nellrel_fbrel";
	static final String file_weight_seednegativepair_nellrel_fbrel = dir+"/weight_seednegativepair_nellrel_fbrel";
	static final String file_weight_defaultnegativepair_nellrel_fbrel = dir+"/weight_defaultnegativepair_nellrel_fbrel";
	static final String file_weight_typesatifying = dir+"/weight_typesatisfying_nellrel_fbrel";
	/**S5 instance and type constraint*/
	static final int PRM_sampledinstance = 100;
	static final String file_queryresult_sample = file_queryresult+".sample";
	static final String file_gnid2fbtype = dir+"/gnid_fbtype";
	static final String file_queryresult_typesatisfy= dir+"/queryresult_typesatisfy";
	/**S6 clauses and inference*/
	static final String file_candidate = dir+"/candidate";
	static final String file_clause = dir+"/clause";
	static final String file_predict = dir+"/predict";
	static int PAR_NUM_CANDIDATE = 10;
	static int PAR_WEIGHT_CANONICAL=1000;
	static double PAR_WEIGHT_SEED_NEG = 10;
	static double PAR_WEIGHT_Default_NEG = 10;
	
	
	public static void main(String []args){
		S6_clause.main(null);
		S7_weightedmaxsat.main(null);
	}
}
