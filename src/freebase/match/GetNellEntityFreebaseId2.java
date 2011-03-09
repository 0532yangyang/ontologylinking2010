package freebase.match;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javatools.webapi.FBSearchEngine;

import nell.preprocess.NellOntology;
import nell.preprocess.NellRelation;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class GetNellEntityFreebaseId2 {

	static HashMap<String, List<Integer>> nellObj = new HashMap<String, List<Integer>>();

	static void step1() throws IOException {
		NellOntology no = new NellOntology();

		for (NellRelation nr : no.nellRelationList) {
			for (String[] a : nr.seedInstances) {
				nellObj.put(a[0].trim(), new ArrayList<Integer>());
				nellObj.put(a[1].trim(), new ArrayList<Integer>());
			}
		}

	}

	public static void step3_by_fbse() throws IOException {
		String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellSeedsWrittenInUrl_multi";
		DelimitedWriter dw = new DelimitedWriter(output);
		NellOntology no = new NellOntology();
		for (NellRelation nr : no.nellRelationList) {
			try {
				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					List<String> ares = FBSearchEngine.query2(a[0], 10);
					List<String> bres = FBSearchEngine.query2(a[1], 10);
					StringBuilder sba = new StringBuilder();
					StringBuilder sbb = new StringBuilder();
					for(String t1:ares){
						sba.append(t1+";");
					}
					for(String t2:bres){
						sbb.append(t2+";");
					}
					dw.write(a[0], a[1], nr.relation_name, sba, sbb);
					dw.flush();
				}
			} catch (Exception e) {
				System.err.println(nr.relation_name);
			}

		}
		dw.close();
	}

	public static void step4_by_fbse() throws IOException {
		String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellSeedsWrittenInUrlNeg_temp";
		DelimitedWriter dw = new DelimitedWriter(output);
		NellOntology no = new NellOntology();
		for (NellRelation nr : no.nellRelationList) {
			try {
				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.known_negatives) {
					List<String> ares = FBSearchEngine.query2(a[0], 10);
					List<String> bres = FBSearchEngine.query2(a[1], 10);
					dw.write(a[0], a[1], nr.relation_name, ares.get(0), bres.get(0));
					dw.flush();
				}
			} catch (Exception e) {
				System.err.println(nr.relation_name);
			}
		}
		dw.close();
	}

	public static void step3_by_fbse_temp() throws IOException {
		String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellSeedsWrittenInMyid";
		DelimitedWriter dw = new DelimitedWriter(output);
		NellOntology no = new NellOntology();
		for (NellRelation nr : no.nellRelationList) {
			try {
				if (nr.seedInstances == null || nr.seedInstances.size() == 0)
					continue;
				for (String[] a : nr.seedInstances) {
					System.out.println(nr.relation_name + "\t" + a[0] + "\t" + a[1]);
					// List<String> ares = FBSearchEngine.query2(a[0], 1);
					// List<String> bres = FBSearchEngine.query2(a[1], 1);
					// dw.write(a[0], a[1], nr.relation_name,ares.get(0),
					// bres.get(0));
					// dw.flush();
				}
			} catch (Exception e) {
				System.err.println(nr.relation_name);
			}

		}
		dw.close();
	}

	public static void step3() throws Exception {
		String input0 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellObjMid";
		String input1 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbNodes";
		String input2 = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/fbEdges";
		String output = "/projects/pardosa/s5/clzhang/ontologylink/tmp2/nellSeedsWrittenInMyid";

		{
			List<String[]> nell2Mid = (new DelimitedReader(input0)).readAll();
			for (String[] a : nell2Mid) {
				String nellname = a[0].trim();
				String[] mids = a[1].replace("[", "").replace("]", "").split(",");
				for (String b : mids) {
					if (b.length() == 0)
						continue;
					b = b.trim();
					int zclid = Integer.parseInt(b);
					nellObj.get(nellname).add(zclid);
				}
				// System.out.println(a[1]);
			}
		}

		NellOntology no = new NellOntology();
		DelimitedWriter dw = new DelimitedWriter(output);
		for (NellRelation nr : no.nellRelationList) {
			for (String[] a : nr.seedInstances) {
				List<Integer> arg1ids = nellObj.get(a[0]);
				List<Integer> arg2ids = nellObj.get(a[1]);
				for (int x : arg1ids) {
					for (int y : arg2ids) {
						dw.write(x, y, a[0], a[1], nr.relation_name);
					}
				}

			}
		}
		dw.close();
	}

	public static void main(String[] args) throws Exception {

		step3_by_fbse();// positive

		step4_by_fbse();
	}
}
