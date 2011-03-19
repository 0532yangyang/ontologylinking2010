package freebase.relationmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import multir.util.delimited.Sort;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S1_infoboxrelation {

	public static void subsetinfobox() {

		try {
			DelimitedWriter dw = new DelimitedWriter(Main.file_infobox_clean_subset_sbwid + ".temp");
			HashSet<Integer> usedWid = new HashSet<Integer>();
			{
				List<String[]> seeds = (new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean))
						.readAll();
				for (String[] s : seeds) {
					if (s[6].equals("+1")) {
						usedWid.add(Integer.parseInt(s[2]));
					}
				}
			}
			DelimitedReader dr = new DelimitedReader(Main.file_infobox_clean);

			String[] l;
			while ((l = dr.read()) != null) {
				int wid = Integer.parseInt(l[0]);
				if (usedWid.contains(wid)) {
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
			Sort.sort(Main.file_infobox_clean_subset_sbwid + ".temp", Main.file_infobox_clean_subset_sbwid, Main.dir,
					new Comparator<String[]>() {
						@Override
						public int compare(String[] o1, String[] o2) {
							// TODO Auto-generated method stub
							int wid1 = Integer.parseInt(o1[0]);
							int wid2 = Integer.parseInt(o2[0]);
							return wid1 - wid2;
						}
					});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void matchArgB() {
		try {
			// DelimitedWriter dw = new
			// DelimitedWriter(Main.file_argBmatchIBValue);
			List<String[]> towrite = new ArrayList<String[]>();
			DelimitedReader dr1 = new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean);
			DelimitedReader dr2 = new DelimitedReader(Main.file_infobox_clean_subset_sbwid);
			List<String[]> ibblock = dr2.readBlock(0);
			String[] l;
			int counted = 0;
			while ((l = dr1.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				if (l[6].equals("-1"))
					continue;
				if (ibblock == null)
					break;
				while (Integer.parseInt(ibblock.get(0)[0]) < wid && (ibblock = dr2.readBlock(0)) != null) {
				}
				if (ibblock != null && Integer.parseInt(ibblock.get(0)[0]) == wid) {
					String argA = l[3];
					String argB = l[4];
					String argBsmall = argB.toLowerCase();
					String relation = l[5];
					String arg1or2 = l[7];
					// D.p(argA,argB,relation,arg1or2);
					for (String[] b : ibblock) {
						String ibname = b[3];
						String ibrelation = b[4];
						String value = b[5];
						String desc = b[6];
						String valuesmall = value.toLowerCase();
						int int_sharewords = StringUtil.numOfShareWords(argB, value);
						if (int_sharewords > 0) {
							towrite.add(new String[] { wid + "", argA, argB, relation, arg1or2, ibname, ibrelation,
									value });
							// dw.write(wid,argA,argB,relation,arg1or2,ibname,ibrelation,value);
						}
					}
					// D.p(wid);
				} else {
					// D.p("Missing",wid);
				}
			}
			StringTable.sortByColumn(towrite, new int[] { 3 });
			StringTable.print(towrite, Main.file_argBmatchIBValue, 20);
			D.p(counted);

			dr1.close();
			dr2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void matchArgB_Manual() {
		try {
			// DelimitedWriter dw = new
			// DelimitedWriter(Main.file_argBmatchIBValue);
			List<String[]> towrite = new ArrayList<String[]>();
			DelimitedReader dr1 = new DelimitedReader(freebase.typematch.Main.fout_fbsearchresult_clean);
			DelimitedReader dr2 = new DelimitedReader(Main.file_infobox_clean_subset_sbwid);
			List<String[]> ibblock = dr2.readBlock(0);
			String[] l;
			int counted = 0;
			while ((l = dr1.read()) != null) {
				int wid = Integer.parseInt(l[2]);
				if (l[6].equals("-1"))
					continue;
				if (ibblock == null)
					break;

				String argA = l[3];
				String argB = l[4];
				String relation = l[5];
				String arg1or2 = l[7];
				if (ibblock == null)
					break;
				while (Integer.parseInt(ibblock.get(0)[0]) < wid && (ibblock = dr2.readBlock(0)) != null) {
				}
				if (ibblock != null && Integer.parseInt(ibblock.get(0)[0]) == wid) {
					String ibname = ibblock.get(0)[3];

					towrite.add(new String[] { wid + "", argA, argB, arg1or2, relation, ibname });

				}
			}
			StringTable.sortByColumn(towrite, new int[] { 1 });
			StringTable.print(towrite, Main.file_argBmatchIBValue, 20);
			D.p(counted);

			dr1.close();
			dr2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// subsetinfobox();
		matchArgB_Manual();
	}
}
