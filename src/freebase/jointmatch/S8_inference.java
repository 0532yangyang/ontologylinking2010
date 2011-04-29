package freebase.jointmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.ml.weightedmaxsat.WeightedClauses;

public class S8_inference {

	public static void mergeClauses() throws Exception {
		List<String[]> all1 = (new DelimitedReader(Main.file_typeclause)).readAll();
		List<String[]> all2 = (new DelimitedReader(Main.file_relationrelclause)).readAll();
		DelimitedWriter dw = new DelimitedWriter(Main.file_jointlclause);
		for (String[] a : all1) {
			dw.write(a);
		}
		for (String[] b : all2) {
			dw.write(b);
		}
		dw.close();

	}

	public static void predrelonly() {

		WeightedClauses wc = new WeightedClauses(Main.file_relationrelclause);
		wc.update();
		wc.printFinalResult(Main.file_predict_relonly);

	}

	public static void predtypeonly() {

		WeightedClauses wc = new WeightedClauses(Main.file_typeclause);
		wc.update();
		wc.printFinalResult(Main.file_predict_typeonly);

	}

	public static void predjoint() {

		WeightedClauses wc = new WeightedClauses(Main.file_jointlclause);
		wc.update();
		wc.printFinalResult(Main.file_predict_joint);

	}

	private static void sampleSql2isntance4DebugPurpose() throws IOException {

		HashMap<Integer, String> wid2title = new HashMap<Integer, String>();
		{
			//load wid 2 title
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);

			String[] l;
			while ((l = dr.read()) != null) {
				wid2title.put(Integer.parseInt(l[2]), l[3]);
			}
		}
		HashCount<String> hc = new HashCount<String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance);
			DelimitedWriter dw = new DelimitedWriter(Main.file_sql2instance + ".sample10");
			String[] l;
			while ((l = dr.read()) != null) {
				String key = l[2] + "\t" + l[3];
				int a = hc.see(key);
				if (a < 10) {
					int wid1 = Integer.parseInt(l[0]);
					int wid2 = Integer.parseInt(l[1]);
					String title1 = wid2title.get(wid1);
					String title2 = wid2title.get(wid2);
					if (title1 != null && title2 != null) {
						dw.write(wid1, wid2, title1, title2, l[2], l[3]);
						hc.add(key);
					}
				}
			}
			dw.close();
			dr.close();
		}
	}

	private static void showDebugInfomation() throws IOException {
		HashMap<String, List<String[]>> data = new HashMap<String, List<String[]>>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance + ".sample10");
			String[] l;
			while ((l = dr.read()) != null) {
				String key = l[4] + "\t" + l[5];
				if (!data.containsKey(key)) {
					data.put(key, new ArrayList<String[]>());
				}
				data.get(key).add(l);
			}
		}
		{
			List<String[]> all = (new DelimitedReader(Main.file_predict_joint)).readAll();
			DelimitedWriter dw = new DelimitedWriter(Main.file_predict_joint+".debug");
			for (String[] a : all) {
				if(a[0].contains("VR::")){
					String []xyz = a[0].split("::");
					String nell = xyz[1];
					String fb = xyz[2];
					String key = nell+"\t"+fb;
					List<String[]>examples = data.get(key);
					dw.write(a);
					for(String []e: examples){
						dw.write("     ", e[0],e[1],e[2],e[3]);
					}
				}
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws Exception {
		//sampleSql2isntance4DebugPurpose();
		//		mergeClauses();
		//		predjoint();
		showDebugInfomation();
	}
}
