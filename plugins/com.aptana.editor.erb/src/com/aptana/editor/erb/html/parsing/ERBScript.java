/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html.parsing;

import com.aptana.editor.ruby.IRubyConstants;
import com.aptana.parsing.ast.ParseNode;
import com.aptana.ruby.core.IRubyScript;

public class ERBScript extends ParseNode
{

	private IRubyScript fScript;
	private String fStartTag;
	private String fEndTag;

	public ERBScript(IRubyScript script, String startTag, String endTag)
	{
		super(IRubyConstants.CONTENT_TYPE_RUBY);
		fScript = script;
		fStartTag = startTag;
		fEndTag = endTag;

		setChildren(fScript.getChildren());
		setLocation(script.getStartingOffset(), script.getEndingOffset());
	}

	public String getStartTag()
	{
		return fStartTag;
	}

	public String getEndTag()
	{
		return fEndTag;
	}

	public IRubyScript getScript()
	{
		return fScript;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!super.equals(obj))
		{
			return false;
		}
		if (!(obj instanceof ERBScript))
		{
			return false;
		}
		ERBScript other = (ERBScript) obj;
		return start == other.start && end == other.end && fScript.equals(other.fScript);
	}

	@Override
	public int hashCode()
	{
		int hash = start * 31 + end;
		hash = hash * 31 + fScript.hashCode();
		return hash;
	}
}
