package faust.subclass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import multir.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.StringTable;
import javatools.webapi.BingApi;

public class ZZZJudeNoble{
	public static void main(String[] args) throws Exception {
		if (args[0].contains("-1")) {
			JudeByClass.step1_sort(Main.dir + "/visible_noble", Main.dir + "/visible_noble.sblinkobj");
			JudeByClass.step2_template2delimited(Main.dir + "/visible_noble.sblinkobj", new String[] {
					"/royalty/noble_title/holders|/royalty/noble_title_tenure/noble_person",
					"/royalty/noble_title/holders|/royalty/noble_title_tenure/noble_title" }, Main.dir
					+ "/nobletitle_person_org");

			//			step3MID2Name(file_pair_in_mid, file_pair_in_mid_name);
		}
		if (args[0].contains("-2")) {
			String file_pair_in_mid = Main.dir + "/jude_nobletitle_person_org";
			String file_pair_in_mid_name = file_pair_in_mid + "_name";
			String file_matchkey_personorg = file_pair_in_mid + "_matchkey";
			String file_matchsen_personorg = file_pair_in_mid + "_matchsen";
			String file_subsetsen = file_pair_in_mid + "_sensubset";
			step3_filterJudeWant(Main.dir + "/nobletitle_person_org", file_pair_in_mid);
			step3MID2Name(file_pair_in_mid, file_pair_in_mid_name);
		}
	}

	public static void step1_sort(String input, String output) throws Exception {
		Sort.sort(input, output, Main.dir, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[4].compareTo(o2[4]);
			}

		});
	}

	public static void step3_filterJudeWant(String input, String output) throws Exception {
		HashCount<String> judetitlecount = new HashCount<String>();
		DelimitedReader dr = new DelimitedReader(input);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			dw.write(l);
		}
		dr.close();
		dw.close();
		judetitlecount.printAll();
	}

	public static void step3MID2Name(String input, String output) throws IOException {
		HashMap<String, Set<String>> mid2names = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				mid2names.put(l[0], new HashSet<String>());
				mid2names.put(l[1], new HashSet<String>());
				mid2names.put(l[2], new HashSet<String>());
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedWriter dwbug = new DelimitedWriter(output + ".debug");
		{
			//get their names by look at Main.file_fbdump_2_len4 
			DelimitedReader dr = new DelimitedReader(Main.file_mid2namesfullset);
			String[] l;
			while ((l = dr.read()) != null) {
				if (mid2names.containsKey(l[0])) {
					mid2names.get(l[0]).add(l[1]);
				}
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			int instanceNum = 1;
			while ((l = dr.read()) != null) {
				Set<String>[] myname = new Set[3];
				boolean success = true;
				for (int i = 0; i < 3; i++) {
					myname[i] = mid2names.get(l[i]);
					if (myname[i] == null || myname[i].size() == 0) {
						success = false;
						break;
					}
				}
				if (success) {
					for (String t : myname[0])
						for (String p : myname[1]) {
							for (String o : myname[2]) {
								dw.write(instanceNum, l[0], l[1], l[2], p, t, o);
								instanceNum++;
							}
						}
				} else {
					dwbug.write(l);
				}
			}
			dr.close();
			dw.close();
			dwbug.close();
		}
	}
}
