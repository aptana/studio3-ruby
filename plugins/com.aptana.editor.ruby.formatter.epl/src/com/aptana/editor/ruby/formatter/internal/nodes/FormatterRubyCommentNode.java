/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.formatter.internal.nodes;

import com.aptana.editor.ruby.formatter.RubyFormatterConstants;
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.IFormatterWriter;
import com.aptana.formatter.nodes.FormatterCommentNode;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class FormatterRubyCommentNode extends FormatterCommentNode
{

	/**
	 * A block comment 'begin' syntax. We make sure that those blocks don't get indented.
	 */
	private static final String BLOCK_COMMENT_BEGIN = "=begin"; //$NON-NLS-1$

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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.nodes.FormatterCommentNode#accept(com.aptana.formatter.IFormatterContext,
	 * com.aptana.formatter.IFormatterWriter)
	 */
	@Override
	public void accept(IFormatterContext context, IFormatterWriter visitor) throws Exception
	{
		String text = getText();
		int indent = context.getIndent();
		boolean isBlockComment = (text != null && text.startsWith(BLOCK_COMMENT_BEGIN));
		if (isBlockComment)
		{
			// We need to make sure we don't indent that block.
			context.setIndent(0);
		}
		super.accept(context, visitor);
		if (isBlockComment)
		{
			context.setIndent(indent);
		}
	}
}
