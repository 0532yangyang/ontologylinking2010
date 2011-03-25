package javatools.dbd;

import java.io.File;
import java.io.IOException;

import javatools.filehandlers.DelimitedReader;

public class ExamplePut {
	private static File myDbEnvPath = new File("/scratch/tmp/");
	private static DataAccessor da;
	private static MyDbEnv myDbEnv = new MyDbEnv();

	public static void main(String[] args) throws IOException {
		myDbEnv.setup(myDbEnvPath, false);
		da = new DataAccessor(myDbEnv.getEntityStore());
		DelimitedReader dr = new DelimitedReader(
				"/projects/pardosa/s5/clzhang/ontologylink/relationmatch/queryresult.name");
		String[] l;
		int id = 0;
		while ((l = dr.read()) != null) {
			//if(id>100000)break;
			DelString ds = new DelString();
			ds.setId(id++);
			String []s = l[2].split(" ");
			ds.setKey(s[0]);
			ds.setValue(l);
			da.delStringById.put(ds);
		}
		dr.close();
	}
}
