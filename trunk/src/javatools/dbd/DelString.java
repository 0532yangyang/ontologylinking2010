package javatools.dbd;

import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class DelString {
	private String[] value;
	@PrimaryKey
	private int id;
	
    @SecondaryKey(relate=MANY_TO_ONE)
    private String key;
    
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	public void setKey(String key){
		this.key = key;
	}
	public String getKey(){
		return key;
	}
	public void setValue(String[] a) {
		value = a;
	}
	public String[] getValue() {
		return value;
	}

}
