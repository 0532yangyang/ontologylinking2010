package freebase.jointmatch4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;
import javatools.mydb.StringTable;

import freebase.candidate.*;

public class S4_variable_relationmatch {

	/**
	 * @param args
	 */
	static Node[] nodes = new Node[Main.MaxNodeId];

	static int MaxNodeSize = 0;

	static void loadgraph(String file) {
		try {
			D.p("load graph!");
			DelimitedReader dr = new DelimitedReader(file);
			String[] line;
			int ln = 0;
			while ((line = dr.read()) != null) {
				if (ln++ % 1000000 == 0)
					System.out.print(ln + "\r");
				// if(ln>1000000)break;
				int arg1 = Integer.parseInt(line[0]);
				int arg2 = Integer.parseInt(line[1]);
				int rel = Integer.parseInt(line[2]);
				if (nodes[arg1] == null)
					nodes[arg1] = new Node(arg1);
				if (nodes[arg2] == null)
					nodes[arg2] = new Node(arg2);
				nodes[arg1].addEdge(arg2, rel);
				MaxNodeSize = Math.max(MaxNodeSize, arg1);
			}
			dr.close();
			MaxNodeSize++;
		} catch (Exception e) {
		}
	}

	static void printNodes() {
		for (int i = 0; i < MaxNodeSize; i++) {
			if (nodes[i] != null)
				System.out.println(nodes[i].toString());
		}
	}

	static List<int[]> bfs(int arg1, int arg2) {
		List<int[]> path = new ArrayList<int[]>();
		List<OneBf> buffer = new ArrayList<OneBf>();

		HashSet<String> occurNodes = new HashSet<String>();
		if (nodes[arg1] == null || nodes[arg2] == null) {
			return null;
		}
		buffer.add(new OneBf(arg1, 0, 0, 0));
		// occurNodes.add(arg1);
		int cur = 0;
		while (cur < buffer.size() && buffer.get(cur).depth < Main.MaxDep) {
			// System.out.println("cur id: "+cur+" buffer size: "+buffer.size());
			OneBf ob = buffer.get(cur);
			if (ob.nid == 14876199 || ob.nid == 712590 || ob.nid == 418401) {
				// System.out.println("Put correct guy here\t" + ob.nid);
			}
			Node node = nodes[ob.nid];
			if (node != null && node.getLink() != null) {
				List<int[]> links = node.getLink();
				for (int[] ab : links) {
					int lnid = ab[0];
					int lrel = ab[1];
					int depPlus = ob.depth + 1;
					// if this node has already been seen before
					if (lnid == arg1 || occurNodes.contains(lnid + "\t" + lrel)) {
						continue;
					}
					if (lnid == arg2) {
						// match success!!!
						int[] pathnew = new int[Main.MaxDep * 2 + 2];
						HashSet<Integer> occEdge = new HashSet<Integer>();
						boolean legal = true;
						pathnew[depPlus * 2] = lnid;
						pathnew[depPlus * 2 + 1] = lrel;
						OneBf obtmp = ob;
						for (int d = ob.depth; d >= 0; d--) {
							pathnew[d * 2] = obtmp.nid;
							if (occEdge.contains(obtmp.edge)) {
								legal = false;
							}
							pathnew[d * 2 + 1] = obtmp.edge;
							occEdge.add(obtmp.edge);
							obtmp = buffer.get(obtmp.prev);
						}
						if (legal) {
							path.add(pathnew);
						}
					} else {
						// System.out.println(buffer.size() + "\t" + ob.nid +
						// "\t" + lnid + "\t" + lrel + "\t" + depPlus);
						OneBf newob = new OneBf(lnid, lrel, cur, depPlus);
						buffer.add(newob);
						occurNodes.add(lnid + "\t" + lrel);
					}
				}
			}
			cur++;
		}
		return path;
	}

