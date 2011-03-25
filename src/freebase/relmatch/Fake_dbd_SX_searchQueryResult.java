package freebase.relmatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.EntityCursor;

import javatools.administrative.D;
import javatools.dbd.DataAccessor;
import javatools.dbd.DelString;
import javatools.dbd.MyDbEnv;
import javatools.filehandlers.DelimitedReader;

public class Fake_dbd_SX_searchQueryResult {
	private File myDbEnvPath = new File("/scratch/tmp/");
	private DataAccessor da;
	private MyDbEnv myDbEnv = new MyDbEnv();
	boolean getOrPut;

	/** Put is false, get is true */
	public SX_searchQueryResult(String rootdir, boolean getOrPut) {
		myDbEnvPath = new File(rootdir);
		if (!myDbEnvPath.exists())
			myDbEnvPath.mkdir();
		myDbEnv = new MyDbEnv();
		this.getOrPut = getOrPut;
		myDbEnv.setup(myDbEnvPath, getOrPut);
		da = new DataAccessor(myDbEnv.getEntityStore());
	}

	public void put(String file) throws IOException {
		if (getOrPut) {
			System.err.println("Dbd is getting now!");
			return;
		}
		DelimitedReader dr = new DelimitedReader(file);
		String[] l;
		int id = 0;
		while ((l = dr.read()) != null) {
//			if (id > 100000)
//				break;
			DelString ds = new DelString();
			ds.setId(id++);
			String s[] = l[2].split(" ");
			ds.setKey(s[0]);
			ds.setValue(l);
			da.delStringById.put(ds);
		}
		dr.close();
	}

	public void get(String keyValue) throws IOException {
		if (!getOrPut) {
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

	public static void main(String[] args) throws IOException {

//		SX_searchQueryResult sxs = new SX_searchQueryResult(Main.file_queryresult_name + ".dbd", false);
//		sxs.put(Main.file_queryresult_name);
		SX_searchQueryResult sxs = new SX_searchQueryResult(Main.file_queryresult_name + ".dbd", true);
		sxs.get("21346598");
	}
}
