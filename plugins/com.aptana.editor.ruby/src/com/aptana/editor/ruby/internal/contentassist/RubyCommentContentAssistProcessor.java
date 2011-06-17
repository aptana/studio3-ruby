/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.internal.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.common.contentassist.CommonCompletionProposal;

@SuppressWarnings("nls")
public class RubyCommentContentAssistProcessor extends CommonContentAssistProcessor
{

	private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

	/**
	 * Proposal text to description.
	 */
	private static final Map<String, String> PROPOSALS = new HashMap<String, String>();
	static
	{
		// YARD tags
		PROPOSALS.put("@abstract",
				"Marks a class/module/method as abstract with optional implementor information.<br />"
						+ "<pre>@abstract Subclass and override {#run} to implement a custom Threadable class.</pre>");
		PROPOSALS
				.put("@api",
						"Declares the API that the object belongs to. Does not display in output, but useful for performing queries (yardoc --query). Any text is allowable in this tag, and there are no predefined values(*).<br /><pre>@api freeform text\n(*) Note that the special name @api private does display a notice in documentation if it is listed, letting users know that the method is not to be used.</pre>");
		PROPOSALS
				.put("@attr",
						"Declares an attribute from the docstring of a class. Meant to be used on Struct classes only (classes that inherit Struct).<br /><pre>@attr [Types] attribute_name a full description of the attribute</pre>");
		PROPOSALS
				.put("@attr_reader",
						"Declares a readonly attribute from the docstring of a class. Meant to be used on Struct classes only (classes that inherit Struct). See @attr.<br /><pre>@attr_reader [Types] name description of a readonly attribute</pre>");
		PROPOSALS
				.put("@attr_writer",
						"Declares a writeonly attribute from the docstring of class. Meant to be used on Struct classes only (classes that inherit Struct). See @attr.<br /><pre>@attr_writer [Types] name description of writeonly attribute</pre>");
		PROPOSALS
				.put("@attribute",
						"Recognizes a DSL class method as an attribute with the given name. Also accepts the r, w, or rw flag to signify that the attribute is readonly, writeonly, or readwrite (default). Only used with DSL methods.<br /><pre>@attribute [rw|r|w] NAME</pre>");
		PROPOSALS.put("@author", "List the author(s) of a class/method<br /><pre>@author Full Name</pre>");
		PROPOSALS
				.put("@deprecated",
						"Marks a method/class as deprecated with an optional reason.<br /><pre>@deprecated Describe the reason or provide alt. references here</pre>");
		PROPOSALS
				.put("@example",
						"Show an example snippet of code for an object. The first line is an optional title.<br /><pre>@example Reverse a string\n\"mystring\".reverse #=> \"gnirtsym\"</pre>");
		PROPOSALS
				.put("@macro",
						"Registers or expands a new macro. See the Macros section for more details. Note that the name parameter is never optional.<br /><pre>@macro [new|attached] macro_name\nThe macro contents to expand</pre>");
		PROPOSALS
				.put("@method",
						"Recognizes a DSL class method as a method with the given name and optional signature. Only used with DSL methods.<br /><pre>@method method_signature(opts = {}, &block)</pre>");
		PROPOSALS
				.put("@note",
						"Creates an emphasized note for the users to read about the object.<br /><pre>@note This method should only be used in outer space.</pre>");
		PROPOSALS
				.put("@option",
						"Describe an options hash in a method. The tag takes the name of the options parameter first, followed by optional types, the option key name, an optional default value for the key and a description of the option.<br />"
								+ "<pre># @param [Hash] opts the options to create a message with.\n"
								+ "# @option opts [String] :subject The subject\n"
								+ "# @option opts [String] :from ('nobody') From address\n"
								+ "# @option opts [String] :to Recipient email\n"
								+ "# @option opts [String] :body ('') The email's body \n"
								+ "def send_email(opts = {})\n" + "end\n</pre>");
		PROPOSALS
				.put("@overload",
						"Describe that your method can be used in various contexts with various parameters or return types. The first line should declare the new method signature, and the following indented tag data will be a new documentation string with its own tags adding metadata for such an overload.<br />"
								+ "<pre># @overload set(key, value)\n"
								+ "#   Sets a value on key\n"
								+ "#   @param [Symbol] key describe key param\n"
								+ "#   @param [Object] value describe value param\n"
								+ "# @overload set(value)\n"
								+ "#   Sets a value on the default key `:foo`\n"
								+ "#   @param [Object] value describe value param\n"
								+ "def set(*args)\n"
								+ "end\n</pre>");
		PROPOSALS.put("@param", "Defines method parameters<br />"
				+ "<pre>@param [optional, types, ...] argname description</pre>");
		PROPOSALS
				.put("@private",
						"Defines an object as private. This exists for classes, modules and constants that do not obey Ruby's visibility rules. For instance, an inner class might be considered \"private\", though Ruby would make no such distinction. By declaring the @private tag, the class can be hidden from documentation by using the --no-private command-line switch to yardoc (see README).<br />"
								+ "<pre>@private</pre>");
		PROPOSALS.put("@raise", "Describes an Exception that a method may throw<br />"
				+ "<pre>@raise [ExceptionClass] description</pre>");
		PROPOSALS.put("@return", "Describes return value of method<br />"
				+ "<pre>@return [optional, types, ...] description</pre>");
		PROPOSALS
				.put("@scope",
						"Sets the scope of a DSL method. Only applicable to DSL method calls. Acceptable values are 'class' or 'instance'<br />"
								+ "<pre>@scope class|instance</pre>");
		PROPOSALS
				.put("@see",
						"\"See Also\" references for an object. Accepts URLs or other code objects with an optional description at the end.<br />"
								+ "<pre>@see http://example.com Description of URL\n"
								+ "@see SomeOtherClass#method</pre>");
		PROPOSALS.put("@since", "Lists the version the feature/object was first added<br />"
				+ "<pre>@since 1.2.4</pre>");
		PROPOSALS.put("@todo", "Marks a TODO note in the object being documented<br />"
				+ "<pre>@todo Add support for Jabberwocky service\n"
				+ "There is an open source Jabberwocky library available \n"
				+ "at http://somesite.com that can be integrated easily<br />" + "into the project.</pre>");
		PROPOSALS.put("@version", "Lists the version of a class, module or method<br />" + "<pre>@version 1.0</pre>");
		PROPOSALS
				.put("@visibility",
						"Sets the visibility of a DSL method. Only applicable to DSL method calls. Acceptable values are public, protected, or private.\n"
								+ "<pre>@visibility public|protected|private</pre>");
		PROPOSALS.put("@yield", "Describes the block. Use types to list the parameter names the block yields.\n"
				+ "<pre># for block {|a, b, c| ... }\n" + "@yield [a, b, c] Description of block</pre>");
		PROPOSALS.put("@yieldparam", "Defines parameters yielded by a block<br />"
				+ "<pre>@yieldparam [optional, types, ...] argname description</pre>");
		PROPOSALS.put("@yieldreturn", "Defines return type of a block<br />"
				+ "<pre>@yieldreturn [optional, types, ...] description</pre>");

		// RDoc modifiers
		PROPOSALS.put(":yields:", "Override list of block parameters");
		PROPOSALS
				.put(":nodoc:",
						"don't include this element in the documentation. For classes and modules, methods, aliases, and attributes directly within the affected class will also be omitted. By default, though, modules and classes within that class of module will be documented. This is turned off by adding the all modifier.\n"
								+ "<pre>module SM  #:nodoc:\n"
								+ "   class Input\n"
								+ "   end\n"
								+ "end\n"
								+ "module Markup #:nodoc: all\n"
								+ "   class Output\n"
								+ "   end\n"
								+ "end</pre>\n"
								+ "In the above code, only class SM::Input will be documented.");
		PROPOSALS
				.put(":doc:",
						"force a method to be documented even if it wouldn't otherwise be. Useful is, for example, you want to include documentation of a particular private method.");
		PROPOSALS
				.put(":notnew:",
						"only applicable to the initialize instance method. Normally RDoc assumes that the documentation and parameters for initialize are actually for the ::new method, and so fakes out a ::new for the class. THe :notnew: modifier stops this. Remember that initialize is protected, so you won't see the documentation unless you use the -a command line option.");
		PROPOSALS
				.put(":include:",
						"include the contents of the named file at this point. The file will be searched for in the directories listed by the --include option, or in the current directory by default. The contents of the file will be shifted to have the same indentation as the ':' at the start of the :include: directive.");
		PROPOSALS
				.put(":title:",
						"Sets the title for the document. Equivalent to the --title command line parameter. (The command line parameter overrides any :title: directive in the source).");
		PROPOSALS.put(":main:", "Equivalent to the --main command line parameter.");
		// TODO Add Author::, Copyright::, License:: ?
	}

