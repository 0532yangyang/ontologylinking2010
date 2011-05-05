package percept.learning.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javatools.filehandlers.DelimitedWriter;

import percept.util.DenseVector;

public class CRFParameters {

	// each of our three nodes have their own features
	public DenseVector[] stateParameters;

	public Model model;

	private DenseVector sum(DenseVector v1, DenseVector v2, float factor) {
		if (v1 == null && v2 == null)
			return null;
		else if (v2 == null)
			return v1.copy();
		else if (v1 == null) {
			DenseVector v = v2.copy();
			v.scale(factor);
			return v;
		} else
			return v1.sum(v2, factor);
	}

	public void sum(CRFParameters p, float factor) {
		for (int i = 0; i < stateParameters.length; i++)
			stateParameters[i] = sum(stateParameters[i], p.stateParameters[i], factor);
	}

	public void init() {
		if (stateParameters == null) {
			stateParameters = new DenseVector[model.numStates];
			//System.out.println(model.numStates);
			//System.out.println("requesting " + (8*stateParameters.length*(long)model.numFeaturesPerState[0]));
			for (int j = 0; j < stateParameters.length; j++) {
				stateParameters[j] = new DenseVector(model.numFeatures(j));
				//new DenseVector(model.relationFactor.numFeatures);
			}
		}
	}

	public void reset() {
		for (int i = 0; i < stateParameters.length; i++)
			if (stateParameters[i] != null)
				stateParameters[i].reset();
	}

	public void serialize(OutputStream os) throws IOException {
		//model.serialize(os);
		//DataOutputStream dos = new DataOutputStream(os);
		DenseVector[] r = stateParameters;
		for (int i = 0; i < r.length; i++)
			r[i].serialize(os);
	}

	public void deserialize(InputStream is) throws IOException {
		init();
		//reset();
		DenseVector[] r = stateParameters;
		for (int i = 0; i < r.length; i++)
			r[i].deserialize(is);
	}


}
