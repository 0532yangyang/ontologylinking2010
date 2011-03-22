package freebase.relmatch;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/relationmatch";
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
	
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
}
