package multir.preprocess.freebase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;


public class ExtractNamedEntitiesFromFreebase {
	
	public static void extract(String freebaseDumpFile, String output, String tmpDir) 
		throws IOException {
		{
			DelimitedWriter w = new DelimitedWriter(output + ".raw");
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(freebaseDumpFile), "utf-8"));
			String line = null;
			while ((line = r.readLine()) != null) {
				String[] c = line.split("\t");
				String id = c[0];
				String rel = c[1];
				// in case of value tuples, v1 is language, v2 is value
				// in case of refs, v1 is ref
				if (c.length > 3) {
					String v2 = c[3];
					if (rel.equals("/type/object/name") 
							|| rel.equals("/common/topic/alias"))
						// only write Freebase ID and name
						w.write(id, v2);
				}
			}
			r.close();
			w.close();
		}

		// sometimes, Freebase contains a /type/object/name and a
		// /common/topic/alias with the exact same string for the
		// exact same object. we need to filter those
		
		// sort by name, Freebase ID
		Sort.sort(output + ".raw", output + ".sorted", tmpDir, new Comparator<String[]>() {
			public int compare(String[] t1, String[] t2) {
				int c = t1[1].compareTo(t2[1]);
				if (c != 0) return c;
				return t1[0].compareTo(t2[0]);
			}
		});

		// eliminate duplicates
		{
			DelimitedReader r = new DelimitedReader(output + ".sorted");
			DelimitedWriter w = new DelimitedWriter(output);
			String[] t = null, prev = null;			
			while ((t = r.read())!= null) {
				if (prev == null || !prev[0].equals(t[0]) || !prev[1].equals(t[1])) {
					w.write(t);
					prev = t;
				}
			}
			r.close();
			w.close();
		}
		
		// delete temporary files
		new File(output + ".raw").delete();
		new File(output + ".sorted").delete();
	}
}
