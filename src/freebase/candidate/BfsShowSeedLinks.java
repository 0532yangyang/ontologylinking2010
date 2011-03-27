package freebase.candidate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

import freebase.match.Setting;


public class BfsShowSeedLinks {

	/**
	 * @param args
	 */
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
			DelimitedReader dr = new DelimitedReader(Setting.file_fbname_mid_zclid);
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
		HashMap<String,List<String>>fbrel_pathes = new HashMap<String,List<String>>();
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
						}else sb_ent.append("N/A->");
					}
					String arg1_str = entity_name.get(arg1);
					String arg2_str = entity_name.get(arg2);
					if(arg1_str == null)arg1_str = "";
					if(arg2_str == null)arg2_str = "";
					String entitypath = "";
					if(rel.contains("inverse")){
						entitypath = arg2_str+"->"+sb_ent.toString();
					}else{
						entitypath = arg1_str+"->"+sb_ent.toString();
					}
					//String entitypath = arg1_str+"->"+sb_ent.toString();
					entitypath = entitypath.substring(0, entitypath.length()-2);
					String sb_rel_id_str = sb_rel_id.toString();
					if(!fbrel_pathes.containsKey(sb_rel_id_str)){
						fbrel_pathes.put(sb_rel_id_str,new ArrayList<String>());
					}
					fbrel_pathes.get(sb_rel_id_str).add(rel+"::"+entitypath);
					dw.write(rel, sb_rel_id.toString(),sb_rel.toString(),entitypath);
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
			// top 3 of seedLinksGroupShow
			DelimitedReader dr = new DelimitedReader(dir + "/seedLinksShow.group");
			DelimitedWriter dw = new DelimitedWriter(dir + "/seedLinksShow.group.top3");
			String[] line;
			String last = "";
			int count = 0;
			while ((line = dr.read()) != null) {
				if (line[0].equals(last)) {
					if (count < 10) {
						toWrite(line,fbrel_pathes,dw);
						count++;
					}
				} else {
					last = line[0];
					toWrite(line,fbrel_pathes,dw);
					count = 1;
				}
			}
			dw.close();
			dr.close();
		}
	}
	
	public static void toWrite(String []line, HashMap<String,List<String>>dict, DelimitedWriter dw)throws Exception{
		String rel = line[1];
		List<String> pathes = dict.get(rel);
		line[0] = "* "+line[0];
		dw.write(line);
		if(pathes== null || pathes.size() == 0)return;
		String lastpair = "";
		int lastcount = 1;
		for(String p: pathes){
			String a[] = p.split("->");
			String toc = a[0]+"\t"+a[a.length-1];
			if(!toc.equals(lastpair)){
				dw.write("** ",p, lastcount);
			}else{
				lastcount++;
			}
			lastpair = toc;
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		showSeedLinks();
	}

}