	// static List<List<int[]>>getPaths(List<OneBf2>buffer, int tail){
	// List<List<int[]>>res = new ArrayList<List<int[]>>();
	// OneBf2 temp = buffer.get(tail);
	// if(temp.prevs.size() == 0){
	// res.add(new ArrayList<int[]>());
	// }
	// for(int i=0;i<temp.prevs.size();i++){
	// List<List<int []>>tmpres = getPaths(buffer,temp.prevs.get(i));
	// for(List<int []>x: tmpres){
	// x.add(new int[]{temp.nid,temp.edges.get(i)});
	// res.add(x);
	// }
	// }
	// return res;
	// }

	public static String[] pathToString(List<int[]> path) {
		String[] res = new String[path.size()];
		for (int k = 0; k < path.size(); k++) {
			int[] p = path.get(k);
			StringBuilder sb = new StringBuilder();
			// StringBuilder sbpath = new StringBuilder();
			for (int i = 0; i < p.length / 2; i++) {
				sb.append(p[i * 2] + ":" + p[i * 2 + 1] + " ");
			}
			res[k] = sb.toString();
		}
		return res;
	}

	public static String[] pathToString3(List<int[]> path) {
		String[] res = new String[path.size()];
		for (int k = 0; k < path.size(); k++) {
			int[] p = path.get(k);
			StringBuilder sb = new StringBuilder();
			// StringBuilder sbpath = new StringBuilder();
			for (int i = 0; i < p.length / 2; i++) {
				if (p[i * 2 + 1] != 0) {
					sb.append(p[i * 2 + 1] + ",");
				}
				res[k] = sb.toString();
			}
		}
		return res;
	}

	public static List<String> pathToString2(List<int[]> path) {
		List<String> res = new ArrayList<String>();
		HashSet<String> reshash = new HashSet<String>();
		for (int k = 0; k < path.size(); k++) {
			int[] p = path.get(k);
			StringBuilder sbpath = new StringBuilder();
			for (int i = 0; i < p.length / 2; i++) {
				sbpath.append(relation_name[p[i * 2 + 1]] + "|");
			}
			reshash.add(sbpath.toString());
		}
		res.addAll(reshash);
		return res;
	}

