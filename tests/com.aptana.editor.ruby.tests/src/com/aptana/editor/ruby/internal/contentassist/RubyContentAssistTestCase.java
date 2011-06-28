package com.aptana.editor.ruby.internal.contentassist;

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

	protected ITextViewer createTextViewer(IDocument fDocument)
	{
		ITextViewer viewer = new TextViewer(new Shell(), SWT.NONE);
		viewer.setDocument(fDocument);
		return viewer;
	}

	protected void assertCompletionCorrect(String document, int offset, String proposalToSelect, String postCompletion)
			throws Exception
	{
		assertCompletionCorrect(document, offset, '\t', -1, proposalToSelect, postCompletion, null);
	}

	protected void assertCompletionCorrect(String document, int offset, char trigger, int proposalCount,
			String proposalToSelect, String postCompletion, Point point)
	{
		fDocument = new Document(document);
		ITextViewer viewer = createTextViewer(fDocument);

		fProcessor = createContentAssistProcessor(null);

		ICompletionProposal[] proposals = fProcessor.computeCompletionProposals(viewer, offset, trigger, false);
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
				ext2.apply(viewer, trigger, SWT.NONE, offset);
			}
			else
			{
				closeProposal.apply(fDocument);
			}
		}
		assertEquals(postCompletion, fDocument.get());

		if (point != null)
		{
			Point p = viewer.getSelectedRange();
			assertEquals(point.x, p.x);
			assertEquals(point.y, p.y);
		}
	}

	protected abstract CommonContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor);

	protected IDocument getDocument()
	{
		return fDocument;
	}
}
