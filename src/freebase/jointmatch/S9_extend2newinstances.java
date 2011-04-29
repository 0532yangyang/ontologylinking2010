package freebase.jointmatch;


public class S9_extend2newinstances {
	public static void main(String []args) throws Exception{
		QueryFBGraph qfb = new QueryFBGraph();
		qfb.getNewEntitiesWithOntologyMapping(Main.file_predict_relonly, Main.file_extendedwidpairs);
	}
}
