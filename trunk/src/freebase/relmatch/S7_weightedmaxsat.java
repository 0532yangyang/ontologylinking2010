package freebase.relmatch;

import javatools.ml.weightedmaxsat.WeightedClauses;

public class S7_weightedmaxsat {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			WeightedClauses wc = new WeightedClauses(Main.file_clause);
			wc.update();
			wc.printFinalResult(Main.file_predict);
		}catch(Exception e){
			
		}
	}

}
