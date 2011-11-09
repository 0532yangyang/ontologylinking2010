package faust.subclass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class JudeTwoArguments {
	static class ParaTwo {
		String dir;
		String visible_subset;
		String output;
		String[] arg0filter;//e.g.  "CEO", "Chairman"; if arg0filter == null; there is no filter
		String[] triggerwords; //e.g. "executive", "chairman", "president"
	}

	public static void run(ParaTwo pt) throws Exception {
		visibleTO2mid(pt.output);
		JudeThreeArgument.step3MID2Name(pt.output + ".3mids", pt.output + ".3names");
		JudeThreeArgument.step4SplitByKeywords(pt.arg0filter, pt.output + ".3names", pt.output + ".tuple");
		JudeThreeArgument.step4PersonOrganizationMatchKey(pt.output + ".tuple", pt.output + ".gen");
		JudeThreeArgument.step5_heuristicmatch_alg2(pt.output + ".tuple", pt.output + ".mtch");
		JudeThreeArgument.step6_subset(pt.output + ".mtch", pt.output);
		JudeThreeArgument.step6_givexiao(pt.output);
	}

	public static void runxiao(ParaTwo pt) throws Exception {
		JudeThreeArgument.takeXiaoParsing(pt.output);
		JudeThreeArgument.labelTriggerWords(pt.output, pt.triggerwords);
	}

	public static void runjude(ParaTwo pt) throws Exception {
		toJude(pt.output);
	}

	public static void main(String[] args) throws Exception {
		{
			ParaTwo pt = new ParaTwo();
			pt.dir = Main.dir + "/attendschool";
			pt.visible_subset = pt.dir + "/visible_education";
			pt.output = pt.dir + "/attendschool";
			pt.arg0filter = new String[] {};
			pt.triggerwords = new String[] {};
			//run(pt);
			//runxiao(pt);
			runjude(pt);
		}

	}

	public static void visibleTO2mid(String output) throws Exception {
		DelimitedReader dr = new DelimitedReader(output + ".visible");
		DelimitedWriter dw = new DelimitedWriter(output + ".3mids");
		String[] l;
		while ((l = dr.read()) != null) {
			dw.write(l[1], l[1], l[3]);
		}
		dr.close();
		dw.close();
	}

	public static void step3MID2Name(String output) throws IOException {
		HashMap<String, Set<String>> mid2names = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(output + ".2mid");
			String[] l;
			while ((l = dr.read()) != null) {
				mid2names.put(l[0], new HashSet<String>());
				mid2names.put(l[1], new HashSet<String>());
				mid2names.put(l[2], new HashSet<String>());
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output + ".2names");
		DelimitedWriter dwbug = new DelimitedWriter(output + ".namesbug");
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
			DelimitedReader dr = new DelimitedReader(output + ".2mid");
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
					StringBuilder sb = new StringBuilder();
					for (String t : myname[0])
						sb.append(t + ";;");
					for (String p : myname[1]) {
						for (String o : myname[2]) {
							dw.write(instanceNum, l[0], l[1], l[2], p, o, sb.toString());
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

	public static void toJude(String in) throws IOException {

		{
			DelimitedReader dr = new DelimitedReader(in + ".xiaoner.trigger");
			DelimitedWriter dw = new DelimitedWriter(in);
			String[] l;
			while ((l = dr.read()) != null) {
				l[11] = l[12] = l[7] = "";
				dw.write(l);
			}
			dr.close();
			dw.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(in);
			DelimitedWriter dw = new DelimitedWriter(in + ".label30");
			List<String[]> table = new ArrayList<String[]>();
			String[] l;
			while ((l = dr.read()) != null) {
				table.add(new String[] { l[1], l[2], l[10] });
			}
			Collections.shuffle(table);
			for (int i = 0; i < 30; i++) {
				String[] t = table.get(i);
				dw.write(t);
			}
			dr.close();
			dw.close();
		}

	}
}
