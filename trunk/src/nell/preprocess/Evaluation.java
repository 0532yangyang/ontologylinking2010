package nell.preprocess;

import java.io.IOException;
import java.util.HashMap;

import multir.util.HashCount;
import multir.util.delimited.DelimitedReader;

public class Evaluation {

	public static void main(String[] args) {
		stat_nellmatch();
	}

	public static void stat_nellmatch() {
		String input = "/projects/pardosa/s5/clzhang/ontologylink/tmp1/matches-pairmentions-relationIDs-nell.sortedByArg1Arg2SentenceID";
		DelimitedReader dr;
		
		HashCount hc = new HashCount();
		try {
			dr = new DelimitedReader(input);
			String[] line;
			int ln = 0;
			while ((line = dr.read()) != null) {
				ln++;
				if(line.length>10){
					hc.add(line[10]);
				}
			}
			System.out.println("Line number is:\t"+ln);
			dr.close();
			
			hc.sort();
			hc.printAll();
			System.out.println("Total match:\t"+hc.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
