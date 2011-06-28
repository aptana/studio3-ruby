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
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;

public class RubyDoubleQuotedStringContentAssistProcessor extends CommonContentAssistProcessor
{

	private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

	/**
	 * Proposal text to description.
	 */
	private static final Map<String, String> PROPOSALS = new TreeMap<String, String>();
	static
	{
		PROPOSALS.put("\\", "\\nnn Octal <i>nnn</i>"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\C-", "Control-<i>x</i>"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\M-", "Meta-<i>x</i>"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\M-\\C-", "Meta-control-<i>x</i>"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\a", "Bell/alert (0x07)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\b", "Backspace (0x08)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\c", "Control-<i>x</i>"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\e", "Escape (0x1b)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\f", "Formfeed (0x0c)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\n", "Newline (0x0a)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\r", "Return (0x0d)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\s", "Space (0x20)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\t", "Tab (0x09)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\v", "Vertical tab (0x0b)"); //$NON-NLS-1$ //$NON-NLS-2$
		PROPOSALS.put("\\x", "\\xnn: Hex <i>nn</i>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public RubyDoubleQuotedStringContentAssistProcessor(AbstractThemeableEditor editor)
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
					// FIXME Don't auto-insert common prefix!
					// FIXME add image?
					result.add(createProposal(entry.getKey(), prefix, offset - prefix.length(), entry.getValue()));
				}
			}
			// TODO Add "#{}" templated proposal!
			// TODO Sort by display string!
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
		CompletionProposal p = new CompletionProposal(proposal, offset, prefix.length(), proposal.length(), null,
				proposal, null, description);
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
				return Character.isLetter(c);
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
