package multir.util;

import java.io.UnsupportedEncodingException;

public class Bytes {

	public static int bytes2UnsignedShort(byte[] b, int s) {
		return (char) 
			(((b[s] & 0xff) << 8) | (b[s+1] & 0xff));
	}
	
	public static int bytes2Short(byte[] b, int s) {
		return (b[s] & 0xff) << 8 | 
		       (b[s+1] & 0xff);
	}

	public static int short2Bytes(short v, byte[] b, int s) {
		b[s++] = (byte)(v>>8);
		b[s++] = (byte)(v);
		return s;
	}

	public static byte[] short2Bytes(short v) {
		byte[] b = new byte[2];
		short2Bytes(v, b, 0);
		return b;
	}

	// int converters
	// by default, Java uses big endian
	public static final int bytes2Int(byte[] b, int s) {
		return b[s] << 24 | 
		      (b[s+1] & 0xff) << 16 | 
		      (b[s+2] & 0xff) << 8 | 
		      (b[s+3] & 0xff);
	}

	public static final byte[] int2Bytes(int i) {
		return new byte[] { (byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i };
	}
	
	public static final int int2Bytes(int i, byte[] b, int off) {
		b[off] = (byte)(i>>24);
		b[off+1] = (byte)(i>>16);
		b[off+2] = (byte)(i>>8);
		b[off+3] = (byte)i;
		return 4;
	}

	public static final int bytes2IntLittleEndian(byte[] b, int s) {
		return (b[s]&0xff) | (b[s+1]&0xff)<<8 | (b[s+2]&0xff)<<16 | (b[s+3]&0xff)<<24;
	}

	// long converters, big endian (Java default)
	public static final long bytes2Long(byte[] b, int s) {
		//return arr2long(b, 0); 
		return ((long)(b[s] & 0xff))<<56 | 
		       ((long)(b[s+1]&0xff))<<48 | 
		       ((long)(b[s+2]&0xff))<<40 | 
		       ((long)(b[s+3]&0xff))<<32 |
		       ((long)(b[s+4]&0xff))<<24 | 
		       ((long)(b[s+5]&0xff))<<16 |
		       ((long)(b[s+6]&0xff))<<8 | 
		       ((long)(b[s+7]&0xff));
	}

	public static final byte[] long2Bytes(long i) {
		return new byte[] { (byte)(i>>56), (byte)(i>>48), (byte)(i>>40), (byte)(i>>32), 
				(byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i};
	}
	
	public static final long bytes2LongLittleEndian(byte[] b, int s) {
		//return arr2long(b, 0); 
		return ((long)(b[s+7]&0xff))<<56 | 
		       ((long)(b[s+6]&0xff))<<48 | 
		       ((long)(b[s+5]&0xff))<<40 | 
		       ((long)(b[s+4]&0xff))<<32 |
		       ((long)(b[s+3]&0xff))<<24 | 
		       ((long)(b[s+2]&0xff))<<16 |
		       ((long)(b[s+1]&0xff))<<8 | 
		       ((long)(b[s]&0xff));
	}

	

	// trim a string, such that the utf-8 byte representation is not longer
	// than given byte limit
	public static int utf8CharsWithinByteLimit(String s, int byteLimit) 
		throws UnsupportedEncodingException {
		byte[] bytes = s.getBytes("utf-8");
	
		if (bytes.length <= byteLimit)
			return s.length();
			
		int j = 0;
		int i = 0;
		int c = 1;
		while (i+c <= byteLimit) {
			if (i+c == bytes.length || (bytes[i+c] & 0xC0) != 0x80) {
				j++;
				i += c;
				c = 1;
			} else
				c++;			
		}
		return j;
	}
}
