package com.aptana.editor.ruby.internal.text.rules;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class InternalTextRulesTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(InternalTextRulesTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(LineContinuationDamagerRepairerTest.class);
		//$JUnit-END$
		return suite;
	}

}
