package freebase.match;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import multir.util.HashCount;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

/**Map nell type into freebase type
 * Give up freebase type starting with /user /base
 * Filter away freebase types that has less than 10 instances
 * */


public class NellTypeMap2FBType {
	
	static HashMap<String,int[]>mid2Type = new HashMap<String,int[]>(1000000);
	//id --> name & count
	static HashMap<Integer,String[]>fbTypeDf = new HashMap<Integer,String[]>();
	static NellOntology no;
	
	//Nell type map to a list of mids, which are instances of that nell type
	static HashMap<String,Set<String>>nelltype2Midlist = new HashMap<String,Set<String>>();
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink";
	public static void load()throws Exception{
		String []line;
		{
			System.out.println("Load Freebase Type Vector");
			String file  = dir+"/freebase_type_vector";
			DelimitedReader dr = new DelimitedReader(file);
			int ln = 0;
			while((line = dr.read())!=null){
				//if(ln++>1000000)break;
				String mid = line[0];
				String []tid_str = line[1].split(" ");
				int []tids = new int[tid_str.length];
				for(int i=0;i<tids.length;i++)tids[i] = Integer.parseInt(tid_str[i]);
				mid2Type.put(mid,tids);
			}
			dr.close();
		}
		{
			System.out.println("Load Freebase Type Name");
			String file =dir+ "/freebase_type_name";
			DelimitedReader dr = new DelimitedReader(file);
			dr.read();
			while((line = dr.read())!=null){
				int id = Integer.parseInt(line[0]);
				fbTypeDf.put(id, line);
			}
			dr.close();
		}
		{
			System.out.println("Load Nell type map to a list of mids");
			String file =dir+ "/tmp2/nellseed2zclid";
			DelimitedReader dr = new DelimitedReader(file);
			while((line = dr.read())!=null){
				String rel = line[4];
				String mid1 = line[5],mid2 = line[6];
				String []domainrange = no.relname2DomainRange.get(rel);
				if(!nelltype2Midlist.containsKey(domainrange[0])){
					nelltype2Midlist.put(domainrange[0], new HashSet<String>());
				}
				if(!nelltype2Midlist.containsKey(domainrange[1])){
					nelltype2Midlist.put(domainrange[1], new HashSet<String>());
				}
				nelltype2Midlist.get(domainrange[0]).add(mid1);
				nelltype2Midlist.get(domainrange[1]).add(mid2);
			}
			dr.close();
		}
	}
	public static void count() throws Exception{
		//iterate through every nell type
		class IdVal{
			int id;
			double value;
			public IdVal(int id,double val){this.id = id;this.value = val;}
		}
		String output = dir+"/tmp2/nellTypeMap2FBType";
		DelimitedWriter dw = new DelimitedWriter(output);
		for(Entry<String,Set<String>>e: nelltype2Midlist.entrySet()){
			HashCount<Integer>typecount = new HashCount<Integer>();
			String nellTypeName = e.getKey();
			for(String mid: e.getValue()){
				int []midtypeinfo = mid2Type.get(mid);
				if(midtypeinfo== null || midtypeinfo.length==0){
					System.err.println("No type info" + mid);
					continue;
				}
				for(int x: midtypeinfo){
					typecount.add(x);
				}
			}
			List<Integer>tids = new ArrayList<Integer>();
			List<Integer>tcs = new ArrayList<Integer>();
			typecount.getAll(tids, tcs);
			StringBuilder sb = new StringBuilder();
			List<IdVal>ivlist = new ArrayList<IdVal>();
			for(int i=0;i<tids.size();i++){
				int tid = tids.get(i);
				int tc = tcs.get(i);
				int df = 0;
				if(fbTypeDf.containsKey(tid)){
					df = Integer.parseInt(fbTypeDf.get(tid)[2]);
				}
				double tcOverDf = tc*1.0/Math.log(df+100);
				IdVal iv = new IdVal(tid,tcOverDf);
				ivlist.add(iv);
			}
			Collections.sort(ivlist,new Comparator<IdVal>(){
				@Override
				public int compare(IdVal o1, IdVal o2) {
					// TODO Auto-generated method stub
					return o2.value-o1.value>0?1:-1;
				}
			});
			for(int i=0;i<ivlist.size();i++){
				IdVal iv = ivlist.get(i);
				String type = fbTypeDf.get(iv.id)[1];
				DecimalFormat format = new DecimalFormat("#.###");
				if(filter(iv.id)){
					sb.append(iv.id+":"+format.format(iv.value)+":"+type+" ");
				}
			}
			dw.write(nellTypeName,sb.toString());
		}
		dw.close();
	}
	
	static boolean filter(int typeid){
		String []line = fbTypeDf.get(typeid);
		if(line == null || line.length ==0)return false;
		int count = Integer.parseInt(line[2]);
		if(count <10)return false;
		String typeName = line[1];
		if(typeName.startsWith("/base")|| typeName.startsWith("/user"))return false;
		return true;
	}
	public static void main(String []args)throws Exception{
		no = new NellOntology();
		load();
		count();
	}
}
