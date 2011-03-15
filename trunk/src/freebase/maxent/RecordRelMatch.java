package freebase.maxent;

import java.io.IOException;

import javatools.filehandlers.DelimitedReader;


public class RecordRelMatch {
	String nellRelation;
	String fbJoinRelation;
	String fbJoinRelationInt; // Format: 123,456,789

	public static RecordRelMatch read(DelimitedReader dr) throws IOException {
		RecordRelMatch rmr = new RecordRelMatch();
		String[] line = dr.read();
		if (line == null) {
			return null;
		} else {
			rmr.nellRelation = line[0];
			rmr.fbJoinRelation = line[2];
			rmr.fbJoinRelationInt = line[1];
			return rmr;
		}
	}
}
