package freebase.jointmatch13icsemi;

import java.io.IOException;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.ml.weightedmaxsat.RandWalkSat;
import javatools.ml.weightedmaxsat.WMaxSatWcnf;

public class S4_inference {

	public static void greedy() {
		WMaxSatWcnf wmsw = new WMaxSatWcnf(Main.file_jointclause);
		wmsw.update();
		wmsw.printFinalResult(Main.file_jointclause + ".predict");
		wmsw.getTotalGainWeight();
	}

	/**./maxwalksat -targetcost 50000 -tries 20 <../jointclause >../jointclause.predictwalk*/
	public static void walksat() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_jointclause + ".predictwalk");
		WMaxSatWcnf wmsw = new WMaxSatWcnf(Main.file_jointclause);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[0].startsWith("v ")) {
				String[] ab = l[0].split(" ");
				wmsw.setLiteralDirectly(Integer.parseInt(ab[1]));
			}
		}
		wmsw.getTotalGainWeight();
		dr.close();
	}
	
	public static void mywalksat() throws IOException{
		RandWalkSat rws = new RandWalkSat(Main.file_jointclause);
		rws.update(100000,0.1);
	}

	public static void main(String[] args) throws IOException {
		mywalksat();
		//greedy();
		//walksat();
	}
}
