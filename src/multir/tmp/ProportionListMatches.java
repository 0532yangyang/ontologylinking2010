package multir.tmp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

import multir.learning.data.MILDocument;
import multir.util.delimited.DelimitedReader;

public class ProportionListMatches {

	static String[] lists = {
		"/location/citytown",
		"/location/country",
		"/location/us_state",
		"/location/administrative_division",
		"/location/region",
		"/architecture/building" };
	

	public static void main(String[] args) throws IOException {
		// iterate over training data
		// if relation is location/contains, see if both arguments
		// match our lists

		// initialize lexicons
		@SuppressWarnings("unchecked")
		HashSet<String>[] lex = (HashSet<String>[])new HashSet[lists.length];
		for (int i=0; i < lex.length; i++)
			lex[i] = new HashSet<String>();
		try {
			DelimitedReader r = new DelimitedReader
				("/projects/pardosa/data14/raphaelh/t/ftexp/freebase-lists-names");
			String[] t = null;
			while ((t = r.read())!= null) {
				for (int i =0; i < lists.length; i++)
					if (t[1].equals(lists[i]))
						lex[i].add(t[0]);
			}
			r.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		// read training data
		
		int[] counts = new int[3];
		
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream
				("/projects/pardosa/data14/raphaelh/t/ftexp/train")));
		
		MILDocument doc = new MILDocument();
		while (doc.read(dis)) {

			// check if is location/contains
			boolean contains = false;
			for (int i=0; i < doc.Y.length; i++)
				if (doc.Y[i]==2) contains = true;
			if (!contains) continue;
			
			String arg1 = doc.arg1;
			String arg2 = doc.arg2;
			boolean arg1c = false;
			boolean arg2c = false;
			
			for (int i=0; i < lists.length; i++) {
				if (lex[i].contains(arg1)) {
					arg1c = true;
					//arg1ner = lists[i];
					break;
				}
			}

			for (int i=0; i < lists.length; i++) {
				if (lex[i].contains(arg2)) {
					arg2c = true;
					//arg2ner = lists[i];
					break;
				}
			}
			
			if (!arg1c && !arg2c) {
				System.out.println(arg1 + " " + arg2);
				counts[0]++;
			}
			if (arg1c && !arg2c) counts[1]++;
			if (!arg1c && arg2c) counts[1]++;
			if (arg1c && arg2c) counts[2]++;

		}
		dis.close();
		
		System.out.println(counts[0] + " " + counts[1] + " " + counts[2]);
	}
	
	
}
