package multir.util.binary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import multir.util.DynamicIntArray;
import multir.util.FileOperations;
import multir.util.binary.RandomAccessStore.ExceededSizeException;

public class Sorter {

	static int MAX_RECORD_LENGTH = 32*1024*1024;  // must be << threadbytes
    //static int MAX_THREAD_BYTES = 512 * 1024 * 1024;
    static int MAX_THREAD_BYTES = 1024* 1024 * 1024;
    
    static int BUFFER_SIZE = 1024*1024;
    //static int BUFFER_SIZE = 128*1024*1024;
    //static int BUFFER_SIZE = 256*1024*1024;
    
    public static void sort(String input, SortComponentsFactory fac) 
    	throws IOException {
    	InputStream is = new BufferedInputStream(new FileInputStream(input));

    	int runs = 0, stage = 0;
    	String prefix = input + "_";

		DynamicIntArray dia = new DynamicIntArray();
		RandomAccessStore ras = new RandomAccessStore(MAX_THREAD_BYTES);
		OutputStream os = ras.getOutputStream();
		Segmenter s = fac.createSegmenter();
		byte[] b = new byte[BUFFER_SIZE];
		// fill up buffer
		boolean readingComplete = false;
		int bp = 0, bl = 0;
		while (b.length - bl > 0) {
			int read = is.read(b, bl, b.length - bl);
			if (read < 0) {
				readingComplete = true;
				break;
			}
			bl += read;
		}
		//if (bl < MAX_RECORD_LENGTH) { System.out.println("warning"); }
		//boolean w = false;
		while (bl - bp > 0) {
			int l = s.recordLength(b, bp);
			//if (w) System.out.println(l);
			try {
				int pos = ras.position();
				os.write(b, bp, l);
				bp += l;
				dia.append(pos);
			} catch (ExceededSizeException e) {
				// launch sort				
				new MemorySortTask2(ras,dia,fac,prefix + stage + "-" + runs++).run();				
				//System.out.println("ok " + ras.position());
				//bl -= ras.position();
				//w = true;
				ras.seek(0);
				dia.clear();
				//System.arraycopy(b, bp, b, 0, bl - bp);
				//bl = bl - bp;
				
				
				// added now
				//os.write(b, bp, l);
				//bp += l;
				//dia.append(0);				
			}
			if (bl - bp < MAX_RECORD_LENGTH && !readingComplete) {
				//System.out.println(l + "<" + bl + " " + bp);
				System.arraycopy(b, bp, b, 0, bl - bp);
				bl = bl - bp;
				bp = 0;

				while (b.length - bl > 0) {
					int read = is.read(b, bl, b.length - bl);
					if (read < 0) {
						readingComplete = true;
						break;
					}
					bl += read;
				}
				
				//int le = is.read(b, bl, b.length-bl);
				//if (le > 0) bl += le;
				//else readingComplete = true;//System.out.println("DONE READING, " + bl + " " + l);
			}
		}
		if (dia.length() > 0) {
			new MemorySortTask2(ras,dia,fac,prefix + stage + "-" + runs++).run();
		}

		while (runs > 1)
		{
			stage++;
			
			// begin merging
			for (int i=0; i < runs/2; i++) {
				String r1 = prefix + (stage-1) + "-" + (2*i);
				String r2 = prefix + (stage-1) + "-" + (2*i + 1);
				
				new MergeTask(r1, r2,
						prefix + stage + "-" + i, fac).run();
				new File(r1).delete();
				new File(r2).delete();
			}
			if (runs % 2 == 1) {   // 2*(runs/2) equals runs-1
				new File(prefix + (stage-1) + "-" + 2*(runs/2)).renameTo(
						new File(prefix + stage + "-" + (runs/2)));
			}
			runs = (runs % 2 == 0) ? runs/2 : runs/2 + 1;
		}
		
		//FileOperations.move(input, input + ".unsorted");
		FileOperations.move(prefix + stage + "-0", input + ".sorted");
    }
    
    private static class MergeTask implements Runnable {
    	private String run1, run2, mergedRun;
    	private SortComponentsFactory fac;
    	
