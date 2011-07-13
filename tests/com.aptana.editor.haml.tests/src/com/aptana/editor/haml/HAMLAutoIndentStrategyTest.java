/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

import com.aptana.editor.common.text.RubyRegexpAutoIndentStrategy;

@SuppressWarnings("nls")
public class HAMLAutoIndentStrategyTest extends TestCase
{

	public void testTagWithoutTrailingText() throws BadLocationException
	{

		addNewLineAtOffset("%div#collection\n  %div.item%div.description What a cool item!", 27,
				"%div#collection\n  %div.item\n  \t%div.description What a cool item!");
	}

	public void testTagWithTrailingText() throws BadLocationException
	{
		addNewLineAtOffset("    %div.description What a cool item!", 38, "    %div.description What a cool item!\n    ");
	}

	public void testSelfClosingTag() throws BadLocationException
	{
		addNewLineAtOffset("    %meta{'http-equiv' => 'Content-Type', :content => 'text/html'}/", 67,
				"    %meta{'http-equiv' => 'Content-Type', :content => 'text/html'}/\n    ");
	}

	public void testBRTag() throws BadLocationException
	{
		addNewLineAtOffset("    %br    ", 11, "    %br    \n    ");
	}

	public void testTagWithBlocks() throws BadLocationException
	{
		addNewLineAtOffset("    %div{'http-equiv' => 'Content-Type', :content => 'text/html'}", 65,
				"    %div{'http-equiv' => 'Content-Type', :content => 'text/html'}\n    \t");
	}

	// Adds a newline at given offset, and compares the result with the expected result
	protected void addNewLineAtOffset(String original, int offset, String expected) throws BadLocationException
	{
		RubyRegexpAutoIndentStrategy strategy = new HAMLAutoIndentStrategy("", null, null, null);
		IDocument document = new Document(original);

		// After end of block comment, don't add a star
		DocumentCommand command = createNewlineCommand(offset);
		strategy.customizeDocumentCommand(document, command);
		assertTrue(command.doit);

		if (command.doit)
		{
			document.replace(command.offset, command.length, command.text);
		}
		assertEquals(expected, document.get());

	}

	protected DocumentCommand createNewlineCommand(int offset)
	{
		return createTextCommand(offset, "\n"); //$NON-NLS-1$
	}

	protected DocumentCommand createTextCommand(int offset, String text)
	{
		DocumentCommand command = new DocumentCommand()
		{
		};
		command.text = text;
		command.offset = offset;
		command.caretOffset = offset;
		command.doit = true;
		return command;
	}

}
