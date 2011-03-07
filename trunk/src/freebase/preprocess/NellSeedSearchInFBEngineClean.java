package freebase.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import percept.util.delimited.DelimitedWriter;
import percept.util.delimited.Sort;

import javatools.administrative.D;

import multir.util.delimited.DelimitedReader;

public class NellSeedSearchInFBEngineClean {

	/**
	 * @param args
	 */
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink";
	static String input1 = dir + "/nellseed_fbsearch";
	static String input2 = dir + "/enid_mid_wid_wtitle";
	static String file_enid_mid_wid_argname_otherarg_relation_label = dir + "/entitymaxent/enid_mid_wid_argname_otherarg_relation_label";
	static HashMap<String, List<String[]>> enid2line = new HashMap<String, List<String[]>>();

	public static void load() throws Exception {
		DelimitedReader dr = new DelimitedReader(input2);
		String[] line;
		while ((line = dr.read()) != null) {
			if (!enid2line.containsKey(line[0])) {
				enid2line.put(line[0], new ArrayList<String[]>());
			}
			enid2line.get(line[0]).add(line);
		}
		dr.close();
	}

	public static void clean2list() throws Exception {
		//String output = dir + "/entitymaxent/enid_mid_wid_argname_otherarg_relation_label";
		// create a list of entit
		DelimitedReader dr = new DelimitedReader(input1);
		DelimitedWriter dw = new DelimitedWriter(file_enid_mid_wid_argname_otherarg_relation_label);
		String[] line;
		while ((line = dr.read()) != null) {
			if (line.length < 5)
				continue;
			String arg1 = line[0];
			String arg2 = line[1];
			String relation = line[2];
			String label = line[3];
			String[] arg1enid = line[4].split(";");
			String[] arg2enid = line[5].split(";");
			for (String a1 : arg1enid) {
				doit(dw, a1, arg1, arg2, relation, label, "arg1");
			}
			for (String a2 : arg2enid) {
				doit(dw, a2, arg2, arg1, relation, label, "arg2");
			}
			// D.p(arg1enid.length,arg2enid.length);
			// break;
		}
		dr.close();
		dw.close();
		{
			Sort.sort(file_enid_mid_wid_argname_otherarg_relation_label,
					file_enid_mid_wid_argname_otherarg_relation_label+".sortbyWid",
					dir,
					new Comparator<String[]>(){

						@Override
						public int compare(String[] o1, String[] o2) {
							// TODO Auto-generated method stub
							int widindex = 2;
							//look at function doit() dw.write(....);
							int wid1 = Integer.parseInt(o1[widindex]); 
							int wid2 = Integer.parseInt(o2[widindex]);
							return wid1-wid2;
						}
						
					});
		}
	}

	private static void doit(DelimitedWriter dw, String a, String arg1, String arg2, String relation, String label,
			String arg1OrArg2) throws IOException {
		if (!a.startsWith("/en")) {
			return;
		}
		a = a.replace("/en/", "");
		List<String[]> enid_mid_wid_wtitle_list = enid2line.get(a);
		if (enid_mid_wid_wtitle_list != null && enid_mid_wid_wtitle_list.size() > 0) {
			for (String[] enid_mid_wid_wtitle_line : enid_mid_wid_wtitle_list) {
				String enid = a;
				String argname = arg1;
				String otherarg = arg2;
				String mid = enid_mid_wid_wtitle_line[1];
				String wid = enid_mid_wid_wtitle_line[2];
				dw.write(enid, mid, wid, argname, otherarg, relation, label, arg1OrArg2);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		load();
		clean2list();
	}

}
