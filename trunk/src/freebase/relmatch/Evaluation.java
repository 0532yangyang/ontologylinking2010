package freebase.relmatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

public class Evaluation {

	/**
	 * IDEA:: In seedbfspathes_show_group, if one fb relation seriously match to
	 * two nell relation, then it is very likely to match none of them rightly.
	 * what will the matching looks like if I filter away these
	 * */
	public static void test1() throws Exception {
		DelimitedWriter dw = new DelimitedWriter(Main.reportdir+"/0324filter_duplicate_fbrelation");
		List<String[]> all = (new DelimitedReader(Main.file_seedbfspath_show_group)).readAll();
		Set<String> bad = new HashSet<String>();
		StringTable.sortByColumn(all, new int[] { 2 });
		for (int i = 1; i < all.size(); i++) {
			if (all.get(i)[2].equals(all.get(i - 1)[2])) {
				String[] x = all.get(i);
				String[] y = all.get(i - 1);
				if (!(x[0].startsWith(y[0]) || y[0].startsWith(x[0]))) {
					bad.add(x[2]);
					// D.p(x[2],x[0],y[0],x[3],y[3]);
				}
			}
		}
		StringTable.sortByColumn(all, new int[] { 0,3 }, new boolean[]{false, true});
		List<String[]>filter = new ArrayList<String[]>();
		for (int i = 0; i < all.size(); i++) {
			String fbr = all.get(i)[2];
			if(!bad.contains(fbr)){
				filter.add(all.get(i));
			}
		}
		List<String[]>top3 = StringTable.selectTopKofBlock(filter, 0, 3);
		for(String []a:top3){
			dw.write(a);
		}
		dw.close();
	}

	public static void main(String[] args) throws Exception {
		test1();

		/**
		 * test2 grep
		 * "/sports/drafted_athlete/drafted|/sports/sports_league_draft_pick/team|/sports/sports_team/arena_stadium|"
		 * queryresult_startid_endid_nellrel_fbrid_fbrstr_entities_entitienames
		 * >report/0323stadium grep
		 * "/people/person/nationality|/olympics/olympic_participating_country/athletes|/olympics/olympic_athlete_affiliation/sport|"
		 * aqueryresult_startid_endid_nellrel_fbrid_fbrstr_entities_entitienames
		 * .sbStartId > report/0323relatedtonation grep
		 * "/location/location/people_born_here|/people/deceased_person/place_of_death|/location/location/containedby|"
		 * queryresult_startid_endid_nellrel_fbrid_fbrstr_entities_entitienames
		 * .sbStartId > report/0323locationborn
		 * */
	}
}
