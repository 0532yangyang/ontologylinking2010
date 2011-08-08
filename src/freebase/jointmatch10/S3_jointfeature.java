package freebase.jointmatch10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class S3_jointfeature {

	static HashMap<Integer, String> map_varid_name = new HashMap<Integer, String>();
	static HashMap<String, Integer> map_name_varid = new HashMap<String, Integer>();

	static void load() throws IOException {
		dwjointname = new DelimitedWriter(Main.file_jointclause + ".name");
		loadOne(Main.file_relationmatch_candidate);
		loadOne(Main.file_typematch_candidate);
		loadOne(Main.file_entitymatch_candidate);
		dwjointname.close();
	}

	private static void loadOne(String input) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				int varid = Integer.parseInt(l[0]);
				String varname = l[1] + "::" + l[2];
				map_varid_name.put(varid, varname);
				map_name_varid.put(varname, varid);
				dwjointname.write(varid, varname);
			}
			dr.close();
		}
	}

	static void instanceFeatureRelation(String input, double weight) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			HashSet<String> appear = new HashSet<String>();
			//musicianPlaysInstrument::Yo Yo Ma::cello	/music/artist/track_contributions|/music/track_contribution/role	/m/011zf2	/m/01xqw	j1	/m/0dpsh0s
			while ((l = dr.read()) != null) {
				String[] abc = l[0].split("::");
				String nellrel = abc[0];
				String arg1 = abc[1];
				String arg2 = abc[2];
				String fbrel = l[1];
				String arg1mapentity = l[2];
				String arg2mapentity = l[3];
				String arg1var = arg1 + "::" + arg1mapentity;
				String arg2var = arg2 + "::" + arg2mapentity;
				String relvarstr = nellrel + "::" + fbrel;

				try {
					//some relations has already been given up because it only hit one example in the seeds
					int arg1varId = map_name_varid.get(arg1var);
					int arg2varId = map_name_varid.get(arg2var);
					int relvarid = map_name_varid.get(relvarstr);
					String key = arg1varId + "\t" + arg2varId + "\t" + relvarid;
					if (!appear.contains(key)) {
						dwjoint.write("relation_instance", weight, -1 * arg1varId, -1 * arg2varId, relvarid, 0,
								arg1var, arg2var, relvarstr);
						dwjoint.write("relation_instance_inv", weight, arg1varId, -1 * relvarid, 0, arg1var, relvarstr);
						dwjoint.write("relation_instance_inv", weight, arg2varId, -1 * relvarid, 0, arg2var, relvarstr);
						appear.add(key);
					}
				} catch (Exception e) {

				}
			}
			dr.close();
		}
	}

	static void instanceFeatureType(double weight) throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
		String[] l;
		HashSet<String> appear = new HashSet<String>();
		while ((l = dr.read()) != null) {
			String entityname = l[0];
			Set<String> entityclass = Main.no.entity2class.get(entityname);
			if (entityclass.contains("writer")) {
				D.p(entityname, l[3]);
			}
			entityclass.remove("NEG_NA");
			for (String nelltype : entityclass) {
				String fbtype = l[3];
				String argstr = l[0] + "::" + l[1];
				String typestr = nelltype + "::" + fbtype;
				try {
					int argvarid = map_name_varid.get(argstr);
					int typevarid = map_name_varid.get(typestr);
					String key = argvarid + "\t" + typevarid;
					if (!appear.contains(key)) {
						dwjoint.write("type_instance", weight, -1 * argvarid, typevarid, 0, argstr, typestr);
						dwjoint.write("type_instance_inv", weight, argvarid, -1 * typevarid, 0, argstr, typestr);
					}
				} catch (Exception e) {

				}
			}
		}
	}

	static void typeConstrain() throws IOException {

	}

	static void mutualexclusive(double weight) throws IOException {
		List<String[]> table = new ArrayList<String[]>();
		for (Entry<Integer, String> e : map_varid_name.entrySet()) {
			int id = e.getKey();
			String[] ab = e.getValue().split("::");
			table.add(new String[] { id + "", ab[0], ab[1] });
		}
		{
			StringTable.sortByColumn(table, new int[] { 1 });
			List<List<String[]>> blocks = StringTable.toblock(table, 1);
			for (List<String[]> b : blocks) {
				for (int i = 0; i < b.size(); i++) {
					for (int j = i + 1; j < b.size(); j++) {
						dwjoint.write("mutual", weight, (-1 * Integer.parseInt(b.get(i)[0])) + "",
								(-1 * Integer.parseInt(b.get(j)[0])) + "", 0, b.get(i)[0] + "::" + b.get(i)[1],
								b.get(j)[0] + "::" + b.get(j)[1]);
					}
				}
			}
		}
	}

	static void knownNegative(String input, double weight) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			HashSet<String> appear = new HashSet<String>();
			//musicianPlaysInstrument::Yo Yo Ma::cello	/music/artist/track_contributions|/music/track_contribution/role	/m/011zf2	/m/01xqw	j1	/m/0dpsh0s
			while ((l = dr.read()) != null) {
				String[] abc = l[0].split("::");
				String nellrel = abc[0];
				String arg1 = abc[1];
				String arg2 = abc[2];
				String fbrel = l[1];
				String arg1mapentity = l[2];
				String arg2mapentity = l[3];
				String arg1var = arg1 + "::" + arg1mapentity;
				String arg2var = arg2 + "::" + arg2mapentity;
				String relvarstr = nellrel + "::" + fbrel;

				try {
					//some relations has already been given up because it only hit one example in the seeds
					int arg1varId = map_name_varid.get(arg1var);
					int arg2varId = map_name_varid.get(arg2var);
					int relvarid = map_name_varid.get(relvarstr);
					String key = arg1varId + "\t" + arg2varId + "\t" + relvarid;
					if (!appear.contains(key)) {
						dwjoint.write("knownneg", -10.0, -1 * arg1varId, -1 * arg2varId, relvarid, 0, arg1var, arg2var,
								relvarstr);
						appear.add(key);
					}
				} catch (Exception e) {

				}
			}
			dr.close();
		}
	}

	static void staticfeature() throws IOException {
		HashMap<String, Double> feature_weight = new HashMap<String, Double>();
		staticfeature_setweight(feature_weight);
		staticfeatureOne(Main.file_relationmatch_staticfeature, feature_weight, "relation");
		staticfeatureOne(Main.file_entitymatch_staticfeature, feature_weight, "entity");
		staticfeatureOne(Main.file_typematch_staticfeature, feature_weight, "type");
	}

	static void staticfeature_setweight(HashMap<String, Double> feature_weight) throws IOException {
		feature_weight.put("ENTNAME", 1.0);
		feature_weight.put("RANK", 1.0);
		feature_weight.put("TYPENAME", 1.0);
		feature_weight.put("RELNAME", 1.0);
		feature_weight.put("LEN", -1.0);
		feature_weight.put("MID", 0.0);//use explode to replace this
		feature_weight.put("EXP", -1.0);
	}

	static void staticfeatureOne(String input, HashMap<String, Double> feature_weight, String appendName)
			throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		String[] l;
		while ((l = dr.read()) != null) {
			int varid = Integer.parseInt(l[0]);
			if (varid == 2881)
				D.p(varid);
			String varname = l[1];
			String[] fv = l[2].split(" |:");
			double sum = 0;
			for (int i = 0; i < fv.length; i += 2) {
				String fname = fv[i];
				double val = Double.parseDouble(fv[i + 1]);
				double weight = feature_weight.get(fname);
				sum += val * weight;
			}
			dwjoint.write("static_" + appendName, sum, varid, 0, varname);
		}
	}

	public static void convert2wcnf() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_jointclause);
		List<String[]> all = (new DelimitedReader(Main.file_jointclause + ".1")).readAll();
		int num_variables = 0, num_clauses = 0;
		double lowest = 0;
		for (String[] a : all) {
			num_clauses++;
			if (a[0].contains("_inv")) {
				continue;
			}
			double w = Double.parseDouble(a[1]);
			lowest = Math.min(w, lowest);
			for (int k = 2; k < a.length; k++) {
				if (a[k].equals("0")) {
					break;
				} else {
					int x = Math.abs(Integer.parseInt(a[k]));
					if (x > num_variables)
						num_variables = x;
				}
			}
		}
		dw.write("p wcnf " + num_variables + " " + num_clauses);
		for (String[] a : all) {
			StringBuilder sb = new StringBuilder();
			int w = (int) Math.round((Double.parseDouble(a[1]) - lowest) * 10);
			sb.append(w + " ");
			for (int k = 2; k < a.length; k++) {
				sb.append(a[k] + " ");
				if (a[k].equals("0"))
					break;
			}
			dw.write(sb.toString());
		}
		dw.close();
	}

	static DelimitedWriter dwjoint;
	static DelimitedWriter dwjointname;

	public static void main(String[] args) throws IOException {
		load();
		{
			dwjoint = new DelimitedWriter(Main.file_jointclause + ".1");
			instanceFeatureRelation(Main.file_fbsearchmatch_len1, 1);
			instanceFeatureRelation(Main.file_fbsearchmatch_len2, 1);
			instanceFeatureType(1);
			mutualexclusive(10000);
			//knownNegative(Main.file_knownnegative + ".match.len1", -10);
			//knownNegative(Main.file_knownnegative + ".match.len2", -10);
			staticfeature();
			dwjoint.close();
			dwjointname.close();
		}
		convert2wcnf();

	}
}
