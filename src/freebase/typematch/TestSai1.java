package freebase.typematch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javatools.administrative.D;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestSai1 extends TestCase {

	static int x = 0;

	public void setUp() {
		System.out.println(x);
	}

	public void tearDown() {
		System.out.println("tear down");
	}

	public void test1() {
		x++;
		D.p("hello, world", x);
	}

	public void test2() {
		x++;
		x--;
		D.p("hello, world", x);
		assertTrue(x == 2);
	}

	public static void test5() {

		LinkedList list = new LinkedList();
		Object o1 = new Object();
		list.addFirst(o1);

		// A TreeSet is an ordered collection. According to Sun's
		// API, this constructor call should throw a ClassCastException
		// because the list element is not Comparable. But
		// the constructor silently (and problematically) accepts the list.
		TreeSet t1 = new TreeSet(list);

		Set s1 = Collections.synchronizedSet(t1);

		// At this point, we have successfully created a set (s1)
		// that violations reflexivity of equality: it is not equal
		// to itself! This assertion fails at runtime on Sun's JDK.
		Assert.assertTrue(s1.equals(s1));
	}

}
