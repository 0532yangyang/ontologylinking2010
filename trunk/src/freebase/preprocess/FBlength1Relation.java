package freebase.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeRead;
import javatools.filehandlers.MergeReadRes;
import javatools.filehandlers.MergeReadResStr;
import javatools.filehandlers.MergeReadStr;
import javatools.mydb.Sort;


public class FBlength1Relation {

	/**
	 * @param args
	 */
	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/length123";
	static String file0 = dir+"/fbGraph2.sort";
	static String file1 = dir + "/fbNodes";
	static String file1sort = dir + "/fbNodes.sortbymid";
	static String file2 = dir + "/zclid_namealias";
	static String file2sort = dir + "/zclid_namealias.sort";
	static String file3sort = dir + "/fbnamealias.sort";
	static String file4 = dir+"/length1.name";
	public static void sortFbNode() throws IOException {
		Sort.sort(file1, file1sort, dir, new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				return o1[0].compareTo(o2[0]);
			}

		});
	}

	public static void getZclidNamealias() throws Exception {
		DelimitedWriter dw = new DelimitedWriter(file2);
		MergeReadStr mrs = new MergeReadStr(file1sort, file3sort, 0, 0);
		MergeReadResStr mrrs;
		while ((mrrs = mrs.read()) != null) {
			List<String[]> fbnode = mrrs.line1_list;
			List<String[]> fbnamealias = mrrs.line2_list;
			for (String[] z : fbnode)
				for (String[] n : fbnamealias) {
					dw.write(z[1], n[1], n[2]);
				}

		}
		mrs.close();
		dw.close();
		{
			Sort.sort(file2, file2sort, dir, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					int x1 = Integer.parseInt(o1[0]);
					int x2 = Integer.parseInt(o2[0]);
					return x1 - x2;
				}

			});
		}
	}
	
	public static void getLength1name()throws Exception{
		String temp1 = dir+"/temp1";
		
		{
//			Sort.sort(file0, temp1,dir, new Comparator<String[]>(){
//				@Override
//				public int compare(String[] o1, String[] o2) {
//					int x1 = Integer.parseInt(o1[0]);
//					int x2 = Integer.parseInt(o2[0]);
//					return x1-x2;
//				}
//				
//			});
		}
		String temp2 = dir+"/temp2";
		
		{
			/**join zclid_namealias.sort with tempfileSortArg1, key: 0,0*/
			D.p("join zclid_namealias.sort with fbGraph2.sort into temp2");
			MergeRead mr = new MergeRead(file2sort,file0,0,0);
			MergeReadRes mrr;
			DelimitedWriter dw = new DelimitedWriter(temp2);
			while((mrr = mr.read())!=null){
				List<String[]> names = mrr.line1_list;
				List<String[]>relations = mrr.line2_list;
				for(String []n:names)for(String []r:relations){
					dw.write(r[0],r[1],r[2],n[1]);
				}
			}
			dw.close();
		}
		String temp3 = dir+"/temp3";
		{
			D.p("Sort temp2 into temp3");
			Sort.sort(temp2, temp3,dir, new Comparator<String[]>(){
				@Override
				public int compare(String[] o1, String[] o2) {
					int x1 = Integer.parseInt(o1[1]);
					int x2 = Integer.parseInt(o2[1]);
					return x1-x2;
				}
			});
		}
		{
			D.p("join zclid_namealias.sort with tempfileSortArg2");
			MergeRead mr = new MergeRead(file2sort,temp3,0,1);
			MergeReadRes mrr;
			DelimitedWriter dw = new DelimitedWriter(file4);
			while((mrr = mr.read())!=null){
				List<String[]> names = mrr.line1_list;
				List<String[]>relations = mrr.line2_list;
				for(String []n:names)for(String []r:relations){
					dw.write(r[0],r[1],r[2],r[3],n[1]);
				}
			}
			dw.close();
		}
		//(new File(tempfileSortArg1)).delete();
		//(new File(tempfileSortArg2)).delete();
	}

	public static void main(String[] args) throws Exception {
		// sortFbNode();
		//getZclidNamealias();
		getLength1name();
	}

}
