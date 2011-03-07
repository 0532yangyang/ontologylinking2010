package multir.exp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import multir.eval.PrecisionRecallCurve;
import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.CollinsTraining;
import multir.learning.algorithm.MILModel;
import multir.learning.data.Dataset;
import multir.learning.data.MemoryDataset;

public class Step3 {

	
	// IT'S 16!!!!
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp1";
	//static String exp = "/expSpan1.10.5";
	static String exp = "";
	
	public static void main(String[] args) throws IOException {

		Random random = new Random(7);
		
		MILModel model = new MILModel();
		model.read(dir + exp + "/model");
		
		CollinsTraining ct = new CollinsTraining(model, random);
		
		Dataset train = new MemoryDataset(random, dir + exp + "/train");
		Dataset test = new MemoryDataset(random, dir + exp + "/test");

//		Dataset train = new ExternalDataset(random, dir + exp + "/train");
//		Dataset test = new ExternalDataset(random, dir + exp + "/test");

		System.out.println("starting training");
		CRFParameters params = ct.train(train);
		
		PrintStream ps = new PrintStream(dir + exp + "/results");		
		PrecisionRecallCurve.eval(test, params, ps);
		
	}
}
