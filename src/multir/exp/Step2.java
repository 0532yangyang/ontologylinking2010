package multir.exp;

import java.io.File;
import java.io.IOException;

import multir.learning.algorithm.MILModel;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.Mappings;

public class Step2 {

	//static String dir = "/projects/pardosa/data14/raphaelh/t";
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/ienyt";
	static{
		if(!(new File(dir)).exists()){
			dir = "o:/unix"+dir;
		}
	}

	/*
	static String input1 = "/projects/pardosa/data14/raphaelh/riedel0506/" +
		"train-all-newlabel-Multiple3.pb";
	static String input2 = "/projects/pardosa/data14/raphaelh/riedel0506/" + 
		"test-all-newlabel-Multiple3.pb";
	*/
	
	/**All nyt data*/
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset05.100.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";

	
	static String input1 = dir+"/subset05.100.pb.gz";
	static String input2 = dir+"/subset07.100.pb.gz";

//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/train-Multiple.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/test-Multiple.pb.gz";

	
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/train-Single.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/test-Multiple.pb.gz";
	
//	static String input1 = "/projects/pardosa/data16/raphaelh/riedel0506/train-all-newlabel-Multiple3.pb";
//	static String input2 = "/projects/pardosa/data16/raphaelh/riedel0506/test-all-newlabel-Multiple3.pb";
	
//	static String input1 = "/projects/pardosa/data16/raphaelh/riedel0506/train-all-newlabel-MultipleTransitive.pb";
//	static String input2 = "/projects/pardosa/data16/raphaelh/riedel0506/test-all-newlabel-MultipleTransitive.pb";
	
	
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/exp1/subset04-1-10.pb";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/exp1/subset04-2.pb";

//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset87-06.1.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";
	
	
//	static String exp = "/test-neg10-min1-06";
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset06.10.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";

	
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset05-06.10.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";

//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset87-06.10.pb";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb";
	
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset87-06.1.pb.gz";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset07.100.pb.gz";
	
	public static void main(String[] args) throws IOException {
		
		String mappingFile = dir + "/mapping";
		String modelFile = dir + "/model";
		
		{
			String output1 = dir + "/train";
			ConvertProtobufToMILDocument.convert(input1, output1, mappingFile, true, true);
		}
		
		{
			String output2 = dir + "/test";
			//ConvertProtobufToMILDocument.convert(input2, output2, mappingFile, true, true);
			ConvertProtobufToMILDocument.convert(input2, output2, mappingFile, false, false);
		}
		
		{
			MILModel m = new MILModel();
			Mappings mappings = new Mappings();
			mappings.read(mappingFile);
			m.numRelations = mappings.numRelations();
			m.numFeaturesPerRelation = new int[m.numRelations];
			for (int i=0; i < m.numRelations; i++)
				m.numFeaturesPerRelation[i] = mappings.numFeatures();
			m.write(modelFile);
		}
	}

}
