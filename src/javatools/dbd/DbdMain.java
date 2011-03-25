package javatools.dbd;

import java.io.File;
import java.io.IOException;

import com.sleepycat.persist.EntityCursor;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;

public class DbdMain {
	private File myDbEnvPath = new File("/scratch/tmp/");
	private DataAccessor da;
	private MyDbEnv myDbEnv = new MyDbEnv();
	boolean getOrPut;

	public DbdMain() {

	}

	/** Put is false, get is true */
	public DbdMain(String rootdir, boolean getOrPut) {
		myDbEnvPath = new File(rootdir);
		myDbEnv = new MyDbEnv();
		this.getOrPut = getOrPut;
		myDbEnv.setup(myDbEnvPath, getOrPut);
	}

	public void put(String file, int key) throws IOException {
		if (getOrPut) {
			System.err.println("Dbd is getting now!");
			return;
		}
		DelimitedReader dr = new DelimitedReader(file);
		String[] l;
		int id = 0;
		while ((l = dr.read()) != null) {
			// if(id>100000)break;
			DelString ds = new DelString();
			ds.setId(id++);
			ds.setKey(l[key]);
			ds.setValue(l);
			da.delStringById.put(ds);
		}
		dr.close();
	}

	public void get(String file, String keyValue) throws IOException {
		if (getOrPut) {
			System.err.println("Dbd is getting now!");
			return;
		}
		EntityCursor<DelString> items = da.delStringByKey.subIndex(keyValue).entities();
		try {
			for (DelString item : items) {
				D.p(item.getValue());
			}
		} catch (Exception e) {

		}
	}
}
