package javatools.datatypes;

/**Written by Xiao*/
public class QuickSort {
	public static int[] quicksort(double[] main) {
		int[] index = new int[main.length];
		for (int i = 0; i < main.length; i++) {
			index[i] = i;
		}
		quicksort(main, index, 0, index.length - 1);
		return index;
	}

	public static int[] quicksort(double[] main, boolean inverse) {
		int[] index = new int[main.length];
		for (int i = 0; i < main.length; i++) {
			index[i] = i;
		}
		if (!inverse) {
			quicksort(main, index, 0, index.length - 1);
		}else{
			double []temp = new double[main.length];
			for(int i=0;i<main.length;i++)temp[i] = -1.0*main[i];
			quicksort(temp, index, 0, index.length - 1);
		}
		return index;
	}

	// quicksort a[left] to a[right]
	public static void quicksort(double[] a, int[] index, int left, int right) {
		if (right <= left)
			return;
		int i = partition(a, index, left, right);
		quicksort(a, index, left, i - 1);
		quicksort(a, index, i + 1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(double[] a, int[] index, int left, int right) {
		int i = left - 1;
		int j = right;
		while (true) {
			while (less(a[++i], a[right]))
				// find item on left to swap
				; // a[right] acts as sentinel
			while (less(a[right], a[--j]))
				// find item on right to swap
				if (j == left)
					break; // don't go out-of-bounds
			if (i >= j)
				break; // check if pointers cross
			exch(a, index, i, j); // swap two elements into place
		}
		exch(a, index, i, right); // swap with partition element
		return i;
	}

	// is x < y ?
	private static boolean less(double x, double y) {
		return (x < y);
	}

	// exchange a[i] and a[j]
	private static void exch(double[] a, int[] index, int i, int j) {
		double swap = a[i];
		a[i] = a[j];
		a[j] = swap;
		int b = index[i];
		index[i] = index[j];
		index[j] = b;
	}
}
