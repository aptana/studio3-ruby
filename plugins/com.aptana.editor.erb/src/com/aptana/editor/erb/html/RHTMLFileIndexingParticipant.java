/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.html.contentassist.index.HTMLFileIndexingParticipant;
import com.aptana.editor.html.parsing.HTMLParseState;
import com.aptana.index.core.AbstractFileIndexingParticipant;
import com.aptana.index.core.Index;
import com.aptana.index.core.build.BuildContext;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.ruby.internal.core.index.RubyFileIndexingParticipant;

public class RHTMLFileIndexingParticipant extends AbstractFileIndexingParticipant
{

	public void index(BuildContext context, Index index, IProgressMonitor monitor) throws CoreException
	{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		try
		{
			sub.subTask(getIndexingMessage(index, context.getURI()));
			indexSource(context, index, sub.newChild(100));
		}
		catch (Throwable e)
		{
			IdeLog.logError(ERBEditorPlugin.getDefault(), e);
		}
		finally
		{
			sub.done();
		}
	}

	private void indexSource(BuildContext context, final Index index, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		try
		{
			String fileContents = context.getContents();
			// minor optimization when creating a new empty file
			if (fileContents == null || fileContents.trim().length() <= 0)
			{
				return;
			}
			HTMLParseState parseState = new HTMLParseState(fileContents);
			parseState.setProgressMonitor(sub.newChild(20));

			IParseNode parseNode = ParserPoolFactory.parse(IERBConstants.CONTENT_TYPE_HTML_ERB, parseState);
			HTMLFileIndexingParticipant part = new HTMLFileIndexingParticipant();
			part.walkAST(context, index, parseNode, sub.newChild(30));

			// TODO Grab the ruby code only, replace rest with whitespace. Then parse and index that too!
			String rubyContents = replaceNonRubyCodeWithWhitespace(fileContents);
			sub.worked(5);
			RubyFileIndexingParticipant rfip = new RubyFileIndexingParticipant();
			rfip.indexSource(index, context, rubyContents, sub.newChild(45));
		}
		catch (Exception e)
		{
			IdeLog.logError(ERBEditorPlugin.getDefault(),
					MessageFormat.format(Messages.RHTMLFileIndexingParticipant_ERR_Indexing, context.getName()), e);
		}
		finally
		{
			sub.done();
		}
	}

	public static String replaceNonRubyCodeWithWhitespace(String source)
	{
		List<String> code = getRubyCodeChunks(source);
		if (code == null || code.size() == 0)
		{
			return fillWithWhitespace(source);
		}

		StringBuilder buffer = new StringBuilder();
		int endOfLastFragment = 0;
		boolean dontIncludeSemicolon = false;
		for (String codeFragment : code)
		{
			int beginningOfCurrentFragment = source.indexOf(codeFragment, endOfLastFragment); // find index of current
			// piece of code,
			// start looking after last piece of
			// code
			// replace from end of last code piece to beginning of next with
			// spaces for any non-whitespace characters in between
			if (codeFragment.length() > 0 && codeFragment.charAt(0) == '#')
			{
				codeFragment = fillWithWhitespace(codeFragment);
				dontIncludeSemicolon = true;
			}
			String portion = source.substring(endOfLastFragment, beginningOfCurrentFragment);
			for (int j = 0; j < portion.length(); j++)
			{
				char chr = portion.charAt(j);
				if (Character.isWhitespace(chr))
				{
					buffer.append(chr);
				}
				else
				{
					if (j != 0 && chr == '>' && portion.charAt(j - 1) == '%')
					{
						if (dontIncludeSemicolon)
						{
							buffer.append(' ');
							dontIncludeSemicolon = false;
						}
						else
							buffer.append(';');
					}
					else
					{
						buffer.append(' ');
					}
				}
			}

			buffer.append(codeFragment); // now add in code piece
			endOfLastFragment = beginningOfCurrentFragment + codeFragment.length(); // now search from end of
			// current fragment
		}
		return buffer.toString();
	}

	/**
	 * Takes a string and replaces all non-whitespace content with space characters (retains any existing whitespace in
	 * place).
	 * 
	 * @param source
	 * @return
	 */
	private static String fillWithWhitespace(String source)
	{
		StringBuilder buffer = new StringBuilder();
		for (int j = 0; j < source.length(); j++)
		{
			char chr = source.charAt(j);
			if (Character.isWhitespace(chr))
			{
				buffer.append(chr);
			}
			else
			{
				buffer.append(' ');
			}
		}
		return buffer.toString();
	}

	private static List<String> getRubyCodeChunks(String stringContents)
	{
		List<String> code = new ArrayList<String>();
		String[] pieces = stringContents.split("(<%%)|(%%>)|(<%=)|(<%)|(\\-?%>)"); //$NON-NLS-1$
		for (int i = 0; i < pieces.length; i++)
		{
			if ((i % 2) == 1)
			{
				code.add(pieces[i]);
			}
		}
		return code;
	}
}
