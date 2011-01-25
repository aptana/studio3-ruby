/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.formatter.internal.nodes;

import com.aptana.editor.ruby.formatter.RubyFormatterConstants;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.FormatterCommentNode;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterRubyCommentNode extends FormatterCommentNode
{

	/**
	 * @param document
	 * @param startOffset
	 * @param endOffset
	 */
	public FormatterRubyCommentNode(IFormatterDocument document, int startOffset, int endOffset)
	{
		super(document, startOffset, endOffset);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterCommentNode#getWrappingKey()
	 */
	@Override
	public String getWrappingKey()
	{
		return RubyFormatterConstants.WRAP_COMMENTS;
	}
}
