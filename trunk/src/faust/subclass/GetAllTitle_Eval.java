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
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.StringTable;
import javatools.webapi.BingApi;

public class GetAllTitle_Eval {
	public static void main(String[] args) throws IOException {
		String file_pair_in_mid_name = Main.dir + "/midnamepair";
		printAllTitles(file_pair_in_mid_name, file_pair_in_mid_name + ".alltitles");
	}

	public static void printAllTitles(String input, String output) throws IOException {
		HashMap<String, String> mid2name = new HashMap<String, String>();
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(input);
		String[] l;
		while ((l = dr.read()) != null) {
			if (!mid2name.containsKey(l[2])) {
				mid2name.put(l[2], l[5]);
			}
		}
		dr.close();
		for (Entry<String, String> e : mid2name.entrySet()) {
			dw.write(e.getKey(), e.getValue());
		}
		dw.close();
	}
}
