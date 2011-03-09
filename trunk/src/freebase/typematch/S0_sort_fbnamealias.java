package freebase.typematch;

import java.util.Comparator;

import multir.util.delimited.Sort;

public class S0_sort_fbnamealias {
	public static void main(String []args){
		try{
			Sort.sort(Main.fin_fbnamealias_unsorted,Main.fin_fbnamealias, Main.dir,new Comparator<String[]>(){

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					return o1[0].compareTo(o2[0]);
				}
				
			});
		}catch(Exception e){
			
		}
	}
}
