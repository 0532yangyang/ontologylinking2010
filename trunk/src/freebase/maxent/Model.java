package freebase.maxent;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;




public class Model{
	
	// stores information about the pass1 features used
	// the features used
	public Feature[] pass1Features;
	HashMap<String,Integer>featureName2Id;
	List<String>featureId2Name;
 
	
	public Model() { }
	
	public Model(Settings s) {
		pass1Features = new Feature[Settings.pass1FeaturesToCompute.length];
		System.arraycopy(Settings.pass1FeaturesToCompute, 0, pass1Features, 0, pass1Features.length);
		featureName2Id = new HashMap<String,Integer>();
		featureId2Name = new ArrayList<String>();
		
//		nodes = new Node[3];
//		nodeFactors = new Factor[3];
//		selectionalPreferenceFactors = new Factor[2];
//		for (int i=0; i < nodes.length; i++) {
//			nodes[i] = new Node();
//			nodeFactors[i] = new Factor();
//			if (i > 0) selectionalPreferenceFactors[i-1] = new Factor();
//		}
//		NO_RELATION_STATE = nodes[1].addState("_none_");
	}


	public boolean usePass1Feature(Feature f) {
		for (int i=0; i < pass1Features.length; i++)
			if (pass1Features[i].equals(f)) return true;
		return false;
	}

	public void read(String file) throws IOException {
		DelimitedReader r = new DelimitedReader(file);
		//NO_RELATION_STATE = Integer.parseInt(r.read()[0]);
		pass1Features = readFeatureArray(r);
//		int numNodes = Integer.parseInt(r.read()[0]);
//		nodes = new Node[numNodes];
//		for (int i=0; i < numNodes; i++) {
//			nodes[i] = new Node();
//			int numStates = Integer.parseInt(r.read()[0]);
//			for (int j=0; j < numStates; j++) {
//				String[] t = r.read();
//				nodes[i].stateId2name.add(t[1]);
//				nodes[i].name2stateId.put(t[1], j);//Integer.parseInt(t[0]));
//			}
//		}
//		nodeFactors = new Factor[numNodes];
//		for (int i=0; i < numNodes; i++) {
//			nodeFactors[i] = new Factor();
//			int numFeatures = Integer.parseInt(r.read()[0]);
//			for (int j=0; j < numFeatures; j++) {
//				String[] t = r.read();
//				nodeFactors[i].featureId2name.add(t[1]);
//				nodeFactors[i].name2featureId.put(t[1], j);//Integer.parseInt(t[0]));
//			}			
//		}
//		selectionalPreferenceFactors = new Factor[numNodes-1];
//		for (int i=0; i < numNodes-1; i++) {
//			selectionalPreferenceFactors[i] = new Factor();
//			int numFeatures = Integer.parseInt(r.read()[0]);
//			for (int j=0; j < numFeatures; j++) {
//				String[] t = r.read();
//				selectionalPreferenceFactors[i].featureId2name.add(t[1]);
//				selectionalPreferenceFactors[i].name2featureId.put(t[1], j);
//			}
//		}
//		r.close();
//		
//		setSuper();
	}
	
	public void write(String file) throws IOException {
		DelimitedWriter w = new DelimitedWriter(file);
//		w.write(NO_RELATION_STATE);
		writeFeatureArray(w, pass1Features);
//		w.write(nodes.length);
//		for (int i=0; i < nodes.length; i++) {
//			w.write(nodes[i].stateId2name.size());
//			for (int j=0; j < nodes[i].stateId2name.size(); j++)
//				w.write(j + "", nodes[i].stateId2name.get(j));			
//		}
//		for (int i=0; i < nodeFactors.length; i++) {
//			w.write(nodeFactors[i].featureId2name.size());
//			for (int j=0; j < nodeFactors[i].featureId2name.size(); j++)
//				w.write(j + "", nodeFactors[i].featureId2name.get(j));			
//		}
//		for (int i=0; i < selectionalPreferenceFactors.length; i++) {
//			w.write(selectionalPreferenceFactors[i].featureId2name.size());
//			for (int j=0; j < selectionalPreferenceFactors[i].featureId2name.size(); j++)
//				w.write(j + "", selectionalPreferenceFactors[i].featureId2name.get(j));
//		}
//	    w.close();
	}
	
	private Feature[] readFeatureArray(DelimitedReader r) throws IOException {
		List<Feature> ftl = new ArrayList<Feature>();
		String[] t = r.read();
		if (t[0].length() > 0) {
			String[] strFts = t[0].split(" ");
			for (String strFt : strFts)
				ftl.add(Feature.valueOf(strFt));
		}
		return ftl.toArray(new Feature[0]);
	}
	
	private void writeFeatureArray(DelimitedWriter w, Feature[] ar) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Feature f : ar) sb.append(f.name() + " ");
		w.write(sb.toString());
	}
}
