/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *     Appcelerator, Inc. - Improved implementation (Shalom Gibly) 
 *******************************************************************************/
package com.aptana.editor.ruby.formatter;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.parser.ParserResult;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.util.EditorUtil;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.formatter.internal.RubyFormatterContext;
import com.aptana.editor.ruby.formatter.internal.RubyFormatterNodeBuilder;
import com.aptana.editor.ruby.formatter.internal.RubyFormatterNodeRewriter;
import com.aptana.formatter.AbstractScriptFormatter;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.FormatterIndentDetector;
import com.aptana.formatter.FormatterUtils;
import com.aptana.formatter.FormatterWriter;
import com.aptana.formatter.IDebugScopes;
import com.aptana.formatter.IFormatterContext;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.formatter.ui.FormatterException;
import com.aptana.formatter.ui.FormatterMessages;
import com.aptana.ruby.core.NullParserResult;
import com.aptana.ruby.core.RubySourceParser;
import com.aptana.ui.util.StatusLineMessageTimerManager;

public class RubyFormatter extends AbstractScriptFormatter
{

	protected static final String[] INDENTING = { RubyFormatterConstants.INDENT_CLASS,
			RubyFormatterConstants.INDENT_MODULE, RubyFormatterConstants.INDENT_METHOD,
			RubyFormatterConstants.INDENT_BLOCKS, RubyFormatterConstants.INDENT_IF, RubyFormatterConstants.INDENT_CASE,
			RubyFormatterConstants.INDENT_WHEN };

	protected static final String[] BLANK_LINES = { RubyFormatterConstants.LINES_FILE_AFTER_REQUIRE,
			RubyFormatterConstants.LINES_FILE_BETWEEN_MODULE, RubyFormatterConstants.LINES_FILE_BETWEEN_CLASS,
			RubyFormatterConstants.LINES_FILE_BETWEEN_METHOD, RubyFormatterConstants.LINES_BEFORE_FIRST,
			RubyFormatterConstants.LINES_BEFORE_MODULE, RubyFormatterConstants.LINES_BEFORE_CLASS,
			RubyFormatterConstants.LINES_BEFORE_METHOD };

	public RubyFormatter(String lineSeparator, Map<String, String> preferences, String mainContentType)
	{
		super(preferences, mainContentType, lineSeparator);
	}

