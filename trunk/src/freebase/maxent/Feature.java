package freebase.maxent;


public enum Feature {
	// first pass features
	REL_NAME_MATCH,     	//Consider every word in the relation name as an atom
	REL_WORD_SYNSET_MATCH, 			//Consider every word in the relation name as synset of wordnet
//	ARG1_WORDS, ARG2_WORDS, REL_WORDS,
//	ARG1_CONTEXTUALIZATION, ARG2_CONTEXTUALIZATION, 
//	ARG1_DEPENDENCIES, ARG2_DEPENDENCIES, REL_DEPENDENCIES,
//	ARG1_PARSE_FEATURES, ARG2_PARSE_FEATURES, REL_PARSE_FEATURES, 
//	
//	REL_2GRAMS,REL_3GRAMS,	
//	REL_MINTZ_LEXICAL,
//	REL_MINTZ_SYNTACTIC,
//	REL_NONARGWORDS,
	
	// selectional preferences
//	TRANSITIONS, // later may distinguish between arg1 and arg2
	
    // second pass features
//	ARG1_ALLVALUES, ARG2_ALLVALUES,
//	ARG1_WEB, ARG2_WEB,
//	ARG1_GAUSSIAN, ARG2_GAUSSIAN 
	
}