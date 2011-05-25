package freebase.jointmatch5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class S41 {
	/**Using seed bfs path to get all instances in FB*/
	public static void main(String[] args) throws Exception {
		FreebaseRelation fbr = new FreebaseRelation(Main.file_seedbfspath_result);
		fbr.selfTest(Main.file_seedbfspath_show + ".3");
		QueryFBGraph qfb = new QueryFBGraph(true);
		qfb.getNewEntitiesWithUnionRelations(fbr.filteredUnionRelations, Main.file_sql2instance_union);
	}
}
