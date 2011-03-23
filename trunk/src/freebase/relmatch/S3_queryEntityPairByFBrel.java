package freebase.relmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import multir.util.delimited.Sort;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

class GraphEdge {
	int relationId;
	int size;
	int nitem;
	int[][] pairs_sorta;
	int[][] pairs_sortb;

	public GraphEdge(int size) {
		this.size = size;
		this.nitem = 0;
		// [2] means: arg1 argb
		pairs_sorta = new int[size][2];
		pairs_sortb = new int[size][2];
	}

	public void add(int arg1, int arg2) {
		pairs_sorta[nitem][0] = arg1;
		pairs_sorta[nitem][1] = arg2;
		pairs_sortb[nitem][0] = arg1;
		pairs_sortb[nitem][1] = arg2;
		nitem++;
	}

	public void sort() {
		Arrays.sort(pairs_sorta, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				// TODO Auto-generated method stub
				return o1[0] - o2[0];
			}
		});
		Arrays.sort(pairs_sortb, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				// TODO Auto-generated method stub
				return o1[1] - o2[1];
			}
		});
	}
}

public class S3_queryEntityPairByFBrel {

	static int MaxRel = 20000; // number of total relations
	static int relsize[] = new int[MaxRel];
	static GraphEdge edges[] = new GraphEdge[MaxRel];
	static int MaxNodeSize = 0;
	static List<int[]> all = new ArrayList<int[]>();

