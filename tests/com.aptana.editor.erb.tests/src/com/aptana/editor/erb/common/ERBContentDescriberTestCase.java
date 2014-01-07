/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.common;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.core.runtime.content.ITextContentDescriber;

public abstract class ERBContentDescriberTestCase
{

	private ERBContentDescriber describer;

//	@Override
	@Before
	public void setUp() throws Exception
	{
//		super.setUp();
		describer = createDescriber();
	}

	protected abstract ERBContentDescriber createDescriber();

//	@Override
	@After
	public void tearDown() throws Exception
	{
		describer = null;
//		super.tearDown();
	}

	@Test
	public void testDescribeEmptyContent() throws Exception
	{
		Reader contents = new StringReader("");
		assertEquals(ITextContentDescriber.INDETERMINATE, describer.describe(contents, null));
	}

	@Test
	public void testDescribeWithPrefix() throws Exception
	{
		Reader contents = new StringReader(describer.getPrefix());
		assertEquals(ITextContentDescriber.VALID, describer.describe(contents, null));
	}

	@Test
	public void testDescribeWithGarbage() throws Exception
	{
		Reader contents = new StringReader("gjfhjdhj");
		assertEquals(ITextContentDescriber.INDETERMINATE, describer.describe(contents, null));
	}

	// TODO Call describe(InputStream, IConentDescription)

	@Test
	public void testDescribeInputStreamWithEmptyContent() throws Exception
	{
		InputStream stream = new ByteArrayInputStream("".getBytes());
		assertEquals(ITextContentDescriber.INDETERMINATE, describer.describe(stream, null));
	}

	@Test
	public void testDescribeInputStreamWithPrefix() throws Exception
	{
		InputStream stream = new ByteArrayInputStream(describer.getPrefix().getBytes());
		assertEquals(ITextContentDescriber.VALID, describer.describe(stream, null));
	}

	@Test
	public void testDescribeInputStreamWithGarbage() throws Exception
	{
		InputStream stream = new ByteArrayInputStream("gjfhjdhj".getBytes());
		assertEquals(ITextContentDescriber.INDETERMINATE, describer.describe(stream, null));
	}
}
