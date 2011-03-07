package freebase.maxent;


public class Settings {
	public static Feature[] pass1FeaturesToCompute = {
		Feature.REL_NAME_MATCH,//Nell name & FB name share some keywords
		Feature.REL_WORD_SYNSET_MATCH //Nell name & FB name share some synsets
	};
}
