package multir.learning.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import multir.util.DenseVector;

public class CRFParameters {

	// each of our three nodes have their own features
	public DenseVector[] relParameters;
	
	public MILModel model;

	private DenseVector sum(DenseVector v1, DenseVector v2, float factor) {
		if (v1 == null && v2 == null) return null;
		else if (v2 == null) return v1.copy();
		else if (v1 == null) {
			DenseVector v = v2.copy();
			v.scale(factor);
			return v;
		}
		else return v1.sum(v2, factor);
	}
	
	public void sum(CRFParameters p, float factor) {
		for (int i=0; i < relParameters.length; i++)
			relParameters[i] = sum(relParameters[i], p.relParameters[i], factor);
	}
	
	public void init() {
		if (relParameters == null) {
			relParameters = new DenseVector[model.numRelations];
			System.out.println(model.numRelations);
			System.out.println("requesting " + (8*relParameters.length*(long)model.numFeaturesPerRelation[0]));
			for (int j=0; j < relParameters.length; j++) {
				relParameters[j] = 
					new DenseVector(model.numFeatures(j));
					//new DenseVector(model.relationFactor.numFeatures);
			}
		}
	}
	
	public void reset() {
		for (int i=0; i < relParameters.length; i++)
			if (relParameters[i] != null)
				relParameters[i].reset();
	}

	public void serialize(OutputStream os) 
		throws IOException {
		//model.serialize(os);
		//DataOutputStream dos = new DataOutputStream(os);
		DenseVector[] r = relParameters;
		for (int i=0; i < r.length; i++)
			r[i].serialize(os);
	}
	
	public void deserialize(InputStream is) 
		throws IOException {
		init();
		//reset();
		DenseVector[] r = relParameters;
		for (int i=0; i < r.length; i++)
			r[i].deserialize(is);
	}	
}
