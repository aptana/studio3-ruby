package com.aptana.ruby.core.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.aptana.ruby.core.RubyParserTest;
import com.aptana.ruby.core.ast.NamespaceVisitorTest;
import com.aptana.ruby.core.ast.SourceElementVisitorTest;
import com.aptana.ruby.core.codeassist.CompletionContextTest;
import com.aptana.ruby.internal.core.inference.TypeInferrerTest;

public class AllTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(CompletionContextTest.class);
		suite.addTestSuite(NamespaceVisitorTest.class);
		suite.addTestSuite(RubyParserTest.class);
		suite.addTestSuite(SourceElementVisitorTest.class);
		suite.addTestSuite(TypeInferrerTest.class);
		// $JUnit-END$
		return suite;
	}

}
