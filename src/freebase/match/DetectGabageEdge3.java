package freebase.match;

import java.util.HashSet;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class DetectGabageEdge3 {

	/**
	 * @param args
	 */

	static final int MAX = 20000;
	static int []count=new int[MAX];
	static String[] relation_name = new String[MAX];
	static String edgefile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges";
	static String fbgraphfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph";
	static String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph2";
	static HashSet<Integer>bad = new HashSet<Integer>();
	
	public static void loadEdges() throws Exception {
		DelimitedReader dr = new DelimitedReader(edgefile);
		String[] line;
		while ((line = dr.read()) != null) {
			relation_name[Integer.parseInt(line[1])] = line[0];
		}
		for(int i=0;i<relation_name.length;i++){
			if(relation_name[i] == null)continue;
			if(relation_name[i].startsWith("/type")
					||relation_name[i].startsWith("/freebase")
					||relation_name[i].startsWith("/common")
					||relation_name[i].startsWith("/base")
					||relation_name[i].startsWith("/user")){
				bad.add(i);
				
			}
		}
	}
	public static void load()throws Exception{
		DelimitedReader dr = new DelimitedReader(fbgraphfile);
		DelimitedWriter dw = new DelimitedWriter(output);
		String []line;
		while((line = dr.read())!=null){
			int relid = Integer.parseInt(line[2]);
			if(!bad.contains(relid)){
				dw.write(line);
			}
			
		}
		dw.close();
		dr.close();
	}
	
	public static void main(String[] args) throws Exception{
//		loadEdges();
//		load();
	}

}
