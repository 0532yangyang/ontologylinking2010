package percept.learning.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import percept.util.SparseBinaryVector;

public class Example {
	
	public String meta = "";
	
	public int Y;
	public SparseBinaryVector features;
	
	public Example() {

	}
	
	public boolean read(DataInputStream dis) throws IOException {
		try {
			meta = dis.readUTF();
			Y = dis.readInt();
			if (features == null) features = new SparseBinaryVector();
			features.deserialize(dis);
			return true;
		} catch (EOFException e) { return false; }
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(meta);
		dos.writeInt(Y);
		features.serialize(dos);
	}
}
