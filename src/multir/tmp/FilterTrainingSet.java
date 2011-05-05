package multir.tmp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import multir.learning.data.MILDocument;
import multir.util.SparseBinaryVector;

public class FilterTrainingSet {

	// 1. reduce mentions to maximum
	// 2. throw out pairs with only 1 mention
	// 3. throw out pairs with same argument twice

	static String dir = "/projects/pardosa/data14/raphaelh/t";

	static String exp = "/ftexp4";

	// h2, h3
	
	static boolean H1 = true;
	static boolean H2 = false;
	static boolean H3 = false;
	static boolean H4 = false;
	
	static int MAX_MENTIONS = 1;
	static double RANDOM_THRESH = .2;
	
	public static void main(String[] args) throws IOException {
		
		Random random = new Random(7);
		
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(dir + exp + "/train_max1")));
		
		MILDocument doc = new MILDocument();
		
		DataInputStream dis = new DataInputStream(new BufferedInputStream(
				new FileInputStream(dir + exp + "/train")));
		
		while (doc.read(dis)) {
			//w.write(doc.numMentions, doc.arg1, doc.arg2);
			
			// throw out pairs with same argument twice
			if (H3 && doc.arg1.equals(doc.arg2)) continue;			
			
			// throw out pairs with only 1 mention
			if (H2 && doc.numMentions < 2) continue;
			
			// randomly throw out pairs to reduce size
			if (H4 && random.nextDouble() > RANDOM_THRESH) continue;
			
			MILDocument ndoc = new MILDocument();
			ndoc.arg1 = doc.arg1;
			ndoc.arg2 = doc.arg2;
			ndoc.Y = doc.Y;
			
			if (H1 && doc.numMentions > MAX_MENTIONS) {
				ndoc.Z = new int[MAX_MENTIONS];
				ndoc.mentionIDs = new int[MAX_MENTIONS];
				ndoc.features = new SparseBinaryVector[MAX_MENTIONS];
				ndoc.numMentions = MAX_MENTIONS;
				for (int i=0; i < MAX_MENTIONS; i++) {
					ndoc.Z[i] = doc.Z[i];
					ndoc.mentionIDs[i] = doc.mentionIDs[i];
					ndoc.features[i] = doc.features[i];
				}
				
			} else {
				ndoc.Z = doc.Z;
				ndoc.numMentions = doc.numMentions;
				ndoc.mentionIDs = doc.mentionIDs;
				ndoc.features = doc.features;
			}
			
			ndoc.write(dos);
		}
		dis.close();
		dos.close();
	}
	
}
