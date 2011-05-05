package multir.exp;

import java.util.ArrayList;
import java.util.List;

import multir.eval.CreateLimitedRelsSubsetDataset;
import multir.eval.GenerateFullSententialPrecisionRecallCurve;
import multir.eval.PrintTopK;
import multir.preprocess.WordCount;
import multir.preprocess.WriteFeaturesToFile;
import multir.util.delimited.DelimitedReader;

public class Batch {

	static String labelsFile = "/projects/pardosa/data16/raphaelh/camera/byrelation_owndataX";
	
	static String[] targets = {
		"/location/location/contains", 
		"/people/person/nationality",
		"/location/neighborhood/neighborhood_of", 
		"/people/person/children",
		"/business/company_advisor/companies_advised",
		"/people/person/religion",
		"/business/business_location/parent_company",
		"/broadcast/content/location",
		"/people/profession/people_with_this_profession"
	};

	static int start2005 = 40962186;
	static int start2006 = 43425267;
	static int start2007 = 45961658;
	static int start2005_2 = 42193727;
	
	public static void main(String[] args) throws Exception {
		/*
		// all relations
		DelimitedReader r = new DelimitedReader("/projects/pardosa/data16/raphaelh/tmp/ecmlOrigRelationsFull");
		List<String> l = new ArrayList<String>();
		String[] t = null;
		while ((t = r.read())!= null) {
			l.add(t[0]);
		}
		r.close();
		targets = l.toArray(new String[0]);
		*/
		
		int minFts = 5;          // min features
		int negs = 5;
		//String data = "Rels05-06.10"; // dataset, negs
		String data = "AllRels05-06." + negs; // dataset, negs
		//String data = "EightRels87-06." + negs; // dataset, negs
		//String data = "Rels05-06." + negs; // dataset, negs

		//int minFts = 10;          // min features
		//String data = "87-06.10"; // dataset, negs

		String exp = "/newexp.min" + minFts + "." + data;
		
		//CreateLimitedRelsSubsetDataset.run(data, targets, 0, start2007, negs / 100.0);		
		//CreateLimitedRelsSubsetDataset.run(data, targets, start2005, start2007, negs / 100.0);		
		//WriteFeaturesToFile.run(data);
		//WordCount.run(data);
		
		//Step2b.run(exp, data, minFts);
		//Step3.run(exp);
		//GenerateFullSententialPrecisionRecallCurve.run(labelsFile, exp, targets);
		//PrintTopK.run(exp, "/business/company_advisor/companies_advised");
		PrintTopK.run(exp, "/people/person/nationality");
		System.out.println(exp);
	}
}
