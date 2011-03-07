package percept.learning.data;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MemoryDataset implements Dataset {
	
	private Random random;
	private Example[] docs;
	private int cursor = 0;
	
	public MemoryDataset() { this(new Random()); }
	
	public MemoryDataset(Random random) { this.random = random; }
	
	public MemoryDataset(Random random, String file) 
		throws IOException {
		this(random);
		Example d = new Example();
		List<Example> l = new ArrayList<Example>();
		DataInputStream dis = new DataInputStream(new BufferedInputStream
				(new FileInputStream(file)));
		while (d.read(dis)) {
			l.add(d);
			d = new Example();
		}
		dis.close();
		docs = l.toArray(new Example[0]);
	}
	
	public int numDocs() { return docs.length; }

	public void shuffle() {
		for (int i=0; i < docs.length; i++) {
			// pick element that we want to swap with
			int e = i + random.nextInt(docs.length - i);
			Example tmp = docs[e];
			docs[e] = docs[i];
			docs[i] = tmp;
		}
	}

	public Example next() { 
		if (cursor < docs.length) 
			return docs[cursor++]; 
		else return null;
	}

	public boolean next(Example doc) {
		if (cursor < docs.length) {
			Example d = docs[cursor++];
			doc.meta = d.meta;
			doc.Y = d.Y;
			doc.features = d.features;
			return true;
		}
		return false;
	}

	public void reset() {
		cursor = 0;
	}

}