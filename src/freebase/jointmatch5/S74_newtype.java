package freebase.jointmatch5;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;

public class S74_newtype {
	public static void getNewArgType(String file_singlerel2mergerel, String file_reltypesign) throws IOException {
		HashMap<String, String> singlerel2typesign = new HashMap<String, String>();
		{
			DelimitedReader dr = new DelimitedReader(file_reltypesign);
			List<String[]> b;
			while ((b = dr.readBlock(0)) != null) {
				singlerel2typesign.put(b.get(0)[0], b.get(0)[1]);
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(file_singlerel2mergerel);
			String[] l;
			while ((l = dr.read()) != null) {
				String mergerel = l[1];
				String[] mergerelsplit = l[1].split("@");
				HashSet<String> tmp = new HashSet<String>();
				for (String r : mergerelsplit) {
					String t = singlerel2typesign.get(r);
					if (t != null) {
						tmp.add(t);
					}
				}
				if (tmp.size() > 1) {
					D.p(tmp,mergerel);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		getNewArgType(Main.file_singlerel2mergerel, Main.file_relation_typesign_arg1);
	}
}
