package freebase.relmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import multir.util.delimited.Sort;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

/**
 * Store the freebase dump as a linked graph; Id and edge are encoded in numbers
 * The result is in /projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph
 */

public class S1_dump2graph {

	public static void step2() throws IOException {
		HashMap<String, Integer> edges = new HashMap<String, Integer>();
		HashMap<String, Integer> nodes = new HashMap<String, Integer>();
		// String file =
		// "/projects/pardosa/s5/clzhang/ontologylink/tmp2/filterDump";
		// String output1 =
		// "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph2";
		// String output2 =
		// "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbNodes2";
		// String output3 =
		// "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges2";
		DelimitedReader dr = new DelimitedReader(Main.file_fbdumpclean);
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbgraph);
		String[] line;
		int ln = 0;
		while ((line = dr.read()) != null) {
			String arg1 = line[0];
			String arg2 = line[2];
			String rel = line[1];
			int arg1id = step2_1(nodes, arg1), arg2id = step2_1(nodes, arg2), relid = step2_1(edges, rel);
			dw.write(arg1id, arg2id, relid);
		}
		dw.close();
		dr.close();
		{
			DelimitedWriter dw0 = new DelimitedWriter(Main.file_fbnode);
			for (Entry<String, Integer> e : nodes.entrySet()) {
				dw0.write(e.getKey(), e.getValue());
			}
			dw0.close();
		}
		{
			DelimitedWriter dw0 = new DelimitedWriter(Main.file_fbedge);
			for (Entry<String, Integer> e : edges.entrySet()) {
				dw0.write(e.getKey(), e.getValue());
			}
			dw0.close();
		}
	}

	private static int step2_1(HashMap<String, Integer> hash, String one) {
		if (!hash.containsKey(one))
			hash.put(one, hash.size());
		return hash.get(one);
	}

	/** Filter away entrees not an edge */
	public static void step1() throws IOException {

		DelimitedWriter dw = new DelimitedWriter(Main.file_fbdumpclean);
		DelimitedReader dr = new DelimitedReader(Main.file_fbdump);
		String[] line;
		int ln = 0;
		while ((line = dr.read()) != null) {
			if (ln++ % 10000 == 0)
				System.out.print(ln + "\r");
			// if(ln>1000000)break;
			if (line[3].length() != 0)
				continue;
			if (!line[0].startsWith("/m/") || !line[2].startsWith("/m/"))
				continue;
			dw.write(line);
		}
		dr.close();
		dw.close();
	}

	public static void step3_clean() {
		final int MAX = 20000;
		int[] count = new int[MAX];
		HashSet<Integer> bad = new HashSet<Integer>();
		String[] relation_name = new String[MAX];
		try {
			{
				DelimitedReader dr = new DelimitedReader(Main.file_fbedge);
				String[] line;
				while ((line = dr.read()) != null) {
					relation_name[Integer.parseInt(line[1])] = line[0];
				}
				for (int i = 0; i < relation_name.length; i++) {
					if (relation_name[i] == null)
						continue;
					if (relation_name[i].startsWith("/type") || relation_name[i].startsWith("/freebase")
							|| relation_name[i].startsWith("/common") || relation_name[i].startsWith("/base")
							|| relation_name[i].startsWith("/user")) {
						bad.add(i);
					}
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_fbgraph);
				DelimitedWriter dw = new DelimitedWriter(Main.file_fbgraph_clean);
				String[] line;
				while ((line = dr.read()) != null) {
					int relid = Integer.parseInt(line[2]);
					if (!bad.contains(relid)) {
						dw.write(line);
					}
				}
				dw.close();
				dr.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void step4_sort() {
		try {
			Sort.sort(Main.file_fbnode, Main.file_fbnode_sbmid, Main.dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[0].compareTo(o2[0]);
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void step5_gnid2title() {
		// load mid wid title
		try {
			DelimitedReader dr1 = new DelimitedReader(freebase.typematch.Main.fout_mid_wid_title);
			DelimitedReader dr2 = new DelimitedReader(Main.file_fbnode_sbmid);
			DelimitedWriter dw = new DelimitedWriter(Main.file_gnid_mid_wid_title);
			String[] l;
			String[] b = dr1.read();
			while ((l = dr2.read()) != null) {
				String mid = l[0];
				int gnid = Integer.parseInt(l[1]);
				if (b == null)
					break;
				while (b[0].compareTo(mid) < 0 && (b = dr1.read()) != null) {

				}
				if(b==null)break;
				if (b[0].equals(mid))
					dw.write(gnid, b[0], b[1], b[2]);
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {
		/** Filter away quad not an edge linking to mid objects */
		// step1();
		/** Convert to a graph, with assigned node id and edge id */
		// step2();
		/** remove */
		// step3_clean();
		/** sort */
		// step4_sort();

		/** get the name for gnid */
		// step5_gnid2title();
	}
}
