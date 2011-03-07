package multir.learning.algorithm;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;
import multir.tmp.TopFalse;
import multir.util.DenseVector;
import multir.util.SparseBinaryVector;

public class CollinsTraining {

	public int maxIterations = 30; //30;
	public float avgIterationsProportion = .8f; //.8f;
	public boolean computeAvgParameters = true;
		
	private Scorer scorer;
	private Random random;
	private MILModel model;
	
	public CollinsTraining(MILModel model, Random random) {
		scorer = new Scorer();
		this.model = model;
		this.random = random;
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
				avgParamsLastUpdatesIter.model = 
					avgParamsLastUpdates.model = model;
				avgParamsLastUpdatesIter.init();
				avgParamsLastUpdates.init();
			}
		}
		
		iterParameters = new CRFParameters();
		iterParameters.model = model;
		iterParameters.init();

		// averaging is only done over the last couple of iterations
		int avgIterations = (int)(maxIterations*avgIterationsProportion);
		
		//Evaluator evaluator = null;
		//if (Level.FINE.equals(Log.getLevel()))
		//	evaluator = new Evaluator(Evaluator.Level.COARSE, avgParameters.model.numStates);
		
		try {
			TopFalse topFalse = new TopFalse("/projects/pardosa/s5/clzhang/ontologylink/tmp1/topFalse");
			
			for (int i = 0; i < maxIterations; i++) {
				float delta = 1;
				boolean useIterAverage = computeAvgParameters && (i >= maxIterations - avgIterations);
	
				
				topFalse.add(i, scorer, iterParameters, model, trainingData);
				
				trainingIteration(trainingData, delta, useIterAverage);
				
				// DON'T FORGET TO CHANGE BACK TO ABOVE
				
				//trainingIteration2(trainingData, delta, useIterAverage);
				
			}
			topFalse.close();
		} catch (IOException e) {e.printStackTrace();}
		
		// required for dense parameters
		if (computeAvgParameters) {
			finalizeRel();
		}
		//nonzero(iterParameters);
		return (computeAvgParameters)? avgParameters : iterParameters;
	}
	/*
	private void nonzero(CRFParameters p) {
		int count = 0;
		for (int i=0; i < p.pos2stateParameters.length;i++)
			for (int j=0; j < p.pos2stateParameters[i].length; j++)
				for (int k=0; k < p.pos2stateParameters[i][j].vals.length; k++)
					if (p.pos2stateParameters[i][j].vals[k] != 0) {
						if (p.pos2stateParameters[i][j].vals[k] < 0)
						System.out.println(p.pos2stateParameters[i][j].vals[k]);
						count++;
					}
		System.out.println(count);
	}*/
	
	int avgIteration = 0;
	
	public void trainingIteration(Dataset trainingData, 
			float delta, boolean useIterAverage) {
		System.out.println("iter");
		
		//topFalsePredictions(trainingData);
		
		trainingData.shuffle();

		MILDocument doc = new MILDocument();
		
		trainingData.reset();
		int j=0;
		while (trainingData.next(doc)) {
			//if (++j % 100 == 0) System.out.println(j);
			
			// compute most likely label under current parameters
			Parse predictedParse = FullInference.infer(doc, scorer, iterParameters);
			
			//System.out.println(doc.numMentions);
			//print(doc.Y);
			//print(predictedParse.Y);
			
			if (!YsAgree(predictedParse.Y, doc.Y)) {
				//System.out.println("  disagree");
				Parse trueParse = ConditionalInference.infer(doc, scorer, iterParameters);
				//System.out.println(trueParse.Z.length + " " + doc.numMentions);

				/*
				if (!YsAgree(doc.Y, YsFromZs(trueParse.Z))) {
					if (doc.Y.length <= doc.numMentions) {
						//print("Y", doc.Y);
						//print("Z", YsFromZs(trueParse.Z));
					}
				}*/
				
				update2(predictedParse, trueParse, delta, useIterAverage);
//				update5(predictedParse, trueParse, delta, useIterAverage);

			}
		}
	}
	
	public void topFalsePredictions(Dataset trainingData) {
		
		class PQE {
			double score;
			int mentionID;
			int predicted;
			String arg1;
			String arg2;
			PQE(double score, int mentionID, int predicted, String arg1, String arg2) {
				this.score = score;
				this.mentionID = mentionID;
				this.predicted = predicted;
				this.arg1 = arg1;
				this.arg2 = arg2;
			}
		}
		
		PriorityQueue<PQE> pq = new PriorityQueue<PQE>(10, new Comparator<PQE>() {
			public int compare(PQE e1, PQE e2) {
				if (e1.score > e2.score) return -1; else return 1;
			}
		});
		
		MILDocument doc = new MILDocument();
		
		scorer.setParameters(iterParameters);
		Viterbi v = new Viterbi(model, scorer);

		
		trainingData.reset();
		while (trainingData.next(doc)) {
			
			// identify mentions with high(est) score for which
			// we predict some relation, but ground truth says
			// no relation/different relation
			
			for (int m=0; m < doc.numMentions; m++) {

				Viterbi.Parse p = v.parse(doc, m);
				if (p.state == 0) continue;
				
				boolean isGroundTruth = false;
				for (int i=0; i < doc.Y.length; i++)
					if (doc.Y[i] == p.state) isGroundTruth = true;
				
				if (!isGroundTruth) {
					pq.add(new PQE(p.score, doc.mentionIDs[m], p.state, doc.arg1, doc.arg2));
				}
			}
		}
		
		// print top5
		for (int j=0; j < 5 && !pq.isEmpty(); j++) {
			PQE pqe = pq.poll();
			System.out.println(pqe.arg1 + " " + pqe.arg2 + " " + pqe.mentionID + " " + pqe.predicted);
		}
	}
	
	
	
	/*
	private static void print(String prefix, int[] y) {
		//System.out.println("print " + y.length);
		if (y.length ==0)
			;//System.out.println("NA");
		else {
			System.out.print(prefix + " ");
			for (int i=0; i < y.length; i++) {
				if (i > 0) System.out.print(" ");
				System.out.print(y[i]);
			}
			System.out.println();
		}
	}

	
	
	private static int[] YsFromZs(int[] z) {
		int[] nz = new int[z.length];
		System.arraycopy(z, 0, nz, 0, z.length);
		Arrays.sort(nz);
		int count = 0;
		for (int i=0; i < nz.length; i++) {
			if (i!=0 && nz[i]==nz[i-1]) continue;
			if (nz[i] == 0) continue;
			count++;
		}
		//System.out.println("count " + count);
		int[] y = new int[count];
		int pos = 0;
		for (int i=0; i < nz.length; i++) {
			if (i!=0 && nz[i]==nz[i-1]) continue;
			if (nz[i] == 0) continue;
			y[pos++] = nz[i];
		}
		//if (y.length > 0) System.out.println(y[0]);
		return y;
	}
	*/
	
	private boolean YsAgree(int[] y1, int[] y2) {
		if (y1.length != y2.length) return false;		
		for (int i=0; i < y1.length; i++)
			if (y1[i] != y2[i]) return false;
		return true;
	}
	
	// TODO: a bit dangereous, since scorer.setDocument is called only inside inference
	public void update2(Parse pred, Parse tru, float delta, boolean useIterAverage) {
		int numMentions = tru.Z.length;
		//float myDelta = delta/numMentions;
		MILDocument doc = tru.doc;
		
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
			SparseBinaryVector v1a = scorer.getMentionRelationFeatures(doc, m, truRel);
			updateRel(truRel, v1a, delta, useIterAverage);

			SparseBinaryVector v2a = scorer.getMentionRelationFeatures(doc, m, predRel);
			updateRel(predRel, v2a, -delta, useIterAverage);
		}
		
		//printParams(iterParameters, model);
		
		// debug
		if (useIterAverage) { avgIteration++; }
	}
	
	
	// do only one update per entity pair: sample random broken mention
	public void update5(Parse pred, Parse tru, float delta, boolean useIterAverage) {
		int numMentions = tru.Z.length;
		MILDocument doc = tru.doc;
		
		// if this is the first avgIteration, then we need to initialize
		// the lastUpdate vector
		if (useIterAverage && avgIteration == 0)
			avgParamsLastUpdates.sum(iterParameters, 1.0f);

		int numBroken = 0;
		for (int m = 0; m < numMentions; m++)
		if (tru.Z[m] != pred.Z[m]) numBroken++;
		
		if (numBroken != 0) { 
			int[] broken = new int[numBroken];
			int bp = 0;
			for (int m = 0; m < numMentions; m++)
				if (tru.Z[m] != pred.Z[m]) broken[bp++] = m;
			
			// sample random broken mention
			int rm = random.nextInt(numBroken);
			
			int truRel = tru.Z[rm];
			int predRel = pred.Z[rm];
			
			// do update
			SparseBinaryVector v1a = scorer.getMentionRelationFeatures(doc, rm, truRel);
			updateRel(truRel, v1a, delta, useIterAverage);

			SparseBinaryVector v2a = scorer.getMentionRelationFeatures(doc, rm, predRel);
			updateRel(predRel, v2a, -delta, useIterAverage);
		}
		
		//printParams(iterParameters, model);
		
		// debug
		if (useIterAverage) {
			avgIteration++;
		}
	}
	
	
	public void trainingIteration2(Dataset trainingData, 
			float delta, boolean useIterAverage) {
		
		trainingData.shuffle();

		MILDocument doc = new MILDocument();
		while (trainingData.next(doc)) {

			// compute most likely label under current parameters
			Parse predictedParse = FullInference.infer(doc, scorer, iterParameters); 

			if (!YsAgree(predictedParse.Y, doc.Y)) {
				//System.out.println("  disagree");
				Parse trueParse = ConditionalInference.infer(doc, scorer, iterParameters);
				//System.out.println(trueParse.Z.length + " " + doc.numMentions);
				
				for (int m = 0; m < doc.numMentions; m++) {
	
					// if this is the first avgIteration, then we need to initialize
					// the lastUpdate vector
					if (useIterAverage && avgIteration == 0)
						avgParamsLastUpdates.sum(iterParameters, 1.0f);
	
					if (predictedParse.Z[m] != trueParse.Z[m]) {
						int truRel = trueParse.Z[m];
						int predRel = predictedParse.Z[m];
						
						// update
						SparseBinaryVector v1a = scorer.getMentionRelationFeatures(doc, m, truRel);
						updateRel(truRel, v1a, delta, useIterAverage);
	
						SparseBinaryVector v2a = scorer.getMentionRelationFeatures(doc, m, predRel);
						updateRel(predRel, v2a, -delta, useIterAverage);
						
						predictedParse = FullInference.infer(doc, scorer, iterParameters); 
						trueParse = ConditionalInference.infer(doc, scorer, iterParameters);					
					}
						
					if (useIterAverage) {
						avgIteration++;
					}
				}
			}
		}
	}

	
	/*
	public void update4(Parse pred, Parse tru, float delta, boolean useIterAverage) {
		int numMentions = tru.Z.length;
		float myDelta = delta/numMentions;
		
		// if this is the first avgIteration, then we need to initialize
		// the lastUpdate vector
		if (useIterAverage && !avgParameters.sparse && avgIteration == 0)
			avgParamsLastUpdates.sum(iterParameters, 1.0f);

		// ground truth has single prediction
		int truth = 0;
		for (int i=0; i < model.numRelations; i++)
			if (tru.Y[i]) { truth = i; break; }
		
		// determine scores for global predictions
		// one feature vector for each relation
		SparseVector[] fts = new SparseVector[model.numRelations];
		for (int i=1; i < model.numRelations; i++) {
			
			if (pred.Y[i] && !tru.Y[i]) {
			
			// normal updates on everything
			if (truRel != predRel) {
				SparseVector v1a = scorer.getRelFeatures(m, truRel);
				updateRel(truRel, v1a, delta, useIterAverage);
	
				SparseVector v2a = scorer.getRelFeatures(m, predRel);
				updateRel(predRel, v2a, -delta, useIterAverage);
			}
			
		}
			if (pred.Y[i] && !tru.Y[i]) {
				// create OR-vector
				SparseVector sv = new SparseVector();
				for (int m = 0; m < pred.Z.length; m++)
					if (pred.Z[m] == i)
						sv.addSparse(scorer.getRelFeatures(m, i), 1);
				// normalize feature vector
				for (int j=0; j < sv.num; j++)
					sv.vals[j] = Math.signum(sv.vals[j]);
				
				updateRel(truRel, v1a, delta, useIterAverage);
				updateRel(predRel, v2a, -delta, useIterAverage);

				
			} else if (!pred.Y[i] && tru.Y[i]) {
				
				
			}

	}
	
	
	
	public void update3(Parse pred, Parse tru, float delta, boolean useIterAverage) {
		int numMentions = tru.Z.length;
		float myDelta = delta/numMentions;
		
		// if this is the first avgIteration, then we need to initialize
		// the lastUpdate vector
		if (useIterAverage && !avgParameters.sparse && avgIteration == 0)
			avgParamsLastUpdates.sum(iterParameters, 1.0f);

		// ground truth has single prediction
		int truth = 0;
		for (int i=0; i < model.numRelations; i++)
			if (tru.Y[i]) { truth = i; break; }
		
		// get the highest scoring relation
		
		// we assume a single
		
		
		
		}
		
		for (int m = 0; m < numMentions; m++) {
	}
	*/
	
	
	
	
	
	private void printParams(CRFParameters p, MILModel m) {
		System.out.println("-----------");
		/*
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
	}
	
	/*
	public void update(Parse pred, Parse tru,
			float delta, boolean useIterAverage) {
		int numMentions = tru.Z.length;
		int numRelations = model.numRelations;
		//int[] predictedLabel = predictedParse.labels;
		//int[] trueLabel = trueParse.labels;
		
		// if this is the first avgIteration, then we need to initialize
		// the lastUpdate vector
		if (useIterAverage && !avgParameters.sparse && avgIteration == 0)
			avgParamsLastUpdates.sum(iterParameters, 1.0f);

		// iterate over mentions
		for (int m = 0; m < numMentions; m++) {
			int trueRel = tru.Z[m] % numRelations;
			int trueArg1 = model.relationSignatures[trueRel][0];
			int trueArg2 = model.relationSignatures[trueRel][1];
			if (tru.Z[m] >= numRelations) { int tmp = trueArg1; trueArg1 = trueArg2; trueArg2 = tmp; }
			int predRel = pred.Z[m] % numRelations;
			int predArg1 = model.relationSignatures[predRel][0];
			int predArg2 = model.relationSignatures[trueRel][1];
			if (pred.Z[m] >= numRelations) { int tmp = predArg1; predArg1 = predArg2; predArg2 = tmp; }
			
			if (trueRel == model.NO_RELATION_STATE && predRel == model.NO_RELATION_STATE) {
				// done
			}
			else if (trueRel == model.NO_RELATION_STATE && predRel != model.NO_RELATION_STATE) {
				// update only feature for relation node
				// (TODO: what about selectional preferences?)
				SparseVector v1a = scorer.getRelFeatures(m, trueRel);
				updateRel(trueRel, v1a, delta, useIterAverage);
	
				SparseVector v2a = scorer.getRelFeatures(m, predRel);
				updateRel(predRel, v2a, -delta, useIterAverage);
			}
			else {
				// normal updates on everything
				if (trueRel != predRel) {
					SparseVector v1 = scorer.getRelFeatures(m, trueRel);
					updateRel(trueRel, v1, delta, useIterAverage);
					SparseVector v2 = scorer.getRelFeatures(m, predRel);
					updateRel(predRel, v2, -delta, useIterAverage);					
				}
				if (trueRel != predRel || trueArg1 != predArg1) {
					SparseVector v1 = scorer.getArgFeatures(m, 0, trueArg1);
					updateArg(trueArg1, v1, delta, useIterAverage);
					if (model.learnTransitionFeatures) {
						v1 = scorer.getPos2rel2argFeatures(0, trueRel, trueArg1);
						updateSP(0, trueRel, v1, delta, useIterAverage);
					}
					
					SparseVector v2 = scorer.getArgFeatures(m, 0, predArg1);
					updateArg(predArg1, v2, -delta, useIterAverage);					
					if (model.learnTransitionFeatures) {
						v2 = scorer.getPos2rel2argFeatures(0, predRel, predArg1);
						updateSP(0, predRel, v2, -delta, useIterAverage);
					}
				}
				if (trueRel != predRel || trueArg2 != predArg2) {
					SparseVector v1 = scorer.getArgFeatures(m, 1, trueArg2);
					updateArg(trueArg2, v1, delta, useIterAverage);
					if (model.learnTransitionFeatures) {
						v1 = scorer.getPos2rel2argFeatures(1, trueRel, trueArg2);
						updateSP(1, trueRel, v1, delta, useIterAverage);
					}
					
					SparseVector v2 = scorer.getArgFeatures(m, 1, predArg2);
					updateArg(predArg2, v2, -delta, useIterAverage);					
					if (model.learnTransitionFeatures) {
						v2 = scorer.getPos2rel2argFeatures(1, predRel, predArg2);
						updateSP(1, predRel, v2, -delta, useIterAverage);
					}
				}
			}
		}
		
		// debug
		if (useIterAverage) {
			avgIteration++;
		}
		
		// update averages for sparse avg
		if (useIterAverage && avgParameters.sparse) {
			avgParameters.sum(iterParameters, 1.0f);
		}
	}
	*/
	private void updateRel(int toState, SparseBinaryVector features, float delta, boolean useIterAverage) {
		iterParameters.relParameters[toState].addSparse(features, delta);
		
		if (useIterAverage) {
			DenseVector lastUpdatesIter = (DenseVector)avgParamsLastUpdatesIter.relParameters[toState];
			DenseVector lastUpdates = (DenseVector)avgParamsLastUpdates.relParameters[toState];
			DenseVector avg = (DenseVector)avgParameters.relParameters[toState];
			DenseVector iter = (DenseVector)iterParameters.relParameters[toState];
			for (int j=0; j < features.num; j++) {
				int id = features.ids[j];
				if (lastUpdates.vals[id] != 0)
					avg.vals[id] += (avgIteration - lastUpdatesIter.vals[id])*lastUpdates.vals[id];
				
				lastUpdatesIter.vals[id] = avgIteration;
				lastUpdates.vals[id] = iter.vals[id];//features.vals[j] * delta;
			}
		}
	}
	
	private void finalizeRel() {
		for (int s = 0; s < model.numRelations; s++) {
			DenseVector lastUpdatesIter = (DenseVector)avgParamsLastUpdatesIter.relParameters[s];
			DenseVector lastUpdates = (DenseVector)avgParamsLastUpdates.relParameters[s];
			DenseVector avg = (DenseVector)avgParameters.relParameters[s];
			for (int id=0; id < avg.vals.length; id++) {
				if (lastUpdates.vals[id] != 0) {
					avg.vals[id] += (avgIteration - lastUpdatesIter.vals[id])*lastUpdates.vals[id];
					lastUpdatesIter.vals[id] = avgIteration;
				}
			}
		}
	}
}
