package freebase.jointmatch8;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import freebase.candidate.Node;
import freebase.candidate.OneBf;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

public class S6 {
	static Node[] nodes = new Node[Main.MaxNodeId];
	static int MaxNodeSize = 0;
	static final int MAX = 20000;
	static String[] relation_name = new String[MAX];

	/**Iterative update string alignment and relation matching
	 * @throws Exception */
	public static void main(String[] args) throws Exception {
		String file_raw = Main.file_fbsearch2;
		String file_init_nellstr2mid = Main.dir + "/iter1/nellstr2mid";
		String file_1_nellstr2mid = Main.dir + "/iter2/nellstr2mid";
		String file_2_nellstr2mid = Main.dir + "/iter2/nellstr2mid.final";

		(new File(Main.dir + "/iter1")).mkdir();
		(new File(Main.dir + "/iter2")).mkdir();
		getNellStr2MidWithRelationMatchingResult(null, file_raw, file_init_nellstr2mid);
		iter(Main.dir + "/iter1", file_init_nellstr2mid, file_1_nellstr2mid);
		//iter(Main.dir + "/iter2", file_1_nellstr2mid, file_2_nellstr2mid);
	}

	public static void iter(String dir, String file_oldnellstr2mid, String file_newnellstr2mid) throws Exception {

		String file_raw = Main.file_fbsearch2;
		String file_gold = Main.dir + "/mycreateontology/manual_entity_mid";
		String file_wikilink_subset = Main.file_wklinksub;
		String file_wksensubset = Main.file_wksensubset;

		//String file_nellstr2Mid = dir + "/nellstr2mid";
		String file_seedbfspathraw = dir + "/seedbfspath.raw";
		String file_seedbfspathresult = dir + "/seedbfspath.result";
		String file_sql2instance_union = dir + "/sql2instance_union";
		String file_sql2instance_union_explosive = dir + "/sql2instance_union_explosive";
		String file_inference_relation = dir + "/inference_relation";
		String file_afterrelmatch_typeconstrain = dir + "/afterrelmatch_typeconstrain";
		String file_afterrelmatch_relmatchres = dir + "/afterrelmatch_relmatchres";
		String file_formanuallabelnewinstance = dir + "/for_manual_label_new_instances";
		String file_weight_similarity_nellrel_fbrel = dir + "/weight_similarity_nellrel_fbrel";
		String file_weight_sharepair_nellrel_fbrel = dir + "/weight_sharepair_nellrel_fbrel";
		String file_weight_explosive_nellrel_fbrel = dir + "/weight_explosive_nellrel_fbrel";
		String file_weight_seednegativepair_nellrel_fbrel = dir + "/weight_seednegativepair_nellrel_fbrel";
		String file_weight_wikilink_nellrel_fbrel = dir + "/weight_wikilink_nellrel_fbrel";
		String file_weight_joinlen_nellrel_fbrel = dir + "/weight_joinlen_nellrel_fbrel";

		String file_weight_fbrank = dir + "/weight_str_fbrank";
		String file_weight_typeconstrain = dir + "/weight_str_typeconstrain";
		String file_weight_shareword = dir + "/weight_str_shareword";
		String file_weight_appearinlink = dir + "/weight_str_appearinlink";
		String file_weight_appearintext = dir + "/weight_str_appearintext";
		String file_inference_nellstr = dir + "/inference_str";
		//String file_newnellstr2mid = dir + "/nellstr2mid.new";
		String file_difference = dir + "/nellstr2mid.difference";
		String file_eval_alignment = dir + "/eval_stralignment";

		/**Find Candidate views*/

		getPath(file_oldnellstr2mid, file_seedbfspathraw);
		showPath(file_seedbfspathraw, file_seedbfspathresult);

		/**Find instances satisfies the views*/
		FreebaseRelation fbr = new FreebaseRelation(file_seedbfspathresult);
		fbr.selfTest(file_seedbfspathraw + ".2");
		QueryFBGraph qfb = new QueryFBGraph(true, file_sql2instance_union_explosive);
		qfb.getNewEntitiesWithUnionRelations(fbr.filteredUnionRelations, file_sql2instance_union);
		//
		/**Ontology matching*/
		step1_name_similarity(fbr, file_weight_similarity_nellrel_fbrel);
		step2_shareInstance_similarity(fbr, file_weight_sharepair_nellrel_fbrel);
		step3_explosivejoin(fbr, file_sql2instance_union_explosive, file_weight_explosive_nellrel_fbrel);
		//step4_negativeseed(file_oldnellstr2mid, file_sql2instance_union, file_weight_seednegativepair_nellrel_fbrel);
		step5_weight_wikilink(file_sql2instance_union, file_weight_wikilink_nellrel_fbrel);
		step6_joinlen(fbr, file_weight_joinlen_nellrel_fbrel);
		stepX_inference(fbr, file_weight_similarity_nellrel_fbrel, //
				file_weight_sharepair_nellrel_fbrel,//
				file_weight_explosive_nellrel_fbrel,// 
				file_weight_seednegativepair_nellrel_fbrel,//
				file_weight_wikilink_nellrel_fbrel,// 
				file_weight_joinlen_nellrel_fbrel,//
				file_inference_relation);
		stepX_setGoodUnionRelation(fbr, file_inference_relation, 3, file_afterrelmatch_relmatchres);
		step7_sampleInstancesForLabel(file_sql2instance_union, file_afterrelmatch_relmatchres, dir,
				file_formanuallabelnewinstance);

		/**Entity matching*/
		stepX_getTypeConstrain(fbr, file_afterrelmatch_typeconstrain);
		a1_fbrank(file_raw, file_weight_fbrank);
		a2_typeconstrain(file_raw, file_afterrelmatch_typeconstrain, file_weight_typeconstrain);
		a3_shareword(file_raw, file_weight_shareword);
		a4_otherarg_appearinlink(file_raw, file_wikilink_subset, file_weight_appearinlink);
		a5_otherarg_appearintext(file_raw, file_wksensubset, file_weight_appearintext);
		b1_inference(file_raw, new String[] { file_weight_fbrank, file_weight_typeconstrain, file_weight_shareword,
				file_weight_appearinlink, file_weight_appearintext }, new NellstrMidScoreIndex[] {
				NellstrMidScoreIndex.FBRANK, NellstrMidScoreIndex.TYPECONSTRIAN, NellstrMidScoreIndex.SHAREWORD,
				NellstrMidScoreIndex.WIKILINK, NellstrMidScoreIndex.WIKITEXT }, file_inference_nellstr,
				file_newnellstr2mid);
		b2_setGood(file_oldnellstr2mid, file_inference_nellstr, file_gold, file_difference);
				string_alignment_accuracy(file_gold, file_newnellstr2mid,//the string alignment output of this iter
						file_eval_alignment);

	}

