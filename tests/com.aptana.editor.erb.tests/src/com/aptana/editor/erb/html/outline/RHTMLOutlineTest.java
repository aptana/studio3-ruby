/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.html.outline;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;

import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.html.HTMLPlugin;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.ast.IParseRootNode;

public class RHTMLOutlineTest extends TestCase
{

	private RHTMLOutlineContentProvider fContentProvider;
	private RHTMLOutlineLabelProvider fLabelProvider;
	private Document fDocument;

	@Override
	protected void setUp() throws Exception
	{
		fContentProvider = new RHTMLOutlineContentProvider();
	}

	@Override
	protected void tearDown() throws Exception
	{
		fDocument = null;
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
	}

	public void testBasic() throws Exception
	{
		String source = "<% content_for :stylesheets do %><style type=\"text/css\"></style><% end %>";
		fDocument = new Document(source);
		fLabelProvider = new RHTMLOutlineLabelProvider(fDocument);
		IParseRootNode root = ParserPoolFactory.parse(IERBConstants.CONTENT_TYPE_HTML_ERB, source).getRootNode();

		Object[] elements = fContentProvider.getElements(root);
		assertEquals(3, elements.length);
		assertEquals("<% content_for :style... %>", fLabelProvider.getText(elements[0]));
		assertEquals(ERBEditorPlugin.getImage("icons/embedded_code_fragment.png"), fLabelProvider.getImage(elements[0]));
		assertEquals("style", fLabelProvider.getText(elements[1]));
		assertEquals(HTMLPlugin.getImage("icons/element.png"), fLabelProvider.getImage(elements[1]));
		assertEquals("<% end %>", fLabelProvider.getText(elements[2]));
		assertEquals(ERBEditorPlugin.getImage("icons/embedded_code_fragment.png"), fLabelProvider.getImage(elements[2]));
	}
}
