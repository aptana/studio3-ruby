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
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.console.AdaptingHyperlink;
import com.aptana.core.logging.IdeLog;

/**
 * Detects URLs or bare hostnames like www.cnn.com and generates hyperlinks for them.
 * 
 * @author cwilliams
 */
public class URLDetector implements IConsoleLineTracker
{
	// FIXME Combine with URLHyperlinkDetector in terminal plugin!

	/**
	 * Detect URLs with protocol, or bare hostnames
	 */
	@SuppressWarnings("nls")
	private static final Pattern URL_DETECT_PATTERN = Pattern.compile("\\b\n"
			+ "  # Match the leading part (proto://hostname, or just hostname)\n" + "  (\n"
			+ "    # http://, or https:// leading part\n" + "    (https?)://[-\\w]+(\\.\\w[-\\w]*)+\n" + "  |\n"
			+ "    # or, try to find a hostname with more specific sub-expression\n"
			+ "    (?i: [a-z0-9] (?:[-a-z0-9]*[a-z0-9])? \\. )+ # sub domains\n"
			+ "    # Now ending .com, etc. For these, require lowercase\n" + "    (?-i: com\\b\n"
			+ "        | edu\\b\n" + "        | biz\\b\n" + "        | gov\\b\n"
			+ "        | in(?:t|fo)\\b # .int or .info\n" + "        | mil\\b\n" + "        | net\\b\n"
			+ "        | org\\b\n" + "        | [a-z][a-z]\\.[a-z][a-z]\\b # two-letter country code\n" + "    )\n"
			+ "  )\n" + "\n" + "  # Allow an optional port number\n" + "  ( : \\d+ )?\n" + "		  \n"
			+ "  # The rest of the URL is optional, and begins with /\n" + "  (\n" + "    /\n"
			+ "    # The rest are heuristics for what seems to work well\n"
			+ "    [^.!,?;\"\\'<>()\\[\\]\\{\\}\\s\\x7F-\\xFF]*\n" + "    (\n"
			+ "      [.!,?]+ [^.!,?;\"\\'<>()\\[\\]\\{\\}\\s\\x7F-\\xFF]+\n" + "    )*\n" + "  )?",
			Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

	private IConsole fConsole;

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
			String contents = this.fConsole.getDocument().get(line.getOffset(), line.getLength());

			Matcher m = URL_DETECT_PATTERN.matcher(contents);
			int start = 0;
			while (m.find(start))
			{
				String urlString = m.group().trim();
				start = m.end();
				IRegion region = new Region(m.start() + line.getOffset(), urlString.length());
				if (!urlString.startsWith("http://")) //$NON-NLS-1$
				{
					urlString = "http://" + urlString; //$NON-NLS-1$ // $codepro.audit.disable stringConcatenationInLoop
				}
				this.fConsole.addLink(new AdaptingHyperlink(new URLHyperlink(region, urlString)), region.getOffset(),
						region.getLength());
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