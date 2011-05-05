package multir.exp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import multir.eval.PrecisionRecallCurve2;
import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.CollinsTraining2;
import multir.learning.algorithm.MILModel;
import multir.learning.data.Dataset;
import multir.learning.data.MemoryDataset;

public class Step3 {

	
	// IT'S 16!!!!
	//static String dir = "/projects/pardosa/data14/raphaelh/t";
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/ienyt";
	//static String exp = "/expSpan1.10.5";
	//static String exp = "/expTrain1";
	//static String exp = "";
	static String exp = "/newexp.min5";
	//static String exp = "/newexp-neg5-min5";
	//static String exp = "/newexp-neg5-min5";
	//static String exp = "/newexp-neg10-min5-0506";
	//static String exp = "/test-neg10-min5-8706";
	//static String exp = "/test-neg1-min1-8706";
	//static String exp = "/test-neg10-min1-06";
	//static String exp = "/test-neg10-min2-0506";
	
	public static void run(String exp) throws Exception {
		Step3.exp = exp;
		main(new String[0]);
	}
	
	public static void main(String[] args) throws IOException {

		//Random random = new Random(7);
		Random random = new Random(1);
		
		MILModel model = new MILModel();
		model.read(dir + exp + "/model");
		
		//CollinsTraining ct = new CollinsTraining(model, random);
		CollinsTraining2 ct = new CollinsTraining2(model);
		
		Dataset train = new MemoryDataset(random, dir + exp + "/train");
		Dataset test = new MemoryDataset(random, dir + exp + "/test");

//		Dataset train = new ExternalDataset(random, dir + exp + "/train");
//		Dataset test = new ExternalDataset(random, dir + exp + "/test");

		System.out.println("starting training");
		CRFParameters params = ct.train(train);
		
		PrintStream psc = new PrintStream(dir + exp + "/curve2");		
		PrecisionRecallCurve2.eval(test, params, psc);
		psc.close();
		
		//PrintStream ps = new PrintStream(dir + exp + "/results");
		//ResultWriter.eval(dir + exp + "/mapping", test, params, ps);
		//ps.close();
	}
}
