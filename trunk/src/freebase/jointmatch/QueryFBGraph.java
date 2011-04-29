package freebase.jointmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class QueryFBGraph {
	int MaxRel = 20000; // number of total relations
	int relsize[] = new int[MaxRel];
	GraphEdge edges[] = new GraphEdge[MaxRel];
	int MaxNodeSize = 0;
	List<int[]> all = new ArrayList<int[]>();

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

	static HashMap<Integer, Integer> gnid2wid = new HashMap<Integer, Integer>();

	private static void loadGnid2wid() throws IOException {
		D.p("Load Gnid 2 wid");
		DelimitedReader dr = new DelimitedReader(freebase.relmatch.Main.file_gnid_mid_wid_title);
		String[] l;
		while ((l = dr.read()) != null) {
			int guid = Integer.parseInt(l[0]);
			int wid = Integer.parseInt(l[2]);
			gnid2wid.put(guid, wid);
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
		for(String []l: mappings){
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

	public static void main(String[] args) throws Exception {
		QueryFBGraph qfb = new QueryFBGraph();
		//qfb.getNewEntitiesWithOntologyMapping(Main.file_predict, Main.file_extendedwidpairs);
	}
}
