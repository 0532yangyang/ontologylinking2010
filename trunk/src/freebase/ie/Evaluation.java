package freebase.ie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class Evaluation {
	/**If I only consider nell_pos, what good patterns can I get?*/
	public static void test1() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_instance_featurize);
			DelimitedWriter dw = new DelimitedWriter(Main.eval_dir+"/0401patternfreq");
			String[] l;
			List<String[]> t = new ArrayList<String[]>();
			while ((l = dr.read()) != null) {
				if (l[3].equals("nell_pos")) {
					String r = l[2];
					String[] arr_p = l[6].split(" ");
					for (String p : arr_p) {
						t.add(new String[] {r,p});
					}
					//D.p(l[6]);
				}
			}
			List<String[]>t2 = StringTable.squeeze(t, new int[]{0,1});
			//StringTable.sortByColumn(t2, new int[]{2,0},new boolean[]{false, true});
			for(String []a:t2){
				int f = Integer.parseInt(a[0]);
				if(f>1){
					dw.write(a);
				}
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test1();
	}
}
