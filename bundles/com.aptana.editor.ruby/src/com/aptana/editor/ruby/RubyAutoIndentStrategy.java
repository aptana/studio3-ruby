/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser.NullWarnings;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserSupport19;
import org.jrubyparser.parser.Ruby19Parser;
import org.jrubyparser.parser.RubyParser;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.text.RubyRegexpAutoIndentStrategy;

/**
 * Special subclass of auto indenter that will auto-close methods/blocks/classes/types with 'end" when needed.
 * 
 * @author cwilliams
 */
class RubyAutoIndentStrategy extends RubyRegexpAutoIndentStrategy
{
	RubyAutoIndentStrategy(String contentType, SourceViewerConfiguration configuration, ISourceViewer sourceViewer,
			IPreferenceStore prefStore)
	{
		super(contentType, configuration, sourceViewer, prefStore);
	}

	private final Pattern openBlockPattern = Pattern.compile(".*[\\S].*do[\\w|\\s]*"); //$NON-NLS-1$
	private static final String BLOCK_CLOSER = "end"; //$NON-NLS-1$

	@Override
	protected boolean autoIndent(IDocument d, DocumentCommand c)
	{
		boolean superAutoIndent = super.autoIndent(d, c);

		int p = Math.max(0, c.offset == d.getLength() ? c.offset - 1 : c.offset);
		int line = 0;
		IRegion currentLineRegion = null;
		int startOfCurrentLine = 0;
		String lineString = null;
		try
		{
			line = d.getLineOfOffset(p);
			currentLineRegion = d.getLineInformation(line);
			startOfCurrentLine = currentLineRegion.getOffset();
			lineString = d.get(startOfCurrentLine, c.offset - startOfCurrentLine);
		}
		catch (BadLocationException e)
		{
			IdeLog.logError(RubyEditorPlugin.getDefault(), "Unable to get text of line at offset: " + p, e); //$NON-NLS-1$
			return false;
		}

		if (!superAutoIndent)
		{
			if (lineString.startsWith("=begin")) //$NON-NLS-1$
			{
				// TODO If doesn't start at beginning of line, move to first column?
				String indent = getIndentString();
				c.text += indent;
				c.caretOffset = c.offset + indent.length();
				c.shiftsCaret = false;
				c.text += TextUtilities.getDefaultLineDelimiter(d) + "=end"; //$NON-NLS-1$
				return true;
			}

			return false;
		}

		// Ruble says we're at an indentation point, this is where we should look for closing with "end"
		String trimmed = lineString.trim();
		if (trimmed.equals("=begin")) //$NON-NLS-1$
		{
			// TODO If doesn't start at beginning of line, move to first column
			String indent = getIndentString();
			c.text += indent;
			c.caretOffset = c.offset + indent.length();
			c.shiftsCaret = false;
			c.text += TextUtilities.getDefaultLineDelimiter(d) + "=end"; //$NON-NLS-1$
		}
		// insert closing "end" on new line after an unclosed block
		if (closeBlock() && unclosedBlock(d, trimmed, c.offset))
		{
			String previousLineIndent = getAutoIndentAfterNewLine(d, c);
			c.text += TextUtilities.getDefaultLineDelimiter(d) + previousLineIndent + BLOCK_CLOSER;
		}

		return true;
	}

	private boolean unclosedBlock(IDocument d, String trimmed, int offset)
	{
		// FIXME wow is this ugly! There has to be an easier way to tell if there's an unclosed block besides parsing
		// and catching a syntaxError!
		if (!atStartOfBlock(trimmed))
		{
			return false;
		}

		// TODO Re-use parser pool? Right now we can't because syntax exceptions get silently swallowed; and we need to
		// pass down warnings/line number/etc in parseState.
		ParserConfiguration config = new ParserConfiguration(0, CompatVersion.BOTH);
		ParserSupport19 support = new ParserSupport19();
		support.setConfiguration(config);
		support.setWarnings(new NullWarnings());
		RubyParser parser = new Ruby19Parser(support);
		LexerSource lexerSource = null;
		Reader reader = null;
		try
		{
			reader = new BufferedReader(new StringReader(d.get()));
			lexerSource = LexerSource.getSource(StringUtil.EMPTY, reader, config);
			parser.parse(config, lexerSource);
		}
		catch (SyntaxException e)
		{
			if (e.getPid() != SyntaxException.PID.GRAMMAR_ERROR)
			{
				return false;
			}
			Reader reader2 = null;
			try
			{
				StringBuffer buffer = new StringBuffer(d.get());
				buffer.insert(offset, TextUtilities.getDefaultLineDelimiter(d) + BLOCK_CLOSER);
				reader2 = new BufferedReader(new StringReader(buffer.toString()));
				lexerSource = LexerSource.getSource(StringUtil.EMPTY, reader2, config);
				parser.parse(config, lexerSource);
			}
			catch (SyntaxException syntaxException)
			{
				return false;
			}
			catch (IOException ioe)
			{
				return false;
			}
			finally
			{
				if (reader2 != null)
				{
					try
					{
						reader2.close();
					}
					catch (IOException e1) // $codepro.audit.disable emptyCatchClause
					{
						// ignore
					}
				}
			}
			return true;
		}
		catch (Throwable t)
		{
			IdeLog.logError(RubyEditorPlugin.getDefault(), "Got unexpected exception parsing file", t); //$NON-NLS-1$
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e) // $codepro.audit.disable emptyCatchClause
				{
					// ignore
				}
			}
		}
		return false;
	}

	@SuppressWarnings("nls")
	private boolean atStartOfBlock(String line)
	{
		return line.startsWith("class ") || line.startsWith("if ") || line.startsWith("while ")
				|| line.startsWith("module ") || line.startsWith("unless ") || line.startsWith("def ")
				|| line.equals("begin") || line.startsWith("case ") || line.startsWith("for ")
				|| openBlockPattern.matcher(line).matches();
	}

	private boolean closeBlock()
	{
		// TODO Set up a pref value for user to turn this behavior off?
		return true;
	}

}
