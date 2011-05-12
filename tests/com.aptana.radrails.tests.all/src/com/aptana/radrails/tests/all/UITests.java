/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.radrails.tests.all;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class UITests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(UITests.class.getName())
		{
			@Override
			public void runTest(Test test, TestResult result)
			{
				System.out.println("Running test: " + test.toString());
				super.runTest(test, result);
			}
		};
		// $JUnit-BEGIN$
		suite.addTest(org.radrails.rails.ui.tests.AllTests.suite());
		suite.addTest(com.aptana.editor.ruby.tests.AllTests.suite());
		suite.addTest(com.aptana.editor.erb.tests.AllTests.suite());
		// FIXME Add tests for HAML!
//		suite.addTest(com.aptana.editor.haml.tests.AllTests.suite());
		suite.addTest(com.aptana.editor.sass.tests.AllTests.suite());
		// $JUnit-END$
		return suite;
	}

}
