package multir.learning.algorithm;

import java.io.IOException;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class MILModel {

	public int numRelations;
	public int[] numFeaturesPerRelation;
	
	public int numFeatures(int rel) {
		return numFeaturesPerRelation[rel];
	}
	
	public int noRelationState;
	
	public void read(String file) throws IOException {
		DelimitedReader r = new DelimitedReader(file);
		numRelations = Integer.parseInt(r.read()[0]);
		numFeaturesPerRelation = new int[numRelations];
		for (int i=0; i < numRelations; i++)
			numFeaturesPerRelation[i] = Integer.parseInt(r.read()[0]);
		r.close();
	}
	
	public void write(String file) throws IOException {
		DelimitedWriter w = new DelimitedWriter(file);
		w.write(numRelations + "");
		for (int i=0; i < numFeaturesPerRelation.length; i++)
			w.write(numFeaturesPerRelation[i] + "");
		w.close();
	}
	
}
