package com.aptana.editor.ruby.internal.contentassist;

import org.junit.Test;

import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.ruby.RubySourceEditor;

public class RubyRegexpContentAssistProcessorTest extends RubyContentAssistTestCase
{

	@Override
	protected CommonContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor)
	{
		return new RubyRegexpContentAssistProcessor(editor);
	}

	@Test
	public void testDollar() throws Exception
	{
		assertCompletionCorrect("/  /", 2, "$", "/ $ /");
	}

	@Test
	public void testAsterisk() throws Exception
	{
		assertCompletionCorrect("/  /", 2, "*", "/ * /");
	}

	@Test
	public void testPlus() throws Exception
	{
		assertCompletionCorrect("/  /", 2, "+", "/ + /");
	}

	@Test
	public void testQuestionMark() throws Exception
	{
		assertCompletionCorrect("/  /", 2, "?", "/ ? /");
	}

	@Test
	public void testPipe() throws Exception
	{
		assertCompletionCorrect("/  /", 2, "|", "/ | /");
	}

	@Test
	public void testLineStart() throws Exception
	{
		assertCompletionCorrect("/  /", 2, "^", "/ ^ /");
	}

	@Test
	public void testSlashCharacterClasses() throws Exception
	{
		String[] characters = new String[] { "\\A", "\\B", "\\D", "\\S", "\\W", "\\Z", "\\b", "\\d", "\\s", "\\w",
				"\\z" };

		for (String c : characters)
		{
			assertCompletionCorrect("/  /", 2, c, "/ " + c + " /");
		}
	}

	@Test
	public void testBracketCharacterClasses() throws Exception
	{
		String[] characters = new String[] { "[:alnum:]", "[:alpha:]", "[:blank:]", "[:cntrl:]", "[:digit:]",
				"[:graph:]", "[:lower:]", "[:print:]", "[:punct:]", "[:space:]", "[:upper:]", "[:xdigit:]" };

		for (String c : characters)
		{
			assertCompletionCorrect("/  /", 2, c, "/ " + c + " /");
		}
	}

}
