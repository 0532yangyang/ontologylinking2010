package freebase.relmatch;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import nell.preprocess.NellOntology;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class S4_relationinstance {

	//	static void shuffleQueryResult(){
	//		try {
	//			List<String[]>all =  (new DelimitedReader(Main.file_queryresult_name)).readAll(40000000);
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_queryresult_name+".shuffle");
	//			Collections.shuffle(all);
	//			for(String []a:all){
	//				dw.write(a);
	//			}
	//			dw.close();
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	}
	static void step0_sampleQueryResult() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_queryresult_name);
			DelimitedWriter dw = new DelimitedWriter(Main.file_queryresult_sample);
			HashCount<String> hc = new HashCount<String>();
			String[] l;
			while ((l = dr.read()) != null) {
				if (hc.see(l[4]) < Main.PRM_sampledinstance) {
					dw.write(l);
					hc.add(l[4]);
				}
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void step0_getGnid2Type() {
		try {
			HashMap<String, Integer> ename2id = new HashMap<String, Integer>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_gnid_mid_wid_title);
				String[] line;
				while ((line = dr.read()) != null) {
					// entity_name.put(Integer.parseInt(line[0]),line[2].replace("\\s","_"));
					int gnid = Integer.parseInt(line[0]);
					String ename = line[1];
					ename2id.put(ename, gnid);
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fin_freebase_type_clean_sortMid);
				DelimitedWriter dw = new DelimitedWriter(Main.file_gnid2fbtype);
				String[] l;
				while ((l = dr.read()) != null) {
					if (ename2id.containsKey(l[0])) {
						int gnid = ename2id.get(l[0]);
						dw.write(gnid, l[0], l[1]);
					}
				}
				dr.close();
				dw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void does_FBRelationInstance_satisfying_TypeMatchResult() {
		NellOntology no = new NellOntology();
		HashMap<String, String> typematchingpredict_nt_ft = new HashMap<String, String>();
		HashMap<Integer, HashSet<String>> gnid2fbtypeset = new HashMap<Integer, HashSet<String>>();
		try {
			DelimitedWriter dw = new DelimitedWriter(Main.file_queryresult_typesatisfy);
			{
				//load file_gnid2fbtype
				DelimitedReader dr = new DelimitedReader(Main.file_gnid2fbtype);
				String[] l;
				while ((l = dr.read()) != null) {
					int gnid = Integer.parseInt(l[0]);
					String fbtype = l[2];
					if (!gnid2fbtypeset.containsKey(gnid))
						gnid2fbtypeset.put(gnid, new HashSet<String>());
					gnid2fbtypeset.get(gnid).add(fbtype);
				}
			}
			{
				//load type matching predict result
				DelimitedReader dr = new DelimitedReader(freebase.typematch.Main.fout_predict1);
				String[] l;
				while ((l = dr.read()) != null) {
					String[] s = l[0].split("::");
					if (s[0].equals("VT")) {
						typematchingpredict_nt_ft.put(s[1], s[2]);
					}
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_queryresult_name);
				String[] l;
				while ((l = dr.read()) != null) {
					int gnid1 = Integer.parseInt(l[0]);
					int gnid2 = Integer.parseInt(l[1]);
					String nellrel = l[2];
					String fbrel = l[4];
					String nelltype1 = "";
					String nelltype2 = "";
					if (nellrel.contains("_inverse")) {
						nellrel = nellrel.replace("_inverse", "");
						String[] t = no.relname2DomainRange.get(nellrel);
						nelltype1 = t[1];
						nelltype2 = t[0];
					} else {
						String[] t = no.relname2DomainRange.get(nellrel);
						nelltype1 = t[0];
						nelltype2 = t[1];
					}
					boolean typematch1 = isMatchGnidNellrel(gnid1, nelltype1, gnid2fbtypeset, typematchingpredict_nt_ft);
					boolean typematch2 = isMatchGnidNellrel(gnid2, nelltype2, gnid2fbtypeset, typematchingpredict_nt_ft);
					String []w = new String[l.length+2];
					System.arraycopy(l, 0, w, 2, l.length);
					w[0] = typematch1+"";
					w[1] = typematch2+"";
					dw.write(w);
				}
			}
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static boolean isMatchGnidNellrel(int gnid, String nelltype, HashMap<Integer, HashSet<String>> gnid2fbtypeset, HashMap<String, String> typematchingpredict_nt_ft) {
		HashSet<String> gnidfbtype = gnid2fbtypeset.get(gnid);
		String nelltype_corresponding_fbtype = typematchingpredict_nt_ft.get(nelltype);
		if (gnidfbtype != null && gnidfbtype.contains(nelltype_corresponding_fbtype)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//shuffleQueryResult();

		/**This is a bad shuffle code, i need to discuss*/
		//step0_sampleQueryResult();

		/**get gnid 2 fbtype*/
		//step0_getGnid2Type();

		/**filter out queryresult, if gnid1 gnid2 does not match the type requirement of the nell relation???
		 * */
		does_FBRelationInstance_satisfying_TypeMatchResult();
	}

}
