package javatools.dbd;


import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class DataAccessor {
	// Open the indices
	public DataAccessor(EntityStore store) throws DatabaseException {

		// Primary key for Inventory classes

		// Secondary key for Inventory classes
		// Last field in the getSecondaryIndex() method must be
		// the name of a class member; in this case, an Inventory.class
		// data member.

		// Primary key for Vendor class
		delStringById = store.getPrimaryIndex(Integer.class, DelString.class);
		delStringByKey = store.getSecondaryIndex(delStringById, String.class, "key");
	}

	public PrimaryIndex<Integer, DelString> delStringById;
	public SecondaryIndex<String, Integer, DelString> delStringByKey;
	
}
