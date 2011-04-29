package freebase.ienyt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import multir.eval.PrecisionRecallCurve2;
import multir.eval.Prediction;
import multir.learning.algorithm.CRFParameters;
import multir.learning.algorithm.CollinsTraining2;
import multir.learning.algorithm.MILModel;
import multir.learning.data.Dataset;
import multir.learning.data.MILDocument;
import multir.learning.data.MemoryDataset;
import multir.preprocess.ConvertProtobufToMILDocument;
import multir.preprocess.Mappings;
import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.Builder;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class S1_seedonly {

	//	static String dir = "o:/unix/projects/pardosa/s5/clzhang/ontologylink/ienyt";
	//	static String in = dir+"/subset05.100.pb.gz";
	//static String in = "/projects/pardosa/data14/raphaelh/t/data/train-Multiple.pb.gz";
	//static String input2 = "/projects/pardosa/data14/raphaelh/t/data/test-Multiple.pb.gz";

	//static String out = "/projects/pardosa/data14/raphaelh/t/data/train-Single.pb.gz";
	//	static String out = dir+"/cllabel_subset05.100.pb.gz";

	static void getTestingWithNoLabel(String file_test_pb, String expdir) throws IOException {
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(expdir + "/testpb")));
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file_test_pb)));
		Relation r = null;

		int count = 0;

		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			//if(count>10000)break;
			//D.p(r.getSourceGuid(), r.getDestGuid());
			String newrel = "NA";
			//			String[] rels = r.getRelType().split(",");
			//			if (rels.length > 1)
			//				continue;

			relBuilder = Relation.newBuilder();
			// need to iterate over mentions, keep only those in the range 
			relBuilder.setRelType(newrel);
			relBuilder.setSourceGuid(r.getSourceGuid());
			relBuilder.setDestGuid(r.getDestGuid());
			for (int i = 0; i < r.getMentionCount(); i++) {
				RelationMentionRef rmf = r.getMention(i);
				relBuilder.addMention(rmf);
			}
			relBuilder.build().writeDelimitedTo(os);

		}
		is.close();
		os.close();
	}

	static void getTrainingWithSeedOnly(String file_pb, String trainpbdir) throws IOException {
		NellOntology no = new NellOntology();
		HashMap<String, NellRelation> map = new HashMap<String, NellRelation>();
		for (NellRelation nr : no.nellRelationList) {
			for (String[] s : nr.seedInstances) {
				String key = (s[0] + "\t" + s[1]).toLowerCase();
				key = key.toLowerCase();
				map.put(key, nr);

				key = (s[1] + "\t" + s[0]).toLowerCase();
				key = key.toLowerCase();
				map.put(key, nr);
			}
		}

		//String temp1 = file_trainpb+".tmp1allentity";
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file_pb)));
		if (!(new File(trainpbdir)).exists()) {
			(new File(trainpbdir)).mkdir();
		}
		OutputStream os = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(trainpbdir + "/trainpb")));
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
			String key = (arg1 + "\t" + arg2).toLowerCase();
			if (map.containsKey(key)) {
				String newrelation = map.get(key).relation_name;
				writeRintoFile(r, newrelation, os);
				//				relBuilder = Relation.newBuilder();
				//				// need to iterate over mentions, keep only those in the range 
				//				relBuilder.setRelType(newrelation);
				//				relBuilder.setSourceGuid(r.getSourceGuid());
				//				relBuilder.setDestGuid(r.getDestGuid());
				//				for (int i = 0; i < r.getMentionCount(); i++) {
				//					RelationMentionRef rmf = r.getMention(i);
				//					relBuilder.addMention(rmf);
				//				}
				//				relBuilder.build().writeDelimitedTo(os);
				size++;
			} else {
				if (nasize < size) {
					writeRintoFile(r, "NA", os);
					nasize++;
				}
			}
			//
			//			Collections.shuffle(narelations);
			//			for (int i = 0; i < size; i++) {
			//				writeRintoFile(narelations.get(i), "NA", os);
			//			}

		}
		os.close();
		D.p("success", size);
	}

	private static void writeRintoFile(Relation r, String relationname, OutputStream os) throws IOException {
		Builder relBuilder = Relation.newBuilder();
		
		// need to iterate over mentions, keep only those in the range 
		relBuilder.setRelType(relationname);
		relBuilder.setSourceGuid(r.getSourceGuid());
		relBuilder.setDestGuid(r.getDestGuid());
		for (int i = 0; i < r.getMentionCount(); i++) {
			RelationMentionRef rmf = r.getMention(i);
			relBuilder.addMention(rmf);
		}
		relBuilder.build().writeDelimitedTo(os);
	}



	public static void func_temp0(String expdir) throws Exception {
		InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(expdir + "/testpb")));
		Relation r = null;

		int count = 0;

		Builder relBuilder = null;
		while ((r = Relation.parseDelimitedFrom(is)) != null) {
			if (++count % 10000 == 0)
				System.out.println(count);
			//D.p(r.getSourceGuid(), r.getDestGuid());
			String newrel = "NA";
			String arg1 = r.getSourceGuid();
			String arg2 = r.getDestGuid();
			D.p(arg1, arg2);
			//			String[] rels = r.getRelType().split(",");
			//			if (rels.length > 1)
			//				continue;
			//
			//			relBuilder = Relation.newBuilder();
			//			// need to iterate over mentions, keep only those in the range 
			//			relBuilder.setRelType(newrel);
			//			relBuilder.setSourceGuid(r.getSourceGuid());
			//			relBuilder.setDestGuid(r.getDestGuid());
			//			for (int i = 0; i < r.getMentionCount(); i++) {
			//				RelationMentionRef rmf = r.getMention(i);
			//				relBuilder.addMention(rmf);
			//			}
			//			relBuilder.build().writeDelimitedTo(os);

		}
		is.close();
	}

	public static void main(String[] args) throws Exception {
		{
//			getTestingWithNoLabel(Main.file_nyt07pb, Main.exp_nellonly_relabel_nyt05pb);
//			getTrainingWithSeedOnly(Main.file_nyt05pb, Main.exp_nellonly_relabel_nyt05pb);
			SX_RphTrainTest.rphTrainTest(Main.exp_nellonly_relabel_nyt05pb);
		}
	}
}
