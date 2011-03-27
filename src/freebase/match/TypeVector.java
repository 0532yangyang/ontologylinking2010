package freebase.match;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;

import multir.util.HashList;

public class TypeVector {

	public static void step0() throws IOException{
		Sort.sort(Setting.file_freebasetype_notsort, Setting.file_freebasetype, Setting.dir, 
				new Comparator<String[]>(){

					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						return o1[0].compareTo(o2[0]);
					}
					
				});
	}
	
	/**Get mid --> t1 t2 t3 t4...*/
	public static void step1(){
		try{
			HashList<String> hl = new HashList<String>();
			//HashCount<Integer>hc = new HashCount<Integer>();
			DelimitedReader dr = new DelimitedReader(Setting.file_freebasetype);
			DelimitedWriter dw = new DelimitedWriter(Setting.file_freebasetype_vector+".temp");
			String []line;
			String last = "";
			List<Integer>tmp = new ArrayList<Integer>();
			int ln = 0;
			while((line = dr.read())!=null){
				//if(ln++>10000)break;
				String mid = line[0];
				String type = line[1];
				int typeid = hl.add(type);
				if(!mid.equals(last)){
					//write out
					if(tmp.size()>0){
						StringBuilder sb = new StringBuilder();
						Collections.sort(tmp);
						for(int a: tmp)sb.append(a+" ");
						dw.write(last,sb.toString());
					}
					tmp.clear();
					last = line[0];
				}
				tmp.add(typeid);
			}
			dr.close();
			dw.close();
			{
				DelimitedWriter dw2 = new DelimitedWriter(Setting.file_freebasetype_name+".temp");
				for(int i=0;i<hl.size();i++){
					dw2.write(i,hl.getElement(i));
				}
				dw2.close();
			}
		}catch(Exception e){}
	}
	
	/**Count the DF of every FB type*/
	public static void step2()throws IOException{
		try{
			//count the number of every fb type
			HashCount<Integer> hc = new HashCount<Integer>();
			DelimitedReader dr = new DelimitedReader(Setting.file_freebasetype_vector);
			
			String []line;
			while((line = dr.read())!=null){
				String mid = line[0];
				String []features = line[1].split(" ");
				int []fs = new int[features.length];
				for(int i=0;i<features.length;i++){
					fs[i] = Integer.parseInt(features[i]);
					hc.add(fs[i]);
				}
			}
			dr.close();
			dr = new DelimitedReader(Setting.file_freebasetype_name+".temp");
			DelimitedWriter dw = new DelimitedWriter(Setting.file_freebasetype_name);
			while((line = dr.read())!=null){
				int typeid = Integer.parseInt(line[0]);
				String typestr = line[1];
				int count = hc.see(typeid);
				dw.write(typeid,typestr,count);
			}
			dw.close();
//			dr = new DelimitedReader(Setting.file_freebasetype_vector+".temp");
//			DelimitedWriter dw = new DelimitedWriter(Setting.file_freebasetype_vector);
//			while((line = dr.read())!=null){
//				String mid = line[0];
//				StringBuilder sb = new StringBuilder();
//				String []features = line[1].split(" ");
//				int []fs = new int[features.length];
//				for(int i=0;i<features.length;i++){
//					fs[i] = Integer.parseInt(features[i]);
//					double weight = 1.0/Math.log(hc.see(fs[i]));
//					sb.append(fs[i]+":"+weight+" ");
//				}
//				dw.write(mid,sb.toString());
//			}
//			dr.close();
//			dw.close();
		}catch(Exception e){}
	}
	
	public static void main(String[] args) throws IOException {
		//step0();
		//step1();
		step2();
	}

}
