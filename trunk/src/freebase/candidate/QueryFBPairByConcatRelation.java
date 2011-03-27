package freebase.candidate;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

import freebase.match.Setting;
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

public class QueryFBPairByConcatRelation {

	/**
	 * @param args
	 */
	//static DelimitedWriter dwlog;
	static int MaxRel = 20000;
	static int relsize[] = new int[MaxRel];
	static GraphEdge edges[] = new GraphEdge[MaxRel];
	static int MaxNodeSize = 0;
	static List<int[]>all = new ArrayList<int[]>();

	static void load2(String fbgraph) {
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
					all.add(new int[]{arg1,arg2,rel});
					relsize[rel]++;
				}
				dr.close();
				System.out.println("Create edges");
				for (int i = 0; i < MaxRel; i++) {
					if(i% 1000 == 0)System.out.print(i+"\r");
					if (relsize[i] > 0) {
						edges[i] = new GraphEdge(relsize[i]);
					}
				}
			}
			int realedge = 0;
			{
				int ln = 0;
				System.out.println("Concrete edges");
				for(int []a: all){
					if(ln ++ % 100000 == 0)System.out.print(ln+"\r");
					edges[a[2]].add(a[0],a[1]);
				}
				for(int i=0;i<MaxRel;i++){
					if(relsize[i]>0){
						realedge++;
						edges[i].sort();
					}
				}
			}
			//debug information
			/*
			dwlog.write("realedge",realedge);
			for(int i=0;i<MaxRel;i++){
				dwlog.write("Relation",i,"size",relsize[i]);
				for(int j=0;j<10 && j<relsize[i];j++){
					dwlog.write("a",edges[i].pairs_sorta[j][0],edges[i].pairs_sorta[j][1]);
				}
				for(int j=0;j<10 && j<relsize[i];j++){
					dwlog.write("b",edges[i].pairs_sortb[j][0],edges[i].pairs_sortb[j][1]);
				}
			}
			*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void queryAllNell()throws Exception{
		System.out.println("Start querying all concate fbrelations");
		DelimitedReader dr = new DelimitedReader(Setting.file_concatedFBRel);
		DelimitedWriter dw = new DelimitedWriter(Setting.file_NellRel2FBPairs);
		String []line;
		while((line =dr.read())!=null){
			System.out.println(line[1]);
			String []abc = line[1].split(",");
			int []query = new int[abc.length];
			for(int i=0;i<abc.length;i++)query[i] = Integer.parseInt(abc[i]);
			List<int[]> result = queryEntity(query);
			//nellrelation, fbrelation,
			String nellrelation = line[0],fbrelation = line[1]/*weightstr = line[2]*/;
			for(int []a: result){
				dw.write(nellrelation, fbrelation,a[0],a[1]);
			}
		}
		dr.close();
		dw.close();
	}
	
	public static void queryAllNellWithWholePath()throws Exception{
		System.out.println("Start querying all concate fbrelations");
		DelimitedReader dr = new DelimitedReader(Setting.file_concatedFBRel);
		DelimitedWriter dw = new DelimitedWriter(Setting.file_NellRel2FBPairs);
		String []line;
		while((line =dr.read())!=null){
			System.out.println(line[1]);
			String []abc = line[1].split(",");
			int []query = new int[abc.length];
			for(int i=0;i<abc.length;i++)query[i] = Integer.parseInt(abc[i]);
			List<int[]> result = queryEntityWithWholePath(query);
			//nellrelation, fbrelation,
			String nellrelation = line[0],fbrelation = line[1]/*weightstr = line[2]*/;
			
			for(int []a: result){
				String []towrite = new String[a.length+2];
				towrite[0] = nellrelation;
				towrite[1] = fbrelation;
				for(int k=0;k<a.length;k++)towrite[k+2] = a[k]+"";
				dw.write(towrite);
			}
		}
		dr.close();
		dw.close();
	}
	
	public static List<int[]> queryEntityWithWholePath(int []query)throws Exception{
		List<int[]>result = new ArrayList<int[]>();
		if(query.length == 0)return result;
		if(query.length == 1){
			int [][]tmp = edges[query[0]].pairs_sorta;
			for(int []a:tmp)result.add(a);
			return result;
		}
		List<int[]>s = new ArrayList<int[]>();
		for(int[]a: edges[query[0]].pairs_sortb){
			s.add(new int[]{a[0],a[1]});
		}
		//int [][]s = edges[query[0]].pairs_sortb;
		for(int i=1;i<query.length;i++){
			int p1 = 0,p2=0;
			int [][]b = edges[query[i]].pairs_sorta;
			//dwlog.write("middle length",s.length,b.length);
			{
				//debug s and b
				//for(int []s0: s)dwlog.write("s",s0[0],s0[1]);
				//for(int []b0:b)dwlog.write("b",b0[0],b0[1]);
			}
			List<int[]> middle = new ArrayList<int[]>();
			while(p1<s.size() && p2<b.length){
				//dwlog.write("s",s[p1][0],s[p1][1],"b",b[p2][0],b[p2][1]);
				int []s_p = s.get(p1);
				int s_p_last = s_p[s_p.length-1];
				if(s_p_last == b[p2][0]){
					//dwlog.write("Here!!!");
					int []m = new int[s_p.length+1];
					for(int k=0;k<s_p.length;k++)m[k]=s_p[k];
					m[s_p.length]=b[p2][1];
					middle.add(m);
					p1++;p2++;
				}else if(s_p_last>b[p2][0]){
					p2++;
				}else{
					p1++;
				}
			}
			if(middle.size()==0)break;
			s = new ArrayList<int[]>();
			for(int j=0;j<middle.size();j++){
				s.add(middle.get(j));
			}
			Collections.sort(s, new Comparator<int[]>(){
				public int compare(int []o1, int []o2){
					return o1[o1.length-1]-o2[o2.length-1];
				}
			});
		}
		for(int []a: s)result.add(a);
		return result;
	}
	public static List<int[]> queryEntity(int []query)throws Exception{
		List<int[]>result = new ArrayList<int[]>();
		if(query.length == 0)return result;
		if(query.length == 1){
			int [][]tmp = edges[query[0]].pairs_sorta;
			for(int []a:tmp)result.add(a);
			return result;
		}
		int [][]s = edges[query[0]].pairs_sortb;
		for(int i=1;i<query.length;i++){
			int p1 = 0,p2=0;
			int [][]b = edges[query[i]].pairs_sorta;
			//dwlog.write("middle length",s.length,b.length);
			{
				//debug s and b
				//for(int []s0: s)dwlog.write("s",s0[0],s0[1]);
				//for(int []b0:b)dwlog.write("b",b0[0],b0[1]);
			}
			List<int[]> middle = new ArrayList<int[]>();
			while(p1<s.length && p2<b.length){
				//dwlog.write("s",s[p1][0],s[p1][1],"b",b[p2][0],b[p2][1]);
				if(s[p1][1] == b[p2][0]){
					//dwlog.write("Here!!!");
					middle.add(new int[]{s[p1][0],b[p2][1]});
					p1++;p2++;
				}else if(s[p1][1]>b[p2][0]){
					p2++;
				}else{
					p1++;
				}
			}
			if(middle.size()==0)break;
			s = new int[middle.size()][2];
			for(int j=0;j<middle.size();j++){
				s[j][0] = middle.get(j)[0];
				s[j][1] = middle.get(j)[1];
			}
			Arrays.sort(s, new Comparator<int[]>(){
				public int compare(int []o1, int []o2){
					return o1[1]-o2[1];
				}
			});
		}
		for(int []a: s)result.add(a);
		return result;
	}
	
	public static void sortResult() throws IOException{
		Sort.sort(Setting.file_NellRel2FBPairs,Setting.file_NellRel2FBPairs+".sort",
				Setting.dir, new Comparator<String[]>(){
					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						StringBuilder sb1 = new StringBuilder();
						StringBuilder sb2 = new StringBuilder();
						for(String a: o1)sb1.append(a).append("\t");
						for(String a: o2)sb2.append(a).append("\t");
						return sb1.toString().compareTo(sb2.toString());
					}
		});
	}
	static HashMap<Integer,String>entity_name = new HashMap<Integer,String>(30000000);
	
	public static void giveEntityName_nellrel2fbpairs() throws IOException{
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
		{
			DelimitedReader dr = new DelimitedReader(Setting.file_NellRel2FBPairs+".sort");
			DelimitedWriter dw = new DelimitedWriter(Setting.file_NellRel2FBPairs+".sort.name");
			String []line;
			while((line = dr.read())!=null){
				int arg1 = Integer.parseInt(line[2]);
				int arg2 = Integer.parseInt(line[3]);
				String arg1Name = entity_name.get(arg1);
				String arg2Name = entity_name.get(arg2);
				if(arg1Name != null && arg2Name != null){
					dw.write(line[0],line[1],line[2],line[3],arg1Name,arg2Name);
				}else{
					System.err.println(arg1+"\t"+arg2);
				}
			}
			dr.close();
			dw.close();
		}
		
	}
	
	public static void giveEntityName() throws IOException{
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
		{
			DelimitedReader dr = new DelimitedReader(Setting.file_NellRel2FBPairs+".sort");
			DelimitedWriter dw = new DelimitedWriter(Setting.file_NellRel2FBPairs+".sort.name");
			String []line;
			
			while((line = dr.read())!=null){
				StringBuilder sb = new StringBuilder();
				for(int i=2;i<line.length;i++){
					int a = Integer.parseInt(line[i]);
					String ename = entity_name.get(a);
					if(ename == null){
						sb.append("NA|");
					}else{
						sb.append(ename+"|");
					}
				}
				dw.write(line[0],line[1],sb.toString());
			}
			
			dr.close();
			dw.close();
		}
		
	}
	
	
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
	//	System.setOut(new PrintStream("sb.out"));
	//	System.setErr(new PrintStream("sb.err"));
		//dwlog = new DelimitedWriter(Setting.log);
		//loadId2Name();

//		List<int[]>list=queryEntity(new int[]{252,1639});
//		result2str(list,dwlog);
		//dwlog.close();
		
		
	
		load2(Setting.graph2);
		queryAllNellWithWholePath();
		sortResult();
	
		giveEntityName();
		
	}

}
