/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.codeassist;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.StaticScope;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.RootNode;
import org.jrubyparser.parser.ParserResult;

import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.RubySourceParser;
import com.aptana.ruby.core.ast.ASTUtils;
import com.aptana.ruby.core.ast.ClosestSpanningNodeLocator;
import com.aptana.ruby.core.ast.INodeAcceptor;
import com.aptana.ruby.core.ast.NamespaceVisitor;
import com.aptana.ruby.core.ast.OffsetNodeLocator;
import com.aptana.ruby.core.inference.ITypeGuess;
import com.aptana.ruby.internal.core.inference.TypeInferrer;

public class CompletionContext
{

	private int offset;
	private boolean isMethodInvokation = false;
	private String correctedSource;
	private String partialPrefix;
	private String fullPrefix;
	private int replaceStart;
	private boolean isAfterDoubleSemiColon = false;
	private Node fRootNode;
	private List<CommentNode> fCommentNodes;
	private boolean inComment;
	private String src;
	// Cache of the enclosing scope (typically type node or root) for instance/class vars
	private Node enclosingScopeNode;

	public CompletionContext(String src, int offset)
	{
		this.src = src;
		if (offset < 0)
			offset = 0;
		this.offset = offset;
		replaceStart = offset + 1;
		try
		{
			run(src);
		}
		catch (RuntimeException e)
		{
			RubyCorePlugin.log(e);
		}
	}

	@SuppressWarnings("nls")
	private void run(final String src)
	{
		StringBuilder source = new StringBuilder(src);
		if (offset >= source.length())
		{
			offset = source.length() - 1;
			replaceStart = offset + 1;
		}
		// Read from offset back until we hit a: space, period
		// if we hit a period, use character before period as offset for
		// inferrer
		StringBuffer tmpPrefix = new StringBuffer();
		boolean setOffset = false;
		for (int i = offset; i >= 0; i--)
		{
			char curChar = source.charAt(i);
			if (offset == i)
			{ // check the first character
				switch (curChar)
				{
					case '@':
						if (((i - 1) >= 0) && (source.charAt(i - 1) == '@'))
						{
							source.deleteCharAt(i);
							source.deleteCharAt(i - 1);
							tmpPrefix.append("@");
							i--;
						}
						else
							source.deleteCharAt(i);
						break;
					case '.':
					case '$': // if it breaks syntax, lets fix it
					case ',':
						// TODO What if there is a valid character after this, so syntax isn't broken?
						source.deleteCharAt(i);
						break;
					case ':':
						if (i > 0)
						{
							// Check character before this for :
							char previous = source.charAt(i - 1);
							if (previous == ':')
							{
								isAfterDoubleSemiColon = true;
								source.deleteCharAt(i);
								source.deleteCharAt(i - 1);
								tmpPrefix.insert(0, "::");
								partialPrefix = "";
								i--;
								continue;
							}
						}
						break;
				}
			}
			if (curChar == '.')
			{
				isMethodInvokation = true;
				if (partialPrefix == null)
					this.partialPrefix = tmpPrefix.toString();
				if (offset - 1 == i)
				{
					offset = i;
				}
				else
				{
					offset = i - 1;
				}
				setOffset = true;
			}
			else if (curChar == ':')
			{
				if (i > 0)
				{
					// Check character before this for :
					char previous = source.charAt(i - 1);
					if (previous == ':')
					{
						isAfterDoubleSemiColon = true;
						if (partialPrefix == null)
							partialPrefix = tmpPrefix.toString();
						tmpPrefix.insert(0, ":");
						i--;
					}
				}
			}
			// FIXME This logic is very much like RubyWordDetector in the UI!
			if (Character.isWhitespace(curChar) || curChar == ',' || curChar == '(' || curChar == '[' || curChar == '{')
			{
				if (!setOffset)
				{
					offset = i + 1;
					setOffset = true;
				}
				break;
			}
			tmpPrefix.insert(0, curChar);
		}
		this.fullPrefix = tmpPrefix.toString();
		if (partialPrefix == null)
			partialPrefix = fullPrefix;
		if (partialPrefix != null)
			replaceStart -= partialPrefix.length();
		this.correctedSource = source.toString();
		// TODO For memory's sake, don't store corrected source if it's the same as original!

		ClosestSpanningNodeLocator spanningLocator = new ClosestSpanningNodeLocator();
		Node selected = spanningLocator.find(getRootNode(), this.offset, new INodeAcceptor()
		{
			public boolean accepts(Node node)
			{
				return true;
			}
		});
		if (selected == null)
		{
			if (fCommentNodes != null)
			{
				for (CommentNode comment : fCommentNodes)
				{
					if (spanningLocator.spansOffset(comment, this.offset))
					{
						inComment = true;
						break;
					}
				}
			}
		}
	}

