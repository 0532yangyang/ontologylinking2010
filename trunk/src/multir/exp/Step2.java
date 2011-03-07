package multir.exp;

import java.io.IOException;

import multir.learning.algorithm.MILModel;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.Mappings;

public class Step2 {

	//static String dir = "/projects/pardosa/data14/raphaelh/t";

	/*
	static String input1 = "/projects/pardosa/data14/raphaelh/riedel0506/" +
		"train-all-newlabel-Multiple3.pb";
	static String input2 = "/projects/pardosa/data14/raphaelh/riedel0506/" + 
		"test-all-newlabel-Multiple3.pb";
	*/
	
//	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/train-Multiple.pb";
//	static String input2 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";

//	static String input1 = "/projects/pardosa/data14/raphaelh/t/exp1/subset04-1-10.pb";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/exp1/subset04-2.pb";


//	static String input1 = "/projects/pardosa/data14/raphaelh/t/exp1/subset87-05.10.pb";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/exp1/subset07.pb";
	
	
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp1";
	static String input1 = dir + "/data/subsetTrain.pb.gz";
	static String input2 = dir + "/data/subsetTest.pb.gz";	

	
	public static void main(String[] args) throws IOException {

		String mappingFile = dir + "/mapping";
		String modelFile = dir + "/model";
		{
			String output1 = dir + "/train";
			ConvertProtobufToMILDocument.convert(input1, output1, mappingFile, true, true);
		}
		
		{
			String output2 = dir + "/test";
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
