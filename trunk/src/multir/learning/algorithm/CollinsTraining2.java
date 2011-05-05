package multir.learning.algorithm;

import java.util.Random;

import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;
import multir.util.DenseVector;
import multir.util.SparseBinaryVector;

public class CollinsTraining2 {

	//public int maxIterations = 50; //30;
	public int maxIterations = 50; //30;
	public float avgIterationsProportion = 1f; //.8f;
	public boolean computeAvgParameters = true;

	private Scorer scorer;
	private MILModel model;

	public CollinsTraining2(MILModel model) {
		scorer = new Scorer();
		this.model = model;
	}

	// the following two are actually not storing weights:
	// the first is storing the iteration in which the average weights were
	// last updated, and the other is storing the next update value
	private CRFParameters avgParamsLastUpdatesIter;
	private CRFParameters avgParamsLastUpdates;

	private CRFParameters avgParameters;
	private CRFParameters iterParameters;
	private Random random = new Random(1);

	public CRFParameters train(Dataset trainingData) {

		if (computeAvgParameters) {
			avgParameters = new CRFParameters();
			avgParameters.model = model;
			avgParameters.init();

			avgParamsLastUpdatesIter = new CRFParameters();
			avgParamsLastUpdates = new CRFParameters();
			avgParamsLastUpdatesIter.model = avgParamsLastUpdates.model = model;
			avgParamsLastUpdatesIter.init();
			avgParamsLastUpdates.init();
		}

		iterParameters = new CRFParameters();
		iterParameters.model = model;
		iterParameters.init();

		// averaging is only done over the last couple of iterations
		int avgIterations = (int) (maxIterations * avgIterationsProportion);

		for (int i = 0; i < maxIterations; i++) {
			double delta = 1;
			boolean useIterAverage = computeAvgParameters && (i >= maxIterations - avgIterations);

			trainingIteration(trainingData, delta, useIterAverage);
		}

		if (computeAvgParameters) {
			finalizeRel(); //avgIterations*trainingData.numDocs());
		}
		//nonzero(iterParameters);
		return (computeAvgParameters) ? avgParameters : iterParameters;
	}

	int avgIteration = 0;

	public void trainingIteration(Dataset trainingData, double delta, boolean useIterAverage) {

		System.out.println("iter");

		MILDocument doc = new MILDocument();

		trainingData.shuffle(random);

		trainingData.reset();

		while (trainingData.next(doc)) {

			// compute most likely label under current parameters
			Parse predictedParse = FullInference.infer(doc, scorer, iterParameters);
			Parse trueParse = ConditionalInference.infer(doc, scorer, iterParameters);

			update2(predictedParse, trueParse, delta, useIterAverage);
		}

	}

	// a bit dangerous, since scorer.setDocument is called only inside inference
	public void update2(Parse pred, Parse tru, double delta, boolean useIterAverage) {
		int numMentions = tru.Z.length;
		//float myDelta = delta/numMentions;

		// if this is the first avgIteration, then we need to initialize
		// the lastUpdate vector
		if (useIterAverage && avgIteration == 0)
			avgParamsLastUpdates.sum(iterParameters, 1.0f);

		// iterate over mentions
		for (int m = 0; m < numMentions; m++) {
			// arg1 always refers to first argument in plate
			// get types for arg1/arg2 from relation signature
			int truRel = tru.Z[m];
			int predRel = pred.Z[m];

			// normal updates on everything
			if (truRel != predRel) {
				SparseBinaryVector v1a = scorer.getMentionRelationFeatures(tru.doc, m, truRel);
				updateRel(truRel, v1a, delta, useIterAverage);

				SparseBinaryVector v2a = scorer.getMentionRelationFeatures(tru.doc, m, predRel);
				updateRel(predRel, v2a, -delta, useIterAverage);
			}
		}

		// debug
		if (useIterAverage) {
			avgIteration++;
		}
	}

	/*
	private void printParams(CRFParameters p, Model m) {
		System.out.println("-----------");
		
		for (int i=0; i < model.numArgumentTypes(); i++) {
			System.out.println(model.argumentNode.stateId2name.get(i));
			DenseVector dv = (DenseVector)p.argParameters[i];
			for (int j=0; j < dv.vals.length; j++) {
				if (dv.vals[j] != 0)
					System.out.println("    " + model.argumentFactor.featureId2name.get(j) + "\t" + dv.vals[j]);
			}
		}
		/*
		for (int i=0; i < model.numRelations(); i++) {
			System.out.println(model.relationNode.stateId2name.get(i));
			System.out.println("ARG1");
			DenseVector dv1 = (DenseVector)p.pos2rel2argParameters[0][i];
			for (int j=0; j < dv1.vals.length; j++) {
				if (dv1.vals[j] != 0)
					System.out.println("    " + model.argumentNode.stateId2name.get(j) + "\t" + dv1.vals[j]);
			}
			System.out.println("ARG2");
			DenseVector dv = (DenseVector)p.pos2rel2argParameters[1][i];
			for (int j=0; j < dv.vals.length; j++) {
				if (dv.vals[j] != 0)
					System.out.println("    " + model.argumentNode.stateId2name.get(j) + "\t" + dv.vals[j]);
			}
		}
		*/
	//}

	private void updateRel(int toState, SparseBinaryVector features, double delta, boolean useIterAverage) {
		iterParameters.relParameters[toState].addSparse(features, delta);

		if (useIterAverage) {
			DenseVector lastUpdatesIter = (DenseVector) avgParamsLastUpdatesIter.relParameters[toState];
			DenseVector lastUpdates = (DenseVector) avgParamsLastUpdates.relParameters[toState];
			DenseVector avg = (DenseVector) avgParameters.relParameters[toState];
			DenseVector iter = (DenseVector) iterParameters.relParameters[toState];
			for (int j = 0; j < features.num; j++) {
				int id = features.ids[j];
				if (lastUpdates.vals[id] != 0)
					avg.vals[id] += (avgIteration - lastUpdatesIter.vals[id]) * lastUpdates.vals[id];

				lastUpdatesIter.vals[id] = avgIteration;
				lastUpdates.vals[id] = iter.vals[id];//features.vals[j] * delta;
			}
		}
	}

	private void finalizeRel() {
		for (int s = 0; s < model.numRelations; s++) {
			DenseVector lastUpdatesIter = (DenseVector) avgParamsLastUpdatesIter.relParameters[s];
			DenseVector lastUpdates = (DenseVector) avgParamsLastUpdates.relParameters[s];
			DenseVector avg = (DenseVector) avgParameters.relParameters[s];
			for (int id = 0; id < avg.vals.length; id++) {
				if (lastUpdates.vals[id] != 0) {
					avg.vals[id] += (avgIteration - lastUpdatesIter.vals[id]) * lastUpdates.vals[id];
					lastUpdatesIter.vals[id] = avgIteration;
				}
			}
		}
	}
}
