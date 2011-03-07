package multir.learning.data;

public interface Dataset {
	
	public int numDocs();
	
	public void shuffle();
	
	public MILDocument next();
	
	public boolean next(MILDocument doc);
	
	public void reset();
}
