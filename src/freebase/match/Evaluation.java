package freebase.match;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

import multir.util.HashList;


public class Evaluation {

	/**
	 * Get the precision recall of my mapping Nell Relation >>> Freebase
	 * relation Coverage (Recall) = \frac{|NellR and FBR|}{|NellR|} Precision
	 * (Confusibility) = \frac{|NellR and FBR|}{|FBR|}
	 * 
	 * @throws IOException
	 * 
	 * */
	public static void PR_NellFBMap1() throws IOException {
		HashList<String> nell_relation = new HashList<String>();
		HashList<String> fb_relation = new HashList<String>();
		String[] fb_relation_str = new String[20000];
		String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp2";
		String file = dir + "/seedLinksshow";
		String output = dir + "/seedLinkds2freebaseshow_groupby";
		String output2 = dir + "/seedLinkds2freebaseshow_groupby_sort";
		{
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			while ((line = dr.read()) != null) {
				String nell = line[0];
				String fb = line[5];
				nell_relation.add(nell);
				fb_relation.add(fb);
				fb_relation_str[fb_relation.getId(fb)] = line[6];
			}
			dr.close();
		}
		// int[][] table = new int[nell_relation.size()][fb_relation.size()];
		HashMap<String, Set<String>> table_str = new HashMap<String, Set<String>>();
		{
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			while ((line = dr.read()) != null) {
				int nellid = nell_relation.getId(line[0]);
				int fbid = fb_relation.getId(line[5]);
				// nellName[nellid] = line[0];
				// fbName[fbid] = line[5];
				// table[nellid][fbid]++;
				String key = nellid + "\t" + fbid;
				if (!table_str.containsKey(key)) {
					table_str.put(key, new HashSet<String>());
				}
				table_str.get(nellid + "\t" + fbid).add(line[3] + "\t" + line[4]);
			}
			dr.close();
		}
		// print the table;
		DelimitedWriter dw = new DelimitedWriter(output);

		for (int i = 0; i < nell_relation.size(); i++) {
			for (int j = 0; j < fb_relation.size(); j++) {
				if (table_str.containsKey(i + "\t" + j)) {
					int cnt = table_str.get(i + "\t" + j).size();
					if (cnt < 2)
						continue;
					dw.write(nell_relation.getElement(i), fb_relation.getElement(j), cnt, fb_relation_str[j]);
					// System.out.println(nell_relation.getElement(i)+"\t"+fb_relation.getElement(j)+"\t"+table[i][j]);
				}
			}
		}
		dw.close();
		Sort.sort(output, output2, dir, new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				if (o1[0].compareTo(o2[0]) == 0) {
					return Integer.parseInt(o2[2]) - Integer.parseInt(o1[2]);
				} else {
					return o1[0].compareTo(o2[0]);
				}
			}

		});

	}

	public static void acquireOnly() throws IOException {
		DelimitedReader dr = new DelimitedReader(Setting.graph2);
		DelimitedWriter dw = new DelimitedWriter(Setting.graph4);
		String[] line;
		while ((line = dr.read()) != null) {
			int rel = Integer.parseInt(line[2]);
			if (rel == 252 || rel == 1639) {
				dw.write(line);
			}
		}
		dr.close();
		dw.close();
	}

	/** Very worthwhile experience */
	public static void getZclid2name() throws Exception {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String output = dir + "/zclid2name";

		DelimitedReader dr1 = new DelimitedReader(dir + "/freebase-names.sbmid");
		DelimitedReader dr2 = new DelimitedReader(dir + "/mid2zclid.sbmid");
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] line1;
		String[] line2 = dr2.read();

		while ((line1 = dr1.read()) != null) {
			String mid1 = line1[0];

			while (line2 != null) {
				String mid2 = line2[0];
				if (mid2.compareTo(mid1) == 0) {
					dw.write(line2[1], line1[1]);
					break;
				}
				// if(mid2.compareTo(mid1)>0)break;
				else
					line2 = dr2.read();
			}
		}

		dw.close();
		dr1.close();
		dr2.close();

		Sort.sort(output, output + ".sbid", dir, new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]);
			}

		});
	}

	public static void getZclid2name2() throws Exception {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String output = dir + "/zclid2name-text";

		DelimitedReader dr1 = new DelimitedReader(dir + "/freebase-names-text");
		DelimitedReader dr2 = new DelimitedReader(dir + "/mid2zclid.sbmid");
		DelimitedWriter dw = new DelimitedWriter(output);
		HashMap<String, Integer> mid2zclid = new HashMap<String, Integer>();
		{
			String[] line;
			while ((line = dr2.read()) != null) {
				mid2zclid.put(line[0], Integer.parseInt(line[1]));
			}
		}
		{
			String[] line;
			while ((line = dr1.read()) != null) {
				if (mid2zclid.get(line[0]) != null) {
					int zclid = mid2zclid.get(line[0]);
					dw.write(zclid, line[0], line[1]);
				}
			}
		}
		dw.close();
		dr1.close();
		dr2.close();
		//
		// Sort.sort(output, output + ".sbid", dir, new Comparator<String[]>() {
		//
		// @Override
		// public int compare(String[] o1, String[] o2) {
		// // TODO Auto-generated method stub
		// return Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]);
		// }
		//
		// });
	}

	public static void stat_newpairs() throws Exception {
		// String input =
		// "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellrel2fbpairs";
		DelimitedReader dr = new DelimitedReader(Setting.file_NellRel2FBPairs);
		String[] line;
		HashCount<String> hc = new HashCount<String>();
		while ((line = dr.read()) != null) {
			hc.add(line[0]);
		}
		dr.close();
		hc.printAll();
	}

	static HashMap<Integer, String> zclid2name_text = new HashMap<Integer, String>();
	static HashMap<String, Integer> nellrelations = new HashMap<String, Integer>();

	public static void get_nellrel2fbpairsmid() throws Exception {
		{
			System.out.println("Begin loading zclid 2 name");
			DelimitedReader dr = new DelimitedReader(Setting.file_mid2zclid);
			String[] line;
			while ((line = dr.read()) != null) {
				// int zclid = Integer.parseInt(line[0]);
				String mid = line[0];
				int zclid = Integer.parseInt(line[1]);
				zclid2name_text.put(zclid, mid);
			}
			dr.close();
		}
		{
			System.out.println("Convert NellRel2FbPairs");
			DelimitedReader dr = new DelimitedReader(Setting.file_NellRel2FBPairs);
			DelimitedWriter dw = new DelimitedWriter(Setting.file_NellRel2FBPairsMid);
			String[] line;
			while ((line = dr.read()) != null) {
				String nellrelStr = line[0];
				if (!nellrelations.containsKey(nellrelStr)) {
					nellrelations.put(nellrelStr, nellrelations.size());
				}
				int nellrelid = nellrelations.get(nellrelStr);
				int arg1zid = Integer.parseInt(line[3]);
				int arg2zid = Integer.parseInt(line[4]);
				try {
					String arg1tt = zclid2name_text.get(arg1zid);
					String arg2tt = zclid2name_text.get(arg2zid);
					dw.write(arg1tt, arg2tt, nellrelid, nellrelStr);
				} catch (Exception e) {
					System.err.println(arg1zid + "\t" + arg2zid);
				}
			}
			dw.close();
		}
	}

	static final int MAX = 20000;
	static final int NMAX = 40000000;
	static String[] relation_name = new String[MAX];
	static HashMap<Integer,String>entity_name = new HashMap<Integer,String>(30000000);
	//static String []entity_name = new String[NMAX];
	public static void loadEdges() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader("/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges");
			String[] line;
			while ((line = dr.read()) != null) {
				relation_name[Integer.parseInt(line[1])] = line[0];
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader("/projects/pardosa/s5/clzhang/ontologylink/fbname_mid_myid");
			String[] line;
			while ((line = dr.read()) != null) {
				//entity_name.put(Integer.parseInt(line[0]),line[2].replace("\\s","_"));
				int eid = Integer.parseInt(line[1]);
				String ename = line[2].replaceAll(" ","_");
				entity_name.put(eid, ename);
			}
			dr.close();
		}
	}

	public static void showSeedLinks() throws Exception {
		loadEdges();
		String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp2";
		HashMap<String, String> pair2rel = new HashMap<String, String>();
		HashSet<String> removeDuplicate = new HashSet<String>();
		HashCount<String> nellrel_fbrel = new HashCount<String>();
		{
			// load Nellseed2myid
			DelimitedReader dr = new DelimitedReader(dir + "/nellseed2zclid");
			String[] line;
			while ((line = dr.read()) != null) {
				pair2rel.put(line[0] + "\t" + line[1], line[4]);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(dir + "/seedLinks20110205");
			String outputtemp = dir + "/seedLinksShow.temp";
			String output = dir+"/seedLinksShow";
			DelimitedWriter dw = new DelimitedWriter(outputtemp);

			String[] line;
			while ((line = dr.read()) != null) {
				int arg1 = Integer.parseInt(line[1]), arg2 = Integer.parseInt(line[2]);
				String rel = line[0];// .replace("inverse_", "");
				if (rel == null) {
					System.out.println(arg1 + "\t" + arg2);
				}
				String[] s = line[3].trim().split(":| ");
				List<Integer> fbconjRel = new ArrayList<Integer>();
				List<Integer> fbconjEnt = new ArrayList<Integer>();
				HashSet<Integer> fbconjRel_set = new HashSet<Integer>();
				boolean legal = true;
				for (int i = 0; i < s.length; i = i + 2) {
					//middle Entity : relation
					int m_ent = Integer.parseInt(s[i]);
					int m_rel = Integer.parseInt(s[i+1]);
					if (m_rel == 0)
						continue;
					if (fbconjRel_set.contains(Math.abs(m_rel))) {
						legal = false;
					}
					fbconjRel_set.add(Math.abs(m_rel));
					fbconjRel.add(m_rel);
					fbconjEnt.add(m_ent);
				}
				if (legal) {
					StringBuilder sb_rel = new StringBuilder();
					StringBuilder sb_rel_id = new StringBuilder();
					StringBuilder sb_ent = new StringBuilder();
					for (int x : fbconjRel) {
						String r = relation_name[x];
						sb_rel.append(r + "|");
						sb_rel_id.append(x+",");
					}
					
					for(int x: fbconjEnt){
						String e = entity_name.get(x);
						if(e!=null){
							sb_ent.append(e+"->");
						}else sb_ent.append("NULL|");
					}
					String arg1_str = entity_name.get(arg1);
					String arg2_str = entity_name.get(arg2);
					if(arg1_str == null)arg1_str = "";
					if(arg2_str == null)arg2_str = "";
					dw.write(rel, sb_rel_id.toString(),sb_rel.toString(),arg1_str+"->"+sb_ent.toString());
					removeDuplicate.add(rel + "\t" + arg1 + "\t" + arg2 + "\t" + sb_rel_id.toString()+"\t"+sb_rel.toString());
				}
			}
			dr.close();
			dw.close();
			Sort.sort(outputtemp,output,dir, new Comparator<String[]>() {
				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					String a = o1[0]+"\t"+o1[1];
					String b = o2[0]+"\t"+o2[1];
					return a.compareTo(b);
				}
			});
		}
		{
			String output = dir + "/seedLinksShow.group";
			String outputtemp = output + ".temp";
			DelimitedWriter dw2 = new DelimitedWriter(outputtemp);
			for (String a : removeDuplicate) {
				String[] s = a.split("\t");
				nellrel_fbrel.add(s[0] + "\t" + s[3]+"\t"+s[4]);
			}
			List<String> matches = new ArrayList<String>();
			List<Integer> counts = new ArrayList<Integer>();
			nellrel_fbrel.getAll(matches, counts);
			for (int i = 0; i < matches.size(); i++) {
				String[] ab = matches.get(i).split("\t");
				if (counts.get(i) > 1) {
					dw2.write(ab[0], ab[1],ab[2], counts.get(i));
				}
			}
			dw2.close();
			Sort.sort(outputtemp, output, dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					int x1 = o1[0].compareTo(o2[0]);
					if (x1 != 0)
						return x1;
					else {
						return Integer.parseInt(o2[3]) - Integer.parseInt(o1[3]);
					}
				}
			});
			(new File(outputtemp)).delete();
		}
		{
			// top 10 of seedLinksGroupShow
			DelimitedReader dr = new DelimitedReader(dir + "/seedLinksShow.group");
			DelimitedWriter dw = new DelimitedWriter(dir + "/seedLinksShow.group.top3");
			String[] line;
			String last = "";
			int count = 0;
			while ((line = dr.read()) != null) {
				if (line[0].equals(last)) {
					if (count < 3) {
						line[0] = "*"+line[0];
						dw.write(line);
						count++;
					}
				} else {
					last = line[0];
					line[0] = "*"+line[0];
					dw.write(line);
					count = 1;
				}
			}
			dw.close();
			dr.close();
		}
	}
	
	public static void getFreebaseName()throws Exception{
		DelimitedReader dr = new DelimitedReader("/projects/pardosa/s5/clzhang/ontologylink/freebase-datadump-quadruples.tsv");
		DelimitedWriter dw = new DelimitedWriter("/projects/pardosa/s5/clzhang/ontologylink/fbname");
		String []line;
		while((line = dr.read())!=null){
			if(line.length!=4)continue;
			if(line[1].equals("/type/object/name") && line[2].equals("/lang/en")){
				dw.write(line[0],line[3]);
			}
		}
		dr.close();
		dw.close();
	}
	
	
	public static void main(String[] args) {
		try {
			System.setErr(new PrintStream(Setting.file_debug));
			// PR_NellFBMap1();
			// acquireOnly();
			// getZclid2name2();
			// stat_newpairs();
			// get_nellrel2fbpairsmid();
			/**This is a very important class*/
			//showSeedLinks();
			//getFreebaseName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		
	}
}