	private static void loadgraph(String fbgraph) {
		try {
			{
				DelimitedReader dr = new DelimitedReader(fbgraph);
				String[] line;
				int ln = 0;
				while ((line = dr.read()) != null) {
					if (ln++ % 1000000 == 0)
						System.out.print(ln + "\r");
					int arg1 = Integer.parseInt(line[0]);
					int arg2 = Integer.parseInt(line[1]);
					int rel = Integer.parseInt(line[2]);
					all.add(new int[] { arg1, arg2, rel });
					relsize[rel]++;
				}
				dr.close();
				System.out.println("Create edges");
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
				System.out.println("Concrete edges");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void queryAllNellWithWholePath() throws Exception {
		loadgraph(Main.file_fbgraph_clean);
		System.out.println("Start querying all concate fbrelations");
		DelimitedReader dr = new DelimitedReader(Main.file_seedbfspath_show
				+ ".group");
		DelimitedWriter dw = new DelimitedWriter(Main.file_queryresult);
		String[] line;
		while ((line = dr.read()) != null) {
			System.out.println(line[1]);
			String[] abc = line[1].split(",");
			int[] query = new int[abc.length];
			for (int i = 0; i < abc.length; i++)
				query[i] = Integer.parseInt(abc[i]);
			List<int[]> result = queryEntityWithWholePath(query);
			// nellrelation, fbrelation,
			String nellrelation = line[0], fbrelation = line[1];

			for (int[] a : result) {
				String[] towrite = new String[a.length + 2];
				towrite[0] = nellrelation;
				towrite[1] = fbrelation;
				for (int k = 0; k < a.length; k++)
					towrite[k + 2] = a[k] + "";
				dw.write(towrite);
			}
		}
		dr.close();
		dw.close();
	}

	public static List<int[]> queryEntityWithWholePath(int[] query)
			throws Exception {
		List<int[]> result = new ArrayList<int[]>();
		if (query.length == 0)
			return result;
		if (query.length == 1) {
			int[][] tmp = edges[query[0]].pairs_sorta;
			for (int[] a : tmp)
				result.add(a);
			return result;
		}
		List<int[]> s = new ArrayList<int[]>();
		for (int[] a : edges[query[0]].pairs_sortb) {
			s.add(new int[] { a[0], a[1] });
		}
		// int [][]s = edges[query[0]].pairs_sortb;
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
			while (p1 < s.size() && p2 < b.length) {
				// dwlog.write("s",s[p1][0],s[p1][1],"b",b[p2][0],b[p2][1]);
				int[] s_p = s.get(p1);
				int s_p_last = s_p[s_p.length - 1];
				if (s_p_last == b[p2][0]) {
					// dwlog.write("Here!!!");
					int[] m = new int[s_p.length + 1];
					for (int k = 0; k < s_p.length; k++)
						m[k] = s_p[k];
					m[s_p.length] = b[p2][1];
					middle.add(m);
					p1++;
					p2++;
				} else if (s_p_last > b[p2][0]) {
					p2++;
				} else {
					p1++;
				}
			}
			if (middle.size() == 0)
				break;
			s = new ArrayList<int[]>();
			for (int j = 0; j < middle.size(); j++) {
				s.add(middle.get(j));
			}
			Collections.sort(s, new Comparator<int[]>() {
				public int compare(int[] o1, int[] o2) {
					return o1[o1.length - 1] - o2[o2.length - 1];
				}
			});
		}
		for (int[] a : s)
			result.add(a);
		return result;
	}

	public static List<int[]> queryEntity(int[] query) throws Exception {
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

	//
	public static void sortResult_fake1() throws IOException {
		Sort.sort(Main.file_queryresult, Main.file_queryresult + ".sort",
				Main.dir, new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						StringBuilder sb1 = new StringBuilder();
						StringBuilder sb2 = new StringBuilder();
						for (String a : o1)
							sb1.append(a).append("\t");
						for (String a : o2)
							sb2.append(a).append("\t");
						return sb1.toString().compareTo(sb2.toString());
					}
				});
	}

	//
	static HashMap<Integer, String> entity_name = new HashMap<Integer, String>(
			30000000);

	//
	public static void giveEntityName_nellrel2fbpairs() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(
					Main.file_gnid_mid_wid_title);
			String[] line;
			while ((line = dr.read()) != null) {
				// entity_name.put(Integer.parseInt(line[0]),line[2].replace("\\s","_"));
				int gnid = Integer.parseInt(line[0]);
				String ename = line[3];
				entity_name.put(gnid, ename);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_queryresult
					+ ".sort");
			DelimitedWriter dw = new DelimitedWriter(Main.file_queryresult
					+ ".sort.name");
			String[] line;
			while ((line = dr.read()) != null) {
				int arg1 = Integer.parseInt(line[2]);
				int arg2 = Integer.parseInt(line[3]);
				String arg1Name = entity_name.get(arg1);
				String arg2Name = entity_name.get(arg2);
				if (arg1Name == null)
					arg1Name = "NA";
				if (arg2Name == null)
					arg2Name = "NA";
				if (arg1Name != null && arg2Name != null) {
					dw.write(line[0], line[1], line[2], line[3], arg1Name,
							arg2Name);
				}
			}
			dr.close();
			dw.close();
		}

	}

	public static void giveEntityName() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(
					Main.file_gnid_mid_wid_title);
			String[] line;
			while ((line = dr.read()) != null) {
				// entity_name.put(Integer.parseInt(line[0]),line[2].replace("\\s","_"));
				int gnid = Integer.parseInt(line[0]);
				if (line[3].length() > 1) {
					String ename = line[3].split(" ")[0];
					entity_name.put(gnid, ename);
				}
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_queryresult);
			DelimitedWriter dw = new DelimitedWriter(Main.file_queryresult
					+ ".name");
			String[] line;

			while ((line = dr.read()) != null) {
				boolean arg1NotNull = false;
				boolean arg2NotNull = false;
				StringBuilder sb_org = new StringBuilder();
				StringBuilder sb = new StringBuilder();
				for (int i = 2; i < line.length; i++) {
					int a = Integer.parseInt(line[i]);
					String ename = entity_name.get(a);
					if (i == 2 && ename != null)
						arg1NotNull = true;
					if (i == line.length - 1 && ename != null)
						arg2NotNull = true;
					if (ename == null) {
						sb.append("NA|");
						sb_org.append(a + " ");
					} else {
						sb.append(ename + "|");
						sb_org.append(a + " ");
					}
				}
				if (arg1NotNull && arg2NotNull) {
					dw.write(line[0], line[1], sb_org.toString(), sb.toString());
				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void sortQueryResultByArg1() throws IOException {
		try {
			Sort.sort(Main.file_queryresult, Main.file_queryresult
					+ ".name.sortByArg1", Main.dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					int id1 = Integer.parseInt(o1[2].split(" ")[0]);
					int id2 = Integer.parseInt(o2[2].split(" ")[0]);
					return id1 - id2;
				}
			});

			Sort.sort(Main.file_queryresult, Main.file_queryresult
					+ ".name.sortByArg2", Main.dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					String[] os1 = o1[2].split(" ");
					String[] os2 = o2[2].split(" ");
					int id1 = Integer.parseInt(os1[os1.length - 1]);
					int id2 = Integer.parseInt(os2[os2.length - 1]);
					return id1 - id2;
				}
			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// queryAllNellWithWholePath();
		// giveEntityName();
		sortQueryResultByArg1();
	}

}
