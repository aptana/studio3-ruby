/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.hyperlink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.console.AdaptingHyperlink;
import com.aptana.core.logging.IdeLog;
import com.aptana.terminal.hyperlink.IHyperlinkDetector;
import com.aptana.workbench.hyperlink.EditorLineHyperlink;

/**
 * Detects references to views that get rendered, resolves them relative to rails app/views
 * 
 * @author cwilliams
 */
public class RenderPathHyperlinkDetector implements IHyperlinkDetector, IConsoleLineTracker
{
	private static Pattern RENDERED_VIEW_PATTERN = Pattern.compile("^Rendered\\s+(\\S.+?)\\s+"); //$NON-NLS-1$
	private static Pattern GENERATOR_CREATED_PATTERN = Pattern
			.compile("^\\s+(identical|exists|create(\\s+mode\\s+\\d+)?)\\s+(\\S.+?)\\s*$"); //$NON-NLS-1$

	private IConsole fConsole;

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
			String filepath = m.group(3);
			int start = m.start(3);
			int length = m.end(3) - start;
			return new IHyperlink[] { new EditorLineHyperlink(new Region(start, length), filepath, 0) };
		}
		return new IHyperlink[0];
	}

	public void init(IConsole console)
	{
		this.fConsole = console;
	}

	public void lineAppended(IRegion line)
	{
		if (this.fConsole == null)
		{
			return;
		}

		try
		{
			String contents = fConsole.getDocument().get(line.getOffset(), line.getLength());
			IHyperlink[] links = detectHyperlinks(contents);
			for (IHyperlink link : links)
			{
				IRegion region = link.getHyperlinkRegion();
				this.fConsole.addLink(new AdaptingHyperlink(link), region.getOffset() + line.getOffset(), region.getLength());
			}
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(RailsUIPlugin.getDefault(), e);
		}
	}

	public void dispose()
	{
		this.fConsole = null;
	}
}
