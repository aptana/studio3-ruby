/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html.outline;

import java.util.StringTokenizer;

import org.eclipse.swt.graphics.Image;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.html.parsing.ERBScript;
import com.aptana.editor.html.outline.HTMLOutlineLabelProvider;
import com.aptana.editor.ruby.outline.RubyOutlineLabelProvider;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.IRubyScript;

public class RHTMLOutlineLabelProvider extends HTMLOutlineLabelProvider
{

	private static final Image ERB_ICON = ERBEditorPlugin.getImage("icons/embedded_code_fragment.png"); //$NON-NLS-1$

	private static final int TRIM_TO_LENGTH = 20;

	private IParseState fParseState;

	public RHTMLOutlineLabelProvider(IParseState parseState)
	{
		fParseState = parseState;
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
		String source = new String(fParseState.getSource());
		// locates the ruby source
		IRubyScript ruby = script.getScript();
		source = source.substring(ruby.getStartingOffset(), Math.min(ruby.getEndingOffset() + 1, source.length()));
		// gets the first line of the ruby source
		StringTokenizer st = new StringTokenizer(source, "\n\r\f"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
		if (st.hasMoreTokens())
		{
			source = st.nextToken();
		}
		else
		{
			source = StringUtil.EMPTY;
		}
		if (source.length() <= TRIM_TO_LENGTH)
		{
			text.append(source);
		}
		else
		{
			text.append(source.substring(0, TRIM_TO_LENGTH - 1)).append("... "); //$NON-NLS-1$
		}
		text.append(script.getEndTag());
		return text.toString();
	}
}
