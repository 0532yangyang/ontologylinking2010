package percept.learning.algorithm;

import java.util.Random;

import percept.learning.algorithm.Viterbi.Parse;
import percept.learning.data.Dataset;
import percept.learning.data.Example;
import percept.util.DenseVector;
import percept.util.SparseBinaryVector;

public class CollinsTraining {

	public int maxIterations = 30; // 25;
	public float avgIterationsProportion = 1f;
	public boolean computeAvgParameters = true;

	private Scorer scorer;
	private Model model;

	public CollinsTraining(Model model, Random random) {
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

	public CRFParameters train(Dataset trainingData) {

		if (computeAvgParameters) {
			avgParameters = new CRFParameters();
			avgParameters.model = model;
			avgParameters.init();

			// required for dense representation of avg parameters
			{
				avgParamsLastUpdatesIter = new CRFParameters();
				avgParamsLastUpdates = new CRFParameters();
				avgParamsLastUpdatesIter.model = avgParamsLastUpdates.model = model;
				avgParamsLastUpdatesIter.init();
				avgParamsLastUpdates.init();
			}
		}

		iterParameters = new CRFParameters();
		iterParameters.model = model;
		iterParameters.init();
		scorer.setParameters(iterParameters);
		// averaging is only done over the last couple of iterations
		int avgIterations = (int) (maxIterations * avgIterationsProportion);

		for (int i = 0; i < maxIterations; i++) {
			float delta = 1;
			boolean useIterAverage = computeAvgParameters && (i >= maxIterations - avgIterations);

			trainingData.shuffle();

			trainingIteration(trainingData, delta, useIterAverage);
		}

		// required for dense parameters
		if (computeAvgParameters) {
			finalizeRel();
		}
		// nonzero(iterParameters);
		return (computeAvgParameters) ? avgParameters : iterParameters;
	}

	int avgIteration = 0;

	public void trainingIteration(Dataset trainingData, float delta, boolean useIterAverage) {
		System.out.println("iter");

		Example doc = new Example();

		trainingData.reset();
		int j = 0;
		while (trainingData.next(doc)) {
			// if (++j % 100 == 0) System.out.println(j);

			// compute most likely label under current parameters

			Viterbi v = new Viterbi(model, scorer);
			Parse predictedParse = v.parse(doc);

			if (predictedParse.state != doc.Y) {
				update(doc, predictedParse.state, doc.Y, delta, useIterAverage);
			}
			j++;
		}
	}

	// TODO: a bit dangereous, since scorer.setDocument is called only inside
	// inference
	public void update(Example doc, int predY, int truY, float delta, boolean useIterAverage) {

		// if this is the first avgIteration, then we need to initialize
		// the lastUpdate vector
		if (useIterAverage && avgIteration == 0)
			avgParamsLastUpdates.sum(iterParameters, 1.0f);

		// normal updates on everything
		SparseBinaryVector v1a = scorer.getFeatures(doc);
		updateRel(truY, v1a, delta, useIterAverage);

		SparseBinaryVector v2a = scorer.getFeatures(doc);
		updateRel(predY, v2a, -delta, useIterAverage);

		// debug
		if (useIterAverage) {
			avgIteration++;
		}
	}

	private void updateRel(int toState, SparseBinaryVector features, float delta, boolean useIterAverage) {
		iterParameters.stateParameters[toState].addSparse(features, delta);

		if (useIterAverage) {
			DenseVector lastUpdatesIter = (DenseVector) avgParamsLastUpdatesIter.stateParameters[toState];
			DenseVector lastUpdates = (DenseVector) avgParamsLastUpdates.stateParameters[toState];
			DenseVector avg = (DenseVector) avgParameters.stateParameters[toState];
			DenseVector iter = (DenseVector) iterParameters.stateParameters[toState];
			for (int j = 0; j < features.num; j++) {
				int id = features.ids[j];
				if (lastUpdates.vals[id] != 0)
					avg.vals[id] += (avgIteration - lastUpdatesIter.vals[id]) * lastUpdates.vals[id];

				lastUpdatesIter.vals[id] = avgIteration;
				lastUpdates.vals[id] = iter.vals[id];// features.vals[j] *
														// delta;
			}
		}
	}

	public void test(Dataset testData, CRFParameters params) {
		Example doc = new Example();
		Scorer s = new Scorer();
		s.setParameters(params);
		int correct = 0,wrong = 0;
		while (testData.next(doc)) {
			Viterbi v = new Viterbi(model, s);
			Parse predictParse = v.parse(doc);
			if(predictParse.state == doc.Y){
				correct++;
			}else{
				wrong++;
			}
		}
		System.out.println(correct+"\t"+wrong);
	}

	private void finalizeRel() {
		for (int s = 0; s < model.numStates; s++) {
			DenseVector lastUpdatesIter = (DenseVector) avgParamsLastUpdatesIter.stateParameters[s];
			DenseVector lastUpdates = (DenseVector) avgParamsLastUpdates.stateParameters[s];
			DenseVector avg = (DenseVector) avgParameters.stateParameters[s];
			for (int id = 0; id < avg.vals.length; id++) {
				if (lastUpdates.vals[id] != 0) {
					avg.vals[id] += (avgIteration - lastUpdatesIter.vals[id]) * lastUpdates.vals[id];
					lastUpdatesIter.vals[id] = avgIteration;
				}
			}
		}
	}
}