package freebase.jointmatch3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.filehandlers.DelimitedReader;


public class NellRelation {

	/**
	 * @param args
	 */

	public String relation_name;
	public boolean populate;
	public String generalizations;
	public String domain;
	public String range;
	public boolean antisymmetric;
	public 	boolean maintainInverse;
	public 	String mutexExceptions;
	public 	List<String[]> known_negatives;
	public 	String masterInverse;
	public 	String inverse;
	public 	List<String[]> seedInstances;
	public boolean symmetric;
	public String nrOfValues;

	public NellRelation(String []line){
		relation_name = line[0];
		populate = line[1].equals("TRUE")?true:false;
		generalizations = line[2];
		domain = line[3];
		range = line[4];
		antisymmetric = line[5].equals("TRUE")?true:false;
		maintainInverse = line[6].equals("TRUE")?true:false;
		mutexExceptions = line[7];
		known_negatives = parseList(line[8]); 
		masterInverse = line[9];
		inverse = line[10];
		seedInstances = parseList(line[11]);
		symmetric = line[12].equals("TRUE")?true:false;
		nrOfValues = line[13];
	}
	
	private List<String[]> parseList2(String a){
		List<String[]> res = new ArrayList<String[]>();
		 Pattern p = Pattern.compile("\"([^\"]*)\",\"([^\"]*)\"");
		 Matcher m = p.matcher(a);
		 while(m.find()){
			 String arg1 = m.group(1);
			 String arg2 = m.group(2);
			 String []tmp = new String[]{arg1,arg2};
			 res.add(tmp);
		 }
		return res;
	}
	private List<String[]> parseList(String a){
		List<String> entities = new ArrayList<String>();
		List<String[]> res = new ArrayList<String[]>();
		char[]s = a.toCharArray();
		int last = -1;
		for(int i=0;i<s.length;i++){
			if(s[i] == '"'){
				if(last == -1){
					last = i;
				}else{
					entities.add(a.substring(last+1, i));
					last = -1;
				}
			}
		}
		if(entities.size() %2 != 0){
			System.err.println("Error");
		}else{
			for(int i=0;i<entities.size();i+=2){
				res.add(new String[]{entities.get(i),entities.get(i+1)});
			}
			
		}
		return res;

	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(relation_name+"\t"+ 	domain+"\t"+range+"\t");
		for(String []a: this.seedInstances){
			sb.append(a[0]+":"+a[1]+"; ");
		}
		return sb.toString();
	}
	public static List<NellRelation> loadNellRelationFile(String file) throws IOException{
		List<NellRelation>result = new ArrayList<NellRelation>();
		DelimitedReader dr = new DelimitedReader(file);
		String []line;
		while((line = dr.read())!=null){
			NellRelation nro = new NellRelation(line);
			result.add(nro);
		}
		dr.close();
		return result;
	}
	public static List<NellRelation> loadNellRelationFile() throws IOException{
		List<NellRelation>result = new ArrayList<NellRelation>();
		String file = "/projects/pardosa/s5/clzhang/ontologylink/nell/relations.nell.seed";
		DelimitedReader dr = new DelimitedReader(file);
		String []line;
		while((line = dr.read())!=null){
			NellRelation nro = new NellRelation(line);
			result.add(nro);
		}
		dr.close();
		return result;
	}
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		List<NellRelation> nrellist = loadNellRelationFile();
		for(NellRelation nro: nrellist){
			System.out.println(nro.toString());
		}
	}

}
