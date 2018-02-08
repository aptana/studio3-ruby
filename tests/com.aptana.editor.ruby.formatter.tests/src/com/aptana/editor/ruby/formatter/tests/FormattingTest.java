/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.formatter.tests;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

import com.aptana.editor.common.formatting.AbstractFormatterTestCase;
import com.aptana.editor.ruby.RubyEditorPlugin;

public class FormattingTest extends AbstractFormatterTestCase
{

	@Parameters(name = "{0}")
	public static Iterable<Object[]> data()
	{
		return Arrays.asList(AbstractFormatterTestCase.getFiles(TEST_BUNDLE_ID, FILE_TYPE));
	}

	private static String FORMATTER_FACTORY_ID = "com.aptana.editor.ruby.formatterFactory"; //$NON-NLS-1$
	private static String TEST_BUNDLE_ID = "com.aptana.editor.ruby.formatter.tests"; //$NON-NLS-1$
	private static String FILE_TYPE = "rb"; //$NON-NLS-1$

	@BeforeClass
	public static void initializePlugin() throws Exception
	{
		// Force the ruby editor plugin to load!
		RubyEditorPlugin.getDefault();
	}

	@Override
	protected String getTestBundleId()
	{
		return TEST_BUNDLE_ID;
	}

	@Override
	protected String getFormatterId()
	{
		return FORMATTER_FACTORY_ID;
	}

	@Override
	protected String getFileType()
	{
		return FILE_TYPE;
	}
}
