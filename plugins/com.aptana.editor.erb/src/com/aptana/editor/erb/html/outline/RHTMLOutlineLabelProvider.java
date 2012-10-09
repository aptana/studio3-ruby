/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html.outline;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.ArrayUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.html.parsing.ERBScript;
import com.aptana.editor.html.outline.HTMLOutlineLabelProvider;
import com.aptana.editor.ruby.outline.RubyOutlineLabelProvider;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.IRubyScript;

public class RHTMLOutlineLabelProvider extends HTMLOutlineLabelProvider
{

	private static final Image ERB_ICON = ERBEditorPlugin.getImage("icons/embedded_code_fragment.png"); //$NON-NLS-1$

	private static final int TRIM_TO_LENGTH = 20;

	private IDocument fDocument;

	public RHTMLOutlineLabelProvider(IDocument document)
	{
		fDocument = document;
		addSubLanguage(IRubyConstants.CONTENT_TYPE_RUBY, new RubyOutlineLabelProvider());
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof CommonOutlineItem)
		{
			IParseNode node = ((CommonOutlineItem) element).getReferenceNode();
			if (node instanceof ERBScript)
			{
				return ERB_ICON;
			}
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof CommonOutlineItem)
		{
			IParseNode node = ((CommonOutlineItem) element).getReferenceNode();
			if (node instanceof ERBScript)
			{
				return getDisplayText((ERBScript) node);
			}
		}
		return super.getText(element);
	}

	private String getDisplayText(ERBScript script)
	{
		StringBuilder text = new StringBuilder();
		text.append(script.getStartTag());

		// locates the ruby source
		IRubyScript ruby = script.getScript();
		int start = Math.max(ruby.getStartingOffset(), 0);

		int endIndex = Math.min(ruby.getEndingOffset(), fDocument.getLength());
		try
		{
			String source = fDocument.get(start, endIndex - start + 1);
			String[] parts = StringUtil.LINE_SPLITTER.split(source);
			if (!ArrayUtil.isEmpty(parts))
			{
				source = parts[0];
			}
			text.append(StringUtil.truncate(source, TRIM_TO_LENGTH));
			String textString = text.toString();
			String end = script.getEndTag();
			if (textString.endsWith(end))
			{
				// already has end, just return it
				return textString;
			}

			// Do we need to add a space before end tag?
			if (!textString.endsWith(" ")) //$NON-NLS-1$
			{
				return textString + ' ' + end;
			}
			return textString + end;
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(ERBEditorPlugin.getDefault(), e);
			return StringUtil.EMPTY;
		}
	}
}
