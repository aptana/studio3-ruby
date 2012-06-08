/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html;

import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.html.parsing.ERBScript;
import com.aptana.editor.erb.html.parsing.RHTMLParser;
import com.aptana.editor.html.IHTMLConstants;
import com.aptana.editor.html.parsing.HTMLParseState;
import com.aptana.editor.js.parsing.ast.JSStringNode;
import com.aptana.editor.js.parsing.ast.JSVarNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.ruby.core.IRubyConstants;

public class RHTMLParserTest extends TestCase
{
	private RHTMLParser fParser;
	private HTMLParseState fParseState;

	@Override
	protected void setUp() throws Exception
	{
		fParser = new RHTMLParser();
	}

	@Override
	protected void tearDown() throws Exception
	{
		fParser = null;
	}

	@SuppressWarnings("nls")
	public void testTopLevelERB() throws Exception
	{
		String source = "<% content_for :stylesheets do %>\n" + "<%= stylesheet_link_tag 'rails' %>\n"
				+ "<style></style>\n" + "<%= javascript_include_tag 'slidedeck/slidedeck.jquery.js' %>\n"
				+ "<script></script>\n" + "<% end %>";
		fParseState = new HTMLParseState(source);

		IParseNode result = parse();
		IParseNode[] children = result.getChildren();
		assertEquals(11, children.length);
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[0].getLanguage());
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[2].getLanguage());
		assertEquals(3, children[4].getNodeType()); // HTMLSpecialNode
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[6].getLanguage());
		assertEquals(3, children[8].getNodeType()); // HTMLSpecialNode
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[10].getLanguage());
	}

	@SuppressWarnings("nls")
	public void testNestedERB() throws Exception
	{
		String source = "<p>Welcome to <em><%= ENV['SERVER_NAME'] %></em>. If you see a server name, <%= 'e' + 'Ruby' %> is probably working.</p>";
		fParseState = new HTMLParseState(source);

		IParseNode result = parse();
		IParseNode[] children = result.getChildren(); // <p></p>
		assertEquals(1, children.length);
		assertEquals(2, children[0].getNodeType()); // HTMLElementNode
		children = children[0].getChildren(); // <em></em><%= %>
		assertEquals(5, children.length);
		assertEquals(2, children[1].getNodeType()); // HTMLElementNode
		assertEquals(IHTMLConstants.CONTENT_TYPE_HTML, children[1].getLanguage());
		children = children[1].getChildren(); // <%= %>
		assertEquals(1, children.length);
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[0].getLanguage());
	}

	@SuppressWarnings("nls")
	public void testDoubleERBBeforeTagClose() throws Exception
	{
		String source = "<table><tr></tr><% content_for :table %><% end %></table>";
		fParseState = new HTMLParseState(source);

		IParseNode result = parse();
		IParseNode[] children = result.getChildren(); // <table></table>
		assertEquals(1, children.length);
		children = children[0].getChildren(); // <tr></tr><% %><% %>
		assertEquals(3, children.length);
		assertEquals(2, children[0].getNodeType()); // HTMLElementNode
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[1].getLanguage());
		assertEquals(IRubyConstants.CONTENT_TYPE_RUBY, children[2].getLanguage());
	}

	public void testAPSTUD4397() throws Exception
	{
		String resource = "parsing/offsets.html.erb";
		InputStream input = FileLocator.openStream(Platform.getBundle(ERBEditorPlugin.PLUGIN_ID), new Path(resource),
				false);
		String source = IOUtil.read(input);

		fParseState = new HTMLParseState(source);
		IParseNode result = parse();

		// check node offsets
		IParseNode varDecl1 = result.getNodeAtOffset(54);
		assertTrue(varDecl1 instanceof JSVarNode);
		assertEquals(54, varDecl1.getStartingOffset());
		assertEquals(320, varDecl1.getEndingOffset());

		IParseNode varDecl2 = result.getNodeAtOffset(352);
		assertTrue(varDecl2 instanceof JSVarNode);
		assertEquals(352, varDecl2.getStartingOffset());
		assertEquals(1199, varDecl2.getEndingOffset());

		IParseNode string = result.getNodeAtOffset(190);
		assertTrue(string instanceof JSStringNode);
		assertEquals(1, string.getChildCount());
		IParseNode erbScript = string.getFirstChild();
		assertTrue(erbScript instanceof ERBScript);
	}

	private IParseRootNode parse() throws Exception
	{
		return fParser.parse(fParseState).getRootNode();
	}
}
