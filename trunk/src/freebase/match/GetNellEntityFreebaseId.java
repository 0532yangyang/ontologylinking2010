package freebase.match;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;


/**My goal is to get the freebase entity id of nell seed instances;
 * 
 * for example, LeBron James --> /m/01jz6d*/

public class GetNellEntityFreebaseId {
	static HashMap<String,List<Integer>>nellObj = new HashMap<String,List<Integer>>();
	static void step1() throws IOException{
		NellOntology no = new NellOntology();
		
		for(NellRelation nr: no.nellRelationList){
			for(String []a: nr.seedInstances){
				nellObj.put(a[0].trim(),new ArrayList<Integer>());
				nellObj.put(a[1].trim(),new ArrayList<Integer>());
			}
		}
		
	}
	
	static void step2() throws IOException{
		String file = "/projects/pardosa/data14/raphaelh/t/raw/freebase-names";
		String input1 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbNodes";
		String input2 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges";
		String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellObjMid";

		

		HashMap<String,Integer>nodeid = new HashMap<String,Integer>();
		{
			DelimitedReader dr = new DelimitedReader(input1);
			String []line;
			int ln = 0;
			while((line = dr.read())!=null){
				if(ln ++ %1000000 == 0)System.out.print(ln+"\r");
				nodeid.put(line[0],Integer.parseInt(line[1]));
			}
		}
		System.out.println("Start Matching!!!");
		DelimitedReader dr = new DelimitedReader(file);
		String []line;
		int ln = 0;
		while((line = dr.read())!=null){
			if(ln ++ %1000000 == 0)System.out.print(ln+"\r");
			String name = line[1].trim();
			if(nellObj.containsKey(name)){
				String fbMid = line[0];
				int zclid = nodeid.get(fbMid);
				nellObj.get(name).add(zclid);
			}
		}
		{
			DelimitedWriter dw = new DelimitedWriter(output);
			for(Entry<String,List<Integer>>e: nellObj.entrySet()){
				dw.write(e.getKey(),e.getValue());
			}
			dw.close();
		}
	}
	public static void main(String []args) throws IOException{
		step1();
		step2();
	}
}
