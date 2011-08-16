/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml;

import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.text.RubyRegexpAutoIndentStrategy;

public class HAMLAutoIndentStrategy extends RubyRegexpAutoIndentStrategy
{

	private static final String TAG_OR_FILTER_ONLY = "^\\s*[%:](\\w|\\.|#)+"; //$NON-NLS-1$
	private static final Pattern BEGIN_WITH_TAG_OR_FILTER = Pattern
			.compile("^\\s*[%:](?!area|base|br|col|hr|img|input|link|meta|param)(\\w|\\.)+.*$"); //$NON-NLS-1$
	private static final Pattern ATTRIBUTE = Pattern.compile("^\\s*\\{.*\\}\\s*$"); //$NON-NLS-1$

	public HAMLAutoIndentStrategy(String contentType, SourceViewerConfiguration configuration,
			ISourceViewer sourceViewer, IPreferenceStore prefStore)
	{
		super(contentType, configuration, sourceViewer, prefStore);
	}

	@Override
	protected boolean autoIndent(IDocument d, DocumentCommand c)
	{
		if (c.offset <= 0 || d.getLength() == 0 || !shouldAutoIndent())
		{
			return false;
		}

		String newline = c.text;
		try
		{
			// Get the line and run a regexp check against it
			IRegion curLineRegion = d.getLineInformationOfOffset(c.offset);
			String lineContent = d.get(curLineRegion.getOffset(), c.offset - curLineRegion.getOffset()).trim();
			boolean shouldAutoIndent = false;

			// check for ruby blocks
			if (lineContent.length() > 1 && lineContent.charAt(0) == '-' && lineContent.endsWith("|")) //$NON-NLS-1$
			{
				shouldAutoIndent = true;
			}
			else if (BEGIN_WITH_TAG_OR_FILTER.matcher(lineContent).matches() && !lineContent.endsWith("/")) //$NON-NLS-1$
			{
				// Remove the tag/filter and check if there is content after. If there is, we only want to indent if
				// it's an attribute
				String contentWithoutTagFilter = lineContent.replaceAll(TAG_OR_FILTER_ONLY, StringUtil.EMPTY);
				if (StringUtil.isEmpty(contentWithoutTagFilter) || ATTRIBUTE.matcher(contentWithoutTagFilter).matches())
				{
					shouldAutoIndent = true;
				}
			}

			if (shouldAutoIndent)
			{

				String previousLineIndent = getAutoIndentAfterNewLine(d, c);
				String restOfLine = d.get(c.offset, curLineRegion.getLength() - (c.offset - curLineRegion.getOffset()));
				String startIndent = newline + previousLineIndent + getIndentString();
				if (indentAndPushTrailingContentAfterNewlineAndCursor(lineContent, restOfLine))
				{
					c.text = startIndent + newline + previousLineIndent;
				}
				else
				{
					c.text = startIndent;
				}
				c.shiftsCaret = false;
				c.caretOffset = c.offset + startIndent.length();
				return true;
			}

		}
		catch (BadLocationException e)
		{
			IdeLog.logError(HAMLEditorPlugin.getDefault(), e);
		}

		return false;
	}

}
