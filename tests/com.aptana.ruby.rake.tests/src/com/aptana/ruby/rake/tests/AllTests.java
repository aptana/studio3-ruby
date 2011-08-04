package com.aptana.ruby.rake.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.aptana.ruby.internal.rake.RakeFileFinderTest;

public class AllTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(RakeFileFinderTest.class);
		// $JUnit-END$
		return suite;
	}

}
