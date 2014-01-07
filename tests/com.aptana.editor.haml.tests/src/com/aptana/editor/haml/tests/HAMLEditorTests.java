package com.aptana.editor.haml.tests;

import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.aptana.editor.haml.HAMLAutoIndentStrategyTest;
import com.aptana.editor.haml.HAMLPartitionTest;
import com.aptana.editor.haml.internal.HAMLFoldingComputerTest;

@RunWith(Suite.class)
@SuiteClasses({HAMLFoldingComputerTest.class, HAMLPartitionTest.class, HAMLAutoIndentStrategyTest.class, })
public class HAMLEditorTests
{

//	public static Test suite()
//	{
//		TestSuite suite = new TestSuite(HAMLEditorTests.class.getName());
//		// $JUnit-BEGIN$
//		suite.addTestSuite(HAMLFoldingComputerTest.class);
//		suite.addTestSuite(HAMLPartitionTest.class);
//		suite.addTestSuite(HAMLAutoIndentStrategyTest.class);
//		// $JUnit-END$
//		return suite;
//	}
//
}
