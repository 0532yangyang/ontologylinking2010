package javatools.filehandlers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javatools.datatypes.HashCount;
import javatools.mydb.StringTable;

import cc.factorie.protobuf.DocumentProtos.Relation;

public class PbReader {

	/**
	 * @param args
	 */
	InputStream is;

	public PbReader(String file) throws IOException {
		try{
			is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
		}catch(IOException e){
			is = new BufferedInputStream(new FileInputStream(file));
		}
	}

	public PbReader(String file, boolean isZip) throws IOException {
		if (isZip) {
			is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
		} else {
			is = new BufferedInputStream(new FileInputStream(file));
		}
	}

	public Relation read() throws IOException {
		Relation r = Relation.parseDelimitedFrom(is);
		return r;
	}
	public void close() throws IOException{
		is.close();
	}
	
	public static void analyzePbData(String pb) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(pb + ".analyze");
		List<Relation> all = new ArrayList<Relation>();
		int numCases = 0;
		{
			HashCount<String> labelDist = new HashCount<String>();
			Relation r = null;
			PbReader pr = new PbReader(pb);
			while ((r = pr.read()) != null) {
				all.add(r);
				labelDist.add(r.getRelType());
				numCases++;
			}
			pr.close();
			dw.write("Label Distribution");
			Iterator<Entry<String, Integer>> it = labelDist.iterator();
			while (it.hasNext()) {
				Entry<String, Integer> e = it.next();
				dw.write(e.getKey(), e.getValue());
			}
		}
		Collections.shuffle(all);
		{
			HashCount<String> hassampled = new HashCount<String>();
			List<String[]> towrite = new ArrayList<String[]>();
			for (int i = 0; i < all.size(); i++) {
				Relation r = all.get(i);
				String rel = r.getRelType();
				if (hassampled.see(rel) < 10) {
					towrite.add(new String[] { rel, r.getSourceGuid(), r.getDestGuid() });
					hassampled.add(rel);
				}
			}
			StringTable.sortByColumn(towrite, new int[] { 0 });
			for (String[] w : towrite) {
				dw.write(w);
			}
		}
		dw.close();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
