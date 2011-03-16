package freebase.maxent;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import percept.util.delimited.Sort;

import freebase.maxent.Featurizer.FeaturizerConfig;


import javatools.administrative.D;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.MergeRead;
import javatools.filehandlers.MergeReadRes;

public class LearnFirstPassInstance {

	static String dir = "/projects/pardosa/s5/clzhang/ontologylink/entitymaxent";
	static String file_trainseeds = dir + "/enid_mid_wid_argname_otherarg_relation_label.sortbyWid";
	static String file_wpsen = dir+"/sentences.stanford.tokenized.sortbywid.subset";
	static String file_featuretrainseeds = dir+"/nellseeds:label_wid_nellrel_arga_argb_object_features";
	static String file_featuretrainseeds_sort = file_featuretrainseeds+".sortbynellrel";
	
	
	public static void main(String []args)throws Exception{
		//wpsensubset();
		featureize();
		evaluation();
	}
	
	public static void featureize()throws Exception{
		DelimitedReader dr_trainseeds = new DelimitedReader(file_trainseeds);
		DelimitedReader dr_wpsen = new DelimitedReader(file_wpsen);
		DelimitedWriter dw = new DelimitedWriter(file_featuretrainseeds);
		List<RecordWpSenToken> list_wpsen = RecordWpSenToken.readByArticleId(dr_wpsen);
		String []trainseed;
		while((trainseed = dr_trainseeds.read())!=null){
			int articleId = Integer.parseInt(trainseed[2]);
			String argA = trainseed[3];
			String argB = trainseed[4];
			String relation = trainseed[5];
			String label = trainseed[6];
			String argA_is_arg1orArg2 = trainseed[7];
			while(list_wpsen.get(0).articleId < articleId){
				list_wpsen = RecordWpSenToken.readByArticleId(dr_wpsen);
			}
			if(list_wpsen.get(0).articleId == articleId){
				//3384201
				if(list_wpsen.get(0).toString().toLowerCase().startsWith("redirect")){
					continue;
				}
				if(argA.equals("Berkshire Hathaway") && argB.equals("NetJets")){
					System.out.print("");
				}
				if(articleId == 8953){
					System.out.print("");
				}
				List<String>features = Featurizer.featureInstance(list_wpsen,argB);
				if(features.size()>0){
					StringBuilder sb = new StringBuilder();
					for(String a:features)sb.append(a).append(" ");
					//D.p(argA,argB);
					dw.write(label,articleId,relation,argA,argB,argA_is_arg1orArg2,sb.toString());
				}
			}	
		}
		dr_trainseeds.close();
		dr_wpsen.close();
		dw.close();
		{
			Sort.sort(file_featuretrainseeds,file_featuretrainseeds_sort,dir,new Comparator<String[]>(){

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					String a = o1[2]+"\t"+o1[3]+"\t"+o1[4];
					String b = o2[2]+"\t"+o2[3]+"\t"+o2[4];
					return a.compareTo(b);
				}
				
			});
		}
	}
	
	static void evaluation()throws Exception{
		//How many training positive instance for every nell relation?
		HashCount<String> hc = new HashCount<String>();
		List<String[]>all = (new DelimitedReader(file_featuretrainseeds_sort)).readAll();
		for(String []line:all){
			hc.add(line[2]);
		}
		hc.printAll();
		D.p(hc.size());
	}
	
	public static void wpsensubset() throws IOException {
		// TODO Auto-generated method stub
		HashSet<Integer> usedArticle = new HashSet<Integer>();
		{
			DelimitedReader dr = new DelimitedReader(file_trainseeds);
			String[] line;
			while ((line = dr.read()) != null) {
				usedArticle.add(Integer.parseInt(line[2]));
			}
			dr.close();
		}
		{
			String file_wpsen_all = "/projects/pardosa/s5/clzhang/data/wp/sentences.stanford.tokenized.sortbywid";
			DelimitedReader dr = new DelimitedReader(file_wpsen_all);
			DelimitedWriter dw = new DelimitedWriter(file_wpsen);
			String[] line;
			while ((line = dr.read()) != null) {
				int wid = Integer.parseInt(line[2]);
				if(usedArticle.contains(wid)){
					dw.write(line);
				}
			}
			dw.close();
			dr.close();
		}
	}
}
