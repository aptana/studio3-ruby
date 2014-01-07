/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html;

import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.aptana.editor.erb.html.outline.RHTMLOutlineTest;

@RunWith(Suite.class)
@SuiteClasses({RHTMLSourcePartitionScannerTest.class, RHTMLTagScannerTest.class, RHTMLParserTest.class, RHTMLContentDescriberTest.class, ERBOpenTagCloserTest.class, RHTMLEditorTest.class, RHTMLOutlineTest.class, })
public class AllTests
{

//	public static Test suite()
//	{
//		TestSuite suite = new TestSuite("Tests for com.aptana.editor.erb.html"); //$NON-NLS-1$
//		// $JUnit-BEGIN$
//		suite.addTestSuite(RHTMLSourcePartitionScannerTest.class);
//		suite.addTestSuite(RHTMLTagScannerTest.class);
//		suite.addTestSuite(RHTMLParserTest.class);
//		suite.addTestSuite(RHTMLContentDescriberTest.class);
//		suite.addTestSuite(ERBOpenTagCloserTest.class);
//		suite.addTestSuite(RHTMLEditorTest.class);
//		suite.addTestSuite(RHTMLOutlineTest.class);
//		// $JUnit-END$
//		return suite;
//	}
}
