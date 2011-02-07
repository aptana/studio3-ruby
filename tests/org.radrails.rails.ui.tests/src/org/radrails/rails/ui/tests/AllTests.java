package org.radrails.rails.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.radrails.rails.internal.ui.hyperlink.RenderPathHyperlinkDetectorTest;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(RenderPathHyperlinkDetectorTest.class);
		// $JUnit-END$
		return suite;
	}

}
