package freebase.tackbp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.Sort;
import javatools.mydb.StringTable;

public class HackResidence {
	static String mappedrelation = "/people/person/places_lived|/people/place_lived/location";

	static String file_targetrelation = Main.dir + "/residence";
	HashMap<String, String> mid2name = new HashMap<String, String>();

	static void step1() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_fbvisibledump);
		DelimitedWriter dw = new DelimitedWriter(file_targetrelation + ".1");
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[2].equals(mappedrelation)) {
				dw.write(l);
			}
		}
		dr.close();
		dw.close();
	}

	static void step2() throws IOException {
		{
			HashMap<String, String[]> mid2other = new HashMap<String, String[]>();
			DelimitedWriter dw = new DelimitedWriter(file_targetrelation + ".2");
			{
				DelimitedReader dr = new DelimitedReader(Main.file_mid2typename);
				String[] l;
				while ((l = dr.read()) != null) {
					String mid = l[0];
					mid2other.put(mid, l);
				}
				dr.close();
			}
			{

				DelimitedReader dr = new DelimitedReader(file_targetrelation + ".1");
				String[] l;
				HashSet<String> avoidduplicate = new HashSet<String>();
				while ((l = dr.read()) != null) {
					String mid1 = l[1];
					String mid2 = l[3];
					String[] info1 = mid2other.get(mid1);
					String[] info2 = mid2other.get(mid2);
					if (!avoidduplicate.contains(mid1 + "\t" + mid2) && info1 != null && info2 != null) {
						dw.write(mid1, mid2, info1[3], info2[3], info1[2], info2[2]);
						avoidduplicate.add(mid1 + "\t" + mid2);
					}
				}
				dr.close();

			}
			dw.close();
		}
		{
			HashSet<String> typeOfArg2 = new HashSet<String>();
			List<String[]> all = (new DelimitedReader(file_targetrelation + ".2")).readAll();
			for (String[] a : all) {
				typeOfArg2.add(a[5]);
			}
			for (String a : typeOfArg2) {
				D.p(a);
			}
		}
	}

	static void getEntitiesOfType(String typerel, String output) throws IOException {
		HashSet<String> entities = new HashSet<String>();
		DelimitedReader dr = new DelimitedReader(Main.file_fulltypeinfo);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[2].equals(typerel)) {
				entities.add(l[0]);
			}
		}
		dr.close();
		DelimitedWriter dw = new DelimitedWriter(output);
		for (String a : entities) {
			dw.write(a);
		}
		dw.close();
	}

	static void getEntitiesOfLocationType(String[] typerels, String output) throws IOException {
		HashSet<String> entities = new HashSet<String>();
		DelimitedReader dr = new DelimitedReader(Main.file_fulltypeinfo);
		String[] l;
		while ((l = dr.read()) != null) {
			boolean take = false;
			for (String t : typerels) {
				if (l[2].contains(t) && l[2].startsWith("/location")) {
					take = true;
				}
			}
			if (take) {
				entities.add(l[0]);
			}
		}
		dr.close();
		DelimitedWriter dw = new DelimitedWriter(output);
		for (String a : entities) {
			dw.write(a);
		}
		dw.close();
	}

	public static void getContainedBy(String output) throws IOException {
		//j0	/m/010016	/location/location/containedby	/m/07b_l
		DelimitedReader dr = new DelimitedReader(Main.file_fbvisibledump);
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[2].equals("/location/location/containedby")) {
				dw.write(l[1], l[3]);
			}
		}
		dw.close();
	}

	public static HashSet<String> loadEntityListOfOneType(String file) throws IOException {
		HashSet<String> entities = new HashSet<String>();
		{
			DelimitedReader dr = new DelimitedReader(file);
			String[] l;
			while ((l = dr.read()) != null) {
				entities.add(l[0]);
			}
			dr.close();

		}
		return entities;
	}

	public static void step3_residenceCity() throws IOException {
		HashSet<String> cities = loadEntityListOfOneType(Main.file_citylist);
		DelimitedWriter dw = new DelimitedWriter(file_targetrelation + ".city");
		{
			DelimitedReader dr = new DelimitedReader(file_targetrelation + ".2");
			String[] l;
			while ((l = dr.read()) != null) {
				if (cities.contains(l[1])) {
					dw.write(l);
				}
			}
			dr.close();
		}
		dw.close();
	}

	public static void step4_residenceProvince() throws IOException {
		HashSet<String> provinces = loadEntityListOfOneType(Main.file_provincelist);
		HashMap<String, String> map2province = new HashMap<String, String>();
		HashMap<String, String[]> mid2other = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_mid2typename);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				mid2other.put(mid, l);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_containedby);
			String[] l;
			while ((l = dr.read()) != null) {
				if (provinces.contains(l[1])) {
					map2province.put(l[0], l[1]);
				}
			}
			dr.close();
		}
		List<String[]> residenceProvince = new ArrayList<String[]>();
		{

			DelimitedReader dr = new DelimitedReader(file_targetrelation + ".2");
			String[] l;
			while ((l = dr.read()) != null) {
				if (provinces.contains(l[1])) {
					residenceProvince.add(new String[] { l[0], l[1] });
				} else {
					String province = map2province.get(l[1]);
					if (province != null) {
						residenceProvince.add(new String[] { l[0], province });
					}
				}
			}
			dr.close();
			StringTable.sortUniq(residenceProvince);

			DelimitedWriter dw = new DelimitedWriter(file_targetrelation + ".province");
			for (String[] a : residenceProvince) {
				String mid1 = a[0];
				String mid2 = a[1];
				String[] info1 = mid2other.get(mid1);
				String[] info2 = mid2other.get(mid2);
				if (info1 != null && info2 != null) {
					dw.write(mid1, mid2, info1[3], info2[3], info1[2], info2[2]);
				}
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		//getEntitiesOfType("/location/citytown", Main.file_citylist);
		//getEntitiesOfLocationType(new String[] { "state", "province" }, Main.file_provincelist);
		//getEntityList("/location/country", Main.file_countrylist);
		//getContainedBy(Main.file_containedby);
		step1();
		step2();
		step3_residenceCity();
		step4_residenceProvince();
	}
}
