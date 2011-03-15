package freebase.match;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class DetectGabageEdge2 {

	/**
	 * @param args
	 */

	static final int MAX = 20000;
	static int []count=new int[MAX];
	static String[] relation_name = new String[MAX];
	static String edgefile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges";
	static String fbgraphfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph";
	static String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/badrelations";
	public static void loadEdges() throws Exception {
		DelimitedReader dr = new DelimitedReader(edgefile);
		String[] line;
		while ((line = dr.read()) != null) {
			relation_name[Integer.parseInt(line[1])] = line[0];
		}
		// for(String []a:all)
	}
	public static void load()throws Exception{
		DelimitedReader dr = new DelimitedReader(fbgraphfile);
		String []line;
		while((line = dr.read())!=null){
			int relid = Integer.parseInt(line[2]);
			count[relid]++;
		}
		dr.close();
	}
	public static void print()throws Exception{
		DelimitedWriter dw = new DelimitedWriter(output);
		for(int i=0;i<MAX;i++){
			if(relation_name[i]!=null){
				dw.write(i+"\t"+relation_name[i]+"\t"+count[i]);
			}
		}
		dw.close();
	}
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
//		loadEdges();
//		load();
//		print();
	}

}
