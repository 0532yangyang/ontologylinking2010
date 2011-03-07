package freebase.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javatools.administrative.D;
import javatools.string.StringUtil;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;


public class Nellseed2zclid_man {

	/**I got a match from RAW STRING to Freebase entities, but I am not sure whether this is a good match or not;
	 * in order to make sure the next few steps bring me good sense that my methods are working,
	 * I need to manually check the matching
	 * INPUT: Google	YouTube	acquired	/m/045c7b	/m/09jcvs
	 * OUTPUT: Google	YouTube	acquired	/m/045c7b	/m/09jcvs	google 7098406	youtube 6746771
	 * 
	 * where google & youtube are the name of /m/045c7b and /m/09jcvs; I can then check whether auto match is correct
	 * where 7098406 & 6746771 are the wikipedia id of Google and Youtube
	 * */
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink";
	static String input1 = dir+"/mid2WikiId.sbmid";
	static String input2 = dir+"/nellseed2zclid";
	static String output = dir+"/nellseed2zclid_man";
	
	static String input3 = dir+"/nellseed2zclidNeg";
	static String output2 = dir+"/nellseed2zclidNeg_man";
	
	static HashMap<String,List<String[]>>mid2WikiIdStr = new HashMap<String,List<String[]>>();
	public static void preprocess()throws IOException{
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
		}	
	}
	public static void processNeg() throws IOException{
		{
			D.p("Start matching Neg");
			DelimitedReader dr = new DelimitedReader(input3);
			DelimitedWriter dw = new DelimitedWriter(output2);
			String []line;
			while((line = dr.read())!=null){
				StringBuilder sb1 = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				if(line.length<5)continue;
				if(mid2WikiIdStr.containsKey(line[5])){
					List<String[]>list = mid2WikiIdStr.get(line[5]);
					double max = -1;
					String t[] = new String[3];
					for(String[]a:list){
						int same = StringUtil.numOfShareWords(line[2], a[2]);
						double s = same*1.0/a[2].length();
						if(s>max){
							t = a;
							max = s;
						}
					}
					sb1.append(t[1]+" "+t[2]+";");
				}
				if(mid2WikiIdStr.containsKey(line[6])){
					List<String[]>list = mid2WikiIdStr.get(line[6]);
					double max = -1;
					String t[] = new String[3];
					for(String[]a:list){
						int same = StringUtil.numOfShareWords(line[3], a[2]);
						double s = same*1.0/a[2].length();
						if(s>max){
							t = a;
							max = s;
						}
					}
					sb2.append(t[1]+" "+t[2]+";");
				}
				line[4] = "*"+line[4];
				dw.write(line[4],line[2],line[3],line[5],line[6],sb1.toString(),sb2.toString());
			}
			dr.close();
			dw.close();
		}
	}
	public static void processPos()throws IOException{
		{
			D.p("Start matching");
			DelimitedReader dr = new DelimitedReader(input2);
			DelimitedWriter dw = new DelimitedWriter(output);
			String []line;
			while((line = dr.read())!=null){
				StringBuilder sb1 = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				if(line.length<5)continue;
				if(mid2WikiIdStr.containsKey(line[5])){
					List<String[]>list = mid2WikiIdStr.get(line[5]);
					double max = -1;
					String t[] = new String[3];
					for(String[]a:list){
						int same = StringUtil.numOfShareWords(line[2], a[2]);
						double s = same*1.0/a[2].length();
						if(s>max){
							t = a;
							max = s;
						}
					}
					sb1.append(t[1]+" "+t[2]+";");
				}
				if(mid2WikiIdStr.containsKey(line[6])){
					List<String[]>list = mid2WikiIdStr.get(line[6]);
					double max = -1;
					String t[] = new String[3];
					for(String[]a:list){
						int same = StringUtil.numOfShareWords(line[3], a[2]);
						double s = same*1.0/a[2].length();
						if(s>max){
							t = a;
							max = s;
						}
					}
					sb2.append(t[1]+" "+t[2]+";");
				}
				line[4] = "*"+line[4];
				dw.write(line[4],line[2],line[3],line[5],line[6],sb1.toString(),sb2.toString());
			}
			dr.close();
			dw.close();
		}
	}
	public static void main(String[] args) throws Exception{
		preprocess();
		//processPos();
		processNeg();
		
	}

}
