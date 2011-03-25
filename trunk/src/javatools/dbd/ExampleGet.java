package javatools.dbd;

import java.io.File;
import java.io.IOException;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;


import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

public class ExampleGet {
	private static File myDbEnvPath = new File("/scratch/tmp/");
	private static DataAccessor da;
	private static MyDbEnv myDbEnv = new MyDbEnv();

	public static void main(String args[]) throws IOException {
		myDbEnv.setup(myDbEnvPath, true);
		da = new DataAccessor(myDbEnv.getEntityStore());

		DelimitedReader dr = new DelimitedReader(
				"/projects/pardosa/s5/clzhang/ontologylink/relationmatch/queryresult.name");
		String[] l;
		D.p("start working");
		while ((l = dr.read()) != null) {
			String []s = l[2].split(" ");
			showItem(s[0]);
			//DelString item =
			// da.vendorByName.subIndex("abc").entities();
			//da.vendorByName.get(l[0]);
		//	System.out.println(item.getValue())
		}
		myDbEnv.close();
		D.p("end");
	}
	
    private static void showItem(String key) throws DatabaseException {

        // Use the inventory name secondary key to retrieve
        // these objects.
        EntityCursor<DelString> items =
            da.delStringByKey.subIndex(key).entities();
        try {
            for (DelString item : items) {
                D.p(item.getValue());
            }
        }catch(Exception e){
        	
        }
    }
    
}