	/**
	 * This is when we have a receiver and a period in the prefix
	 * 
	 * @return
	 */
	public boolean isExplicitMethodInvokation()
	{
		return isMethodInvokation;
	}

	/**
	 * This is when it could be a method call with an implicit self, or when it may just be a local
	 * 
	 * @return
	 */
	public boolean isMethodInvokationOrLocal()
	{
		return !isExplicitMethodInvokation()
				&& (emptyPrefix() || (getPartialPrefix().length() > 0 && Character.isLowerCase(getPartialPrefix()
						.charAt(0))));
	}

	/**
	 * The last portion of prefix is not null, not empty and starts with an uppercase letter
	 * 
	 * @return
	 */
	public boolean isConstant()
	{
		String partial = getPartialPrefix();
		return partial != null && partial.length() > 0 && Character.isUpperCase(partial.charAt(0));
	}

	public int getReplaceStart()
	{
		return replaceStart;
	}

	/**
	 * Modified source which should not fail parsing.
	 * 
	 * @return
	 */
	public String getCorrectedSource()
	{
		return correctedSource;
	}

	public boolean isBroken()
	{
		return !getCorrectedSource().equals(getSource());
	}

	public boolean hasReceiver()
	{
		return getFullPrefix().indexOf('.') > 1;
	}

	/**
	 * The original source
	 * 
	 * @return
	 */
	public String getSource()
	{
		return src;
	}

	public String getFullPrefix()
	{
		return fullPrefix;
	}

	public String getPartialPrefix()
	{
		return partialPrefix;
	}

	public int getOffset()
	{
		return offset;
	}

	public boolean emptyPrefix()
	{
		return getFullPrefix() == null || getFullPrefix().length() == 0;
	}

	public boolean prefixStartsWith(String name)
	{
		return name != null && getPartialPrefix() != null && name.startsWith(getPartialPrefix());
	}

	public boolean isGlobal()
	{
		return !emptyPrefix() && !isExplicitMethodInvokation() && getPartialPrefix().startsWith("$"); //$NON-NLS-1$
	}

	public boolean isDoubleSemiColon()
	{
		return isAfterDoubleSemiColon && !isMethodInvokation;
	}

	public boolean fullPrefixIsConstant()
	{
		String full = getFullPrefix();
		if (full == null || full.length() == 0)
		{
			return false;
		}
		if (full.endsWith("\".") || full.endsWith("'.")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return false;
		}
		return Character.isUpperCase(full.charAt(0));
	}

