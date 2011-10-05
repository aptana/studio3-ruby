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

	public RubyComment(CommentNode commentNode)
	{
		super(IRubyConstants.CONTENT_TYPE_RUBY);
		setLocation(commentNode.getPosition().getStartOffset(), commentNode.getPosition().getEndOffset());
	}

	@Override
	public short getNodeType()
	{
		return IRubyElement.COMMENT;
	}

	@Override
	public String toString()
	{
		return "COMMENT"; //$NON-NLS-1$
	}

}
