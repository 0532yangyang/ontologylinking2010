package percept.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javatools.administrative.D;

import percept.learning.algorithm.CRFParameters;
import percept.learning.algorithm.CollinsTraining;
import percept.learning.algorithm.Model;
import percept.learning.data.Dataset;
import percept.learning.data.MemoryDataset;
import percept.preprocess.ConvertToExamples;
import percept.preprocess.Mappings;

public class LogLinear {
	// store everything
	String dir = "";
	String trainfile = "";

	Model model;
	CRFParameters params;
	CollinsTraining ct;
	Random random;
	Mappings mappings;

	/** temp files */
	String mappingfile;
	String binaryTrain;
	String mappingFile;
	String modelFile;

	public LogLinear(String dir, String trainfile) {
		this.dir = dir;
		this.trainfile = trainfile;
		{
			String[] a = trainfile.split("/");
			String filehand = a[a.length - 1];
			binaryTrain = dir + "/" + filehand + ".tempbinarytrain";
			mappingFile = dir + "/" + filehand + ".tempmapping";
			modelFile = dir + "/" + filehand + ".tempmodel";

			try {
				ConvertToExamples.convert(trainfile, binaryTrain, mappingFile, true, true);
				model = new Model();
				mappings = new Mappings();
				mappings.read(mappingFile);
				model.numStates = mappings.numStates();
				model.numFeaturesPerState = new int[model.numStates];
				for (int i = 0; i < model.numStates; i++)
					model.numFeaturesPerState[i] = mappings.numFeatures();
				model.write(modelFile);

				Random random = new Random((new Date()).getTime());

				Model model = new Model();
				model.read(modelFile);

				ct = new CollinsTraining(model, random);

				Dataset train = new MemoryDataset(random, binaryTrain);

				D.p("starting training", trainfile);
				params = ct.train(train);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Fail to train the log linear model!!!");
				e.printStackTrace();
			}
		}
	}

	public LogLinear(String modelfile) {

	}

	public List<String> predict(String testfile) {
		List<String> predicts = new ArrayList<String>();
		try {
			String[] temp = testfile.split("/");
			String binaryTest = dir + "/" + temp[temp.length - 1] + ".binarytest";
			ConvertToExamples.convert(testfile, binaryTest, mappingFile, false, false);
			Dataset test = new MemoryDataset(random, binaryTest);
			List<Integer> intres = ct.testUnknown(test, params);
			for (int i = 0; i < intres.size(); i++) {
				predicts.add(mappings.getStateName(intres.get(i)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return predicts;
	}

	public static void main(String[] args) {
		String dir = "/scratch/Downloads/example1";
		LogLinear ll = new LogLinear(dir, dir + "/train.dat");
		List<String> predicts = ll.predict(dir + "/test.dat");
		D.p(predicts);
	}
}
