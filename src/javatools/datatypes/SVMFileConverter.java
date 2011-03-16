package javatools.datatypes;

import java.io.IOException;
import java.util.HashMap;

import javatools.filehandlers.DelimitedReader;

public class SVMFileConverter {
	public static void convertBinaryFeatureFile(String input, String output){
		try {
			HashMap<String,Integer>fid = new HashMap<String,Integer>();
			DelimitedReader dr = new DelimitedReader(input);
			
			dr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String []args){
		
	}
}	
