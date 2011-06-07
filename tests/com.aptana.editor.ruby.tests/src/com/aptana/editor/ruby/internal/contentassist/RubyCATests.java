package com.aptana.editor.ruby.internal.contentassist;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RubyCATests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(RubyCATests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(RubyContentAssistProcessorTest.class);
		suite.addTestSuite(RubyCommentContentAssistProcessorTest.class);
		suite.addTestSuite(RubyRegexpContentAssistProcessorTest.class);
		suite.addTestSuite(RubyDoubleQuotedStringContentAssistProcessorTest.class);
		// $JUnit-END$
		return suite;
	}

}
