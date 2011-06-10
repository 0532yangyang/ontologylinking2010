package freebase.preprocess3;

import java.io.IOException;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.string.StringUtil;

public class ParseWex {
	public static void getWidIbrelValue() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_ibcall);
		DelimitedReader drv = new DelimitedReader(Main.file_ibvalue);
		DelimitedWriter dw = new DelimitedWriter(Main.file_ibwex);
		List<String[]> valueblock = drv.readBlock(0);
		String[] l;
		while ((l = dr.read()) != null) {
			int wid = Integer.parseInt(l[0]);
			int templateid = Integer.parseInt(l[1]);
			String ibname = l[3].replace("Template:", "");
			while (valueblock != null && valueblock.size() > 0 && Integer.parseInt(valueblock.get(0)[0]) < templateid) {
				valueblock = drv.readBlock(0);
			}
			if (valueblock != null && valueblock.size()>0 && Integer.parseInt(valueblock.get(0)[0]) == templateid) {
				for (String[] v : valueblock) {
					String value = StringUtil.removeBlanket(v[2], '<', '>');
					value = value.replaceAll("\n", "\\n");
					//D.p(wid, ibname, v[1], value);
					dw.write(wid, ibname, v[1], value);
				}
				//D.p(valueblock);
			}
		}
		dr.close();
		dw.close();
	}

	
	public static void main(String[] args) throws IOException {
		getWidIbrelValue();
	}
}