	public int detectIndentationLevel(IDocument document, int offset, boolean isSelection,
			IFormattingContext formattingContext)
	{
		try
		{
			ITypedRegion partition = document.getPartition(offset);
			if (partition != null && partition.getOffset() == offset)
			{
				return super.detectIndentationLevel(document, offset);
			}
			final String source = document.get();
			final ParserResult result;

			RubySourceParser sourceParser = getSourceParser();
			result = sourceParser.parse(source);
			if (!(result instanceof NullParserResult))
			{
				final RubyFormatterNodeBuilder builder = new RubyFormatterNodeBuilder();
				final FormatterDocument fDocument = createDocument(source);
				IFormatterContainerNode root = builder.build(result, fDocument);
				new RubyFormatterNodeRewriter(result).rewrite(root);
				final IFormatterContext context = new RubyFormatterContext(0);
				FormatterIndentDetector detector = new FormatterIndentDetector(offset);
				try
				{
					root.accept(context, detector);
					return detector.getLevel();
				}
				catch (Exception e)
				{
					// ignore
				}
			}
		}
		catch (Throwable t)
		{
			IdeLog.logError(RubyFormatterPlugin.getDefault(), t, IDebugScopes.DEBUG);
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.IScriptFormatter#getIndentSize()
	 */
	public int getIndentSize()
	{
		return getInt(RubyFormatterConstants.FORMATTER_INDENTATION_SIZE, 1);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.IScriptFormatter#getIndentType()
	 */
	public String getIndentType()
	{
		return getString(RubyFormatterConstants.FORMATTER_TAB_CHAR);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.IScriptFormatter#getTabSize()
	 */
	public int getTabSize()
	{
		return getInt(RubyFormatterConstants.FORMATTER_TAB_SIZE, getEditorSpecificTabWidth());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatter#getEditorSpecificTabWidth()
	 */
	public int getEditorSpecificTabWidth()
	{
		return EditorUtil.getSpaceIndentSize(RubyEditorPlugin.getDefault().getBundle().getSymbolicName());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatter#isEditorInsertSpacesForTabs()
	 */
	public boolean isEditorInsertSpacesForTabs()
	{
		return FormatterUtils.isInsertSpacesForTabs(RubyEditorPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.IScriptFormatter#format(java.lang.String, int, int, int, boolean,
	 * org.eclipse.jface.text.formatter.IFormattingContext, java.lang.String)
	 */
	public TextEdit format(String source, int offset, int length, int indent, boolean isSelection,
			IFormattingContext context, String indentSufix) throws FormatterException
	{
		String input = source.substring(offset, offset + length);
		if (isSlave())
		{
			// We are formatting an ERB content
			if (input.startsWith("<%=")) { //$NON-NLS-1$
				input = input.substring(3);
				offset += 3;
				length -= 3;
			}
			else if (input.startsWith("<%")) { //$NON-NLS-1$
				input = input.substring(2);
				offset += 2;
				length -= 2;
			}
			if (input.endsWith("%>")) { //$NON-NLS-1$
				input = input.substring(0, input.length() - 2);
				length -= 2;
			}
			// We also skip any new-line characters for the ERB case. Otherwise, the formatting will jump the code up.
			int toTrim = 0;
			for (int i = 0; i < input.length(); i++)
			{
				char c = input.charAt(i);
				if (c == '\n' || c == '\r')
				{
					toTrim++;
				}
				else
				{
					break;
				}
			}
			if (toTrim > 0)
			{
				input = input.substring(toTrim);
				offset += toTrim;
				length -= toTrim;
			}
		}
		RubySourceParser sourceParser = getSourceParser();
		ParserResult result = sourceParser.parse(input);
		try
		{
			if (!(result instanceof NullParserResult))
			{
				String output = format(input, result, indent, isSelection);
				if (output != null)
				{
					output = trimLeft(output);
					if (offset > 0)
					{
						output = ' ' + output;
					}
					if (!input.equals(output))
					{
						if (!isValidation()
								|| equalLinesIgnoreBlanks(new StringReader(input), new StringReader(output)))
						{
							return new ReplaceEdit(offset, length, output);
						}
						else
						{
							logError(input, output);
						}
					}
					else
					{
						return new MultiTextEdit(); // NOP
					}
				}
			}
			else
			{
				StatusLineMessageTimerManager.setErrorMessage(NLS
						.bind(FormatterMessages.Formatter_formatterParsingErrorStatus,
								Messages.RubyFormatter_rubyParserError), ERROR_DISPLAY_TIMEOUT, true);
			}
		}
		catch (Throwable t)
		{
			StatusLineMessageTimerManager.setErrorMessage(FormatterMessages.Formatter_formatterErrorStatus,
					ERROR_DISPLAY_TIMEOUT, true);
			IdeLog.logError(RubyFormatterPlugin.getDefault(), t, IDebugScopes.DEBUG);
		}
		return null;
	}

	/**
	 * @param output
	 * @return
	 */
	private static String trimLeft(String output)
	{
		int offset = 0;
		for (; offset < output.length(); offset++)
		{
			if (!Character.isWhitespace(output.charAt(offset)))
			{
				break;
			}
		}
		if (offset != 0)
		{
			return output.substring(offset);
		}
		return output;
	}

	/**
	 * @return RubySourceParser
	 */
	protected RubySourceParser getSourceParser()
	{
		return new RubySourceParser(CompatVersion.BOTH);
	}

	protected boolean isValidation()
	{
		return !getBoolean(RubyFormatterConstants.WRAP_COMMENTS);
	}

	/**
	 * @param input
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private String format(String input, ParserResult result, int indent, boolean isSelection) throws Exception
	{
		int spacesCount = -1;
		if (isSelection)
		{
			spacesCount = countLeftWhitespaceChars(input);
		}
		final RubyFormatterNodeBuilder builder = new RubyFormatterNodeBuilder();
		final FormatterDocument document = createDocument(input);
		IFormatterContainerNode root = builder.build(result, document);
		new RubyFormatterNodeRewriter(result).rewrite(root);
		IFormatterContext context = new RubyFormatterContext(indent);
		FormatterWriter writer = new FormatterWriter(document, lineSeparator, createIndentGenerator());
		writer.setWrapLength(getInt(RubyFormatterConstants.WRAP_COMMENTS_LENGTH));
		writer.setLinesPreserve(getInt(RubyFormatterConstants.LINES_PRESERVE));
		root.accept(context, writer);
		writer.flush(context);
		String output = writer.getOutput();
		List<IRegion> offOnRegions = builder.getOffOnRegions();
		if (offOnRegions != null && !offOnRegions.isEmpty())
		{
			// We re-parse the output to extract its On-Off regions, so we will be able to compute the offsets and
			// adjust it.
			List<IRegion> outputOnOffRegions = getOutputOnOffRegions(output,
					getString(RubyFormatterConstants.FORMATTER_OFF), getString(RubyFormatterConstants.FORMATTER_ON),
					document);
			output = FormatterUtils.applyOffOnRegions(input, output, offOnRegions, outputOnOffRegions);
		}
		if (isSelection)
		{
			output = leftTrim(output, spacesCount);
		}
		return output;
	}

	private FormatterDocument createDocument(String input)
	{
		FormatterDocument document = new FormatterDocument(input);
		for (String key : INDENTING)
		{
			document.setBoolean(key, getBoolean(key));
		}
		for (String key : BLANK_LINES)
		{
			document.setInt(key, getInt(key));
		}
		document.setInt(RubyFormatterConstants.FORMATTER_TAB_SIZE, getInt(RubyFormatterConstants.FORMATTER_TAB_SIZE));
		document.setBoolean(RubyFormatterConstants.WRAP_COMMENTS, getBoolean(RubyFormatterConstants.WRAP_COMMENTS));

		// Formatter OFF/ON
		document.setBoolean(RubyFormatterConstants.FORMATTER_OFF_ON_ENABLED,
				getBoolean(RubyFormatterConstants.FORMATTER_OFF_ON_ENABLED));
		document.setString(RubyFormatterConstants.FORMATTER_ON, getString(RubyFormatterConstants.FORMATTER_ON));
		document.setString(RubyFormatterConstants.FORMATTER_OFF, getString(RubyFormatterConstants.FORMATTER_OFF));

		return document;
	}

	// Collect and return the regions that will be excluded from formatting (between the 'OFF' and 'ON' tags).
	private List<IRegion> getOutputOnOffRegions(String output, String formatterOffPattern, String formatterOnPattern,
			IFormatterDocument document)
	{
		RubySourceParser sourceParser = getSourceParser();
		ParserResult result = sourceParser.parse(output);
		if (result != null && !(result instanceof NullParserResult))
		{
			return collectOffOnRegions(result.getCommentNodes(), document);
		}
		return null;
	}

	/**
	 * Resolves the formatter's Off-On regions.<br>
	 * The method will try to collect the 'Off' and 'On' tags and set the regions that will be ignored when formatting.<br>
	 * 
	 * @param commentNodes
	 * @param document
	 * @return A list of regions (may be null)
	 */
	public static List<IRegion> collectOffOnRegions(List<CommentNode> commentNodes, IFormatterDocument document)
	{
		if (!document.getBoolean(RubyFormatterConstants.FORMATTER_OFF_ON_ENABLED) || commentNodes == null)
		{
			return null;
		}
		LinkedHashMap<Integer, String> commentsMap = new LinkedHashMap<Integer, String>(commentNodes.size());
		for (CommentNode comment : commentNodes)
		{
			int start = comment.getPosition().getStartOffset();
			commentsMap.put(start, comment.getContent());
		}
		// Generate the OFF/ON regions
		if (!commentsMap.isEmpty())
		{
			Pattern onPattern = Pattern.compile(Pattern.quote(document.getString(RubyFormatterConstants.FORMATTER_ON)));
			Pattern offPattern = Pattern
					.compile(Pattern.quote(document.getString(RubyFormatterConstants.FORMATTER_OFF)));
			return FormatterUtils.resolveOnOffRegions(commentsMap, onPattern, offPattern, document.getLength() - 1);
		}
		return null;
	}
}
