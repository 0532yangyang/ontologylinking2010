package javatools.ml.weightedmaxsat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javatools.administrative.D;
import javatools.datatypes.MySparseVector;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class WMaxSatWcnf {

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

	/**
	 * When unknown literal num of the clause equals zero, the value of the
	 * clause is decided When the number is 1, the clause is a unit clause
	 */
	int[] unknown_literal_num_clause;

	//
	// /**The value of the clause after */
	// int []clause_values;
	//
	double gain = 0;

	public WMaxSatWcnf(String file_wcnf) {
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

			unknown_literal_num_clause = new int[csize];
			for (int i = 0; i < csize; i++) {
				unknown_literal_num_clause[i] = clauses[i].getDims().size();
			}
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
			DelimitedWriter dw2 = new DelimitedWriter(file+".id");
			for (int i = 1; i < vsize; i++) {
				String name = list_variables[i];
				if (name != null && literal_values[2 * i] == 1) {
					dw.write(name);
					dw2.write(i);
				}else{
					dw2.write(-1*i);
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

	public void update() {
		for (int i = 0; i < vsize; i++) {

			//			if (i == 4600) {
			//				D.p("");
			//			}
			int literaltoset = updateOneStep();
			D.p(i, literaltoset);
			//D.p("Iter: "+i , literal2setToString(literaltoset));
		}
	}

	int updateOneStep() {
		int literalToSet = 0;
		List<Integer> unitclause = new ArrayList<Integer>();
		{
			// get all unit clauses
			for (int i = 0; i < unknown_literal_num_clause.length; i++) {
				if (unknown_literal_num_clause[i] == 1) {
					unitclause.add(i);
				}
			}
		}

		if (unitclause.size() == 0) {
			List<Integer> temp = new ArrayList<Integer>();
			// random pick one literal
			for (int i = 0; i < literals.length; i++) {
				if (literal_values[i] != 0) {
					continue;
				}
				temp.add(i);
			}
			Collections.shuffle(temp);
			literalToSet = temp.get(0);
		} else {
			// choose the best unknown literal
			double[] wvector = new double[lsize];
			for (int unitId : unitclause) {
				MySparseVector msv = clauses[unitId];
				for (int d : msv.getDims()) {
					if (literal_values[d] == 0) {
						/** Only consider those variables have not been set */
						wvector[d] += cweights[unitId];
					}
				}
			}
			double largestDiff = -1;
			for (int i = 0; i < lsize; i += 2) {
				if (literal_values[i] != 0)
					continue;
				double w1 = wvector[i];
				double w2 = wvector[i + 1];
				double check = Math.abs(w1 - w2);
				if (check > largestDiff) {
					largestDiff = check;
					literalToSet = w1 > w2 ? i : i + 1;
				}
			}
		}
		assignVariableValue(literalToSet);
		return literalToSet;
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

	void assignVariableValue(int literalId) {
		// same variable literal id
		int olitId = 0;

		if (literalId % 2 == 0) {
			olitId = literalId + 1;
		} else {
			olitId = literalId - 1;
		}

		literal_values[literalId] = 1;
		literal_values[olitId] = -1;

		{
			/**
			 * for literalId, its corresponding "unsat" clauses are SAT now; use
			 * -1 to say it
			 */
			List<Integer> satClauses = literals[literalId].getDims();
			for (int cid : satClauses) {
				unknown_literal_num_clause[cid] = -1;
			}
		}
		{
			/**
			 * For the counterpart literal, its corresponding "unsat" clauses's
			 * unknown literals should be -1 now
			 */
			List<Integer> satClauses = literals[olitId].getDims();
			for (int cid : satClauses) {
				if (unknown_literal_num_clause[cid] > 0) {
					unknown_literal_num_clause[cid]--;

					{
						/** check code: */
						int test = 0;
						MySparseVector msv = clauses[cid];
						for (int d : msv.getDims()) {
							if (literal_values[d] == 0) {
								test++;
							}
						}
						if (test != unknown_literal_num_clause[cid]) {
							D.p("unknown value not match!");
						}
					}
				}

			}
		}

	}

	public void getTotalGainWeight() {
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
		D.p("Total gain:", gain);
		D.p("Total lost:", lost);

	}
}
