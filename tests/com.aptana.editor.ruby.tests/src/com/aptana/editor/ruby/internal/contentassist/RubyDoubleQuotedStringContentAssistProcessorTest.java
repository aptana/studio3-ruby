package com.aptana.editor.ruby.internal.contentassist;

import org.junit.Test;

import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.ruby.RubySourceEditor;

public class RubyDoubleQuotedStringContentAssistProcessorTest extends RubyContentAssistTestCase
{

	@Override
	protected CommonContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor)
	{
		return new RubyDoubleQuotedStringContentAssistProcessor(editor);
	}

	@Test
	public void testSlashCharacterClasses() throws Exception
	{
		String[] characters = new String[] { "\\", "\\C-", "\\M-", "\\M-\\C-", "\\a", "\\b", "\\c", "\\e", "\\f",
				"\\n", "\\r", "\\s", "\\t", "\\v", "\\x" };

		for (String c : characters)
		{
			assertCompletionCorrect("\"  \"", 2, c, "\" " + c + " \"");
			assertCompletionCorrect("`  `", 2, c, "` " + c + " `");
		}
	}
}
