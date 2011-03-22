package freebase.candidate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;



public class Bfs2 {

	/**
	 * @param args
	 */
	static Node[] nodes = new Node[4500 * 100 * 100];
	static int MaxNodeSize = 0;
	static int MaxDep = 3;

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/tmp2";
	static String dir2 = "/projects/pardosa/s5/clzhang/ontologylink/";
	static String input1 = dir + "/fbGraph3_nellonly";
	static String input2 = dir + "/nellseed2zclid";
	static String input3 = dir + "/fbEdges";
	static String output1 = dir + "/seedLinks20110205";

	//static String output2 = dir + "/seedLinks_";
	// static String output3 = dir + "/seedLinkdsPathInId";

	static void load(String fbgraph) {
		try {
			DelimitedReader dr = new DelimitedReader(fbgraph);
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
		;
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
		while (cur < buffer.size() && buffer.get(cur).depth < MaxDep) {
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
						int[] pathnew = new int[MaxDep * 2 + 2];
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

	public static void calc_subprint(List<int[]> path, DelimitedWriter dw1, String line[],
			int arg1, int arg2,String relation) throws IOException {
		if (path != null && path.size() > 0) {
			String[] res = pathToString(path);
//			List<String> show = pathToString2(path);
//			String[] pathInId = pathToString3(path);
			for (String a : res) {
				dw1.write(relation,arg1, arg2, a);
				dw1.flush();
			}
//			for (int i = 0; i < show.size(); i++) {
//				dw2.write(line[4], arg1, arg2, line[2], line[3], pathInId[i], show.get(i));
//				dw2.flush();
//			}
		}
	}

	public static void calc() throws IOException {
		load(input1);
		DelimitedReader dr = new DelimitedReader(input2);
		DelimitedWriter dw = new DelimitedWriter(output1);
		String[] line;
		while ((line = dr.read()) != null) {
			int arg1 = Integer.parseInt(line[0]);
			int arg2 = Integer.parseInt(line[1]);
			String relation = line[4];
			System.out.println(arg1 + "\t" + arg2 + line[2] + "\t" + line[3]);
			List<int[]> pathA2B = bfs(arg1, arg2);
			List<int[]> pathB2A = bfs(arg2, arg1);
			calc_subprint(pathA2B,dw,line,arg1,arg2,relation);
			calc_subprint(pathB2A,dw,line,arg1,arg2,relation+"_inverse");
		}
		dw.close();
		dr.close();
	}

	static final int MAX = 20000;
	static String[] relation_name = new String[MAX];

	public static void loadEdges() throws IOException {
		DelimitedReader dr = new DelimitedReader(input3);
		String[] line;
		while ((line = dr.read()) != null) {
			relation_name[Integer.parseInt(line[1])] = line[0];
		}
	}

	// public static void show() throws Exception{
	// //Load relation names;
	// //group by seed_instance
	// loadEdges();
	// DelimitedReader dr = new DelimitedReader(output1);
	// String []line;
	// while((line = dr.read())!=null){
	// int arg1 =
	// }
	// dr.close();
	//
	// }
	public static void test() throws IOException {
		load(dir + "/b");
		// 841367 418403
		int arg1 = 841367;
		int arg2 = 418403;
		List<int[]> path = bfs(arg1, arg2);
		List<int[]>path2 = bfs(arg2,arg1);
		DelimitedWriter dw = new DelimitedWriter(dir + "/a");
		String[] res = pathToString(path);
		String[] res2 = pathToString(path2);
		for (String a : res) {
			dw.write(arg1, arg2, a);
		}
		for (String a : res2) {
			dw.write(arg1, arg2, a);
		}
		dw.close();
	}

	public static void main(String[] args) throws IOException {
		loadEdges();
		calc();
		//test();
	}

}
