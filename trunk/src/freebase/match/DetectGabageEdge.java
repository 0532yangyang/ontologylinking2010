package freebase.match;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

/**This algorithm */
public class DetectGabageEdge {

	/**
	 * @param args
	 * @throws IOException 
	 */
	static String []relation_name = new String[20000];
	static HashSet<Integer>bad = new HashSet<Integer>();
	static String badrelationsfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/badrelations";
	public static void load(String file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		
		while((line= br.readLine())!=null){
			String []ab = line.trim().split("\\s");
			int count = Integer.parseInt(ab[0]);
			int eid = Integer.parseInt(ab[1]);
			if(count>300){
				bad.add(eid);
				//System.out.println(eid);
			}
		}
		DelimitedWriter dw = new DelimitedWriter(badrelationsfile);
		for(int x:bad){
			dw.write(relation_name[x]);
		}
		dw.close();
	}
	
	public static void loadEdges(String file)throws Exception{
		DelimitedReader dr = new DelimitedReader(file);
		String []line;
		while((line =dr.read())!=null){
			relation_name[Integer.parseInt(line[1])] = line[0];
		}
		//for(String []a:all)
	}
	static String graphfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph";
	static String newgraph = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph2";
	public static void rewriteGraph()throws Exception{
		DelimitedReader dr = new DelimitedReader(graphfile);
		DelimitedWriter dw = new DelimitedWriter(newgraph);
		String []line;
		int ln = 0;
		System.out.println("Rewrite the graph!!!");
		while((line = dr.read())!=null){
			if(ln ++ % 100000 == 0)System.out.print(ln+"\r");
			if(line.length<3)continue;
			if(!bad.contains(Integer.parseInt(line[2]))){
				dw.write(line);
			}
		}
		dw.close();
		dr.close();
		
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		loadEdges("/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges");
//		load("/projects/pardosa/s5/clzhang/ontologylink/tmp2/edgeCount.tmp");
//		System.out.println(bad.size());
//		rewriteGraph();
	}

}