	public static void getNellStr2MidWithRelationMatchingResult(FreebaseRelation fbr, String fbsearchraw2,
			String file_Nellstr2Mid) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_Nellstr2Mid);
		DelimitedReader dr = new DelimitedReader(fbsearchraw2);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String nellstr = b.get(0)[0];
			String mid = "";
			if (fbr == null) {
				mid = b.get(0)[1];
			}
			if (mid.length() > 0) {
				dw.write(b.get(0));
			}
		}
		dw.close();
	}

	public static void getPath(String file_nellstr2Mid, String output_pathraw) {
		try {
			D.p("get path!");

			HashMap<String, String> nellstr2mid = new HashMap<String, String>();
			// graph node id
			HashMap<String, Integer> mid2gnid = new HashMap<String, Integer>();
			HashSet<Integer> level0Gnid = new HashSet<Integer>();
			HashSet<Integer> level1Gnid = new HashSet<Integer>();
			List<String[]> listNellstr2Mid = (new DelimitedReader(file_nellstr2Mid)).readAll();
			for (String[] a : listNellstr2Mid) {
				nellstr2mid.put(a[0], a[1]);
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
				DelimitedWriter dw = new DelimitedWriter(output_pathraw);
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
				dw.close();
				D.p("\n");
				D.p("good", good, "bad", bad);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	public static void showPath(String file_seedbfspathraw, String file_seedbfspathresult) throws Exception {
		step0_loadedge();
		HashMap<Integer, String> gnid2type = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type);
			String[] l;
			while ((l = dr.read()) != null) {
				try {
					int gnid = Integer.parseInt(l[0]);
					String type = l[5];
					gnid2type.put(gnid, type);
				} catch (Exception e) {
				}
			}
			dr.close();
		}
		HashSet<String> removeDuplicate = new HashSet<String>();
		HashCount<String> nellrel_fbrel = new HashCount<String>();
		HashMap<String, List<String>> fbrel_pathes = new HashMap<String, List<String>>();
		HashMap<Integer, String> entity_name = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[3].trim().length() > 0 && l[0].length() > 0) {
					String name = l[4].trim().split(" ")[0];
					entity_name.put(Integer.parseInt(l[0]), name);
				}
			}
			dr.close();
		}
		List<String[]> towrite = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(file_seedbfspathraw);
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
					towrite.add(new String[] { rel, arg1 + "", arg2 + "", sb_rel.toString(), entitypath });
					//dw.write(rel, arg1, arg2, sb_rel.toString(), entitypath);
					removeDuplicate.add(rel + "\t" + arg1 + "\t" + arg2 + "\t" + sb_rel_id.toString() + "\t"
							+ sb_rel.toString());
				}
			}

			{
				DelimitedWriter dw = new DelimitedWriter(file_seedbfspathraw + ".2");
				StringTable.sortUniq(towrite);
				List<String[]> table2 = new ArrayList<String[]>();
				for (String[] w : towrite) {
					dw.write(w);
					table2.add(new String[] { w[0], w[3], w[1], w[2] });
				}
				dr.close();
				dw.close();
				StringTable.sortUniq(table2);
				DelimitedWriter dw2 = new DelimitedWriter(file_seedbfspathresult);
				for (String[] w : table2) {
					int gnid1 = Integer.parseInt(w[2]);
					int gnid2 = Integer.parseInt(w[3]);
					String type1 = gnid2type.containsKey(gnid1) ? gnid2type.get(gnid1) : "NA";
					String type2 = gnid2type.containsKey(gnid2) ? gnid2type.get(gnid2) : "NA";
					dw2.write(w[0], w[1], type1, type2, gnid1, gnid2);
				}
				dw2.close();
			}
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

	public static void step1_name_similarity(FreebaseRelation fbr, String output) throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
		DelimitedWriter dw = new DelimitedWriter(output);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			String fbname = ur.unionRelationName;
			int urid = ur.unionRelId;
			List<String> t1 = StringUtil.tokenize(nrname.replace("_inverse", ""), new char[] { '_' });
			List<String> t2 = StringUtil.tokenize(fbname, new char[] { ' ', '_', '/', '|' });
			int share = StringUtil.numOfShareWords(t1, t2, new boolean[] { true, true, true });
			// double score = share * 1.0 / t1.size();
			dw.write(nrname, urid + "", share, t1.size(), t2.size(), fbname);
		}
		dw.close();
	}

	public static void step2_shareInstance_similarity(FreebaseRelation fbr, String output) throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
		DelimitedWriter dw = new DelimitedWriter(output);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			String fbname = ur.unionRelationName;
			int urid = ur.unionRelId;
			// double score = share * 1.0 / t1.size();
			dw.write(nrname, urid + "", ur.instances.size(), fbname);
		}
		dw.close();
	}

	public static void step3_explosivejoin(FreebaseRelation fbr, String file_explosive, String output)
			throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
		HashSet<String> explosive = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(file_explosive);
			String[] l;
			while ((l = dr.read()) != null) {
				explosive.add(l[2]);
			}
			dr.close();
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			//String fbname = ur.unionRelationName;
			String[] singleNames = ur.getSingleRelNames();
			int urid = ur.unionRelId;
			boolean isExplosive = false;
			for (String n : singleNames) {
				if (explosive.contains(n)) {
					isExplosive = true;
				}
			}
			if (isExplosive) {
				dw.write(nrname, urid + "", ur.unionRelationName);
			}
		}
		dw.close();
	}

	public static void step4_negativeseed(String file_nellstrmap, String file_sql2instance_union, String output)
			throws IOException {
		HashMap<String, Integer> nellstr2wid = new HashMap<String, Integer>();
		{
			DelimitedReader dr = new DelimitedReader(file_nellstrmap);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellstr = l[0];
				int wid = Integer.parseInt(l[2]);
				if (!nellstr2wid.containsKey(nellstr)) {
					nellstr2wid.put(nellstr, wid);
				}
			}
		}
		HashMap<String, List<String>> seed_negative = new HashMap<String, List<String>>();
		for (NellRelation nr : Main.no.nellRelationList) {
			for (String[] a : nr.known_negatives) {
				if (nellstr2wid.containsKey(a[0]) && nellstr2wid.containsKey(a[1])) {
					int wid1 = nellstr2wid.get(a[0]);
					int wid2 = nellstr2wid.get(a[1]);
					String key = wid1 + "\t" + wid2;
					if (!seed_negative.containsKey(key)) {
						seed_negative.put(key, new ArrayList<String>());
					}

					seed_negative.get(key).add(nr.relation_name);
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		List<String[]> towrite = new ArrayList<String[]>();
		{
			DelimitedReader dr = new DelimitedReader(file_sql2instance_union);
			String[] l;
			while ((l = dr.read()) != null) {
				String wid1str = l[0];
				String wid2str = l[1];
				String key1 = wid1str + "\t" + wid2str;
				if (seed_negative.containsKey(key1)) {
					List<String> negnellstrs = seed_negative.get(key1);
					for (String r : negnellstrs) {
						towrite.add(new String[] { l[0], l[1], l[2], l[3], l[4], r });
					}
				}
				String key2 = wid2str + "\t" + wid1str;
				if (seed_negative.containsKey(key2)) {
					List<String> negnellstrs = seed_negative.get(key2);
					for (String r : negnellstrs) {
						towrite.add(new String[] { l[0], l[1], l[2], l[3], l[4], r });
					}
				}
			}
			dr.close();
		}
		StringTable.sortUniq(towrite);
		for (String[] w : towrite) {
			dw.write(w);
		}
		dw.close();
	}

	public static void step5_weight_wikilink(String file_sql2instance_union, String output) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		HashMap<Long, Set<Integer>> unionwidpair2relationId = new HashMap<Long, Set<Integer>>();
		HashSet<Long> hasLink = new HashSet<Long>();
		{
			DelimitedReader dr = new DelimitedReader(file_sql2instance_union);
			String[] l;
			while ((l = dr.read()) != null) {
				long v = StringTable.intPair2Long(l[0], l[1]);
				if (!unionwidpair2relationId.containsKey(v)) {
					unionwidpair2relationId.put(v, new HashSet<Integer>());
				}
				unionwidpair2relationId.get(v).add(Integer.parseInt(l[4]));
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_wikipediapagelink);
			String[] l;
			while ((l = dr.read()) != null) {
				long v1 = StringTable.intPair2Long(l[0], l[1]);
				long v2 = StringTable.intPair2Long(l[1], l[0]);
				if (unionwidpair2relationId.containsKey(v1)) {
					hasLink.add(v1);
				}
				if (unionwidpair2relationId.containsKey(v2)) {
					hasLink.add(v2);
				}
			}
		}
		HashCount<Integer> urid2allpairs = new HashCount<Integer>();
		HashCount<Integer> urid2linkedpairs = new HashCount<Integer>();
		{
			for (Entry<Long, Set<Integer>> e : unionwidpair2relationId.entrySet()) {
				long v = e.getKey();
				boolean ok = false;
				if (hasLink.contains(v)) {
					ok = true;
				}
				for (int urid : e.getValue()) {
					urid2allpairs.add(urid);
					if (ok) {
						urid2linkedpairs.add(urid);
					}
				}
			}
			Iterator<Entry<Integer, Integer>> it = urid2allpairs.iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> e = it.next();
				int urid = e.getKey();
				int all = e.getValue();
				int linkvalue = urid2linkedpairs.see(urid);
				dw.write(urid, linkvalue, all);
			}
		}
		dw.close();
	}

	public static void step6_joinlen(FreebaseRelation fbr, String output) throws IOException {
		List<UnionRelation> filtererelations = fbr.filteredUnionRelations;
		DelimitedWriter dw = new DelimitedWriter(output);
		for (UnionRelation ur : filtererelations) {
			String nrname = ur.nellRelationName;
			String fbname = ur.unionRelationName;
			String fbname1 = fbname.split("@")[0];
			int length = fbname1.split("\\|").length;
			int urid = ur.unionRelId;
			// double score = share * 1.0 / t1.size();
			dw.write(nrname, urid + "", length);
		}
		dw.close();
	}

	public static void stepX_inference(FreebaseRelation fbr, //
			String file_weight_similarity_nellrel_fbrel,//
			String file_weight_sharepair_nellrel_fbrel,// 
			String file_weight_explosive_nellrel_fbrel,//
			String file_weight_seednegativepair_nellrel_fbrel,// 
			String file_weight_wikilink_nellrel_fbrel,// 
			String file_weight_joinlen_nellrel_fbrel,//
			String file_inference_relation) throws IOException {
		{
			/**name*/
			List<String[]> all = (new DelimitedReader(file_weight_similarity_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				double score = Integer.parseInt(l[2]);
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.SHAREWORDS, score);
			}
		}
		{
			/**share instances*/
			List<String[]> all = (new DelimitedReader(file_weight_sharepair_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				double score = Integer.parseInt(l[2]);
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.SHAREINSTANCE, score);
			}
		}
		{
			/**explosive during join*/
			List<String[]> all = (new DelimitedReader(file_weight_explosive_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.EXPLOSIVEJOIN, 1);
			}
		}
		{
			/**seed negative*/
			List<String[]> all = (new DelimitedReader(file_weight_seednegativepair_nellrel_fbrel)).readAll();
			HashCount<Integer> urid_badtimes = new HashCount<Integer>();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[4]);
				String nellrel = l[5];
				UnionRelation ur = fbr.getUnionRelationById(urid);
				if (ur.nellRelationName.equals(nellrel)) {
					urid_badtimes.add(urid);
				}
			}
			Iterator<Entry<Integer, Integer>> it = urid_badtimes.iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> e = it.next();
				//more than two negative matches
				if (e.getValue() > 1) {
					int urid = e.getKey();
					UnionRelation ur = fbr.getUnionRelationById(urid);
					ur.setScore(UnionRelationScoreIndex.SEEDNEGATIVE, 1);
				}
			}
		}
		{
			/**pairs have wikipedia page links*/
			List<String[]> all = (new DelimitedReader(file_weight_wikilink_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[0]);
				int haslink = Integer.parseInt(l[1]);
				int total = Integer.parseInt(l[2]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.WIKILINK, (total - haslink) * 1.0 / total);
				ur.setScore(UnionRelationScoreIndex.NUMINSTANCE, total);
			}
		}
		{
			List<String[]> all = (new DelimitedReader(file_weight_joinlen_nellrel_fbrel)).readAll();
			for (String[] l : all) {
				double score = Integer.parseInt(l[2]);
				int urid = Integer.parseInt(l[1]);
				UnionRelation ur = fbr.getUnionRelationById(urid);
				ur.setScore(UnionRelationScoreIndex.JOINLEN, score);
			}
		}
		{
			/**Set weights for differen features*/
			UnionRelation.setWeight(UnionRelationScoreIndex.EXPLOSIVEJOIN, -100);
			UnionRelation.setWeight(UnionRelationScoreIndex.SEEDNEGATIVE, -100);
			UnionRelation.setWeight(UnionRelationScoreIndex.SHAREWORDS, 1);
			UnionRelation.setWeight(UnionRelationScoreIndex.SHAREINSTANCE, 1);
			UnionRelation.setWeight(UnionRelationScoreIndex.NUMINSTANCE, 0);
			UnionRelation.setWeight(UnionRelationScoreIndex.WIKILINK, -10);
			UnionRelation.setWeight(UnionRelationScoreIndex.JOINLEN, -1);
		}
		{
			/**Inference*/
			List<String[]> towrite = new ArrayList<String[]>();
			for (UnionRelation ur : fbr.filteredUnionRelations) {
				double sum = 0;
				StringBuilder sb = new StringBuilder();
				for (Entry<UnionRelationScoreIndex, Double> e : ur.scores.entrySet()) {
					sum += UnionRelation.getWeight(e.getKey()) * e.getValue();
					sb.append(e.getKey() + "=" + e.getValue() + " ");
				}
				if (!ur.nellRelationName.contains("_inverse")) {
					towrite.add(new String[] { ur.nellRelationName, sum + "", ur.unionRelId + "", ur.unionRelationName,
							sb.toString() });
				}
				//dw.write(ur.nellRelationName, sum + "", ur.unionRelId, ur.unionRelationName, sb.toString());
			}
			StringTable.sortByColumn(towrite, new int[] { 0, 1 }, new boolean[] { false, true });
			StringTable.delimitedWrite(towrite, file_inference_relation);
		}

	}

	public static void stepX_setGoodUnionRelation(FreebaseRelation fbr, //all union relations 
			String file_inference_relation,//input: details about the inferece 
			int topk,//take top k as the result match
			String output //output match result
	) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedReader dr = new DelimitedReader(file_inference_relation);
		List<String[]> b;
		
		while ((b = dr.readBlock(0)) != null) {
			int take = 0;
			for (int i = 0; i < topk && i < b.size(); i++) {
				String[] l = b.get(i);
				double sum = Double.parseDouble(l[1]);
				int urid = Integer.parseInt(l[2]);
				if(!l[4].contains("NUMINSTANCE")){
					continue;
				}
				if (sum > 0 || sum > -10 && take == 0) {
					UnionRelation ur = fbr.getUnionRelationById(urid);
					ur.isGood = true;
					dw.write(ur.nellRelationName, ur.unionRelId, l[1], l[3], l[4]);
					take++;
				}
			}
		}
		dw.close();
	}

	/**I want to draw a table: relation_name, new instance_num, accuracy
	 * @throws IOException */
	public static void step7_sampleInstancesForLabel(String file_sql2instance_union,
			String file_afterrelmatch_relmatchres, String tempdir, String output) throws IOException {
		HashMap<Integer, String[]> urid2nellstr = new HashMap<Integer, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(file_afterrelmatch_relmatchres);
			String[] l;
			while ((l = dr.read()) != null) {
				int urid = Integer.parseInt(l[1]);
				String nellstr = l[0];
				urid2nellstr.put(urid, l);
			}
			dr.close();
		}
		//shuffle file_sql2instance_union
		{
			List<String[]> all = (new DelimitedReader(file_sql2instance_union)).readAll();
			Collections.shuffle(all);
			List<String[]> towrite = new ArrayList<String[]>();
			HashCount<Integer> hc = new HashCount<Integer>();
			for (String[] l : all) {
				int urid = Integer.parseInt(l[4]);
				if (urid2nellstr.containsKey(urid) && hc.see(urid) < 20) {
					String mapping[] = urid2nellstr.get(urid);
					String nellrel = mapping[0];
					String fbrel = mapping[3];
					towrite.add(new String[] { nellrel, urid + " ", fbrel, l[0], l[1], l[2], l[3] });
					hc.add(urid);
				}
			}

			StringTable.sortByColumn(towrite, new int[] { 0, 1 });
			StringTable.delimitedWrite(towrite, output);

		}
	}

	public static void stepX_getTypeConstrain(FreebaseRelation fbr, String output) throws IOException {
		List<String[]> argstuff = new ArrayList<String[]>();
		HashMap<Integer, String> gnid2type = new HashMap<Integer, String>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
			String[] l;
			while ((l = dr.read()) != null) {
				int gnid = Integer.parseInt(l[0]);
				gnid2type.put(gnid, l[5]);
			}
			dr.close();
		}

		for (UnionRelation ur : fbr.filteredUnionRelations) {
			if (ur.isGood) {
				for (String ins : ur.instances) {
					String[] arg1arg2 = ins.split("\t");
					int gnid1 = Integer.parseInt(arg1arg2[0]);
					int gnid2 = Integer.parseInt(arg1arg2[1]);
					String type1 = gnid2type.containsKey(gnid1) ? gnid2type.get(gnid1) : "NA";
					String type2 = gnid2type.containsKey(gnid2) ? gnid2type.get(gnid2) : "NA";

					String[] domainrange = Main.no.relname2DomainRange.get(ur.nellRelationName);
					argstuff.add(new String[] { domainrange[0], type1, gnid1 + "", });
					argstuff.add(new String[] { domainrange[1], type2, gnid2 + "" });
				}
			}
		}
		StringTable.sortUniq(argstuff);
		DelimitedWriter dw = new DelimitedWriter(output);
		for (String[] w : argstuff) {
			dw.write(w);
		}
		dw.close();
	}

	public static void a1_fbrank(String file_raw, String file_weight_fbrank) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_fbrank);
		DelimitedReader dr = new DelimitedReader(file_raw);
		HashSet<String> appear = new HashSet<String>();
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			int score = 0;
			for (String[] l : b) {
				String mid = l[1];
				if (!appear.contains(argname + "\t" + mid)) {
					dw.write(argname, mid, score);
					appear.add(argname + "\t" + mid);
					score++;
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void a2_typeconstrain(String file_raw, String file_afterrelmatch_typeconstrain,
			String file_weight_typeconstrain) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_typeconstrain);
		HashMap<String, HashMap<String, Double>> nelltype_fbtypedist = new HashMap<String, HashMap<String, Double>>();
		{
			DelimitedReader dr = new DelimitedReader(file_afterrelmatch_typeconstrain);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				String nelltype = b.get(0)[0];
				nelltype_fbtypedist.put(nelltype, new HashMap<String, Double>());
				List<List<String[]>> s = StringTable.toblock(b, 1);
				int sum = 0;
				for (List<String[]> s0 : s) {
					int size = s0.size();
					sum += size;
				}
				for (List<String[]> s0 : s) {
					String fbtype = s0.get(0)[1];
					int size = s0.size();
					nelltype_fbtypedist.get(nelltype).put(fbtype, size * 1.0 / sum);
				}
			}
			dr.close();
		}
		HashMap<String, String> arg2nelltype = new HashMap<String, String>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					arg2nelltype.put(s[0], nr.domain);
					arg2nelltype.put(s[1], nr.range);
				}
				for (String[] s : nr.known_negatives) {
					arg2nelltype.put(s[0], nr.domain);
					arg2nelltype.put(s[1], nr.range);
				}
			}
		}
		DelimitedReader dr = new DelimitedReader(file_raw);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			if (argname.equals("Green Bay")) {
				//	D.p("Green Bay");
			}
			String nelltype = arg2nelltype.get(argname);
			HashMap<String, Double> fbtype_dist = nelltype_fbtypedist.get(nelltype);

			for (String[] l : b) {
				String mid = l[1];
				String midfbtype = l[3];
				if (fbtype_dist == null) {
					dw.write(argname, mid, "1");//error
				} else {
					//double score = fbtype_dist.containsKey(midfbtype) ? fbtype_dist.get(midfbtype) : 0;
					double score = fbtype_dist.containsKey(midfbtype) ? 1 : 0;
					dw.write(argname, mid, score);
				}
			}
		}
		dr.close();
		dw.close();
	}

	public static void a3_shareword(String file_raw, String file_weight_shareword) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_shareword);
		DelimitedReader dr = new DelimitedReader(file_raw);
		List<String[]> b;
		while ((b = dr.readBlock(0)) != null) {
			String argname = b.get(0)[0];
			for (String[] l : b) {
				String mid = l[1];
				String names = l[4];
				String[] split = names.split(" ");
				double highestscore = 0;
				for (String n : split) {
					List<String> t1 = StringUtil.tokenize(n.toLowerCase(), new char[] { '_', ' ' });
					List<String> t2 = StringUtil.tokenize(argname.toLowerCase(), new char[] { '_', ' ' });
					int shareword = StringUtil.numOfShareWords(n.toLowerCase(), argname.toLowerCase(), new char[] {
							'_', ' ' });
					double score = shareword * 1.0 / Math.max(t1.size(), t2.size());
					if (score > highestscore) {
						highestscore = score;
					}
				}
				dw.write(argname, mid, highestscore);
			}
		}
		dr.close();
		dw.close();
	}

	public static void a4_otherarg_appearinlink(String file_raw, String file_wikilink_subset,
			String file_weight_otherarginlink) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_otherarginlink);
		HashMap<String, Set<String>> arg2otherArg = new HashMap<String, Set<String>>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					StringTable.mapKey2SetAdd(arg2otherArg, s[0], s[1]);
					StringTable.mapKey2SetAdd(arg2otherArg, s[1], s[0]);
				}
			}
		}
		List<String[]> allraw = (new DelimitedReader(file_raw)).readAll();
		StringTable.sortByIntColumn(allraw, new int[] { 2 });
		//List<String[]> alllink = (new DelimitedReader(file_wikilink_subset)).readAll();
		{
			DelimitedReader dr = new DelimitedReader(file_wikilink_subset);
			List<String[]> b = dr.readBlock(0);
			for (String[] l : allraw) {
				int wid = Integer.parseInt(l[2]);
				String argname = l[0];
				String mid = l[1];
				Set<String> otherargs = arg2otherArg.get(argname);
				int sum = 0;

				while (b != null && Integer.parseInt(b.get(0)[0]) < wid) {
					b = dr.readBlock(0);
				}
				if (b != null && otherargs != null && Integer.parseInt(b.get(0)[0]) == wid) {
					for (String[] s : b) {
						String link = s[1].replace("_", " ");
						for (String a : otherargs) {
							if (a.equals(link)) {
								sum++;
							}
						}
					}
				}
				if (sum > 0)
					sum = 1;
				dw.write(argname, mid, sum + "");
			}
		}
		dw.close();
	}

	public static void a5_otherarg_appearintext(String file_raw, String file_wksensubset,
			String file_weight_appearintext) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file_weight_appearintext);
		HashMap<String, Set<String>> arg2otherArg = new HashMap<String, Set<String>>();
		{
			for (NellRelation nr : Main.no.nellRelationList) {
				for (String[] s : nr.seedInstances) {
					StringTable.mapKey2SetAdd(arg2otherArg, s[0], s[1]);
					StringTable.mapKey2SetAdd(arg2otherArg, s[1], s[0]);
				}
			}
		}
		List<String[]> allraw = (new DelimitedReader(file_raw)).readAll();
		StringTable.sortByIntColumn(allraw, new int[] { 2 });
		//List<String[]> alllink = (new DelimitedReader(file_wikilink_subset)).readAll();
		{
			DelimitedReader dr = new DelimitedReader(file_wksensubset);
			List<String[]> b = dr.readBlock(1);
			for (String[] l : allraw) {
				int wid = Integer.parseInt(l[2]);
				String argname = l[0];
				String mid = l[1];
				Set<String> otherargs = arg2otherArg.get(argname);
				int sum = 0;

				while (b != null && Integer.parseInt(b.get(0)[1]) < wid) {
					b = dr.readBlock(1);
				}
				if (b != null && otherargs != null && Integer.parseInt(b.get(0)[1]) == wid) {
					for (String[] s : b) {
						String text = s[3];
						for (String a : otherargs) {
							if (text.contains(a)) {
								sum++;
							}
						}
					}
				}
				if (sum > 0)
					sum = 1;
				dw.write(argname, mid, sum + "");
			}
		}
		dw.close();
	}

	public static void b1_inference(String file_raw, String[] weightfiles, NellstrMidScoreIndex[] weighttype,//weight files
			String output_inference,//detailed inference result
			String output_newnellstr2mid) throws IOException {
		HashMap<String, NellstrMidPred> map_strmid2pred = new HashMap<String, NellstrMidPred>();
		{
			DelimitedReader dr = new DelimitedReader(file_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellstr = l[0];
				String mid = l[1];
				NellstrMidPred nmp = new NellstrMidPred(nellstr, mid, l[5]);
				map_strmid2pred.put(nellstr + "\t" + mid, nmp);
			}
			dr.close();
		}
		{
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.FBRANK, -1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.TYPECONSTRIAN, 1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.SHAREWORD, 1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.WIKILINK, 1.0);
			NellstrMidPred.weightForScore.put(NellstrMidScoreIndex.WIKITEXT, 1.0);
		}
		for (int i = 0; i < weightfiles.length; i++) {
			String file = weightfiles[i];
			NellstrMidScoreIndex si = weighttype[i];
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {
				NellstrMidPred p = map_strmid2pred.get(l[0] + "\t" + l[1]);
				p.setScore(si, Double.parseDouble(l[2]));
			}
			dr.close();
		}
		List<String[]> towrite = new ArrayList<String[]>();
		{
			/**Inference*/

			for (Entry<String, NellstrMidPred> e : map_strmid2pred.entrySet()) {
				NellstrMidPred p = e.getValue();
				StringBuilder sb = new StringBuilder();
				double sum = 0;
				for (Entry<NellstrMidScoreIndex, Double> ee : p.scores.entrySet()) {
					sum += NellstrMidPred.weightForScore.get(ee.getKey()) * ee.getValue();
					sb.append(ee.getKey() + "=" + ee.getValue() + " ");
				}
				towrite.add(new String[] { p.nellstr, p.mid, p.enurl, sum + "", sb.toString() });
			}
			StringTable.sortByColumn(towrite, new int[] { 0, 3 }, new boolean[] { false, true });
			StringTable.delimitedWrite(towrite, output_inference);
		}
		{
			HashSet<String> result = new HashSet<String>();
			List<List<String[]>> blocks = StringTable.toblock(towrite, 0);
			for (List<String[]> b : blocks) {
				result.add(b.get(0)[0] + "\t" + b.get(0)[1]);
			}
			DelimitedWriter dw = new DelimitedWriter(output_newnellstr2mid);
			DelimitedReader dr = new DelimitedReader(file_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				if (result.contains(l[0] + "\t" + l[1])) {
					dw.write(l);
				}
			}
			dw.close();
			dr.close();
		}
	}

	public static void b2_setGood(String file_raw, String file_inference_nellstr, String file_gold, String output)
			throws IOException {
		HashMap<String, String> nellstr2mid = new HashMap<String, String>();
		HashMap<String, String> nellstr2goldmid = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_gold);
			String[] l;
			while ((l = dr.read()) != null) {
				nellstr2goldmid.put(l[0], l[1]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_inference_nellstr);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				nellstr2mid.put(b.get(0)[0], b.get(0)[1]);
			}
		}
		/**check the difference to no1 fbsearch*/
		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(file_raw);
			String[] l;
			while ((l = dr.read()) != null) {
				String nellstr = l[0];
				String oldmid = l[1];
				String mypredmid = nellstr2mid.get(nellstr);
				String goldmid = nellstr2goldmid.containsKey(nellstr) ? nellstr2goldmid.get(nellstr) : "NA";
				if (!oldmid.equals(mypredmid)) {
					dw.write(nellstr, mypredmid, oldmid, goldmid);
				}
			}
			dw.close();
		}
	}

	public static void string_alignment_accuracy(String file_gold, String file_newnellstr2mid, String output)
			throws IOException {
		HashMap<String, String> answer = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_gold);
			String[] l;
			while ((l = dr.read()) != null) {
				answer.put(l[0], l[1]);
			}
		}

		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedReader dr = new DelimitedReader(file_newnellstr2mid);
			String[] l;
			int correct = 0, total = 0;
			while ((l = dr.read()) != null) {
				String name = l[0];
				String right_mid = answer.get(name);
				String pred_mid = l[1];
				if (right_mid != null) {
					if (right_mid.equals(pred_mid)) {
						correct++;
					}
					total++;
				}
			}
			dw.write("Accuracy:", correct, total, correct * 1.0 / total);
			D.p("Accuracy:", correct, total, correct * 1.0 / total);
			dw.close();
		}
	}

}
