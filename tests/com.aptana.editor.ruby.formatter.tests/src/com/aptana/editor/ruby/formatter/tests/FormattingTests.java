/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.formatter.tests;

import com.aptana.editor.common.formatting.AbstractFormatterTestCase;

public class FormattingTests extends AbstractFormatterTestCase
{

	// Turn this flag on for development only (used to generate the formatted files)
	// To generate formatted files, place js files under the 'formatting' folder and run these tests from the
	// com.aptana.editor.js.formatter.tests plugin
	// NOTE: Ensure that the contents section ends with a newline, or the generation may not work.
	private static boolean INITIALIZE_MODE = false;
	// Turning on the overwrite will re-generate the formatted block and overwrite it into the test files.
	// This is a drastic move that will require a review of the output right after to make sure we have the
	// right formatting for all the test file, so turn it on at your own risk.
	private static boolean OVERWRITE_MODE = false;

	private static String FORMATTER_FACTORY_ID = "com.aptana.editor.ruby.formatterFactory"; //$NON-NLS-1$
	private static String TEST_BUNDLE_ID = "com.aptana.editor.ruby.formatter.tests"; //$NON-NLS-1$
	private static String FILE_TYPE = "rb"; //$NON-NLS-1$

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

	@Override
	protected boolean isOverriteMode()
	{
		return OVERWRITE_MODE;
	}

	@Override
	protected boolean isInitializeMode()
	{
		return INITIALIZE_MODE;
	}

}
