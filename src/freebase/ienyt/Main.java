package freebase.ienyt;

import java.io.File;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/ienyt";
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
			pdir = "o:/unix" + pdir;
		}
	}
	static String file_nyt05pb = dir + "/subset05.100.pb.gz";
	static String file_nyt07pb = dir + "/subset07.100.pb.gz";
	/**give nyt relations my id, replace "Type" with the id*/
	static String file_myidnyt05pb = dir + "/myid.subset05.100.pb.gz";
	static String file_myidnyt07pb = dir + "/myid.subset07.100.pb.gz";
	static String file_myidnyt05pbwidpair = file_myidnyt05pb+".widpair";
	static String file_myidnyt07pbwidpair = file_myidnyt07pb+".widpair";
//	static String file_myidnyt05pbwidpairnellrelation = file_myidnyt05pbwidpair+".nellrelation";
//	static String file_myidnyt07pbwidpairnellrelation = file_myidnyt07pbwidpair+".nellrelation";
	static String file_allentitypair = dir+"/subset05.allentitypair";
	static String exp_nellonly_relabel_nyt05pb = dir+"/exp0_nellonly_relabel_nyt05";
	static String exp_mylabel0 = dir+"/exp_mylabel0";
	static String exp_mylabel1 = dir+"/exp_mylabel1";
	//static String file_nyt07pb = dir+"/"
	
	public static void main(String []args)throws Exception{
		//S0_seedonly.main(null);
		S2_withmapping.main(null);
	}
}
