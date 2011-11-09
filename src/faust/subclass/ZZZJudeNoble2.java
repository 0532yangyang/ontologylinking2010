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

import javatools.filehandlers.DelimitedReader;

public class ZZZJudeNoble2 {
	static String MIDOFNOBLEPERSON = "/m/04l4q52";

	public static void main(String[] args) throws IOException {

		String file_noble = Main.dir + "/visible_noble";
		String file_noble_wid = file_noble + "_wid";
		String file_noble_wfrstSnt = file_noble_wid + "_frstSnt";
		if (args[0].equals("-1")) {
			getAllNobleMen(file_noble);
		}
		if (args[0].equals("-2")) {
			getWidOfNobleMen(file_noble, file_noble_wid);
		}
		if (args[0].equals("-3")) {
			getSentence(file_noble_wid, file_noble_wfrstSnt);
		}
	}

	public static void getAllNobleMen(String output) throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.dir_freebase + "/visible");
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		///j0	/m/04l4q52	/type/type/instance	/m/02rg_
		while ((l = dr.read()) != null) {
			if (l[1].equals(MIDOFNOBLEPERSON) && l[2].equals("/type/type/instance")) {
				dw.write(l[3]);
			}
		}
		dr.close();
		dw.close();
	}

	public static void getWidOfNobleMen(String input, String output) throws IOException {
		HashMap<String, Integer> mid2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.dir_freebase + "/mid2wid");
			String[] l;
			while ((l = dr.read()) != null) {
				mid2wid.put(l[0], Integer.parseInt(l[1]));
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input);
			DelimitedWriter dw = new DelimitedWriter(output);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				Integer wid = mid2wid.get(mid);
				if (wid != null) {
					dw.write(mid, wid);
				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void getSentence(String input, String output) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		HashMap<Integer, String> targetArticles = new HashMap<Integer, String>();
		HashCount<String> mid2numSen = new HashCount<String>();
		{
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				int x = Integer.parseInt(l[1]);
				targetArticles.put(x, l[0]);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wiki_sentence + ".tokens");
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[1];
				int artid = Integer.parseInt(mid);
				if (targetArticles.containsKey(artid)) {
					//the first sentence of this article
					if (mid2numSen.see(mid) < 5) {
						dw.write(mid, l[3]);
						mid2numSen.add(mid);

					}
				}
			}
			dr.close();
		}
		dw.close();

	}
}
