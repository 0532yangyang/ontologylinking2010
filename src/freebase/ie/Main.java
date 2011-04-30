package freebase.ie;

public class Main {

	/**
	 * @param args
	 */
	static String pdir = "/projects/pardosa/s5/clzhang/ontologylink";
	static String dir = pdir + "/ie";
	static String file_background_pattern_raw = dir + "/background_pattern.raw";
	static String file_background_pattern_sort = dir + "/background_pattern.sort";
	static String file_background_pattern_uniqc = dir + "/background_pattern.uniqc";
	public static String file_background_pattern_uniqc_10 = dir + "/background_pattern.uniqc.10";
	/**S1: pair 2 feature*/
	static String file_instance_widpair = dir + "/instance_widpair";
	static String file_wiki_sample = dir + "/wikisample.sbwid";
	static String file_instance_featurize = dir + "/instance_featurize";
	static String file_instance_loglinear = dir + "/instance_loglinear";

	/**S2: pipeline Ontology mapping for relation extraction*/
	static final double SMOOTH = 0.1;

	/**Evaluation*/
	static String eval_dir = dir + "/report";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		S0_background.main(null);

	}

}
