package javatools.ml.weightedmaxsat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javatools.administrative.D;
import javatools.datatypes.MySparseVector;
import javatools.datatypes.QuickSort;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class RandWalkSat {

	Map<String, Integer> map_variables = new HashMap<String, Integer>();
	//List<String> list_variables = new ArrayList<String>();
	String[] list_variables;
	/**
	 * record the number of clauses that each literal appears in; literal 2*i
	 * and 2i+1 are x and \neg x
	 */
	MySparseVector[] literals;
	MySparseVector[] clauses;
	double[] cweights;
	int csize;
	int lsize; // literal size should be 2*vsize
	int vsize;

	/**
	 * value of the literal, it can be 1, -1 or 0 (0 means unknown) notice 2*i &
	 * 2*i+1 must be 0,0 or 1, -1
	 */
	int[] literal_values;

	//
	// /**The value of the clause after */
	// int []clause_values;
	//
	double gain = 0;

	public RandWalkSat(String file_wcnf) {
		try {
			List<MySparseVector> tempclauses = new ArrayList<MySparseVector>();
			List<Double> tempweight = new ArrayList<Double>();
			// A line is a clause
			int biggestrawid = 0;
			{
				DelimitedReader dr = new DelimitedReader(file_wcnf);
				String[] line = dr.read();
				while ((line = dr.read()) != null) {

					ArrayList<Integer> dimension = new ArrayList<Integer>();

					String[] lits = line[0].split(" ");
					double w = Double.parseDouble(lits[0]);
					for (int i = 1; i < lits.length; i++) {
						String lit = lits[i];
						int rawid = Integer.parseInt(lit);
						biggestrawid = Math.max(biggestrawid, Math.abs(rawid));
						if (rawid == 0) {
							break;
						} else if (rawid > 0) {
							dimension.add(rawid * 2);
						} else {
							dimension.add(rawid * (-2) + 1);
						}
					}
					// int clauseId = clauses.size();
					tempweight.add(w);
					MySparseVector c = new MySparseVector(1, dimension);
					tempclauses.add(c);
				}
			}
			list_variables = new String[biggestrawid + 1];
			{
				DelimitedReader dr = new DelimitedReader(file_wcnf + ".name");
				String[] l;
				while ((l = dr.read()) != null) {
					int varid = Integer.parseInt(l[0]);
					String varname = l[1];
					map_variables.put(varname, varid);
					list_variables[varid] = varname;
				}
			}

			// intialize literals and clauses
			vsize = list_variables.length;
			lsize = vsize * 2;
			csize = tempclauses.size();
			literals = new MySparseVector[lsize];
			clauses = new MySparseVector[csize];
			{
				/** init literals and clauses */
				for (int i = 0; i < lsize; i++)
					literals[i] = new MySparseVector();
			}
			cweights = new double[csize];
			for (int clauseId = 0; clauseId < tempclauses.size(); clauseId++) {
				clauses[clauseId] = tempclauses.get(clauseId);
				cweights[clauseId] = tempweight.get(clauseId);
				for (int lit : clauses[clauseId].getDims()) {
					literals[lit].add(clauseId, 1);
				}
			}
			literal_values = new int[lsize];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String literal2setToString(int literaltoset) {
		int vid = literaltoset / 2;
		boolean vv = literaltoset % 2 == 0 ? true : false;
		return list_variables[vid] + "\t" + vv;
	}

	public void printFinalResult(String file) {
		try {
			DelimitedWriter dw = new DelimitedWriter(file);
			DelimitedWriter dw2 = new DelimitedWriter(file + ".id");
			for (int i = 1; i < vsize; i++) {
				String name = list_variables[i];
				if (name != null && literal_values[2 * i] == 1) {
					dw.write(name);
					dw2.write(i);
				} else {
					dw2.write(-1 * i);
				}
			}
			dw.close();
			dw2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> printFinalResult() {
		List<String> result = new ArrayList<String>();
		try {
			for (int i = 0; i < vsize; i++) {
				String name = list_variables[i];
				if (literal_values[2 * i] == 1) {
					result.add(name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void update(int maxflips, double notbestpos) {
		randomInit();

		for (int i = 0; i < maxflips; i++) {
			double gain[] = checkLocalImprovement();
			int[] index = QuickSort.quicksort(gain, true);
			if (gain[index[0]] <= 0) {
				//nothing worth to change
				break;
			}
			if (Math.random() < notbestpos) {
				//random flip
				int j=0;
				for (; j < index.length; j++) {
					if (gain[index[j]] <= 0) {
						break;
					}
				}
				flipVar((int)Math.round(j*Math.random()));
			}else{
				if (gain[index[0]] > 0) {
					flipVar(index[0]);
				}
			}
			getTotalLossWeight();
		}
	}

	private double[] checkLocalImprovement() {
		/**gain[i] says, by flipping variable i, how many benifit can I gain?*/
		double[] gain = new double[vsize];
		for (int k = 0; k < csize; k++) {
			MySparseVector msv = clauses[k];
			List<Integer> dims = msv.getDims();
			boolean sat = false;
			List<Integer> varMakeItTrue = new ArrayList<Integer>();
			for (int d : dims) {
				if (literal_values[d] == 1) {
					varMakeItTrue.add(d);
					sat = true;
				}
			}
			if (sat) {
				if (varMakeItTrue.size() == 1) {
					int x = varMakeItTrue.get(0);
					int varId = x / 2;
					gain[varId] -= cweights[k];
				}
				/**if two variables in varMakeItTrue is true, nothing will be changed by flipping one variable*/
			} else {
				for (int d : dims) {
					gain[d / 2] += cweights[k];
				}
			}
		}
		return gain;
	}

	public void randomInit() {
		Random r = new Random();
		for (int i = 0; i < vsize; i++) {
			if (r.nextBoolean()) {
				literal_values[i * 2] = 1;
				literal_values[i * 2 + 1] = -1;
			} else {
				literal_values[i * 2] = -1;
				literal_values[i * 2 + 1] = 1;
			}
		}
		getTotalLossWeight();
	}

	public void setLiteralDirectly(int id) {
		if (id > 0) {
			literal_values[id * 2] = 1;
			literal_values[id * 2 + 1] = -1;
		} else {
			id = -1 * id;
			literal_values[id * 2] = -1;
			literal_values[id * 2 + 1] = 1;
		}
	}

	public void flipVar(int varId) {
		literal_values[varId * 2] *= -1;
		literal_values[varId * 2 + 1] *= -1;
	}

	public double getTotalLossWeight() {
		double gain = 0;
		double lost = 0;
		for (int k = 0; k < clauses.length; k++) {
			MySparseVector msv = clauses[k];
			List<Integer> dims = msv.getDims();
			boolean sat = false;
			for (int d : dims) {
				if (literal_values[d] == 1) {
					gain += cweights[k];
					sat = true;
					break;
				}
			}
			if (!sat) {
				lost += cweights[k];
			}
		}
		D.p("Total lost:", lost);
		return lost;
	}

}
