/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.hyperlink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.aptana.terminal.hyperlink.IHyperlinkDetector;
import com.aptana.workbench.hyperlink.EditorLineHyperlink;

/**
 * Detects references to views that get rendered, resolves them relative to rails app/views
 * 
 * @author cwilliams
 */
public class RenderPathHyperlinkDetector implements IHyperlinkDetector
{
	private static Pattern RENDERED_VIEW_PATTERN = Pattern.compile("^Rendered\\s+(\\S.+?)\\s+"); //$NON-NLS-1$
	private static Pattern GENERATOR_CREATED_PATTERN = Pattern.compile("^\\s+(identical|create|exists)\\s+(\\S.+?)\\s*$"); //$NON-NLS-1$

	public IHyperlink[] detectHyperlinks(String contents)
	{
		Matcher m = RENDERED_VIEW_PATTERN.matcher(contents);
		if (m.find())
		{
			String filepath = m.group(1);
			int start = m.start(1);
			int length = m.end(1) - start;
			if (!filepath.startsWith("/")) //$NON-NLS-1$
			{
				filepath = "app/views/" + filepath; //$NON-NLS-1$
			}
			return new IHyperlink[] { new EditorLineHyperlink(new Region(start, length), filepath, 0) };
		}
		m = GENERATOR_CREATED_PATTERN.matcher(contents);
		if (m.find())
		{
			String filepath = m.group(2);
			int start = m.start(2);
			int length = m.end(2) - start;
			return new IHyperlink[] { new EditorLineHyperlink(new Region(start, length), filepath, 0) };
		}
		return new IHyperlink[0];
	}
}
