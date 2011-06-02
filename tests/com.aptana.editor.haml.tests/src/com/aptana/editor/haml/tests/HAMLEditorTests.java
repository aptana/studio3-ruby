package com.aptana.editor.haml.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.aptana.editor.haml.internal.HAMLFoldingComputerTest;

public class HAMLEditorTests extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(HAMLEditorTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(HAMLFoldingComputerTest.class);
		// $JUnit-END$
		return suite;
	}

}
