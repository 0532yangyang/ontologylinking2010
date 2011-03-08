package javatools.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySparseVector {
	List<Integer> dim  =new ArrayList<Integer>();
	List<Double> val = new ArrayList<Double>();
	
	private boolean sorted  = false; 
	public MySparseVector(){
		
	}
	
	public MySparseVector(double v, List<Integer> dimension){
		Collections.sort(dimension);
		dim = dimension;
		for(int i=0;i<dim.size();i++){
			val.add(v);
		}
	}
	
	public void add(int d, double v){
		sorted = false;
		dim.add(d);
		val.add(v);
	}
	
	public void sortByDim(){
		double []d = new double[dim.size()];
		for(int i = 0;i<dim.size();i++)d[i] = dim.get(i);
		int []index = QuickSort.quicksort(d);
		List<Integer> dimtemp  =new ArrayList<Integer>();
		List<Double> valtemp = new ArrayList<Double>();
		for(int k=0;k<index.length;k++){
			dimtemp.add( dim.get(index[k]));
			valtemp.add(val.get(index[k]));
		}
		dim = null;
		val = null;
		dim = dimtemp;
		val = valtemp;
	}
	
	public List<Integer> getDims(){
		return this.dim;
	}
	
}
