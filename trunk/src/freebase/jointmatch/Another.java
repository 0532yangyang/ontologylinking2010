package freebase.jointmatch;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import multir.util.delimited.Sort;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class Another {
	static void step1_getnytWid() throws IOException {

		//String temp1 = file_trainpb+".tmp1allentity";
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(Main.file_myidnyt05pb)));
		String output = Main.file_myidnyt05pb + ".debug";
		DelimitedWriter dw = new DelimitedWriter(output);
		Relation r = null;
		int count = 0;
		Builder relBuilder = null;
		int size = 0;
		int nasize = 0;
		//DelimitedWriter dw = new DelimitedWriter(Main.file_allentitypair);
		List<Relation> narelations = new ArrayList<Relation>();
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			//if(count>10000)break;
			String arg1 = r.getSourceGuid();
			String arg2 = r.getDestGuid();
			/**replace the type id with relation id in S0_reid_nyt0507*/
			String myid = r.getRelType();
			List<RelationMentionRef> list = r.getMentionList();
			for (RelationMentionRef rmf : list) {
				D.p(rmf.toString());
			}
		}
		D.p("success", size);
		dw.close();
	}

	static void temp_grab_raphael_20110428() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_sql2instance);
			DelimitedWriter dw = new DelimitedWriter(Main.pdir+"/tmp1/sql2instance.subset");
			String[] l;
			while ((l = dr.read()) != null) {
				if(l[2].equals("acquired") && l[3].equals("/organization/organization/companies_acquired|/business/acquisition/company_acquired|")||
						l[2].equals("actorStarredInMovie") && l[3].equals("/film/actor/film|/film/performance/film|")){
					dw.write(l);
				}
			}
			dr.close();
			dw.close();
		}
	}

	public static void main(String[] args) throws Exception {
		//step1_getnytWid();
		temp_grab_raphael_20110428();
	}
}
