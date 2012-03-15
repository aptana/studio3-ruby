/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.outline;

import junit.framework.TestCase;

import com.aptana.parsing.ParseState;
import com.aptana.ruby.core.RubyParser;

public class RubyOutlineTest extends TestCase
{

	private RubyOutlineContentProvider fContentProvider;
	private RubyOutlineLabelProvider fLabelProvider;

	private RubyParser fParser;

	@Override
	protected void setUp() throws Exception
	{
		fContentProvider = new RubyOutlineContentProvider();
		fLabelProvider = new RubyOutlineLabelProvider();
		fParser = new RubyParser();
	}

	@Override
	protected void tearDown() throws Exception
	{
		if (fContentProvider != null)
		{
			fContentProvider.dispose();
			fContentProvider = null;
		}
		if (fLabelProvider != null)
		{
			fLabelProvider.dispose();
			fLabelProvider = null;
		}
		fParser = null;
	}

	public void testBasic() throws Exception
	{
		// TODO Add more types and ensure we have the right order: imports, class vars, globals, etc.
		String source = "class Test\n\tdef initialize(files)\n\t\t@files = files\n\tend\nend";
		ParseState parseState = new ParseState();
		parseState.setEditState(source, 0);
		fParser.parse(parseState);

		Object[] elements = fContentProvider.getElements(parseState.getParseResult());
		assertEquals(1, elements.length); // class Test
		assertEquals("Test", fLabelProvider.getText(elements[0]));
		assertEquals(RubyOutlineLabelProvider.CLASS, fLabelProvider.getImage(elements[0]));

		Object[] level1 = fContentProvider.getChildren(elements[0]); // initialize(files) and @files
		assertEquals(2, level1.length);		
		assertEquals("@files", fLabelProvider.getText(level1[0]));
		assertEquals(RubyOutlineLabelProvider.INSTANCE_VAR, fLabelProvider.getImage(level1[0]));
		assertEquals("initialize(files)", fLabelProvider.getText(level1[1]));
		assertEquals(RubyOutlineLabelProvider.METHOD_CONSTRUCTOR, fLabelProvider.getImage(level1[1]));
		
		Object[] level2 = fContentProvider.getChildren(level1[1]); // files
		assertEquals(1, level2.length);
		assertEquals("files", fLabelProvider.getText(level2[0]));
		assertEquals(RubyOutlineLabelProvider.LOCAL_VAR, fLabelProvider.getImage(level2[0]));

		level2 = fContentProvider.getChildren(level1[0]);
		assertEquals(0, level2.length);
	}
}
