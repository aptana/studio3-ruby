package com.aptana.editor.ruby.internal.contentassist;

import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.ruby.RubySourceEditor;

public class RubyCommentContentAssistProcessorTest extends RubyContentAssistTestCase
{
	private static final String[] TAGS = new String[] { "@abstract", "@api", "@attr", "@attr_reader", "@attr_writer",
			"@attribute", "@author", "@deprecated", "@example", "@macro", "@method", "@note", "@option", "@overload",
			"@param", "@private", "@raise", "@return", "@scope", "@see", "@since", "@todo", "@version", "@visibility",
			"@yield", "@yieldparam", "@yieldreturn" };

	private static final String[] RDOC_TOKENS = new String[] { ":yields:", ":nodoc:", ":title:", ":doc:", ":notnew:",
			":include:", ":main:" };

	@Override
	protected CommonContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor)
	{
		return new RubyCommentContentAssistProcessor(editor);
	}

	public void testYARDTags() throws Exception
	{
		for (String tag : TAGS)
		{
			assertCompletionCorrect("# @", 3, tag, "# " + tag);
		}
	}

	public void testRDOCTokens() throws Exception
	{
		for (String tag : RDOC_TOKENS)
		{
			assertCompletionCorrect("# :", 3, tag, "# " + tag);
		}
	}

}
