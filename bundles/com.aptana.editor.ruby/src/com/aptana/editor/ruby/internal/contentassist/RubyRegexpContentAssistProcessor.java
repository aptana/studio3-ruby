/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.internal.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.common.contentassist.CommonCompletionProposal;

public class RubyRegexpContentAssistProcessor extends CommonContentAssistProcessor
{

	private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

	/**
	 * Proposal text to description.
	 */
	private static final Map<String, String> PROPOSALS = new TreeMap<String, String>();
	static
	{
		// FIXME Keep ordering of proposals same as here
		PROPOSALS.put("$", "End of line"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("()", "Grouping"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("*", "Zero or more repetitions of preceding"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("+", "One or more repetitions of preceding"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("?", "At most one repetition of preceding"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:alnum:]", "Alphanumeric character class"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:alpha:]", "Uppercase or lowercase letter"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:blank:]", "Blank and tab"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:cntrl:]", "Control characters (at least 0x00-0x1f,0x7f)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:digit:]", "Digit"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:graph:]", "Printable character excluding space"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:lower:]", "Lowercase letter"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:print:]", "Any printable character (including space)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:punct:]", "Printable character excluding space and alp"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:space:]", "Whitespace (same as \\s)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:upper:]", "Uppercase letter"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("[:xdigit:]", "Hex digit (0-9, a-f, A-F)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\A", "Beginning of string"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\B", "Non-word boundary"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\D", "Non-digit character"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\S", "Non-space character"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\W", "Neither letter or digit"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\Z", "End of string (except \\n)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put(
				"\\b", "Word boundary (outside range specification); Backspace (0x08) (if in a range specification)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\d", "Digit character; same as [0-9]"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\s", "Space character; same as [ \\t\\n\\r\\f]"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\w", "Letter or digit; same as [0-9A-Za-z]"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\z", "End of string"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("^", "Start of line"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("{m, n}", "At least <i>m</i> and at most <i>n</i> repetitions of the preceding"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("|", "Either preceding or next expression may match"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public RubyRegexpContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	@Override
	protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int offset, char activationChar,
			boolean autoActivated)
	{
		try
		{
			String prefix = getPrefix(viewer, offset);
			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			for (Map.Entry<String, String> entry : PROPOSALS.entrySet())
			{
				if (entry.getKey().startsWith(prefix))
				{
					result.add(createProposal(entry.getKey(), prefix, offset - prefix.length(), entry.getValue()));
				}
			}
			return result.toArray(new ICompletionProposal[result.size()]);
		}
		catch (BadLocationException x)
		{
			// ignore and return no proposals
			return NO_PROPOSALS;
		}
	}

	private ICompletionProposal createProposal(String proposal, String prefix, int offset, String description)
	{
		CommonCompletionProposal p = new CommonCompletionProposal(proposal, offset, prefix.length(), proposal.length(),
				null, proposal, null, description);
		return p;
	}

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException
	{
		IDocument doc = viewer.getDocument();
		if (doc == null || offset > doc.getLength())
		{
			return null;
		}

		int length = 0;
		while (--offset >= 0 && isPrefixChar(doc.getChar(offset)))
		{
			length++;
		}

		return doc.get(offset + 1, length);
	}

	private boolean isPrefixChar(char c)
	{
		switch (c)
		{
			case '\\':
				return true;

			default:
				return false;
		}
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		return NO_CONTEXTS;
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return new char[] { '\\' };
	}
}