	public RubyCommentContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	@Override
	protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int offset, char activationChar,
			boolean autoActivated)
	{
		// TODO Suggest types/methods after "see" like "see User#initialize"
		// TODO Handle type suggestions after some tags, i.e. "@param [String, #read]"
		// TODO Add support for +arg+ for arguments to methods...
		try
		{
			String prefix = getPrefix(viewer, offset);
			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			for (Map.Entry<String, String> entry : PROPOSALS.entrySet())
			{
				if (entry.getKey().startsWith(prefix))
				{
					result.add(createProposal(entry.getKey(), prefix, offset - prefix.length(), entry.getValue()));
				}
			}

			ICompletionProposal[] proposals = result.toArray(new ICompletionProposal[result.size()]);
			sortProposals(proposals);
			return proposals;
		}
		catch (BadLocationException x)
		{
			// ignore and return no proposals
			return NO_PROPOSALS;
		}
	}

	private ICompletionProposal createProposal(String proposal, String prefix, int offset, String description)
	{
		CommonCompletionProposal p = new CommonCompletionProposal(proposal, offset, prefix.length(), proposal.length(),
				null, proposal, null, description);
		return p;
	}

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException
	{
		IDocument doc = viewer.getDocument();
		if (doc == null || offset > doc.getLength())
		{
			return null;
		}

		int length = 0;
		while (--offset >= 0 && isPrefixChar(doc.getChar(offset)))
		{
			length++;
		}

		return doc.get(offset + 1, length);
	}

	private boolean isPrefixChar(char c)
	{
		switch (c)
		{
			case '@':
			case ':':
				return true;

			default:
				return Character.isLetter(c);
		}
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		return NO_CONTEXTS;
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return new char[] { '@', ':' };
	}

}
