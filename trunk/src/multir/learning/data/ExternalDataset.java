package multir.learning.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import multir.util.Bytes;
import multir.util.FileOperations;
import multir.util.binary.Comparator;
import multir.util.binary.Segmenter;
import multir.util.binary.SortComponentsFactory;
import multir.util.binary.Sorter;

public class ExternalDataset implements Dataset {
	private DataInputStream dis;
	private String file;
	private Random random;

	public ExternalDataset(String file) throws IOException {
		this(new Random(), file);
	}
	
	public ExternalDataset(Random random, String file) throws IOException {
		this.file = file;
		this.random = random;
	}
	
	public void setRandom(Random random) {
		this.random = random;
	}
	
	public int numDocs() { return -1; }

	public void shuffle() {
		try {			
			// sort by random
			close();
			System.out.println("shuffling " + file);
			final Random fr = random;
			
			// solution 1: write new random values for each MILRecord
			// then sort
			{
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream
						(new FileOutputStream(file + "._insort")));			
				DataInputStream dis = new DataInputStream(new BufferedInputStream
						(new FileInputStream(file)));
				MILDocument d = new MILDocument();
				while (d.read(dis)) {
					d.random = random.nextInt();
					d.write(dos);
				}
				dis.close();
				dos.close();
			}
			//System.in.read();
			FileOperations.remove(file);
			
			Sorter.sort(file + "._insort", new SortComponentsFactory() {
				public Comparator createComparator() {
					return new Comparator() {
						int v1, v2;
						public void setArg1(byte[] b1, int p1) {
							v1 = Bytes.bytes2Int(b1, p1); }
						public void setArg2(byte[] b2, int p2) {
							v2 = Bytes.bytes2Int(b2, p2); }
						public int compare() { return v1 - v2; } }; }
				public Segmenter createSegmenter() {
					return new Segmenter() {
						//MILDocument doc = new MILDocument();
						public int recordLength(byte[] buffer, int start) {
							int s = start;
							s += 4; // random number
							int l1 = Bytes.bytes2UnsignedShort(buffer, s);
							s+= 2 + l1;
							int l2 = Bytes.bytes2UnsignedShort(buffer, s);
							s+= 2 + l2;
							int lenY = Bytes.bytes2Int(buffer, s);
							s += 4;
							s += lenY*4;
							int lenM = Bytes.bytes2Int(buffer, s);
							s += 4;
							for (int i=0; i < lenM; i++) {
								s+=8;
								int lenV = Bytes.bytes2Int(buffer, s);
								s +=4;
								s += lenV*4;
							}
							return s - start;
							/*							
							// some records could be very long ...
							ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 
									start, buffer.length-start);
							try { doc.read(new DataInputStream(bais));
							} catch (IOException e) {} // cannot happen
							return buffer.length - bais.available() - start;
							*/
						}
					};
				}
			});

			/*
			// solution 2:
			Sorter.sort(file, new SortComponentsFactory() {
				public Comparator createComparator() {
					return new Comparator() {
						public void setArg1(byte[] b1, int p1) {}
						public void setArg2(byte[] b2, int p2) {}
						public int compare() { return fr.nextBoolean()? 1 : -1; } }; }
				public Segmenter createSegmenter() {
					return new Segmenter() {
						//MILDocument doc = new MILDocument();
						public int recordLength(byte[] buffer, int start) {
							int s = start;
							s += 4; // random number
							int l1 = Bytes.bytes2UnsignedShort(buffer, s);
							s+= 2 + l1;
							int l2 = Bytes.bytes2UnsignedShort(buffer, s);
							s+= 2 + l2;
							int lenY = Bytes.bytes2Int(buffer, s);
							s += 4;
							s += lenY*4;
							int lenM = Bytes.bytes2Int(buffer, s);
							s += 4;
							for (int i=0; i < lenM; i++) {
								s+=8;
								int lenV = Bytes.bytes2Int(buffer, s);
								s +=4;
								s += lenV*4;
							}
							return s - start;
						}
					};
				}
			});
			*/
			FileOperations.remove(file + "._insort");
			FileOperations.move(file + "._insort.sorted", file);
			dis = null;
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("shuffling done");
	}
	
	public MILDocument next() {
		MILDocument doc = new MILDocument();
		if (!next(doc)) return null;
		else return doc;
	}
	
	public boolean next(MILDocument doc) {
		try {
			//if (dis == null) reset();
			if (dis == null) return false;
			return doc.read(dis);
		} catch (IOException e) { e.printStackTrace(); return false; }
	}
	
	public void reset() {
		try {
			dis = new DataInputStream(new BufferedInputStream
					(new FileInputStream(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
		try {
		dis.close();
		} catch (Exception e) {}
	}
}
