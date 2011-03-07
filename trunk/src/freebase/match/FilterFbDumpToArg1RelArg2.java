package freebase.match;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

/**Store the freebase dump as a linked graph;
 * Id and edge are encoded in numbers
 * The result is in /projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph*/

public class FilterFbDumpToArg1RelArg2 {
	
	public static void step2() throws IOException{
		HashMap<String,Integer>edges = new HashMap<String,Integer>();
		HashMap<String,Integer>nodes = new HashMap<String,Integer>();
		String file = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/filterDump";
		String output1 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph2";
		String output2 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbNodes2";
		String output3 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges2";
		DelimitedReader dr = new DelimitedReader(file);
		DelimitedWriter dw = new DelimitedWriter(output1);
		String []line;
		int ln = 0;
		while((line = dr.read())!=null){
			if(ln++%1000000 == 0)System.out.print(ln+"\r");
			String arg1 = line[0];
			String arg2 = line[2];
			String rel = line[1];
			int arg1id = step2_1(nodes,arg1), arg2id = step2_1(nodes,arg2), relid = step2_1(edges,rel);
			dw.write(arg1id,arg2id,relid);
		}
		dw.close();
		dr.close();
		{
			DelimitedWriter dw0  = new DelimitedWriter(output2);
			for(Entry<String,Integer>e: nodes.entrySet()){
				dw0.write(e.getKey(),e.getValue());
			}
			dw0.close();
		}
		{
			DelimitedWriter dw0  = new DelimitedWriter(output3);
			for(Entry<String,Integer>e: edges.entrySet()){
				dw0.write(e.getKey(),e.getValue());
			}
			dw0.close();
		}
	}
	
	private static int step2_1(HashMap<String,Integer>hash, String one){
		if(!hash.containsKey(one)) hash.put(one, hash.size());
		return hash.get(one);
	}
	/**Filter away entrees not an edge*/
	public static void step1() throws IOException{
		String file = "/projects/pardosa/data10/raphaelh/freebase/freebase-datadump-quadruples.tsv";
		DelimitedWriter dw = new DelimitedWriter("/projects/pardosa/s5/clzhang/ontologylink/tmp2/filterDump");
		
		DelimitedReader dr = new DelimitedReader(file);
		String []line;
		int ln=0;
		while((line = dr.read())!=null){
			if(ln++%10000 == 0)System.out.print(ln+"\r");
			//if(ln>1000000)break;
			if(line[3].length()!=0) continue;
			if(!line[0].startsWith("/m/") || !line[2].startsWith("/m/")) continue;
			dw.write(line);
		}
		dr.close();
		dw.close();	
	}
	
	public static void main(String []args) throws IOException{
		//step1();
		step2();
	}
}
