package multir.exp;

import java.io.IOException;

import multir.preprocess.CreateProtobufDatasets;
import multir.preprocess.freebase.ComputeMatches;

public class Step1 {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	public static void main(String[] args) throws IOException {

		String freebaseDumpFile =
			"/projects/pardosa/data10/raphaelh/freebase/freebase-datadump-quadruples.tsv";
		
		// process freebase
//		ExtractNamedEntitiesFromFreebase.extract(freebaseDumpFile, dir + "/freebase-names", dir);
	
		// for easier processing, we first reduce the number of Freebase 
		// entity names to those we discovered in the text
		//ComputeMatches.createFreebaseEntitiesInText(dir + "/sentences.ner", dir + "/freebase-names",
		//		dir + "/freebase-names-text", dir);

		ComputeMatches.main(args);
	
		CreateProtobufDatasets.main(args);
	}
}
