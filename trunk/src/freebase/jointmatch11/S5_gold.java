package freebase.jointmatch11;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S5_gold {
	public static void main(String[] args) throws IOException {
		createGoldView();
	}

	public static void createGoldView() throws IOException {
		//load type
		String localdir = Main.dir+"/manual";
		HashMap<String, List<String>> mapresult = new HashMap<String, List<String>>();
		{
			DelimitedReader dr = new DelimitedReader(localdir + "/goldtype");
			String[] l;
			while ((l = dr.read()) != null) {
				StringTable.mapKey2SetAdd(mapresult, l[0], l[1], true);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(localdir + "/goldrel");
			String[] l;
			while ((l = dr.read()) != null) {
				StringTable.mapKey2SetAdd(mapresult, l[0], l[1], true);
			}
			dr.close();
		}

		DelimitedWriter dw = new DelimitedWriter(Main.file_goldview);
		int viewid = 1;
		for (NellRelation nr : Main.no.nellRelationList) {
			String domain = nr.domain;
			String range = nr.range;
			String relname = nr.relation_name;
			int viewnumforthisrel = 0;
			List<String> relMap = mapresult.get(relname);
			List<String> rangeMap = mapresult.get(range);
			List<String> domainMap = mapresult.get(domain);

			if (relMap != null && rangeMap != null && domainMap != null) {
				for (String rm : relMap) {
					for (String rgm : rangeMap) {
						for (String dm : domainMap) {
							dw.write(viewid, relname, rm, dm, rgm);
							viewnumforthisrel++;
							viewid++;
						}
					}
				}
			}
			if (viewnumforthisrel == 0) {
				D.p("NO VIEW:", nr.relation_name);
			}
		}
		dw.close();
	}
}
