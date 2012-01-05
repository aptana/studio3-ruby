/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.ruby.formatter.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.util.EditorUtil;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.formatter.RubyFormatterConstants;
import com.aptana.editor.ruby.formatter.RubyFormatterPlugin;
import com.aptana.formatter.IDebugScopes;
import com.aptana.formatter.ui.CodeFormatterConstants;

public class RubyFormatterPreferenceInitializer extends AbstractPreferenceInitializer
{

	public void initializeDefaultPreferences()
	{
		IEclipsePreferences store = EclipseUtil.defaultScope().getNode(RubyFormatterPlugin.PLUGIN_ID);
		//
		store.putBoolean(RubyFormatterConstants.INDENT_CLASS, true);
		store.putBoolean(RubyFormatterConstants.INDENT_MODULE, true);
		store.putBoolean(RubyFormatterConstants.INDENT_METHOD, true);
		store.putBoolean(RubyFormatterConstants.INDENT_BLOCKS, true);
		store.putBoolean(RubyFormatterConstants.INDENT_CASE, false);
		store.putBoolean(RubyFormatterConstants.INDENT_WHEN, true);
		store.putBoolean(RubyFormatterConstants.INDENT_IF, true);
		//
		store.putInt(RubyFormatterConstants.LINES_FILE_AFTER_REQUIRE, 1);
		//
		store.putInt(RubyFormatterConstants.LINES_FILE_BETWEEN_MODULE, 1);
		store.putInt(RubyFormatterConstants.LINES_FILE_BETWEEN_CLASS, 1);
		store.putInt(RubyFormatterConstants.LINES_FILE_BETWEEN_METHOD, 1);
		//
		store.putInt(RubyFormatterConstants.LINES_BEFORE_FIRST, 0);
		store.putInt(RubyFormatterConstants.LINES_BEFORE_MODULE, 1);
		store.putInt(RubyFormatterConstants.LINES_BEFORE_CLASS, 1);
		store.putInt(RubyFormatterConstants.LINES_BEFORE_METHOD, 1);
		//
		store.putInt(RubyFormatterConstants.LINES_PRESERVE, 1);
		//
		store.putBoolean(RubyFormatterConstants.WRAP_COMMENTS, false);
		store.putInt(RubyFormatterConstants.WRAP_COMMENTS_LENGTH, 80);

		store.put(RubyFormatterConstants.FORMATTER_TAB_CHAR, CodeFormatterConstants.EDITOR);
		store.put(
				RubyFormatterConstants.FORMATTER_TAB_SIZE,
				Integer.toString(EditorUtil.getSpaceIndentSize(RubyEditorPlugin.getDefault().getBundle()
						.getSymbolicName())));
		store.put(RubyFormatterConstants.FORMATTER_INDENTATION_SIZE, "2"); //$NON-NLS-1$

		store.putBoolean(RubyFormatterConstants.FORMATTER_OFF_ON_ENABLED, false);
		store.put(RubyFormatterConstants.FORMATTER_ON, RubyFormatterConstants.DEFAULT_FORMATTER_ON);
		store.put(RubyFormatterConstants.FORMATTER_OFF, RubyFormatterConstants.DEFAULT_FORMATTER_OFF);

		try
		{
			store.flush();
		}
		catch (BackingStoreException e)
		{
			IdeLog.logError(RubyFormatterPlugin.getDefault(), e, IDebugScopes.DEBUG);
		}
	}
}
