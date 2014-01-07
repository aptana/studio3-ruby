package com.aptana.editor.ruby;

import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@SuiteClasses({RubyCodeScannerTest.class, RubyEditorTest.class, RubyRegexScannerTest.class, RubySourceConfigurationTest.class, RubySourcePartitionScannerTest.class, RubyTokenScannerTest.class, })
public class RubyEditorTests
{

//	public static Test suite()
//	{
//		TestSuite suite = new TestSuite(RubyEditorTests.class.getName());
//		// $JUnit-BEGIN$
//		suite.addTestSuite(RubyCodeScannerTest.class);
//		suite.addTestSuite(RubyEditorTest.class);
//		suite.addTestSuite(RubyRegexScannerTest.class);
//		suite.addTestSuite(RubySourceConfigurationTest.class);
//		suite.addTestSuite(RubySourcePartitionScannerTest.class);
//		suite.addTestSuite(RubyTokenScannerTest.class);
//		// $JUnit-END$
//		return suite;
//	}
//
}
