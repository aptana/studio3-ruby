package com.aptana.editor.erb.html;

import org.eclipse.jface.text.ITextViewer;

import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.html.HTMLOpenTagCloser;

public class ERBOpenTagCloser extends HTMLOpenTagCloser
{

	public ERBOpenTagCloser(ITextViewer textViewer)
	{
		super(textViewer);
	}

	protected boolean skipOpenTag(String openTag)
	{
		return super.skipOpenTag(openTag) || openTag.startsWith(IERBConstants.OPEN_EVALUATE_TAG);
	}

}
