package com.aptana.editor.ruby;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import com.aptana.editor.ruby.internal.contentassist.RubyCommentContentAssistProcessor;
import com.aptana.editor.ruby.internal.contentassist.RubyContentAssistProcessor;
import com.aptana.editor.ruby.internal.contentassist.RubyDoubleQuotedStringContentAssistProcessor;
import com.aptana.editor.ruby.internal.contentassist.RubyRegexpContentAssistProcessor;

public class RubySourceConfigurationTest
{
	private RubySourceConfiguration fConfig;

	@Before
	public void setUp() throws Exception
	{
//		super.setUp();
		fConfig = RubySourceConfiguration.getDefault();
	}

	@After
	public void tearDown() throws Exception
	{
		fConfig = null;
//		super.tearDown();
	}

	@Test
	public void testCommandCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null, RubySourceConfiguration.COMMAND);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyDoubleQuotedStringContentAssistProcessor);
	}

	@Test
	public void testDoubleQuotedStringCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null,
				RubySourceConfiguration.STRING_DOUBLE);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyDoubleQuotedStringContentAssistProcessor);
	}

	@Test
	public void testCodeCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null, RubySourceConfiguration.DEFAULT);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyContentAssistProcessor);
	}

	@Test
	public void testDefaultCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null, IDocument.DEFAULT_CONTENT_TYPE);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyContentAssistProcessor);
	}

	@Test
	public void testLineCommentCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null,
				RubySourceConfiguration.SINGLE_LINE_COMMENT);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyCommentContentAssistProcessor);
	}

	@Test
	public void testBlockCommentCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null,
				RubySourceConfiguration.MULTI_LINE_COMMENT);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyCommentContentAssistProcessor);
	}

	@Test
	public void testSingleQuotedStringCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null,
				RubySourceConfiguration.STRING_SINGLE);
		assertNull(processor);
	}

	@Test
	public void testRegexpCAProcessor()
	{
		IContentAssistProcessor processor = fConfig.getContentAssistProcessor(null,
				RubySourceConfiguration.REGULAR_EXPRESSION);
		assertNotNull(processor);
		assertTrue(processor instanceof RubyRegexpContentAssistProcessor);
	}

}
