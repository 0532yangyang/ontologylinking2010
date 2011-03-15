package javatools.filehandlers;

import java.io.*;



public class MergeReadStr {
	/**column_i is the key column of file_i*/
	DelimitedReader dr1=null;
	DelimitedReader dr2=null;
	int key1;
	int key2;
	String []buf1;
	String []buf2;
	int lastKey = Integer.MIN_VALUE;
	String curKey1,curKey2;
	public MergeReadStr(String file1,String file2,int key1,int key2)throws Exception{
		dr1 = new DelimitedReader(file1);
		dr2 = new DelimitedReader(file2);
		this.key1=key1;
		this.key2 = key2;
		buf1 = dr1.read();
		//curKey1 = Integer.parseInt(buf1[key1]);
		curKey1 = buf1[key1];
		buf2 = dr2.read();
		//curKey2 = Integer.parseInt(buf2[key2]);
		curKey2 = buf2[key2];
	}
	
	private String takeBigStr(String a,String b){
		if(a.compareTo(b)>=0){
			return a;
		}else{
			return b;
		}
	}
	
	public MergeReadResStr read()throws Exception{
		if(buf1 == null || buf2 == null){
			return null;
		}
		//tring id = Math.max(curKey1, curKey2);
		String id = takeBigStr(curKey1,curKey2);
		
		MergeReadResStr res;
		String oldid;
		do{
			res = read(id);
			oldid = id;
			id = takeBigStr(curKey1, curKey2);
			if(id.equals(oldid))break;
		}while(! res.key_value.equals(oldid) );
		if(! res.key_value.equals("")){
			return res;
		}else{
			return null;
		}
	}

	private MergeReadResStr read(String id) throws Exception{
		MergeReadResStr res = new MergeReadResStr();
		while(curKey1.compareTo(id)<0 && (buf1=dr1.read())!=null){
			//buf1 = dr1.read();
			curKey1 = buf1[key1];
		}
		while(curKey2.compareTo(id)<0 && (buf2=dr2.read())!=null){
			//buf1 = dr1.read();
			curKey2 = buf2[key2];
		}
		//if(buf1 == null || buf2 == null) return null;
		setCurKey();
		
		if(curKey1.equals( id) && curKey2.equals(id)){
			res.key_value = id;
			do{
				if(buf1 == null)continue;
				String []temp = new String[buf1.length];
				System.arraycopy(buf1, 0, temp, 0, buf1.length);
				res.line1_list.add(temp);
			}while((buf1=dr1.read())!=null && buf1[key1].compareTo(id)==0);
			
			do{
				if(buf2==null)continue;
				String []temp = new String[buf2.length];
				System.arraycopy(buf2, 0, temp, 0, buf2.length);
				res.line2_list.add(temp);
			}while((buf2=dr2.read())!=null && buf2[key2].compareTo(id)==0);	
		}else{
			res.key_value = "";
		}
		setCurKey();
		
		return res;
	}
	
	private void setCurKey(){
		if(buf1!=null && buf2 !=null){
			curKey1 = buf1[key1];
			curKey2 = buf2[key2];
		}
	}
	public void close()throws Exception{
		this.dr1.close();
		this.dr2.close();
	}
	/**
	 * @param args
	 * 

	 */
//	public static void main(String[] args) throws Exception{
//		// TODO Auto-generated method stub
//		MergeReadStr mr = new MergeReadStr(args[0],args[1],0,0);
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(args[2]),"utf-8");
//		MergeReadResStr mrr;
//		int ln = 0;
//		while((mrr = mr.read())!=null ){
//			if(mrr.key_value.equals("345")){
//				System.out.println();
//			}
//			//System.out.println(mrr.key_value);
//			Debug.processDebug(ln++, 10000);
//			String subjectName = mrr.line1_list.get(0)[0];
//			osw.write(mrr.key_value+"\r\n");
//
//		}
//		osw.close();
//	}
	
//	public static void main(String[] args) throws Exception{
//	// TODO Auto-generated method stub
//	MergeRead mr = new MergeRead(args[0],args[1],0,0);
//	OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(args[2]),"utf-8");
//	MergeReadRes mrr;
//	int ln = 0;
//	while((mrr = mr.read())!=null ){
//		System.out.println(mrr.key_value);
////		String subjectName = mrr.line1_list.get(0)[2];
////		for(String []line : mrr.line2_list){
////			osw.write(mrr.key_value+"\t"+subjectName+"\t"+line[4]+"\t"+line[5]+"\t"+line[6]+"\n");
////		}
//	}
//	osw.close();
//}

}
