package freebase.preprocess3;

import java.io.File;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/freebase";
	static {
		if (!(new File(dir)).exists()) {
			dir = "o:/unix" + dir;
		}
	}

	static String file_fbdump = dir + "/freebase-datadump-quadruples.tsv";
	static String file_fbdump_1_tuple = dir + "/fbdump_1_tuple";
	public static String file_fbdump_2_len4 = dir + "/fbdump_2_len4";
	static String file_fbdump_3_str = dir + "/fbdump_3_str";
	static String file_fbenglishname = dir + "/visible_name";

	static String file_fbenglishname_sbmid = dir + "/visible_name.sbmid";
	static String file_fbdump_1_tuple_sb1 = dir + "/fbdump_1_tuple.sb1";
	static String file_fbdump_1_tuple_sb2 = dir + "/fbdump_1_tuple.sb2";
	static String file_fbdump_3_str_noh_sb1 = file_fbdump_3_str + ".nohidden.sb1";

	static String file_visible = dir + "/visible";
	static String file_visible_filter = dir + "/visible.filter";
	static String file_visible_str = dir + "/visible_str";
	static String file_visible_jump = dir + "/visible_jump";

	static String file_graph_matrix = dir + "/graph.matrix";//format: relid, myid1, myid2
	static String file_graph_string = dir + "/graph.string";//format: relid, myid1, stringvalue;
	static String file_graph_mid2myid = dir + "/graph.mid2myid";
	static String file_graph_rel2myid = dir + "/graph.rel2myid";
	

	static String file_mid2wid = dir + "/mid2wid";
	static String file_mid2notabletype = dir + "/mid2notabletype";
	static String file_typeInStr2typeInID = dir+"/typeInStr2typeInID";
	static String file_mid2typeIdLst = dir+"/mid2typeIdLst";
	static String file_midWidTypeNameAlias = dir + "/mid_wid_type_name_alias";
	public static String file_mid2namesfullset = dir + "/mid2namefullset";

	/**Parse Wex*/
	static String file_ibcall = dir + "/freebase-wex-2011-05-27-template_calls.tsv";
	static String file_ibvalue = dir + "/freebase-wex-2011-05-27-template_values.tsv";
	static String file_ibwex = dir + "/ibdump";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
