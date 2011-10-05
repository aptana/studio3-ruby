package com.aptana.editor.ruby;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RubyEditorTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(RubyEditorTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(RubyCodeScannerTest.class);
		suite.addTestSuite(RubyEditorTest.class);
		suite.addTestSuite(RubyRegexScannerTest.class);
		suite.addTestSuite(RubySourceConfigurationTest.class);
		suite.addTestSuite(RubySourcePartitionScannerTest.class);
		suite.addTestSuite(RubyTokenScannerTest.class);
		// $JUnit-END$
		return suite;
	}

}
