/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
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
