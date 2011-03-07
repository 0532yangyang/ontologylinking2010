package freebase.maxent;

import java.io.IOException;
import java.io.PrintStream;

import multir.util.delimited.DelimitedReader;

public class MainRelLevel {

	/**
	 * @param args
	 */
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp2";
	static String input1 = dir+"/seedLinksShow.group";
	public static void main(String[] args) throws Exception {
		System.setOut(new PrintStream(dir+"/debug"));
		DelimitedReader dr =new DelimitedReader(input1);
		RecordRelMatch rmr ;//= RelMatchRecord.read(dr);
		while((rmr = RecordRelMatch.read(dr))!=null){
			Featurizer.featurizeRelation(rmr);
		}
		dr.close();
	}

}
