package com.aptana.editor.ruby.internal.text;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class InternalTextTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(InternalTextTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(RubyFoldingComputerTest.class);
		//$JUnit-END$
		return suite;
	}

}
