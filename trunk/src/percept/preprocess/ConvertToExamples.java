package percept.preprocess;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import percept.learning.data.Example;
import percept.util.SparseBinaryVector;
import percept.util.delimited.DelimitedReader;

public class ConvertToExamples {

	static String tmpDir = "";
	
	static String input1 = "";
	static String input2 = "";
	static String input3 = "";
	
	static String output = "";
	
	public static void main(String[] args) throws IOException {
		// arg1 is protobuf file
		// arg2 is MIL file		
		String input = args[0];
		String output= args[1];
		String mappingFile = args[2];
		boolean writeMapping = false;
		boolean writeRelations = false;
		convert(input, output, mappingFile, writeMapping, writeRelations);
	}
	
	public static void convert(String input, String output, String mappingFile, 
			boolean writeFeatureMapping, boolean writeRelationMapping) throws IOException {
		
		// This tool can be used in two ways:
		//  1) a new Mapping is created and saved at the end
		//  2) an existing Mapping is used; non-existent relations
		//     or features are ignored
		
		Mappings m = new Mappings();
		
		if (!writeFeatureMapping || !writeRelationMapping)
			m.read(mappingFile);
		else
			// ensure that state NA gets ID 0
			m.getStateID("NA", true);
		
		DataOutputStream os = new DataOutputStream
			(new BufferedOutputStream(new FileOutputStream(output)));
	
		// read source data in text format
		
		DelimitedReader r = new DelimitedReader(input);
		String[] t = null;
		Example doc = new Example();
	    
	    int count = 0;
    
	    while ((t = r.read())!= null) {
	    	if (++count % 10000 == 0) System.out.println(count);
	    	
	    	doc.meta = ""; // FB relation ...
	    	
	    	// set ground truth label (e.g. Nell relation)
	    	{
	    		String state = t[1]; // Nell relation
	    		doc.Y = m.getStateID(state, writeRelationMapping);
	    	}
	    	
	    	doc.features = new SparseBinaryVector();
    		//SparseBinaryVector sv = new SparseBinaryVector();
	    	String []s = t[2].split(" ");
    		int countFts = s.length; // set to number of features for this example
    		int[] fts = new int[countFts];
    		for (int i=0; i < countFts; i++) {
    			String feature = s[i]; // feature
	    		fts[i] = m.getFeatureID(feature, writeFeatureMapping);
    		}
	    	
    		Arrays.sort(fts);
		    int countUnique = 0;
		    for (int i=0; i < fts.length; i++)
		    	if (fts[i] != -1 && (i == 0 || fts[i-1] != fts[i]))
		    		countUnique++;
		    doc.features.num = countUnique;
		    doc.features.ids = new int[countUnique];
		    int pos = 0;
		    for (int i=0; i < fts.length; i++)
		    	if (fts[i] != -1 && (i == 0 || fts[i-1] != fts[i]))
		    		doc.features.ids[pos++] = fts[i];
	    	doc.write(os);
	    }
		
		r.close();
		os.close();
		
		if (writeFeatureMapping || writeRelationMapping)
			m.write(mappingFile);
	}
	
	
	
	public static void convert_svm(String input, String output, String mappingFile, 
			boolean writeFeatureMapping, boolean writeRelationMapping) throws IOException {
		
		// This tool can be used in two ways:
		//  1) a new Mapping is created and saved at the end
		//  2) an existing Mapping is used; non-existent relations
		//     or features are ignored
		
		Mappings m = new Mappings();
		
		if (!writeFeatureMapping || !writeRelationMapping)
			m.read(mappingFile);
		else
			// ensure that state NA gets ID 0
			m.getStateID("NA", true);
		
		DataOutputStream os = new DataOutputStream
			(new BufferedOutputStream(new FileOutputStream(output)));
	
		// read source data in text format
		
		DelimitedReader r = new DelimitedReader(input);
		String[] t = null;
		Example doc = new Example();
	    
	    int count = 0;
    
	    while ((t = r.read())!= null) {
	    	if (++count % 10000 == 0) System.out.println(count);
	    	String []s = t[0].split(" ");
	    	doc.meta = ""; // FB relation ...
	    	
	    	// set ground truth label (e.g. Nell relation)
	    	{
	    		String state = s[0]; // Nell relation
	    		doc.Y = m.getStateID(state, writeRelationMapping);
	    	}
	    	
	    	doc.features = new SparseBinaryVector();
    		//SparseBinaryVector sv = new SparseBinaryVector();
	    	
    		int countFts = s.length-1; // set to number of features for this example
    		int[] fts = new int[countFts];
    		for (int i=0; i < countFts; i++) {
    			String feature = s[i+1].split(":")[0]; // feature
	    		fts[i] = m.getFeatureID(feature, writeFeatureMapping);
    		}
	    	
    		Arrays.sort(fts);
		    int countUnique = 0;
		    for (int i=0; i < fts.length; i++)
		    	if (fts[i] != -1 && (i == 0 || fts[i-1] != fts[i]))
		    		countUnique++;
		    doc.features.num = countUnique;
		    doc.features.ids = new int[countUnique];
		    int pos = 0;
		    for (int i=0; i < fts.length; i++)
		    	if (fts[i] != -1 && (i == 0 || fts[i-1] != fts[i]))
		    		doc.features.ids[pos++] = fts[i];
	    	doc.write(os);
	    }
		
		r.close();
		os.close();
		
		if (writeFeatureMapping || writeRelationMapping)
			m.write(mappingFile);
	}
}
