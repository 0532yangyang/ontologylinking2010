package freebase.jointmatch4;

import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedWriter;
import multir.eval.PrecisionRecallCurve2;
import multir.eval.Prediction;
import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.CollinsTraining2;
import multir.learning.algorithm.MILModel;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;
import multir.learning.data.MemoryDataset;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.Mappings;

public class SX_RphTrainTest {
	static void rphTrainTest(String dir) throws Exception {
		String mappingFile = dir + "/mapping";
		String modelFile = dir + "/model";

		String trainFile = dir + "/train";
		String testFile = dir + "/test";
		String curveFile = dir + "/curve";
		String resultFile = dir + "/result";
		MILModel model;
		Mappings mappings;
		CRFParameters params = null;
		Random random = new Random(1);

		{
			ConvertProtobufToMILDocument.convert(dir + "/trainpb", trainFile, mappingFile, true, true);

			model = new MILModel();
			mappings = new Mappings();
			mappings.read(mappingFile);
			model.numRelations = mappings.numRelations();
			model.numFeaturesPerRelation = new int[model.numRelations];
			for (int i = 0; i < model.numRelations; i++)
				model.numFeaturesPerRelation[i] = mappings.numFeatures();
			model.write(modelFile);
		}

		{
			CollinsTraining2 ct = new CollinsTraining2(model);
			Dataset train = new MemoryDataset(random, trainFile);
			//how many null?
			{
				int NAsize = 0;
				MILDocument mdoc = new MILDocument();
				while (train.next(mdoc)) {
					if (mdoc.Y.length == 0) {
						NAsize++;
					}
				}
				D.p("NAsize", NAsize);
			}
			System.out.println("starting training");
			params = ct.train(train);
		}
		{
			ConvertProtobufToMILDocument.convert(dir + "/testpb", testFile, mappingFile, false, false);
			DelimitedWriter dw = new DelimitedWriter(resultFile);
			String[] relationnames = new String[1000];
			for (Entry<String, Integer> e : mappings.getRel2RelID().entrySet()) {
				relationnames[e.getValue()] = e.getKey();
			}
			PrintStream psc = new PrintStream(curveFile);
			Dataset test = new MemoryDataset(random, testFile);
			List<Prediction> preds = PrecisionRecallCurve2.eval(test, params, psc);

			for (int i = 0; i < preds.size(); i++) {
				Prediction p0 = preds.get(i);
				//MILDocument doc = p0.doc;
				int predY = 0;
				if (p0.parse.Y.length > 0) {
					predY = p0.parse.Y[0];
				}
				dw.write(relationnames[predY], p0.doc.arg1, p0.doc.arg2);
			}
			psc.close();
			dw.close();
		}
	}
}
