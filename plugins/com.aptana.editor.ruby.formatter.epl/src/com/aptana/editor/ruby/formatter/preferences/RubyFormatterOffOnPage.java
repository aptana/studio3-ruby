/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.formatter.preferences;

import java.net.URL;

import com.aptana.editor.ruby.formatter.RubyFormatterConstants;
import com.aptana.formatter.ui.IFormatterModifyDialog;
import com.aptana.formatter.ui.preferences.AbstractFormatterOffOnPage;

/**
 * A formatter On/Off page for Ruby.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class RubyFormatterOffOnPage extends AbstractFormatterOffOnPage
{
	private static final String ON_OFF_PREVIEW_FILE = "off-on-preview.rb"; //$NON-NLS-1$

	/**
	 * @param dialog
	 */
	public RubyFormatterOffOnPage(IFormatterModifyDialog dialog)
	{
		super(dialog);
	}

	protected URL getPreviewContent()
	{
		return getClass().getResource(ON_OFF_PREVIEW_FILE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.preferences.AbstractFormatterOffOnPage#getOffOnEnablementKey()
	 */
	@Override
	protected String getOffOnEnablementKey()
	{
		return RubyFormatterConstants.FORMATTER_OFF_ON_ENABLED;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.preferences.AbstractFormatterOffOnPage#getOffTextIdentifierKey()
	 */
	@Override
	protected String getOffTextIdentifierKey()
	{
		return RubyFormatterConstants.FORMATTER_OFF;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.preferences.AbstractFormatterOffOnPage#getOnTextIdentifierKey()
	 */
	@Override
	protected String getOnTextIdentifierKey()
	{
		return RubyFormatterConstants.FORMATTER_ON;
	}
}
