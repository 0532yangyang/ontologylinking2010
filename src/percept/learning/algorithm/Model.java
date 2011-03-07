package percept.learning.algorithm;

import java.io.IOException;

import percept.util.delimited.DelimitedReader;
import percept.util.delimited.DelimitedWriter;

public class Model {

	public int numStates;
	public int[] numFeaturesPerState;
	
	public int numFeatures(int state) {
		return numFeaturesPerState[state];
	}
	
	public int noRelationState;
	
	public void read(String file) throws IOException {
		DelimitedReader r = new DelimitedReader(file);
		numStates = Integer.parseInt(r.read()[0]);
		numFeaturesPerState = new int[numStates];
		for (int i=0; i < numStates; i++)
			numFeaturesPerState[i] = Integer.parseInt(r.read()[0]);
		r.close();
	}
	
	public void write(String file) throws IOException {
		DelimitedWriter w = new DelimitedWriter(file);
		w.write(numStates + "");
		for (int i=0; i < numFeaturesPerState.length; i++)
			w.write(numFeaturesPerState[i] + "");
		w.close();
	}

}