	public static String pathToString(int arg1, int arg2, List<int[]> path) {
		StringBuilder sb = new StringBuilder();
		for (int[] p : path) {
			sb.append(arg1 + "\t" + arg2 + "\t");
			for (int i = 0; i < p.length / 2; i++) {
				sb.append(p[i * 2] + ":" + p[i * 2 + 1] + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static void calc_subprint(List<int[]> path, DelimitedWriter dw1, int arg1, int arg2, String relation)
			throws IOException {
		if (path != null && path.size() > 0) {
			String[] res = pathToString(path);
			// List<String> show = pathToString2(path);
			// String[] pathInId = pathToString3(path);
			for (String a : res) {
				dw1.write(relation, arg1, arg2, a);
				dw1.flush();
			}
			// for (int i = 0; i < show.size(); i++) {
			// dw2.write(line[4], arg1, arg2, line[2], line[3], pathInId[i],
			// show.get(i));
			// dw2.flush();
			// }
		}
	}

	/** Every nellstring, instead of using predict, using fbsearch result */
	public static void getPath_fbsearchresultonly() {
		try {
			D.p("get path!");

			HashMap<String, String> nellstr2mid = new HashMap<String, String>();
			// graph node id
			HashMap<String, Integer> mid2gnid = new HashMap<String, Integer>();
			HashSet<Integer> level0Gnid = new HashSet<Integer>();
			HashSet<Integer> level1Gnid = new HashSet<Integer>();
			List<String[]> nellstrMidPred = (new DelimitedReader(
					Main.file_enid_mid_wid_argname_otherarg_relation_label_top1)).readAll();
			for (String[] a : nellstrMidPred) {
				nellstr2mid.put(a[3], a[1]);
			}
			D.p(nellstr2mid.size());
			{
				List<String> usedMid = new ArrayList<String>();
				for (NellRelation nr : Main.no.nellRelationList) {
					for (String[] s : nr.seedInstances) {
						String arg1 = s[0];
						String arg2 = s[1];
						String mid1 = nellstr2mid.get(arg1);
						String mid2 = nellstr2mid.get(arg2);
						// D.p(mid1,mid2);
						if (mid1 != null) {
							usedMid.add(mid1);
						} else {
							System.err.println("Missing\t" + arg1);
						}
						if (mid2 != null) {
							usedMid.add(mid2);
						} else {
							System.err.println("Missing\t" + arg2);
						}
					}
				}
				Collections.sort(usedMid);
				// get mid2gnid
				DelimitedReader drnode = new DelimitedReader(Main.file_fbnode_sbmid);
				String[] l = drnode.read();
				for (String m : usedMid) {
					if (l == null)
						break;
					while (l[0].compareTo(m) < 0 && (l = drnode.read()) != null) {

					}
					if (l[0].equals(m)) {
						int gnid = Integer.parseInt(l[1]);
						mid2gnid.put(m, gnid);
						level0Gnid.add(gnid);
					}
				}
			}
			{
				/** subset the fbGraph; */
				DelimitedReader dr = new DelimitedReader(Main.file_fbgraph_clean);
				String[] line;
				while ((line = dr.read()) != null) {
					int arg1 = Integer.parseInt(line[0]);
					int arg2 = Integer.parseInt(line[1]);
					if (level0Gnid.contains(arg1) || level0Gnid.contains(arg2)) {
						level1Gnid.add(arg1);
						level1Gnid.add(arg2);
					}
				}
				System.out.println("Total Number of level two seeds\t" + level1Gnid.size());
				dr = new DelimitedReader(Main.file_fbgraph_clean);
				DelimitedWriter dw = new DelimitedWriter(Main.file_fbgraph_subset);
				while ((line = dr.read()) != null) {
					int arg1 = Integer.parseInt(line[0]);
					int arg2 = Integer.parseInt(line[1]);
					if (level1Gnid.contains(arg1) || level1Gnid.contains(arg2)) {
						dw.write(line);
					}
				}
				dw.close();
			}
			loadgraph(Main.file_fbgraph_subset);
			{
				// do the path finding now!!!
				D.p("Now getting the pathes");
				DelimitedWriter dw = new DelimitedWriter(Main.file_seedbfspath_raw);
				DelimitedWriter dw0 = new DelimitedWriter(Main.file_arg2gnid);
				int good = 0, bad = 0;
				for (NellRelation nr : Main.no.nellRelationList) {
					for (String[] s : nr.seedInstances) {
						String arg1 = s[0];
						String arg2 = s[1];
						String mid1 = nellstr2mid.get(arg1);
						String mid2 = nellstr2mid.get(arg2);

						if (mid1 != null && mid2 != null) {
							int gnid1 = mid2gnid.get(mid1);
							int gnid2 = mid2gnid.get(mid2);
							dw0.write(arg1, arg2, mid1, mid2, gnid1, gnid2);
							List<int[]> pathA2B = bfs(gnid1, gnid2);
							List<int[]> pathB2A = bfs(gnid2, gnid1);
							String relation = nr.relation_name;
							calc_subprint(pathA2B, dw, gnid1, gnid2, relation);
							calc_subprint(pathB2A, dw, gnid1, gnid2, relation + "_inverse");
							good++;
						} else {
							bad++;
						}
					}
				}
				dw0.close();
				dw.close();
				D.p("\n");
				D.p("good", good, "bad", bad);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void filterArg2NotAppearing() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_seedbfspath_filteredby_wikiarticle);
		HashSet<String> pairs = new HashSet<String>();

		List<String[]> tocheck = new ArrayList<String[]>();
		HashMap<Integer, Integer> map_gnid2wid = new HashMap<Integer, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				int gnid = Integer.parseInt(l[0]);
				int wid = Integer.parseInt(l[2]);
				map_gnid2wid.put(gnid, wid);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				int a = Integer.parseInt(l[1]);
				int b = Integer.parseInt(l[2]);
				if (map_gnid2wid.containsKey(a) && map_gnid2wid.containsKey(b)) {
					int awid = map_gnid2wid.get(a);
					int bwid = map_gnid2wid.get(b);
					pairs.add(awid + "::" + bwid);
				}
			}
			dr.close();
			for (String p : pairs) {
				String[] ab = p.split("::");
				tocheck.add(ab);
			}
		}
		HashSet<String> good = new HashSet<String>();
		{
			String tempfile = Main.dir + "/temp_patternize_candidatepairs";
			Pattern.widpair2feature(tocheck, Main.file_wp_stanford_subset, tempfile);
			List<String[]> all = (new DelimitedReader(tempfile)).readAll();
			for (String[] l : all) {
				good.add(l[0] + "::" + l[1]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				int a = Integer.parseInt(l[1]);
				int b = Integer.parseInt(l[2]);
				if (map_gnid2wid.containsKey(a) && map_gnid2wid.containsKey(b)) {
					int awid = map_gnid2wid.get(a);
					int bwid = map_gnid2wid.get(b);
					if (good.contains(awid + "::" + bwid) || good.contains(bwid + "::" + awid)) {
						dw.write(l);
					}
				}
			}
		}
		dw.close();
	}

	public static void showPath() throws Exception {
		step0_loadedge();
		//HashSet<String> removeDuplicate = new HashSet<String>();

		HashMap<String, List<String>> fbrel_pathes = new HashMap<String, List<String>>();
		HashMap<Integer, String> entity_name = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[3].trim().length() > 0) {
					String name = l[3].trim().split(" ")[0];
					entity_name.put(Integer.parseInt(l[0]), name);
				}
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_filteredby_wikiarticle);
			DelimitedWriter dw = new DelimitedWriter(Main.file_seedbfspath_show + ".1");
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
					// middle Entity : relation
					int m_ent = Integer.parseInt(s[i]);
					int m_rel = Integer.parseInt(s[i + 1]);
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
						sb_rel_id.append(x + ",");
					}

					for (int x : fbconjEnt) {
						String e = entity_name.get(x);
						if (e != null) {
							sb_ent.append(e + "->");
						} else
							sb_ent.append("N/A->");
					}
					String arg1_str = entity_name.get(arg1);
					String arg2_str = entity_name.get(arg2);
					if (arg1_str == null)
						arg1_str = "";
					if (arg2_str == null)
						arg2_str = "";
					String entitypath = "";
					if (rel.contains("inverse")) {
						entitypath = arg2_str + "->" + sb_ent.toString();
					} else {
						entitypath = arg1_str + "->" + sb_ent.toString();
					}
					// String entitypath = arg1_str+"->"+sb_ent.toString();
					entitypath = entitypath.substring(0, entitypath.length() - 2);
					String sb_rel_id_str = sb_rel_id.toString();
					if (!fbrel_pathes.containsKey(sb_rel_id_str)) {
						fbrel_pathes.put(sb_rel_id_str, new ArrayList<String>());
					}
					fbrel_pathes.get(sb_rel_id_str).add(rel + "::" + entitypath);
					dw.write(rel, sb_rel_id.toString(), sb_rel.toString(), arg1 + "::" + arg2, entitypath);
					//					removeDuplicate.add(rel + "\t" + arg1 + "\t" + arg2 + "\t" + sb_rel_id.toString() + "\t"
					//							+ sb_rel.toString());
				}
			}
			dr.close();
			dw.close();
			//			Sort.sort(Main.file_seedbfspath_show + ".temp", Main.file_seedbfspath_show + ".temp2", Main.dir,
			//					new Comparator<String[]>() {
			//						@Override
			//						public int compare(String[] o1, String[] o2) {
			//							// TODO Auto-generated method stub
			//							String a = o1[0] + "\t" + o1[1];
			//							String b = o2[0] + "\t" + o2[1];
			//							return a.compareTo(b);
			//						}
			//					});
		}
		{
			// String output = dir + "/seedLinksShow.group";
			// String outputtemp = output + ".temp";
			HashCount<String> nellrel_fbrel = new HashCount<String>();

			DelimitedWriter dw2 = new DelimitedWriter(Main.file_seedbfspath_show + ".2");
			{
				DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_show + ".1");
				String[] l;
				HashSet<String> uniq = new HashSet<String>();
				while ((l = dr.read()) != null) {
					uniq.add(l[0] + "\t" + l[1] + "\t" + l[2] + "\t" + l[3]);
				}
				dr.close();
				for (String a : uniq) {
					String[] s = a.split("\t");
					nellrel_fbrel.add(s[0] + "\t" + s[1] + "\t" + s[2]);
				}
			}
			List<String> matches = new ArrayList<String>();
			List<Integer> counts = new ArrayList<Integer>();
			nellrel_fbrel.getAll(matches, counts);
			for (int i = 0; i < matches.size(); i++) {
				String[] ab = matches.get(i).split("\t");
				if (counts.get(i) > 1) {
					dw2.write(ab[0], ab[1], ab[2], counts.get(i));
				}
			}
			dw2.close();
			Sort.sort(Main.file_seedbfspath_show + ".2", Main.file_seedbfspath_show_group, Main.dir,
					new Comparator<String[]>() {

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
			// (new File(outputtemp)).delete();
		}
		{
			// top 3 of seedLinksGroupShow
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_show_group);
			DelimitedWriter dw = new DelimitedWriter(Main.file_seedbfspath_show_group + ".sample");
			String[] line;
			String last = "";
			int count = 0;
			while ((line = dr.read()) != null) {
				if (line[0].equals(last)) {
					if (count < 10) {
						toWrite(line, fbrel_pathes, dw);
						count++;
					}
				} else {
					last = line[0];
					toWrite(line, fbrel_pathes, dw);
					count = 1;
				}
			}
			dw.close();
			dr.close();
		}
	}

	public static void toWrite(String[] line, HashMap<String, List<String>> dict, DelimitedWriter dw) throws Exception {
		String rel = line[1];
		List<String> pathes = dict.get(rel);
		line[0] = "* " + line[0];
		dw.write(line);
		if (pathes == null || pathes.size() == 0)
			return;
		String lastpair = "";
		int lastcount = 1;
		for (String p : pathes) {
			String a[] = p.split("->");
			String toc = a[0] + "\t" + a[a.length - 1];
			if (!toc.equals(lastpair)) {
				dw.write("** ", p, lastcount);
			} else {
				lastcount++;
			}
			lastpair = toc;
		}
	}

	public static void step0_loadedge() {
		try {
			D.p("load edge!");
			DelimitedReader dr = new DelimitedReader(Main.file_fbedge);
			String[] line;
			while ((line = dr.read()) != null) {
				relation_name[Integer.parseInt(line[1])] = line[0];
			}
			dr.close();
		} catch (Exception e) {

		}
	}

	static void getCandidateRelations2Instance() throws Exception {
		List<String[]> mappings = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_show_group);
			String[] l;
			while ((l = dr.read()) != null) {
				mappings.add(new String[] { l[0], l[2] });
			}
			dr.close();
		}
		{
			QueryFBGraph qfb = new QueryFBGraph();
			qfb.getNewEntitiesWithOntologyMapping(mappings, Main.file_sql2instance);
		}
	}

	static final int MAX = 20000;
	static String[] relation_name = new String[MAX];

	public static void main(String[] args) throws Exception {

		/** with fbsearch result's top 1 as best mid, consider the subgraph */
		getPath_fbsearchresultonly();//this function cost a lot of time
		filterArg2NotAppearing();
		showPath();

		/**Query the fb database to get all instances satisfying SQL query in
		 * seedbfspathes_show_group*/
		getCandidateRelations2Instance();
	}

}
