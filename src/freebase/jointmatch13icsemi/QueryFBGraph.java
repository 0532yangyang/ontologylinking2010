package freebase.jointmatch13icsemi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import multir.util.delimited.Sort;

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
	HashMap<Integer, String> map_myid2mid = new HashMap<Integer, String>();

	public QueryFBGraph() {
		try {
			loadrelations(Main.file_fbgraph_rel2myid);
			loadgraph(Main.file_fbgraph_matrix);
			loadmid(Main.file_fbgraph_mid2myid);
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
		DelimitedReader dr = new DelimitedReader(fileedge);
		String[] l;
		while ((l = dr.read()) != null) {
			int rid = Integer.parseInt(l[1]);
			relations[rid] = l[0];
			map_relation2id.put(l[0], rid);
		}
		dr.close();
	}

	public static List<List<int[]>> toblock(int[][] table, int index) {
		List<List<int[]>> result = new ArrayList<List<int[]>>();
		List<int[]> b = new ArrayList<int[]>();
		b.add(table[0]);
		for (int i = 1; i < table.length; i++) {
			if (table[i][index] != (b.get(0)[index])) {
				result.add(b);
				b = new ArrayList<int[]>();
			}
			b.add(table[i]);
		}
		if (b.size() > 0)
			result.add(b);
		return result;
	}

	private void loadmid(String filemid) throws IOException {
		D.p("Loading mid 2 myid");
		DelimitedReader dr = new DelimitedReader(filemid);
		String[] l;
		while ((l = dr.read()) != null) {
			int myid = Integer.parseInt(l[1]);
			map_myid2mid.put(myid, l[0]);
		}
		dr.close();
	}

	private void queryEntity(String querystr, DelimitedWriter dw1, DelimitedWriter dw2, DelimitedWriter dw3)
			throws Exception {
		String[] rels = querystr.split("\\|\\|");
		int[] query = new int[rels.length];
		for (int i = 0; i < rels.length; i++) {
			String r = rels[i];
			int rid = map_relation2id.get(r);
			query[i] = rid;
		}
		if (query.length == 0 || query.length > 2)
			return;
		if (query.length == 1) {
			int[][] tmp = edges[query[0]].pairs_sorta;
			for (int[] a : tmp) {
				String mid1 = map_myid2mid.get(a[0]);
				String mid2 = map_myid2mid.get(a[1]);
				dw1.write(mid1, mid2, querystr,"");
			}
			return;
		} else {
			int[][] s = edges[query[0]].pairs_sortb;
			List<List<int[]>> sblocks = toblock(s, 1);
			int[][] b = edges[query[1]].pairs_sorta;
			List<List<int[]>> bblocks = toblock(b, 0);
			int p1 = 0, p2 = 0;
			while (p1 < sblocks.size() && p2 < bblocks.size()) {
				List<int[]> sb = sblocks.get(p1);
				List<int[]> bb = bblocks.get(p2);
				int x = sb.get(0)[1];
				int y = bb.get(0)[0];
				if (x == y) {
					if (sb.size() == 1 || bb.size() == 1) {
						for (int[] sb0 : sb) {
							for (int[] bb0 : bb) {
								if (map_myid2mid.containsKey(sb0[0]) && map_myid2mid.containsKey(sb0[1])
										&& map_myid2mid.containsKey(bb0[1])) {
									String mid1 = map_myid2mid.get(sb0[0]);
									String midm = map_myid2mid.get(sb0[1]);
									String mid2 = map_myid2mid.get(bb0[1]);
									dw1.write(mid1, mid2, querystr, midm);
								}
							}
						}
					}
//					else if (sb.size() * bb.size() < 1000) {
//						for (int[] sb0 : sb) {
//							for (int[] bb0 : bb) {
//								if (map_myid2mid.containsKey(sb0[0]) && map_myid2mid.containsKey(sb0[1])
//										&& map_myid2mid.containsKey(bb0[1])) {
//									String mid1 = map_myid2mid.get(sb0[0]);
//									String midm = map_myid2mid.get(sb0[1]);
//									String mid2 = map_myid2mid.get(bb0[1]);
//									dw2.write(mid1, mid2, querystr, midm);
//								}
//							}
//						}
//					}
					else {
						String midm = map_myid2mid.get(sb.get(0)[1]);
						dw3.write(querystr, midm, sb.size(), bb.size());
					}
					p1++;
					p2++;
				} else if (x < y) {
					p1++;
				} else {
					p2++;
				}
			}
		}
	}

	//	private void queryEntity_relationname(String concateRelName, String output1, String output2) throws Exception {
	//		String[] rels = concateRelName.split("\\|\\|");
	//		int[] q = new int[rels.length];
	//		for (int i = 0; i < rels.length; i++) {
	//			String r = rels[i];
	//			int rid = map_relation2id.get(r);
	//			q[i] = rid;
	//		}
	//		queryEntity(concateRelName, output1, output2);
	//	}

	public void getNewEntitiesWithOntologyMapping(String input, String output) throws Exception {
		{
			DelimitedReader dr = new DelimitedReader(input);
			HashSet<String> rels = new HashSet<String>();
			String[] l;
			while ((l = dr.read()) != null) {
				rels.add(l[2]);
			}
			DelimitedWriter dw1 = new DelimitedWriter(output+".1");
			DelimitedWriter dw2 = new DelimitedWriter(output + ".multiple");
			DelimitedWriter dw3 = new DelimitedWriter(output + ".explode");

			for (String r : rels) {
				queryEntity(r, dw1, dw2, dw3);
			}
			dw1.close();
			dw2.close();
			dw3.close();
			dr.close();
		}
		{
			Sort.sort(output + ".1", output + ".2", Main.dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] arg0, String[] arg1) {
					// TODO Auto-generated method stub
					String key0 = arg0[0] + "\t" + arg0[1] + "\t" + arg0[2];
					String key1 = arg1[0] + "\t" + arg1[1] + "\t" + arg1[2];
					return key0.compareTo(key1);
				}
			});
		}
		{
			DelimitedReader dr = new DelimitedReader(output + ".2");
			DelimitedWriter dw = new DelimitedWriter(output);
			String[] l;
			String[] last = dr.read();
			String lastkey = last[0] + "\t" + last[1] + "\t" + last[2];
			int size = 1;
			while ((l = dr.read()) != null) {
				String key = l[0] + "\t" + l[1] + "\t" + l[2];
				if (!lastkey.equals(key)) {
					dw.write(last[0], last[1], last[2],last[3]);
					last = l;
					lastkey = key;
					size = 1;
				} else {
					size++;
				}
			}
			dr.close();
			dw.close();
		}
	}

	static void sampleGraph() throws Exception {
		DelimitedReader dr = new DelimitedReader(Main.file_fbgraph_matrix);
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbgraph_matrix + ".temp");
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[2].equals("5249") || l[2].equals("5266")) {
				dw.write(l);
			}
		}
		dr.close();
		dw.close();
	}

	public static void main(String[] args) throws Exception {
		//		sampleGraph();
		QueryFBGraph qfb = new QueryFBGraph();
		qfb.getNewEntitiesWithOntologyMapping(Main.file_relationmatch_candidate, Main.file_fbsql2instances);
	}
}
