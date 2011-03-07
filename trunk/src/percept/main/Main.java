package percept.main;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import percept.learning.algorithm.CRFParameters;
import percept.learning.algorithm.CollinsTraining;
import percept.learning.algorithm.Model;
import percept.learning.data.Dataset;
import percept.learning.data.MemoryDataset;
import percept.preprocess.ConvertToExamples;
import percept.preprocess.Mappings;

public class Main {

	static String dir = "/scratch/Downloads/example1";
	static String exp = dir + "";
	static String input1 = dir + "/train.dat"; // training set
	static String input2 = dir + "/test.dat"; // test set
	static String binaryTrain = dir+"/train";
	static String binaryTest = dir+"/test";
	static String modelFile = dir + "/model";
	public static void main(String[] args) throws IOException {

		// preprocessing
		{
			String mappingFile = dir + "/mapping";
			
			{
				//String output1 = dir + "/train";
				ConvertToExamples.convert(input1, binaryTrain, mappingFile, true, true);
			}

			{
				//String output2 = dir + "/test";
				ConvertToExamples.convert(input2, binaryTest, mappingFile, false, false);
			}

			{
				Model m = new Model();
				Mappings mappings = new Mappings();
				mappings.read(mappingFile);
				m.numStates = mappings.numStates();
				m.numFeaturesPerState = new int[m.numStates];
				for (int i = 0; i < m.numStates; i++)
					m.numFeaturesPerState[i] = mappings.numFeatures();
				m.write(modelFile);
			}
		}

		// learning
		CRFParameters params = null;
		{

			Random random = new Random(7);

			Model model = new Model();
			model.read(modelFile);

			CollinsTraining ct = new CollinsTraining(model, random);

			Dataset train = new MemoryDataset(random, binaryTrain);
			Dataset test = new MemoryDataset(random, binaryTest);

			System.out.println("starting training");
			params = ct.train(train);
			ct.test(test, params);
		}

		// testing
		{
			
			// PrintStream ps = new PrintStream(dir + exp + "/results");
			// PrecisionRecallCurve.eval(test, params, ps);
		}

	}

}
