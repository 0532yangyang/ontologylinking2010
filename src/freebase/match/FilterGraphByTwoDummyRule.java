package freebase.match;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;


/**Let us assume at most two dummies between Two objects*/
public class FilterGraphByTwoDummyRule {
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/";
	static String seedfile = "/projects/pardosa/s5/clzhang/ontologylink/nellseed2myid";
	static String graphfile = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph2";
	static String output= "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbGraph3";
	static HashSet<Integer>seeds = new HashSet<Integer>();
	static HashSet<Integer>levelTwoSeeds = new HashSet<Integer>();
	static void loadseeds()throws Exception{
		List<String[]> all = (new DelimitedReader(seedfile)).readAll();
		for(String []a: all){
			seeds.add(Integer.parseInt(a[0]));
			seeds.add(Integer.parseInt(a[1]));
		}
	}
	static void loadLevelTwoSeeds()throws Exception{
		if(seeds.size() == 0)loadseeds();
		DelimitedReader dr = new DelimitedReader(graphfile);
		String []line;
		int ln = 0;
		while((line = dr.read())!=null){
			if(ln++ % 1000000 == 0)System.out.print(ln+"\r");
			int arg1 = Integer.parseInt(line[0]);
			int arg2 = Integer.parseInt(line[1]);
			if(seeds.contains(arg1) || seeds.contains(arg2)){
				levelTwoSeeds.add(arg1);
				levelTwoSeeds.add(arg2);
			}
		}
		System.out.println("Total Number of level two seeds\t"+levelTwoSeeds.size());
	}
	static void filter()throws Exception{
		String tempOutput = output+".tmp";
		DelimitedReader dr = new DelimitedReader(graphfile);
		DelimitedWriter dw= new DelimitedWriter(tempOutput);
		String []line;
		while((line = dr.read())!=null){
			int arg1 = Integer.parseInt(line[0]);
			int arg2 = Integer.parseInt(line[1]);
			if(levelTwoSeeds.contains(arg1)||levelTwoSeeds.contains(arg2)){
				dw.write(line);
			}
		}
		dr.close();
		dw.close();
		
		Sort.sort(tempOutput, output, dir, new Comparator<String[]>(){
			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				int a0 = Integer.parseInt(o1[0]),a1= Integer.parseInt(o1[1]),a2 = Integer.parseInt(o1[2]);
				int b0 = Integer.parseInt(o2[0]),b1= Integer.parseInt(o2[1]),b2 = Integer.parseInt(o2[2]);
				if(a0 == b0){
					if(a1== b1){
						return a2-b2;
					}else{
						return a1-b1;
					}
				}else{
					return a0-b0;
				}
			}
		});
		(new File(tempOutput)).delete();
	}
	public static void main(String []args)throws Exception{
		loadLevelTwoSeeds();
		filter();
	}
}
