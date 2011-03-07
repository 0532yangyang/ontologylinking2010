package multir.util.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RandomAccessStore {
	byte[] buf;
	int pos = 0;
	
	public RandomAccessStore(int size) {
		this.buf = new byte[size];
	}
	
	public RandomAccessStore(byte[] buf) { this.buf = buf; }
	
	public void seek(int pos) {
		this.pos = pos;
	}
	/*
	public void setLength(int count) {
		this.count = count;
		if (pos > count) pos = count;
	}*/
	
	public int position() {
		return pos;
	}
	
	public InputStream getInputStream() {
		return new InputStream() {
		    public int read() {
		    	return (pos < buf.length) ? (buf[pos++] & 0xff) : -1;
		    }

		    public synchronized int read(byte b[], int off, int len) {
		    	if (b == null) {
		    	    throw new NullPointerException();
		    	} else if (off < 0 || len < 0 || len > b.length - off) {
		    	    throw new IndexOutOfBoundsException();
		    	}
		    	if (pos >= buf.length) {
		    	    return -1;
		    	}
		    	if (pos + len > buf.length) {
		    	    len = buf.length - pos;
		    	}
		    	if (len <= 0) {
		    	    return 0;
		    	}
		    	System.arraycopy(buf, pos, b, off, len);
		    	pos += len;
		    	return len;
		    }

		    public synchronized long skip(long n) {
		    	if (pos + n > buf.length) {
		    	    n = buf.length - pos;
		    	}
		    	if (n < 0) {
		    	    return 0;
		    	}
		    	pos += n;
		    	return n;
		    }
		        
		    public int available() { return buf.length - pos; }
		        
		    public boolean markSupported() { return false; }    			
		};
	}
	
	public OutputStream getOutputStream() {
		return new OutputStream() {
			public void write(int b) throws IOException {
				if (pos >= buf.length) throw new ExceededSizeException();
				buf[pos++] = (byte)b;
			}
			
		    public void write(byte b[], int off, int len) throws IOException {
		    	if ((off < 0) || (off > b.length) || (len < 0) ||
		                ((off + len) > b.length) || ((off + len) < 0)) {
		    	    throw new IndexOutOfBoundsException();
		    	} else if (len == 0) {
		    	    return;
		    	}
		        int newpos = pos + len;
		        if (newpos > buf.length)
		        	throw new ExceededSizeException();
		        System.arraycopy(b, off, buf, pos, len);
		        pos = newpos;		        
		    }
		};
	}
	
	public static class ExceededSizeException extends IOException {
		private static final long serialVersionUID = 7573737704327365239L;}
}
