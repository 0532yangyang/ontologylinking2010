package freebase.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;


public class Evaluation {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink";
	static String input1 = dir+"/mid2WikiId.sbmid";

	static HashMap<String,List<String[]>>mid2WikiIdStr = new HashMap<String,List<String[]>>();

	private static void preprocess()throws IOException{
		{
			DelimitedReader dr = new DelimitedReader(input1);
			String []line;
			while((line = dr.read())!=null){
				if(!mid2WikiIdStr.containsKey(line[0])){
					mid2WikiIdStr.put(line[0],new ArrayList<String[]>());
				}
				mid2WikiIdStr.get(line[0]).add(line);
			}
			dr.close();
			D.p("Please input string");
		}	
	}
	/**given mid, search the related wikipedia articles*/
	public static void searchMid_main()throws IOException{
		preprocess();
		while(true){
			D.p("Please input string");
			String in = D.r();
			List<String[]> list = mid2WikiIdStr.get(in);
			if(list == null){
				continue;
			}	
			for(String []a: list){
				D.p(a);
			}
		}
	}
	/**print NellEntity to Wikiarticle then I can manually change them*/
	public static void printNellEntity2Wikiarticle()throws IOException{
		String file = "/projects/pardosa/s5/clzhang/ontologylink/nellseed2zclid_man";
		List<String[]> all = (new DelimitedReader(file)).readAll();
		HashMap<String,String[]>str2other = new HashMap<String,String[]>();
		for(String []a:all){
			//arg1
			if(Character.isLowerCase(a[1].toCharArray()[0])){
				str2other.put(a[1],new String[]{a[3],a[5]});
			}
			if(Character.isLowerCase(a[2].toCharArray()[0])){
				str2other.put(a[2],new String[]{a[4],a[6]});
			}
			//
		}
		List<String[]> tosort = new ArrayList<String[]>();
		for(Entry<String,String[]>e: str2other.entrySet()){
			String []temp = new String[3];
			temp[0] = e.getKey();
			temp[1] = e.getValue()[0];
			temp[2] = e.getValue()[1];
			tosort.add(temp);
		}
		Collections.sort(tosort,new Comparator<String[]>(){

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[2].compareTo(o2[2]);
			}
			
		});
		int size = 0;
		for(String[]a: tosort){
			if(a[2].length()==0){
				size++;
			}
			D.p(a);
		}
		D.p(size);
	}
	public static void main(String []args) throws IOException{
		//preprocess();
		//searchMid_main();
		printNellEntity2Wikiarticle();
	}
}