    	public MergeTask(String run1, String run2, String mergedRun, SortComponentsFactory fac) {
    		this.run1 = run1;
    		this.run2 = run2;
    		this.mergedRun = mergedRun;
    		this.fac = fac;
    	}
    
    	public void run() {
    		try {
    			System.out.println("run sort task");
				Comparator comparator = fac.createComparator();
				Segmenter segmenter = fac.createSegmenter();
				
				InputStream is1 = new BufferedInputStream(new FileInputStream(run1));
				InputStream is2 = new BufferedInputStream(new FileInputStream(run2));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(mergedRun));
				
				byte[] b1 = new byte[BUFFER_SIZE];
				byte[] b2 = new byte[BUFFER_SIZE];
				int b1p = 0, b2p = 0;
				int b1l = 0, b2l = 0;
				
				b1l = is1.read(b1);
				b2l = is2.read(b2);
				
				boolean readingComplete1 = false;
				boolean readingComplete2 = false;
				
				while (b1l - b1p > 0 && b2l - b2p > 0) {
					comparator.setArg1(b1, b1p);
					comparator.setArg2(b2, b2p);
					int c = comparator.compare();
					if (c <= 0) {
						int l = segmenter.recordLength(b1, b1p);
						os.write(b1, b1p, l);
						b1p += l;
					} 
					if (c >= 0) {
						int l = segmenter.recordLength(b2, b2p);
						os.write(b2, b2p, l);
						b2p += l;
					}
					if (b1l - b1p < MAX_RECORD_LENGTH && !readingComplete1) {
						System.arraycopy(b1, b1p, b1, 0, b1l - b1p);
						b1l = b1l - b1p;
						b1p = 0;
						int l = is1.read(b1, b1l, b1.length-b1l);
						if (l > 0) b1l += l;
						else readingComplete1 = true;
					}
					if (b2l - b2p < MAX_RECORD_LENGTH && !readingComplete2) {
						System.arraycopy(b2, b2p, b2, 0, b2l - b2p);				
						b2l = b2l - b2p;
						b2p = 0;
						int l = is2.read(b2, b2l, b2.length-b2l);
						if (l > 0) b2l += l;
						else readingComplete2 = true;
					}
				}
				while (b1l - b1p > 0) {
					os.write(b1, b1p, b1l-b1p);
					b1p = 0;
					b1l = is1.read(b1, 0, b1.length);
				}
				while (b2l - b2p > 0) {
					os.write(b2, b2p, b2l-b2p);
					b2p = 0;
					b2l = is2.read(b2, 0, b2.length);			
				}
				is1.close();
				is2.close();
				os.close();
	    	} catch (Exception e) { e.printStackTrace(); }
    	}
	}
    
    
    static class MemorySortTask2 implements Runnable {
    	private RandomAccessStore ras;
    	private DynamicIntArray dia;
    	private SortComponentsFactory fac;
    	private String target;
    	private Comparator comparator;
    	
    	public MemorySortTask2(RandomAccessStore ras, DynamicIntArray dia, SortComponentsFactory fac, String target) {
    		this.ras = ras;
    		this.dia = dia;
    		this.fac = fac;
    		this.target = target;
    		this.comparator = fac.createComparator();
    	}
    	
    	public void run() { 
    		System.out.println("sorting " + dia.length() + " elements (" + dia.get(dia.length()-1) + "++ bytes)");
    		Comparison c = new Comparison(comparator);
    		c.buffer = ras.buf;
    		int[] d = dia.getData();
    		int dlen = dia.length();
    		/*
    		try {
    			sort(0, dlen, d, c);
    		} catch (Exception e) {
    			System.out.println(e.getMessage());
    		}*/
    		
    		Integer[] tmp = new Integer[dlen];
    		for (int i=0; i < dlen; i++) tmp[i] = d[i];

    		//Random r = new Random();
    		
    		final byte[] buf = ras.buf;
    		
    		Arrays.<Integer>sort(tmp, 0, dlen, new java.util.Comparator<Integer>() {
    			public int compare(Integer a, Integer b) {
    				comparator.setArg1(buf, a);
    				comparator.setArg2(buf, b);
    				return comparator.compare();
    				//return r.nextDouble() < .5;
    			}
    		});
    		
    		
    		try {
    			// serialize
    			DataOutputStream dos = new DataOutputStream
    				(new BufferedOutputStream(new FileOutputStream(target)));
    			Segmenter s = fac.createSegmenter();
    			for (int i=0; i < dlen; i++) {
    				int pos = tmp[i];
    				int len = s.recordLength(ras.buf, pos);
    				dos.write(ras.buf, pos, len);
    			}
    			dos.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		
    		/*
    
    		int[] d = dia.getData();
    		int dlen = dia.length();
    		
    		Integer[] tmp = new Integer[dlen];
    		for (int i=0; i < dlen; i++) tmp[i] = d[i];

    		//Random r = new Random();
    		
    		final byte[] buf = ras.buf;
    		
    		Arrays.<Integer>sort(tmp, 0, dlen, new java.util.Comparator<Integer>() {
    			public int compare(Integer a, Integer b) {
    				comparator.setArg1(buf, a);
    				comparator.setArg2(buf, b);
    				return comparator.compare();
    				//return r.nextDouble() < .5;
    			}
    		});
    		*/
    	}
    	/*
    	private static int SIMPLE_LENGTH = 7;
    	
    	private static void mergeSort(Object[] in, Object[] out, int start,
    			int end, java.util.Comparator c) {
    		int len = end - start;
    		// use insertion sort for small arrays
    		if (len <= SIMPLE_LENGTH) {
    			for (int i = start + 1; i < end; i++) {
    				Object current = out[i];
    				Object prev = out[i - 1];
    				if (c.compare(prev, current) > 0) {
    					int j = i;
    					do {
    						out[j--] = prev;
    					} while (j > start
    							&& (c.compare(prev = out[j - 1], current) > 0));
    					out[j] = current;
    				}
    			}
    			return;
    		}
    		int med = (end + start) >>> 1;
    		mergeSort(out, in, start, med, c);
    		mergeSort(out, in, med, end, c);

    		// merging
    		
    		// if arrays are already sorted - no merge
    		if (c.compare(in[med - 1],in[med] ) <= 0) {
    			System.arraycopy(in, start, out, start, len);
    			return;
    		}
    		int r = med, i = start;
    		
    		// use merging with exponential search
    		do {
    			Object fromVal = in[start];
    			Object rVal = in[r];
    			if (c.compare(fromVal, rVal) <= 0) {
    				int l_1 = find(in, rVal, -1, start + 1, med - 1, c);
    				int toCopy = l_1 - start + 1;
    				System.arraycopy(in, start, out, i, toCopy);
    				i += toCopy;
    				out[i++] = rVal;
    				r++;
    				start = l_1 + 1;
    			} else {
    				int r_1 = find(in, fromVal, 0, r + 1, end - 1, c);
    				int toCopy = r_1 - r + 1;
    				System.arraycopy(in, r, out, i, toCopy);
    				i += toCopy;
    				out[i++] = fromVal;
    				start++;
    				r = r_1 + 1;
    			}
    		} while ((end - r) > 0 && (med - start) > 0);
    		
    		// copy rest of array
    		if ((end - r) <= 0) {
    			System.arraycopy(in, start, out, i, med - start);
    		} else {
    			System.arraycopy(in, r, out, i, end - r);
    		}
    	}

    	private static int find(Object[] arr, Object val, int bnd, int l, int r,
    			java.util.Comparator c) {
    		int m = l;
    		int d = 1;
    		while (m <= r) {
    			if (c.compare(val, arr[m]) > bnd) {
    				l = m + 1;
    			} else {
    				r = m - 1;
    				break;
    			}
    			m += d;
    			d <<= 1;
    		}
    		while (l <= r) {
    			m = (l + r) >>> 1;
    			if (c.compare(val, arr[m]) > bnd) {
    				l = m + 1;
    			} else {
    				r = m - 1;
    			}
    		}
    		return l - 1;
    	}
    	*/		 
    	
    	
    	
    	// cannot use quicksort if comparison is random!!!
    	static class Comparison {
    		Comparator c;
    		byte[] buffer;
    		Comparison(Comparator c) {
    			this.c = c;
    			
    		}
    		void setBuffer(byte[] buffer) {
    			this.buffer = buffer;
    		}
    		
    		int c(int v1, int v2) {
    			c.setArg1(buffer, v1);
    			c.setArg1(buffer, v2);
    			return c.compare();
    		}
    	}
    	
    	
    	private static int med3(int[] array, int a, int b, int c, Comparison o) {
    		int x = array[a], y = array[b], z = array[c];
    		return o.c(x,y) < 0 ? (o.c(y, z) < 0 ? b : (o.c(x,z) < 0 ? c : a)) : (o.c(y,z) > 0 ? b : (o.c(x,z) > 0 ? c
    				: a));
    	}
    		 
    	private static void sort(int start, int end, int[] array, Comparison o) {
    		System.out.println("[" + start + "," + end + "]");
    		int temp;
    		int length = end - start;
    		if (length < 7) {
    			for (int i = start + 1; i < end; i++) {
    				for (int j = i; j > start && o.c(array[j - 1], array[j]) > 0; j--) {
    					temp = array[j];
    					array[j] = array[j - 1];
    					array[j - 1] = temp;
    				}
    			}
    			return;
    		}
    		int middle = (start + end) / 2;
    		if (length > 7) {
    			int bottom = start;
    			int top = end - 1;
    			if (length > 40) {
    				length /= 8;
    				bottom = med3(array, bottom, bottom + length, bottom
    						+ (2 * length), o);
    				middle = med3(array, middle - length, middle, middle + length, o);
    				top = med3(array, top - (2 * length), top - length, top, o);
    			}
    			middle = med3(array, bottom, middle, top, o);
    		}
    		int partionValue = array[middle];
    		int a, b, c, d;
    		a = b = start;
    		c = d = end - 1;
    		while (true) {
    			while (b <= c && o.c(array[b],partionValue) <= 0) {
    				if (o.c(array[b],partionValue) == 0) {
    					temp = array[a];
    					array[a++] = array[b];
    					array[b] = temp;
    				}
    				b++;
    			}
    			while (c >= b && o.c(array[c],partionValue) >= 0) {
    				if (o.c(array[c],partionValue) == 0) {
    					temp = array[c];
    					array[c] = array[d];
    					array[d--] = temp;
    				}
    				c--;
    			}
    			if (b > c) {
    				break;
    			}
    			temp = array[b];
    			array[b++] = array[c];
    			array[c--] = temp;
    		}
    		length = a - start < b - a ? a - start : b - a;
    		int l = start;
    		int h = b - length;
    		while (length-- > 0) {
    			temp = array[l];
    			array[l++] = array[h];
    			array[h++] = temp;
    		}
    		length = d - c < end - 1 - d ? d - c : end - 1 - d;
    		l = b;
    		h = end - length;
    		while (length-- > 0) {
    			temp = array[l];
    			array[l++] = array[h];
    			array[h++] = temp;
    		}
    		if ((length = b - a) > 0) {
    			sort(start, start + length, array, o);
    		}
    		if ((length = d - c) > 0) {
    			sort(end - length, end, array, o);
    		}
    	}
	
    	
    	
    	/*
    	private static int med3(byte[] array, int a, int b, int c) {
    		byte x = array[a], y = array[b], z = array[c];
    		return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
    				: a));
    	}
    		 
    	private static void sort(int start, int end, byte[] array) {
    		byte temp;
    		int length = end - start;
    		if (length < 7) {
    			for (int i = start + 1; i < end; i++) {
    				for (int j = i; j > start && array[j - 1] > array[j]; j--) {
    					temp = array[j];
    					array[j] = array[j - 1];
    					array[j - 1] = temp;
    				}
    			}
    			return;
    		}
    		int middle = (start + end) / 2;
    		if (length > 7) {
    			int bottom = start;
    			int top = end - 1;
    			if (length > 40) {
    				length /= 8;
    				bottom = med3(array, bottom, bottom + length, bottom
    						+ (2 * length));
    				middle = med3(array, middle - length, middle, middle + length);
    				top = med3(array, top - (2 * length), top - length, top);
    			}
    			middle = med3(array, bottom, middle, top);
    		}
    		byte partionValue = array[middle];
    		int a, b, c, d;
    		a = b = start;
    		c = d = end - 1;
    		while (true) {
    			while (b <= c && array[b] <= partionValue) {
    				if (array[b] == partionValue) {
    					temp = array[a];
    					array[a++] = array[b];
    					array[b] = temp;
    				}
    				b++;
    			}
    			while (c >= b && array[c] >= partionValue) {
    				if (array[c] == partionValue) {
    					temp = array[c];
    					array[c] = array[d];
    					array[d--] = temp;
    				}
    				c--;
    			}
    			if (b > c) {
    				break;
    			}
    			temp = array[b];
    			array[b++] = array[c];
    			array[c--] = temp;
    		}
    		length = a - start < b - a ? a - start : b - a;
    		int l = start;
    		int h = b - length;
    		while (length-- > 0) {
    			temp = array[l];
    			array[l++] = array[h];
    			array[h++] = temp;
    		}
    		length = d - c < end - 1 - d ? d - c : end - 1 - d;
    		l = b;
    		h = end - length;
    		while (length-- > 0) {
    			temp = array[l];
    			array[l++] = array[h];
    			array[h++] = temp;
    		}
    		if ((length = b - a) > 0) {
    			sort(start, start + length, array);
    		}
    		if ((length = d - c) > 0) {
    			sort(end - length, end, array);
    		}
    	}
    	*/
    }
    	
    	
    
    
    
    static class MemorySortTask implements Runnable {
    	private RandomAccessStore ras;
    	private DynamicIntArray dia;
    	private SortComponentsFactory fac;
    	private String target;
    	private Comparator comparator;
    	
    	public MemorySortTask(RandomAccessStore ras, DynamicIntArray dia, SortComponentsFactory fac, String target) {
    		this.ras = ras;
    		this.dia = dia;
    		this.fac = fac;
    		this.target = target;
    		this.comparator = fac.createComparator();
    	}
    	
    	public void run() { 
    		System.out.println("sorting " + dia.length() + " elements (" + dia.get(dia.length()-1) + "++ bytes)");
    		
    		try {
    			// sort
    			quicksort(0, dia.length()-1);

    			// serialize
    			DataOutputStream dos = new DataOutputStream
    				(new BufferedOutputStream(new FileOutputStream(target)));
    			Segmenter s = fac.createSegmenter();
    			for (int i=0; i < dia.length(); i++) {
    				int pos = dia.get(i);
    				int len = s.recordLength(ras.buf, pos);
    				dos.write(ras.buf, pos, len);
    			}
    			dos.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    	}
    	
        private void quicksort (int lo, int hi)
        {
        	int i=lo, j=hi;
        	int pivot = (lo+hi)/2; 
			comparator.setArg2(ras.buf, dia.get(pivot));

			while (i <= j) {
				
				comparator.setArg1(ras.buf, dia.get(i));
				while (comparator.compare() < 0) {
					i++;
					comparator.setArg1(ras.buf, dia.get(i));
				}
				comparator.setArg1(ras.buf, dia.get(j));
				while (comparator.compare() > 0) {
					j--;
					comparator.setArg1(ras.buf, dia.get(j));
				}
                if (i<=j)
                {
                    swap(i, j);
                    i++; j--;
                }
            }

            // recursion
            if (lo<j) quicksort(lo, j);
            if (i<hi) quicksort(i, hi);
        }
        
        private void swap(int i, int j) {
        	int t1 = dia.get(i);
        	int t2 = dia.get(j);
        	dia.set(i, t2);
        	dia.set(j, t1);
        }       
    }    
}