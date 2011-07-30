package com.aptana.ruby.core.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.aptana.ruby.core.RubyParserTest;
import com.aptana.ruby.core.ast.SourceElementVisitorTest;

public class AllTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(RubyParserTest.class);
		suite.addTestSuite(SourceElementVisitorTest.class);
		// $JUnit-END$
		return suite;
	}

}
