package multir.exp;

import java.io.File;
import java.io.IOException;

import multir.learning.algorithm.MILModel;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.Mappings;
import multir.util.delimited.DelimitedReader;

public class Step2b {

	static String dir = "/projects/pardosa/data14/raphaelh/t";

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
	
//	static String input0 = "/projects/pardosa/data14/raphaelh/t/fts.unique2";
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/exp1/subset87-05.10.pb";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/exp1/subset06.pb";

//	static String input0 = "/projects/pardosa/data14/raphaelh/t/fts87-05.25.unique2";
//	static String input1 = "/projects/pardosa/data14/raphaelh/t/exp1/subset87-05.25.pb";
//	static String input2 = "/projects/pardosa/data14/raphaelh/t/exp1/subset06.pb";

/*
	static String exp = "/exp05-2.10.unique5";
	static String input0 = "/projects/pardosa/data14/raphaelh/t/fts/fts05-2.10.unique5";
	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset05-2.10.pb";
	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset06.pb";
*/	

	static String exp = "/expTrain1";
	static String input0 = "/projects/pardosa/data14/raphaelh/t/fts/fts05-2.100.gz.counts"; //unique5";
	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subset05-2.100.pb.gz";
	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset06.100.pb.gz";

/*	
	static String exp = "/expSpan1.10.5";
	static String input0 = "/projects/pardosa/data14/raphaelh/t/fts/ftsSpan1.10.gz.counts"; //unique5";
	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subsetSpan1.10.pb.gz";
	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset06.100.pb.gz";
*/
	
	/*
	static String exp = "/expTrain3";
	static String input0 = "/projects/pardosa/data14/raphaelh/t/fts/ftsSpan1.100.gz.counts"; //unique5";
	static String input1 = "/projects/pardosa/data14/raphaelh/t/data/subsetSpan1.100.pb.gz";
	static String input2 = "/projects/pardosa/data14/raphaelh/t/data/subset06.100.pb.gz";
	*/
	
	public static void main(String[] args) throws IOException {
		new File(dir + exp).mkdir();
		
		String mappingFile = dir + exp + "/mapping";
		String modelFile = dir + exp + "/model";
		
		{
			Mappings m = new Mappings();
			DelimitedReader r = new DelimitedReader(input0);
			String[] t = null;
			while ((t = r.read())!= null) {
				if (Integer.parseInt(t[1]) >= 10)
				m.getFeatureID(t[0], true);
			}
			r.close();
			m.write(mappingFile);
		}
		
		{
			String output1 = dir + exp + "/train";
			ConvertProtobufToMILDocument.convert(input1, output1, mappingFile, false, true); //was true
		}
		
		{
			String output2 = dir + exp + "/test";
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
