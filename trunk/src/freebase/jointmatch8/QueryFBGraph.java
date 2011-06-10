package freebase.jointmatch8;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class QueryFBGraph {
	int MaxRel = 20000; // number of total relations
	int relsize[] = new int[MaxRel];
	GraphEdge edges[] = new GraphEdge[MaxRel];
	int MaxNodeSize = 0;
	List<int[]> all = new ArrayList<int[]>();

	HashMap<Integer, String> gnid2types = new HashMap<Integer, String>();
	HashMap<Integer, String> gnid2names = new HashMap<Integer, String>();
	String[] relations = new String[MaxRel];
	HashMap<String, Integer> map_relation2id = new HashMap<String, Integer>();

	public QueryFBGraph() {
		try {
			loadrelations(freebase.relmatch.Main.file_fbedge);
			loadgraph(freebase.relmatch.Main.file_fbgraph_clean);
			loadGnid2wid();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public QueryFBGraph(boolean withType, String file_explosive) {
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(file_explosive))));
			if (withType) {
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
				//DelimitedReader dr = new DelimitedReader(Main.dir + "/debug/gnid_subset");
				String[] l;
				while ((l = dr.read()) != null) {
					int gnid = Integer.parseInt(l[0]);
					String type = l[5];
					gnid2types.put(gnid, type);
					gnid2wid.put(gnid, Integer.parseInt(l[3]));
					gnid2names.put(gnid, l[2]);
				}
				dr.close();
			}
			loadrelations(Main.file_fbedge);
			loadgraph(Main.file_fbgraph_clean);
			//loadgraph(Main.dir + "/debug/subgraph");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadgraph(String fbgraph) throws IOException {
		{
			D.p("Loading fb graph");
			DelimitedReader dr = new DelimitedReader(fbgraph);
			String[] line;
			int ln = 0;
			while ((line = dr.read()) != null) {
				if (ln++ % 10000000 == 0)
					System.out.print(ln + "\r");
				int arg1 = Integer.parseInt(line[0]);
				int arg2 = Integer.parseInt(line[1]);
				int rel = Integer.parseInt(line[2]);
				all.add(new int[] { arg1, arg2, rel });
				relsize[rel]++;
			}
			dr.close();

			for (int i = 0; i < MaxRel; i++) {
				if (i % 1000 == 0)
					System.out.print(i + "\r");
				if (relsize[i] > 0) {
					edges[i] = new GraphEdge(relsize[i]);
				}
			}
		}

		int realedge = 0;
		{
			int ln = 0;
			D.p("Create edges");
			for (int[] a : all) {
				if (ln++ % 100000 == 0)
					System.out.print(ln + "\r");
				edges[a[2]].add(a[0], a[1]);
			}
			for (int i = 0; i < MaxRel; i++) {
				if (relsize[i] > 0) {
					realedge++;
					edges[i].sort();
				}
			}
		}
	}

	private void loadrelations(String fileedge) throws IOException {
		D.p("Loading relations");
		DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_fbedge);
		String[] l;
		while ((l = dr.read()) != null) {
			int rid = Integer.parseInt(l[1]);
			relations[rid] = l[0];
			map_relation2id.put(l[0], rid);
		}
		dr.close();
	}

	private List<int[]> queryEntity(int[] query) throws Exception {
		List<int[]> result = new ArrayList<int[]>();
		if (query.length == 0)
			return result;
		if (query.length == 1) {
			int[][] tmp = edges[query[0]].pairs_sorta;
			for (int[] a : tmp)
				result.add(a);
			return result;
		}
		int[][] s = edges[query[0]].pairs_sortb;
		for (int i = 1; i < query.length; i++) {
			int p1 = 0, p2 = 0;
			int[][] b = edges[query[i]].pairs_sorta;
			// dwlog.write("middle length",s.length,b.length);
			{
				// debug s and b
				// for(int []s0: s)dwlog.write("s",s0[0],s0[1]);
				// for(int []b0:b)dwlog.write("b",b0[0],b0[1]);
			}
			List<int[]> middle = new ArrayList<int[]>();
			while (p1 < s.length && p2 < b.length) {
				// dwlog.write("s",s[p1][0],s[p1][1],"b",b[p2][0],b[p2][1]);
				if (s[p1][1] == b[p2][0]) {
					// dwlog.write("Here!!!");
					middle.add(new int[] { s[p1][0], b[p2][1] });
					p1++;
					p2++;
				} else if (s[p1][1] > b[p2][0]) {
					p2++;
				} else {
					p1++;
				}
			}
			if (middle.size() == 0)
				break;
			s = new int[middle.size()][2];
			for (int j = 0; j < middle.size(); j++) {
				s[j][0] = middle.get(j)[0];
				s[j][1] = middle.get(j)[1];
			}
			Arrays.sort(s, new Comparator<int[]>() {
				public int compare(int[] o1, int[] o2) {
					return o1[1] - o2[1];
				}
			});
		}
		for (int[] a : s)
			result.add(a);
		return result;
	}

	private boolean filterArg1ByType(int[] a, Set<String> arg1Types) {
		String type1 = gnid2types.containsKey(a[0]) ? gnid2types.get(a[0]) : "NA";
		if (arg1Types.contains(type1)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean filterByType(int[] a, Set<String> arg1Types, Set<String> arg2Types) {
		String type1 = gnid2types.containsKey(a[0]) ? gnid2types.get(a[0]) : "NA";
		String type2 = gnid2types.containsKey(a[1]) ? gnid2types.get(a[1]) : "NA";
		if (arg1Types.contains(type1) && arg2Types.contains(type2)) {
			return true;
		} else {
			return false;
		}
	}

	public static List<List<int[]>> toblock(List<int[]> table, final int index) {
		Collections.sort(table, new Comparator<int[]>() {
			@Override
			public int compare(int[] arg0, int[] arg1) {
				// TODO Auto-generated method stub
				return arg0[index] - arg1[index];
			}
		});
		List<List<int[]>> result = new ArrayList<List<int[]>>();
		List<int[]> b = new ArrayList<int[]>();
		b.add(table.get(0));
		for (int i = 1; i < table.size(); i++) {
			if (table.get(i)[index] != b.get(0)[index]) {
				result.add(b);
				b = new ArrayList<int[]>();
			}
			b.add(table.get(i));
		}
		if (b.size() > 0)
			result.add(b);
		return result;
	}

	private List<int[]> queryEntityWithTypeConstrain(String querystr, int[] query, Set<String> arg1Types,
			Set<String> arg2Types) throws Exception {

		List<int[]> result = new ArrayList<int[]>();
		if (query.length == 0)
			return result;

		int[][] s = edges[query[0]].pairs_sortb;
		List<int[]> start = new ArrayList<int[]>();
		for (int[] s0 : s) {
			if (filterArg1ByType(s0, arg1Types)) {
				start.add(s0);
			}
		}
		boolean stopquerying = false;
		for (int i = 1; i < query.length; i++) {
			if (stopquerying) {
				break;
			}
			int[][] b = edges[query[i]].pairs_sorta;
			List<int[]> against = new ArrayList<int[]>();
			for (int[] b0 : b) {
				against.add(b0);
			}
			if (start.size() == 0) {
				stopquerying = true;
				break;
			}
			List<List<int[]>> start2blocks = toblock(start, 1);
			List<List<int[]>> against2blocks = toblock(against, 0);
			int p1 = 0, p2 = 0;
			List<int[]> middle = new ArrayList<int[]>();

			while (p1 < start2blocks.size() && p2 < against2blocks.size()) {
				//				if (middle.size() > 10000) {
				//					System.err.println("too big query\t" + querystr);
				//					break;
				//				}
				List<int[]> block_start = start2blocks.get(p1);
				List<int[]> block_against = against2blocks.get(p2);
				int index_start = block_start.get(0)[1];
				int index_against = block_against.get(0)[0];
				if (index_start == index_against) {
					if (block_start.size() > 100 || block_against.size() > 100) {
						System.err.println("Nonesense join\t" + block_start.get(0)[0] + " " + index_start + " "
								+ block_against.get(0)[0] + "\t" + querystr);
						stopquerying = true;
						break;
					}
					for (int[] x : block_start) {
						for (int[] y : block_against) {
							middle.add(new int[] { x[0], y[1] });
						}
					}
					p1++;
					p2++;
				} else if (index_start > index_against) {
					p2++;
				} else {
					p1++;
				}
			}

			if (middle.size() > 0) {
				start.clear();
				start.addAll(middle);
			}
		}
		if (!stopquerying) {
			for (int[] a : start) {
				if (filterByType(a, arg1Types, arg2Types)) {
					result.add(a);
				}
			}
		}
		return result;
	}

	private List<int[]> queryEntity_relationname(String concateRelName) throws Exception {
		String[] rels = concateRelName.split("\\|");
		int[] q = new int[rels.length];
		for (int i = 0; i < rels.length; i++) {
			String r = rels[i];
			int rid = map_relation2id.get(r);
			q[i] = rid;
		}
		return queryEntity(q);
	}

	private List<int[]> queryEntity_relationname(String concateRelName, Set<String> arg1Type, Set<String> arg2Type)
			throws Exception {
		String[] rels = concateRelName.split("\\|");
		int[] q = new int[rels.length];
		for (int i = 0; i < rels.length; i++) {
			String r = rels[i];
			int rid = map_relation2id.get(r);
			q[i] = rid;
		}
		return queryEntityWithTypeConstrain(concateRelName, q, arg1Type, arg2Type);
	}

	static HashMap<Integer, Integer> gnid2wid = new HashMap<Integer, Integer>();

	private static void loadGnid2wid() throws IOException {
		D.p("Load Gnid 2 wid");
		DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[0].length() > 0 && l[3].length() > 0 && l.length > 3) {
				int guid = Integer.parseInt(l[0]);
				int wid = Integer.parseInt(l[3]);
				gnid2wid.put(guid, wid);
			}
		}
		dr.close();
	}

	public void getNewEntitiesWithOntologyMapping(String file_ontologymapping, String output) throws Exception {
		DelimitedReader dr = new DelimitedReader(file_ontologymapping);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[0].startsWith("VR")) {
				String[] ab = l[0].split("::");
				String nellrelation = ab[1];
				String fbcatrelation = ab[2];
				List<int[]> result = queryEntity_relationname(fbcatrelation);
				for (int[] a : result) {
					//					dw.write(a[0],a[1],nellrelation,fbcatrelation);
					if (gnid2wid.containsKey(a[0]) && gnid2wid.containsKey(a[1])) {
						int wid1 = gnid2wid.get(a[0]);
						int wid2 = gnid2wid.get(a[1]);
						dw.write(wid1, wid2, nellrelation, fbcatrelation);
					}
				}
			}
		}
		dw.close();
		dr.close();
	}

	public void getNewEntitiesWithOntologyMapping(List<String[]> mappings, String output) throws Exception {
		DelimitedWriter dw = new DelimitedWriter(output);
		for (String[] l : mappings) {
			String nellrelation = l[0];
			String fbcatrelation = l[1];
			List<int[]> result = queryEntity_relationname(fbcatrelation);
			for (int[] a : result) {
				//					dw.write(a[0],a[1],nellrelation,fbcatrelation);
				if (gnid2wid.containsKey(a[0]) && gnid2wid.containsKey(a[1])) {
					int wid1 = gnid2wid.get(a[0]);
					int wid2 = gnid2wid.get(a[1]);
					dw.write(wid1, wid2, nellrelation, fbcatrelation);
				}
			}
		}
		dw.close();
	}

	public void getNewEntitiesWithUnionRelations(List<UnionRelation> listunionrelations, String output)
			throws Exception {
		DelimitedWriter dw = new DelimitedWriter(output);
		for (int i = 0; i < listunionrelations.size(); i++) {
			UnionRelation ur = listunionrelations.get(i);
			for (SingleRelation sr : ur.listSingleRelation) {
				String singleFbRelname = sr.fbRelationName;
				Set<String> arg1Types = sr.mapArg1TypeWeight.keySet();
				Set<String> arg2Types = sr.mapArg2TypeWeight.keySet();
				D.p("Finding", singleFbRelname, arg1Types, arg2Types);
				List<int[]> result = queryEntity_relationname(singleFbRelname, arg1Types, arg2Types);
				for (int[] a : result) {
					//					dw.write(a[0],a[1],nellrelation,fbcatrelation);
					if (gnid2wid.containsKey(a[0]) && gnid2wid.containsKey(a[1])) {
						int wid1 = gnid2wid.get(a[0]);
						int wid2 = gnid2wid.get(a[1]);
						String enurl1 = gnid2names.get(a[0]);
						String enurl2 = gnid2names.get(a[1]);
						dw.write(wid1, wid2, enurl1, enurl2, ur.unionRelId);
					}
				}
			}
		}
		dw.close();
	}

	public static void debug_sampleSmallset() throws IOException {
		//		String dir_debug = Main.dir + "/debug";
		//		FreebaseRelation fbr = new FreebaseRelation();
		//		fbr.init(dir_debug + "/show");
		//		String relations[] = new String[20000];
		//		HashMap<String, Integer> map_relation2id = new HashMap<String, Integer>();
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_fbedge);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				int rid = Integer.parseInt(l[1]);
		//				relations[rid] = l[0];
		//				map_relation2id.put(l[0], rid);
		//			}
		//			dr.close();
		//		}
		//		HashSet<Integer> usedrids = new HashSet<Integer>();
		//		HashSet<Integer> usedgnid = new HashSet<Integer>();
		//		for (UnionRelation ur : fbr.listUnionRelation) {
		//			for (SingleRelation sr : ur.listsingleRelation) {
		//				String[] rels = sr.fbRelationName.split("\\|");
		//				for (int i = 0; i < rels.length; i++) {
		//					String r = rels[i];
		//					int rid = map_relation2id.get(r);
		//					usedrids.add(rid);
		//				}
		//			}
		//		}
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_fbgraph_clean);
		//			DelimitedWriter dw = new DelimitedWriter(dir_debug + "/subgraph");
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				int id = Integer.parseInt(l[2]);
		//				if (usedrids.contains(id)) {
		//					dw.write(l);
		//					usedgnid.add(Integer.parseInt(l[0]));
		//					usedgnid.add(Integer.parseInt(l[1]));
		//				}
		//			}
		//			dw.close();
		//			dr.close();
		//		}
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_enurl_wid_title_type_clean);
		//			DelimitedWriter dw = new DelimitedWriter(dir_debug + "/gnid_subset");
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				int gnid = Integer.parseInt(l[0]);
		//				if (usedgnid.contains(gnid)) {
		//					dw.write(l);
		//				}
		//			}
		//			dw.close();
		//		}

	}

	public static void main(String[] args) throws Exception {
		{
			//			debug_sampleSmallset();
			//			FreebaseRelation fbr = new FreebaseRelation();
			//			fbr.init(Main.dir + "/debug/show");
			//			QueryFBGraph qfb = new QueryFBGraph(true);
			//			qfb.getNewEntitiesWithUnionRelations(fbr.listUnionRelation, Main.dir + "/debug/result");

		}
		{
			/**everything need*/
			//			FreebaseRelation fbr = new FreebaseRelation();
			//			fbr.init(Main.file_seedbfspath_result);
			//			fbr.selfTest(Main.file_seedbfspath_show + ".3");
			//			QueryFBGraph qfb = new QueryFBGraph(true);
			//			qfb.getNewEntitiesWithUnionRelations(fbr.listUnionRelation, Main.file_sql2instance_union);
		}

		//qfb.getNewEntitiesWithOntologyMapping(Main.file_predict, Main.file_extendedwidpairs);
	}
}
