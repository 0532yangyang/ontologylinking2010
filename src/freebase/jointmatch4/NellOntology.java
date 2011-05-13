package freebase.jointmatch4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;

public class NellOntology {
	public List<NellRelation> nellRelationList = new ArrayList<NellRelation>();

	/** Relation names and their Ids */
	public String[] relationNames;
	public HashMap<String, Integer> relationName2Id;
	/** Class names and their Ids */
	public String[] classNames;
	public HashMap<String, Integer> className2Id;

	/** Entity 2 class */
	public HashMap<String, HashSet<String>> entity2class = new HashMap<String, HashSet<String>>();
	public HashMap<String, HashSet<String>> entitylower2class = new HashMap<String, HashSet<String>>();
	/** Entity 2 class */
	public HashMap<String, Integer> entity2classId;

	/** Pair 2 relationId */
	public HashMap<String, List<Integer[]>> entitypair2eidrid = new HashMap<String, List<Integer[]>>();

	/** Relation Name 2 domain-range strings */
	public HashMap<String, String[]> relname2DomainRange = new HashMap<String, String[]>();

	public NellOntology(String file) {
		try {
			createNellOntology(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public NellOntology(String file, int style) {
		try {
			createNellOntology(file, style);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void putEntityClass(String entity, String classname) {
		if (!entity2class.containsKey(entity)) {
			entity2class.put(entity, new HashSet<String>());
		}
		entity2class.get(entity).add(classname);
	}

	private void createNellOntology(String file) throws IOException {
		DelimitedReader dr = new DelimitedReader(file);
		String[] line;
		HashSet<String> relationNamesTmp = new HashSet<String>();
		HashSet<String> classNamesTmp = new HashSet<String>();

		while ((line = dr.read()) != null) {
			// if(line[0].startsWith("actorStarredInMovie")){
			// //System.out.println(line[0]);
			// }

			NellRelation nro = new NellRelation(line);
			relationNamesTmp.add(nro.relation_name);
			classNamesTmp.add(nro.domain);
			classNamesTmp.add(nro.range);
			for (String[] a : nro.seedInstances) {
				putEntityClass(a[0], nro.domain);
				putEntityClass(a[1], nro.range);
			}
			for (String[] a : nro.known_negatives) {
				putEntityClass(a[0], nro.domain);
				putEntityClass(a[1], nro.range);
			}
			relname2DomainRange.put(nro.relation_name, new String[] { nro.domain, nro.range });
			this.nellRelationList.add(nro);
		}
		dr.close();

		{
			/** Init relation names */
			relationNames = new String[relationNamesTmp.size() + 1];
			relationName2Id = new HashMap<String, Integer>();
			int rid = 1;
			for (String a : relationNamesTmp) {
				relationNames[rid] = a;
				relationName2Id.put(a, rid);
				rid++;
			}
		}
		{
			/** Init class names */
			classNames = new String[classNamesTmp.size() + 1];
			className2Id = new HashMap<String, Integer>();
			int cid = 1;
			for (String a : classNamesTmp) {
				classNames[cid] = a;
				className2Id.put(a, cid);
				cid++;
			}
		}
		// get Entity 2 Class id;
		getEntity2classId();
		// Get entity pair to relationId
		getEntitypair2eidrid();

		for (Entry<String, HashSet<String>> e : this.entity2class.entrySet()) {
			String entity = e.getKey();
			String entitylower = entity.toLowerCase();
			this.entitylower2class.put(entitylower, e.getValue());
		}
	}

	private void createNellOntology(String file, int style) throws IOException {
		DelimitedReader dr = new DelimitedReader(file);
		String[] line;
		HashSet<String> relationNamesTmp = new HashSet<String>();
		HashSet<String> classNamesTmp = new HashSet<String>();

		while ((line = dr.read()) != null) {
			// if(line[0].startsWith("actorStarredInMovie")){
			// //System.out.println(line[0]);
			// }

			NellRelation nro = new NellRelation(line, style);
			relationNamesTmp.add(nro.relation_name);
			classNamesTmp.add(nro.domain);
			classNamesTmp.add(nro.range);
			for (String[] a : nro.seedInstances) {
				putEntityClass(a[0], nro.domain);
				putEntityClass(a[1], nro.range);
			}
			for (String[] a : nro.known_negatives) {
				putEntityClass(a[0], nro.domain);
				putEntityClass(a[1], nro.range);
			}
			relname2DomainRange.put(nro.relation_name, new String[] { nro.domain, nro.range });
			this.nellRelationList.add(nro);
		}
		dr.close();

		{
			/** Init relation names */
			relationNames = new String[relationNamesTmp.size() + 1];
			relationName2Id = new HashMap<String, Integer>();
			int rid = 1;
			for (String a : relationNamesTmp) {
				relationNames[rid] = a;
				relationName2Id.put(a, rid);
				rid++;
			}
		}
		{
			/** Init class names */
			classNames = new String[classNamesTmp.size() + 1];
			className2Id = new HashMap<String, Integer>();
			int cid = 1;
			for (String a : classNamesTmp) {
				classNames[cid] = a;
				className2Id.put(a, cid);
				cid++;
			}
		}
		// get Entity 2 Class id;
		getEntity2classId();
		// Get entity pair to relationId
		getEntitypair2eidrid();

		for (Entry<String, HashSet<String>> e : this.entity2class.entrySet()) {
			String entity = e.getKey();
			String entitylower = entity.toLowerCase();
			this.entitylower2class.put(entitylower, e.getValue());
		}
	}

	private void createNellOntology3(String file) throws IOException {
		DelimitedReader dr = new DelimitedReader(file);
		String[] line;
		HashSet<String> relationNamesTmp = new HashSet<String>();
		HashSet<String> classNamesTmp = new HashSet<String>();

		while ((line = dr.read()) != null) {
			// if(line[0].startsWith("actorStarredInMovie")){
			// //System.out.println(line[0]);
			// }

			NellRelation nro = new NellRelation(line);
			relationNamesTmp.add(nro.relation_name);
			classNamesTmp.add(nro.domain);
			classNamesTmp.add(nro.range);
			relname2DomainRange.put(nro.relation_name, new String[] { nro.domain, nro.range });
			this.nellRelationList.add(nro);
		}
		dr.close();

		{
			/** Init relation names */
			relationNames = new String[relationNamesTmp.size() + 1];
			relationName2Id = new HashMap<String, Integer>();
			int rid = 1;
			for (String a : relationNamesTmp) {
				relationNames[rid] = a;
				relationName2Id.put(a, rid);
				rid++;
			}
		}
		{
			/** Init class names */
			classNames = new String[classNamesTmp.size() + 1];
			className2Id = new HashMap<String, Integer>();
			int cid = 1;
			for (String a : classNamesTmp) {
				classNames[cid] = a;
				className2Id.put(a, cid);
				cid++;
			}
		}
		// get Entity 2 Class id;
		getEntity2classId();
		// Get entity pair to relationId
		getEntitypair2eidrid();
	}

	private void myTest() {
		System.out.println("#relations\t" + nellRelationList.size());
		System.out.println("#relationNames\t" + relationNames.length);
		System.out.println("#classNames\t" + classNames.length);
		{
			for (String a : className2Id.keySet()) {
				System.out.println(a);
			}
		}
		{
			int temp = 0;
			for (NellRelation nr : nellRelationList) {
				if (nr.seedInstances.size() > 0) {
					StringBuilder sb = new StringBuilder();
					for (String[] tmp : nr.seedInstances)
						sb.append(tmp[0] + "," + tmp[1] + " ");
					System.out.println(nr.relation_name + "\t" + sb.toString());
					temp++;
				}
			}
			System.out.println("#Relation has Seed\t" + temp);
		}

		System.out.println(this.entitypair2eidrid.size());

	}

	public void getEntity2classId() {
		entity2classId = new HashMap<String, Integer>();
		for (NellRelation nr : this.nellRelationList) {
			for (String[] t : nr.seedInstances) {
				{
					String domain = nr.domain;
					this.entity2classId.put(t[0], className2Id.get(domain));
				}
				{
					String range = nr.range;
					this.entity2classId.put(t[1], className2Id.get(range));
				}
			}
		}
	}

	public void getEntitypair2eidrid() {
		for (NellRelation nr : this.nellRelationList) {
			for (String[] t : nr.seedInstances) {
				String key = t[0] + "\t" + t[1];
				Integer[] onevalue = new Integer[] { this.entity2classId.get(t[0]), this.entity2classId.get(t[1]),
						this.relationName2Id.get(nr.relation_name) };
				if (!entitypair2eidrid.containsKey(key)) {
					List<Integer[]> value = new ArrayList<Integer[]>();
					entitypair2eidrid.put(key, value);
				}
				this.entitypair2eidrid.get(key).add(onevalue);
			}
		}
	}

	private void printAllEntity() {
		for (NellRelation nr : this.nellRelationList) {
			for (String[] b : nr.seedInstances) {
				D.p(nr.relation_name, "POS", b);
			}
			for (String[] a : nr.known_negatives) {
				D.p(nr.relation_name, "NEG", a);
			}
		}
	}

	//	public NellOntology() {
	//		try {
	//			String file = "/projects/pardosa/s5/clzhang/ontologylink/nell/relations.nell.seed";
	//			if (!(new File(file)).exists()) {
	//				file = "o:/unix" + file;
	//			}
	//			createNellOntology(file);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//	}

	public static void main(String[] args) {
		try {
			NellOntology no = new NellOntology(Main.file_ontology,1);
			no.printAllEntity();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
