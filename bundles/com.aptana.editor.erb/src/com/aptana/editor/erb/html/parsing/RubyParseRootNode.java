/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html.parsing;

import beaver.Symbol;

import com.aptana.parsing.ast.ParseRootNode;
import com.aptana.ruby.core.IRubyConstants;

/**
 * @author cwilliams
 */
public class RubyParseRootNode extends ParseRootNode
{

	protected RubyParseRootNode(int start, int end)
	{
		super(new Symbol[0], start, end);
	}

	public String getLanguage()
	{
		return IRubyConstants.CONTENT_TYPE_RUBY;
	}

}