	/**
	 * Returns whether we're inside a type definition and not inside a method definition (used to determine if we should
	 * only show class level methods)
	 * 
	 * @return
	 */
	public boolean inTypeDefinition()
	{
		if (getRootNode() == null)
		{
			return false;
		}
		Node spanner = new ClosestSpanningNodeLocator().find(getRootNode(), getOffset(), new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node instanceof MethodDefNode || node instanceof ClassNode || node instanceof ModuleNode;
			}

		});
		return spanner instanceof ClassNode || spanner instanceof ModuleNode;
	}

	synchronized Node getRootNode()
	{
		if (fRootNode != null)
			return fRootNode;
		// TODO Use ParserPoolFactory here!
		RubySourceParser parser = new RubySourceParser(CompatVersion.BOTH);
		if (!isBroken())
		{
			try
			{
				ParserResult result = parser.parse(getSource());
				fRootNode = result.getAST();
				fCommentNodes = result.getCommentNodes();
			}
			catch (RuntimeException e)
			{
				// ignore
			}
		}
		if (fRootNode == null)
		{
			try
			{
				ParserResult result = parser.parse(getCorrectedSource());
				fRootNode = result.getAST();
				fCommentNodes = result.getCommentNodes();
			}
			catch (RuntimeException e)
			{
				// ignore
			}
		}
		return fRootNode;
	}

	public boolean inComment()
	{
		return inComment;
	}

	/**
	 * User may be trying to complete either a class or an instance variable (we only have '@')
	 * 
	 * @return
	 */
	public boolean isInstanceOrClassVariable()
	{
		return getPartialPrefix() != null && getPartialPrefix().startsWith("@") && getPartialPrefix().length() == 1; //$NON-NLS-1$
	}

	/**
	 * We know for certain user is trying to complete an instance var (we have '@[a-z]+')
	 * 
	 * @return
	 */
	public boolean isInstanceVariable()
	{
		return getPartialPrefix() != null && getPartialPrefix().startsWith("@") && !isClassVariable() //$NON-NLS-1$
				&& getPartialPrefix().length() > 1;
	}

	/**
	 * We know for certain user is trying to complete a class var (we have '@@[a-z]*')
	 * 
	 * @return
	 */
	public boolean isClassVariable()
	{
		return getPartialPrefix() != null && getPartialPrefix().startsWith("@@"); //$NON-NLS-1$
	}

	/**
	 * Grab the enclosing type's fully qualified name.
	 * 
	 * @return
	 */
	public String getEnclosingType()
	{
		Node typeNode = new ClosestSpanningNodeLocator().find(getRootNode(), getOffset(), new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node instanceof ClassNode || node instanceof ModuleNode;
			}
		});
		if (typeNode == null)
		{
			return "Object";
		}
		// Also grab the namespace at this point and prefix it here!
		String namespace = getNamespace();
		if (namespace != null && namespace.length() > 0)
		{
			return namespace + "::" + ASTUtils.getName(typeNode);
		}
		return ASTUtils.getName(typeNode);
	}

	public String getNamespace()
	{
		return new NamespaceVisitor().getNamespace(getRootNode(), getOffset());
	}

	/**
	 * Return the enclosing type's node or the root node if not in a type, used for traversing via visitor to pick up
	 * variables/methods/etc
	 * 
	 * @return
	 */
	public synchronized Node getEnclosingTypeNode()
	{
		if (enclosingScopeNode == null)
		{
			Node typeNode = new ClosestSpanningNodeLocator().find(getRootNode(), getOffset(), new INodeAcceptor()
			{

				public boolean accepts(Node node)
				{
					return node instanceof ClassNode || node instanceof ModuleNode;
				}
			});
			if (typeNode == null)
			{
				enclosingScopeNode = getRootNode();
			}
			else
			{
				enclosingScopeNode = typeNode;
			}
		}
		return enclosingScopeNode;
	}

	public Set<String> getLocalsInScope()
	{
		Set<String> locals = new TreeSet<String>();
		Node enclosingMethod = new ClosestSpanningNodeLocator().find(getRootNode(), getOffset(), new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node instanceof MethodDefNode;
			}
		});
		StaticScope scope = null;
		if (enclosingMethod == null)
		{
			scope = ((RootNode) getRootNode()).getStaticScope();
		}
		else
		{
			MethodDefNode methodDef = (MethodDefNode) enclosingMethod;
			scope = methodDef.getScope();
		}
		while (scope != null)
		{
			for (String var : scope.getAllNamesInScope())
			{
				locals.add(var);
			}
			scope = scope.getEnclosingScope();
		}
		return locals;
	}

	public Node getReceiver()
	{
		// FIXME We need to grab the receiver of the callnode, which will presumably be at the offset. If source was
		// busted because of just a period, then we need to take the node at offset probably
		return new OffsetNodeLocator().find(getRootNode(), offset);
	}

	public Collection<ITypeGuess> inferReceiver()
	{
		return new TypeInferrer().infer(getRootNode(), getReceiver());
	}
}
