package freebase.typematch;

import javatools.administrative.D;
import javatools.ml.weightedmaxsat.WeightedClauses;

public class S4_weightedmaxsat {
	public static void main(String []args){
		try{
			WeightedClauses wc = new WeightedClauses(Main.fout_clauses);
			wc.update();
			wc.printFinalResult(Main.fout_predict1);
		}catch(Exception e){
			
		}
	}
}
