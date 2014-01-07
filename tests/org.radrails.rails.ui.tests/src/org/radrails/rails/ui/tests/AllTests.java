package org.radrails.rails.ui.tests;

import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.radrails.rails.internal.ui.hyperlink.RenderPathHyperlinkDetectorTest;

@RunWith(Suite.class)
@SuiteClasses({RenderPathHyperlinkDetectorTest.class, })
public class AllTests
{

//	public static Test suite()
//	{
//		TestSuite suite = new TestSuite(AllTests.class.getName());
//		// $JUnit-BEGIN$
//		suite.addTestSuite(RenderPathHyperlinkDetectorTest.class);
//		// $JUnit-END$
//		return suite;
//	}
//
}
