package freebase.preprocess;

import java.io.IOException;
import java.util.Comparator;

import percept.util.delimited.Sort;

public class Freebasedumpsort {
	public static void main(String []args) throws IOException{
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String input = dir+"/freebase-datadump-quadruples.tsv";
		String output = dir+"/freebasedump.sort";
		Sort.sort(input, output, dir, new Comparator<String[]>(){

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[0].compareTo(o2[0]);
			}
			
		});
	}
}
