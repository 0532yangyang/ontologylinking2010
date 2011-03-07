package freebase.preprocess;

import javatools.administrative.D;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class FreebaseNameAlias {

	public static void main(String[] args) throws Exception {
		String file = "/projects/pardosa/s5/clzhang/ontologylink/freebase-datadump-quadruples.tsv";
		String output = "/projects/pardosa/s5/clzhang/ontologylink/fbnamealias";
		DelimitedReader dr = new DelimitedReader(file);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] line;
		while ((line = dr.read()) != null) {
			// if(!line[1].equals("/type/object/name") &&
			// !line[1].equals("/common/topic/alias") &&
			// line[2].equals("/lang/en")){
			// D.p(line);
			// }
			if (line[1].equals("/type/object/name") && line[2].equals("/lang/en")) {
				dw.write(line[0],line[3],"Name");
			}
			if (line[1].equals("/common/topic/alias") && line[2].equals("/lang/en")) {
				dw.write(line[0],line[3],"Alias");
			}
		}
		dr.close();
		dw.close();
	}
}
