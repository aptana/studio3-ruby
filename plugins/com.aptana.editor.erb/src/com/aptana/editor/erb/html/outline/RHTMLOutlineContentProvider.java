/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html.outline;

import com.aptana.editor.html.outline.HTMLOutlineContentProvider;
import com.aptana.editor.ruby.outline.RubyOutlineContentProvider;
import com.aptana.editor.ruby.parsing.IRubyParserConstants;

public class RHTMLOutlineContentProvider extends HTMLOutlineContentProvider
{
	public RHTMLOutlineContentProvider()
	{
		addSubLanguage(IRubyParserConstants.LANGUAGE, new RubyOutlineContentProvider());
	}
}
