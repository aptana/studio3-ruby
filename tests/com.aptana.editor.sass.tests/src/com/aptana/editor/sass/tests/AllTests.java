/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.aptana.editor.sass.SassCodeScannerTest;
import com.aptana.editor.sass.SassEditorTest;
import com.aptana.editor.sass.SassFoldingComputerTest;
import com.aptana.editor.sass.SassSourcePartitionScannerTest;

@RunWith(Suite.class)
@SuiteClasses({ SassSourcePartitionScannerTest.class, SassCodeScannerTest.class, SassEditorTest.class,
		SassFoldingComputerTest.class, })
public class AllTests
{

}
