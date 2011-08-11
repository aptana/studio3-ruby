package com.aptana.editor.ruby.internal.contentassist;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.ruby.RubySourceEditor;

public abstract class RubyContentAssistTestCase extends TestCase
{

	private CommonContentAssistProcessor fProcessor;
	private Document fDocument;
	private ITextViewer fViewer;

	@Override
	protected void setUp() throws Exception
	{
		// FIXME Need to set up a real project with core stubs and all!
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{

		}
		finally
		{
			fProcessor = null;
			fViewer = null;
			fDocument = null;
			super.tearDown();
		}
	}

	protected ICompletionProposal findProposal(String string, ICompletionProposal[] proposals)
	{
		for (ICompletionProposal proposal : proposals)
		{
			if (proposal.getDisplayString().equals(string))
			{
				return proposal;
			}
		}
		return null;
	}

	protected void assertCompletionCorrect(String document, int offset, String proposalToSelect, String postCompletion)
			throws Exception
	{
		assertCompletionCorrect(document, offset, '\t', -1, proposalToSelect, postCompletion, null);
	}

	protected void assertCompletionCorrect(String document, int offset, char trigger, int proposalCount,
			String proposalToSelect, String postCompletion, Point point)
	{
		ICompletionProposal[] proposals = computeProposals(document, offset, trigger);
		if (proposalCount >= 0)
		{
			assertEquals(proposalCount, proposals.length);
		}

		if (proposalToSelect != null)
		{
			ICompletionProposal closeProposal = findProposal(proposalToSelect, proposals);
			assertNotNull("Unable to find proposal you wanted to select: " + proposalToSelect, closeProposal);
			if (closeProposal instanceof ICompletionProposalExtension2)
			{
				ICompletionProposalExtension2 ext2 = (ICompletionProposalExtension2) closeProposal;
				assertTrue("Selected proposal doesn't validate against document",
						ext2.validate(fDocument, offset, null));
				ext2.apply(getViewer(), trigger, SWT.NONE, offset);
			}
			else
			{
				closeProposal.apply(fDocument);
			}
		}
		assertEquals(postCompletion, fDocument.get());

		if (point != null)
		{
			Point p = getViewer().getSelectedRange();
			assertEquals(point.x, p.x);
			assertEquals(point.y, p.y);
		}
	}

	protected synchronized ITextViewer getViewer()
	{
		if (fViewer == null)
		{
			fViewer = new TextViewer(new Shell(), SWT.NONE);
		}
		if (!getDocument().equals(fViewer.getDocument()))
		{
			fViewer.setDocument(getDocument());
		}
		return fViewer;
	}

	protected ICompletionProposal[] computeProposals(String document, int offset)
	{
		return computeProposals(document, offset, '\t');
	}

	protected ICompletionProposal[] computeProposals(String document, int offset, char trigger)
	{
		fDocument = new Document(document);
		fProcessor = createContentAssistProcessor(null);

		return fProcessor.computeCompletionProposals(getViewer(), offset, trigger, false);
	}

	protected abstract CommonContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor);

	protected IDocument getDocument()
	{
		return fDocument;
	}

	/**
	 * Tests if the proposals list contains a set of proposals using specific display names.
	 * 
	 * @param proposals
	 * @param displayNames
	 */
	protected void assertDoesntContain(ICompletionProposal[] proposals, String... displayNames)
	{
		Set<String> uniqueDisplayNames = new HashSet<String>(Arrays.asList(displayNames));
		Set<String> matches = new HashSet<String>(uniqueDisplayNames.size());
		for (ICompletionProposal proposal : proposals)
		{
			if (uniqueDisplayNames.contains(proposal.getDisplayString()))
			{
				matches.add(proposal.getDisplayString());
			}
		}

		if (!matches.isEmpty())
		{
			fail(MessageFormat.format("Proposals contain an entry for disallowed display string(s): {0}", matches));
		}
	}

	protected void assertContains(ICompletionProposal[] proposals, String... displayNames)
	{
		Set<String> uniqueDisplayNames = new HashSet<String>(Arrays.asList(displayNames));
		for (ICompletionProposal proposal : proposals)
		{
			if (uniqueDisplayNames.contains(proposal.getDisplayString()))
			{
				uniqueDisplayNames.remove(proposal.getDisplayString());
			}
		}

		if (!uniqueDisplayNames.isEmpty())
		{
			fail(MessageFormat.format("Proposals do not contain an entry for expected display string(s): {0}",
					uniqueDisplayNames));
		}
	}
}
