/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.ruby.preferences.messages"; //$NON-NLS-1$

	public static String RubyPreferencePage_Ruby_Page_Title;
	public static String RubyPreferencePage_initial_fold_options_label;
	public static String RubyPreferencePage_fold_comments_label;
	public static String RubyPreferencePage_fold_methods_label;
	public static String RubyPreferencePage_fold_inner_types_label;
	public static String RubyPreferencePage_fold_blocks_label;
	public static String RubyPreferencePage_fold_requires_label;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
