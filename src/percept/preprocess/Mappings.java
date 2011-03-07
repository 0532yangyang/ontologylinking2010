package percept.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import percept.util.delimited.DelimitedReader;
import percept.util.delimited.DelimitedWriter;

public class Mappings {

	private Map<String,Integer> state2stateID = new HashMap<String,Integer>();
	private Map<String,Integer> ft2ftID = new HashMap<String,Integer>();
	
	public int getStateID(String state, boolean addNew) {
		Integer i = state2stateID.get(state);
		if (i == null) {
			if (!addNew) return -1;
			i = state2stateID.size();
			state2stateID.put(state, i);
		}
		return i;
	}
	
	public Map<String,Integer> getState2StateID() {
		return state2stateID;
	}
	
	public int getFeatureID(String feature, boolean addNew) {
		Integer i = ft2ftID.get(feature);
		if (i == null) {
			if (!addNew) return -1;
			i = ft2ftID.size();
			ft2ftID.put(feature, i);
		}
		return i;
	}
	
	public int numStates() {
		return state2stateID.size();
	}
	
	public int numFeatures() {
		return ft2ftID.size();
	}
	
	public void write(String file) throws IOException {
		DelimitedWriter w = new DelimitedWriter(file);
		writeMap(state2stateID, w);
		writeMap(ft2ftID, w);
		w.close();
	}
	
	public void read(String file) throws IOException {
		DelimitedReader r = new DelimitedReader(file);
		readMap(state2stateID, r);
		readMap(ft2ftID, r);
		r.close();
	}
	
	private void writeMap(Map<String,Integer> m, DelimitedWriter w) 
		throws IOException {
		w.write("" + m.size());
		List<Map.Entry<String,Integer>> l = new
			ArrayList<Map.Entry<String,Integer>>(m.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String,Integer>>() {
			public int compare(Map.Entry<String,Integer> e1, Map.Entry<String,Integer> e2) {
				return e1.getValue() - e2.getValue(); } } );		
		for (Map.Entry<String,Integer> e : l)
			w.write("" + e.getValue(), e.getKey());
	}
	
	private void readMap(Map<String,Integer> m, DelimitedReader r)
		throws IOException {
		int count = Integer.parseInt(r.read()[0]);
		for (int i=0; i < count; i++) {
			String[] t = r.read();
			m.put(t[1], Integer.parseInt(t[0]));
		}
	}
}
