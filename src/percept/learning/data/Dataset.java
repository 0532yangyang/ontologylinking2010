package percept.learning.data;

public interface Dataset {
	
	public int numDocs();
	
	public void shuffle();
	
	public Example next();
	
	public boolean next(Example doc);
	
	public void reset();
}
