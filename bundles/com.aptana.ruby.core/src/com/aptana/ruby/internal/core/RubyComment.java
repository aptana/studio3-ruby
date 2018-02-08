/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core;

import org.jrubyparser.ast.CommentNode;

import com.aptana.parsing.ast.ParseNode;
import com.aptana.ruby.core.IRubyComment;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.IRubyElement;

public class RubyComment extends ParseNode implements IRubyComment
{

	private String text;

	public RubyComment(CommentNode commentNode, String text)
	{
		super();
		setLocation(commentNode.getPosition().getStartOffset(), commentNode.getPosition().getEndOffset());
		this.text = text;
	}

	public String getLanguage()
	{
		return IRubyConstants.CONTENT_TYPE_RUBY;
	}

	@Override
	public short getNodeType()
	{
		return IRubyElement.COMMENT;
	}

	@Override
	public String getText()
	{
		return this.text;
	}

	@Override
	public String toString()
	{
		return "COMMENT"; //$NON-NLS-1$
	}

}
