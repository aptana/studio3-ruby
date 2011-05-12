package com.aptana.editor.ruby;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;

/**
 * All the heavy lifting is actually done by the content assist implementation in the ruby ruble. This class just exists
 * to set auto-activation on '.' for now.
 * 
 * @author cwilliams
 */
public class RubyContentAssistProcessor extends CommonContentAssistProcessor
{

	public RubyContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	@Override
	public char[] getCompletionProposalAutoActivationCharacters()
	{
		// TODO Maybe also ':' for "::"?
		return new char[] { '.' };
	}

}
