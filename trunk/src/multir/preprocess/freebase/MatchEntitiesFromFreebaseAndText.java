package multir.preprocess.freebase;

import java.io.IOException;
import java.util.Comparator;

import multir.util.delimited.Sort;

public class MatchEntitiesFromFreebaseAndText {

	static String tmpDir = "/projects/pardosa/data16/raphaelh/tmp";
	static String ds = "/nytner3"; // "/wp"; "/nyt"
	
	static String input1 = tmpDir + ds + "/names";
	static String input2 = tmpDir + "/fb/names.en";

	static String tmp1 = tmpDir + ds + "/names.sorted";
	static String tmp2 = tmpDir + "/fb/names.en.sorted";

	/*
	static String output1 = tmpDir + ds + "/fb_matches";
	static String output2 = tmpDir + ds + "/fb_matches_ambiguities";
	static String output3 = tmpDir + ds + "/fb_matches_freqs";
	*/
	public static void main(String[] args) throws IOException {
		
		Sort.sort(input1, tmp1, tmpDir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2 ) {
				return t1[3].compareTo(t2[3]); } });
		
		//Sort.sort(input2, tmp2, tmpDir, new Comparator<String[]>() {
		//	public int compare(String[] t1, String[] t2 ) {
		//		return t1[3].compareTo(t2[3]); } });

	}
}
