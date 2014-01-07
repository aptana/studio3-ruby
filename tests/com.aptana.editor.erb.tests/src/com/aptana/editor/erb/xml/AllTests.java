/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.xml;

import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@SuiteClasses({RXMLContentDescriberTest.class, RXMLEditorTest.class, })
public class AllTests
{

//	public static Test suite()
//	{
//		TestSuite suite = new TestSuite("Tests for com.aptana.editor.erb.xml"); //$NON-NLS-1$
//		// $JUnit-BEGIN$
//		suite.addTestSuite(RXMLContentDescriberTest.class);
//		suite.addTestSuite(RXMLEditorTest.class);
//		// $JUnit-END$
//		return suite;
//	}
}
