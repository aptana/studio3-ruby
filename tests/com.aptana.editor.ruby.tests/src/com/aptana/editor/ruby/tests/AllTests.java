/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.aptana.editor.ruby.RubyEditorTests;
import com.aptana.editor.ruby.internal.contentassist.RubyCATests;
import com.aptana.editor.ruby.internal.text.rules.InternalTextRulesTests;
import com.aptana.editor.ruby.outline.RubyOutlineTest;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for com.aptana.editor.ruby.tests");
		// $JUnit-BEGIN$
		suite.addTest(RubyEditorTests.suite());
		suite.addTest(RubyCATests.suite());
		suite.addTest(InternalTextRulesTests.suite());
		suite.addTestSuite(RubyOutlineTest.class);
		// $JUnit-END$
		return suite;
	}
}
