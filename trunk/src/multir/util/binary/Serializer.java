package multir.util.binary;

import java.io.DataInput;
import java.io.DataOutput;

public interface Serializer {
	public boolean read(DataInput in);
	public void write(DataOutput out);
}
