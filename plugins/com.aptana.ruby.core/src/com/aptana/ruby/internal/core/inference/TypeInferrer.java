/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.inference;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.AssignableNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassVarNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstDeclNode;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.InstVarNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.LocalVarNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.ReturnNode;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.parser.ParserConfiguration;

import com.aptana.core.util.StringUtil;
import com.aptana.index.core.Index;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.ast.ASTUtils;
import com.aptana.ruby.core.ast.ClosestSpanningNodeLocator;
import com.aptana.ruby.core.ast.FirstPrecursorNodeLocator;
import com.aptana.ruby.core.ast.INodeAcceptor;
import com.aptana.ruby.core.ast.OffsetNodeLocator;
import com.aptana.ruby.core.ast.ScopedNodeLocator;
import com.aptana.ruby.core.index.IRubyIndexConstants;
import com.aptana.ruby.core.index.RubyIndexUtil;
import com.aptana.ruby.core.inference.ITypeGuess;
import com.aptana.ruby.core.inference.ITypeInferrer;

@SuppressWarnings("nls")
public class TypeInferrer implements ITypeInferrer
{

	// TODO Create constants for all the type names

	/**
	 * Hard-coded mapping from common method names to their possible return types.
	 */
	private static final Map<String, Collection<ITypeGuess>> TYPICAL_METHOD_RETURN_TYPE_NAMES = new HashMap<String, Collection<ITypeGuess>>();
	static
	{
		// TODO Read this in from some config file/property file rather than hardcode it!
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("ceil", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("center", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("concat", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("count", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("crypt", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("dump", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("floor", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gets", createSet("String", "NilClass"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("hash", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("index", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("inspect", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("intern", createSet("Symbol"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("length", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("now", createSet("Time"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("round", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("size", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("slice", createSet("String", "Array", "NilClass", "Object", "Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("slice!", createSet("String", "Array", "NilClass", "Object", "Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_a", createSet("Array"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_ary", createSet("Array"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_i", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_int", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_f", createSet("Float"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_proc", createSet("Proc"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_s", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_str", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_string", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_sym", createSet("Symbol"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("unpack", createSet("Array"));
	}

	private IProject project;

	public TypeInferrer(IProject project)
	{
		this.project = project;
	}

	private static Set<ITypeGuess> createSet(String... strings)
	{
		// TODO Allow for un-equal weighting of types!
		int weight = 100 / strings.length;
		Set<ITypeGuess> set = new HashSet<ITypeGuess>();
		for (String string : strings)
		{
			set.add(new BasicTypeGuess(string, weight));
		}
		return set;
	}

	public Collection<ITypeGuess> infer(String source, int offset)
	{
		Parser parser = new Parser();
		Node root = parser.parse("", new StringReader(source), new ParserConfiguration(0, CompatVersion.BOTH)); //$NON-NLS-1$
		if (root == null)
		{
			return Collections.emptyList();
		}
		Node atOffset = new OffsetNodeLocator().find(root, offset);
		if (atOffset == null)
		{
			return Collections.emptyList();
		}
		return infer(root, atOffset);
	}

	public Collection<ITypeGuess> infer(Node rootNode, Node toInfer)
	{
		switch (toInfer.getNodeType())
		{
			case CONSTNODE:
				// FIXME Treat this like we do in inferColon2Node where we look for matching constant decl!
				return createSet(((ConstNode) toInfer).getName());
			case CALLNODE:
			case FCALLNODE:
			case VCALLNODE:
				return inferMethod(rootNode, (INameNode) toInfer);
			case SYMBOLNODE:
			case DSYMBOLNODE:
				return createSet("Symbol");
			case ARRAYNODE:
			case ZARRAYNODE:
				return createSet("Array");
			case BIGNUMNODE:
				return createSet("Bignum");
			case FIXNUMNODE:
				return createSet("Fixnum");
			case FLOATNODE:
				return createSet("Float");
			case HASHNODE:
				return createSet("Hash");
			case DREGEXPNODE:
			case REGEXPNODE:
				return createSet("Regexp");
			case TRUENODE:
				return createSet("TrueClass");
			case FALSENODE:
				return createSet("FalseClass");
			case NILNODE:
				return createSet("NilClass");
			case DSTRNODE:
			case DXSTRNODE:
			case STRNODE:
			case XSTRNODE:
				return createSet("String");
			case LOCALVARNODE:
				return inferLocal(rootNode, (LocalVarNode) toInfer);
			case INSTVARNODE:
				return inferInstance(rootNode, (InstVarNode) toInfer);
			case CLASSVARNODE:
				return inferClassVar(rootNode, (ClassVarNode) toInfer);
			case COLON2NODE:
				return inferColon2Node((Colon2Node) toInfer);
			case CLASSVARASGNNODE:
			case CLASSVARDECLNODE:
			case CONSTDECLNODE:
			case DASGNNODE:
			case GLOBALASGNNODE:
			case INSTASGNNODE:
			case LOCALASGNNODE:
			case MULTIPLEASGN19NODE:
			case MULTIPLEASGNNODE:
				AssignableNode assignable = (AssignableNode) toInfer;
				return infer(rootNode, assignable.getValueNode());
			default:
				break;
		}
		return createSet("Object");
	}

	private Collection<ITypeGuess> inferInstance(Node rootNode, InstVarNode toInfer)
	{
		return inferClassOrInstanceVar(rootNode, toInfer, NodeType.INSTASGNNODE);
	}

	private Collection<ITypeGuess> inferClassVar(Node rootNode, ClassVarNode toInfer)
	{
		return inferClassOrInstanceVar(rootNode, toInfer, NodeType.CLASSVARASGNNODE, NodeType.CLASSVARDECLNODE);
	}

	private Collection<ITypeGuess> inferClassOrInstanceVar(Node rootNode, final INameNode varRefNode,
			final NodeType... nodeTypes)
	{
		final String varName = varRefNode.getName();
		Node enclosingTypeNode = enclosingType(rootNode, ((Node) varRefNode).getPosition().getStartOffset());
		if (enclosingTypeNode == null)
		{
			enclosingTypeNode = rootNode;
		}
		List<Node> assigns = new ScopedNodeLocator().find(enclosingTypeNode, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				boolean found = false;
				for (NodeType type : nodeTypes)
				{
					if (node.getNodeType() == type)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					return false;
				}
				return ((INameNode) node).getName().equals(varName);
			}
		});
		if (assigns == null)
		{
			return createSet("Object");
		}
		Collection<ITypeGuess> guesses = new ArrayList<ITypeGuess>();
		for (Node assignment : assigns)
		{
			AssignableNode assignmentNode = (AssignableNode) assignment;
			guesses.addAll(infer(rootNode, assignmentNode.getValueNode()));
		}
		return guesses;
	}

	private Node enclosingType(Node rootNode, int startOffset)
	{
		return new ClosestSpanningNodeLocator().find(rootNode, startOffset, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node.getNodeType() == NodeType.CLASSNODE || node.getNodeType() == NodeType.MODULENODE;
			}
		});
	}

	private Collection<ITypeGuess> inferColon2Node(Colon2Node toInfer)
	{
		// Break name into constant name, type base name, type namespace. Ugh!
		String fullName = ASTUtils.getFullyQualifiedName(toInfer);
		String namespace = StringUtil.EMPTY;
		String typeName = StringUtil.EMPTY;
		String constantName = fullName;
		int namespaceIndex = fullName.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER);
		if (namespaceIndex != -1)
		{
			typeName = fullName.substring(0, namespaceIndex);
			constantName = fullName.substring(namespaceIndex + 2);

			namespaceIndex = typeName.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER);
			if (namespaceIndex != -1)
			{
				namespace = typeName.substring(0, namespaceIndex);
				typeName = typeName.substring(namespaceIndex + 2);
			}
		}
		// TODO Check the indices to see if this is a constant or a type! If constant, we need to infer that constant
		// decl!
		final String key = constantName + IRubyIndexConstants.SEPARATOR + typeName + IRubyIndexConstants.SEPARATOR
				+ namespace;
		String matchingDocURI = null;
		for (Index index : RubyIndexUtil.allIndices(project))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.CONSTANT_DECL }, key,
					SearchPattern.EXACT_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null || results.isEmpty())
			{
				continue;
			}
			for (QueryResult result : results)
			{
				// Found a match! Exit early, don't keep searching indices...
				matchingDocURI = result.getDocuments().iterator().next();
				break;
			}
			if (matchingDocURI != null)
			{
				break;
			}
		}

		if (matchingDocURI != null)
		{
			try
			{
				// TODO Move parsing code into one method, and try to use the parser pool
				IFileStore store = EFS.getStore(URI.create(matchingDocURI));
				InputStream stream = store.openInputStream(EFS.NONE, new NullProgressMonitor());

				Parser parser = new Parser();
				Node root = parser.parse(
						"", new InputStreamReader(stream), new ParserConfiguration(0, CompatVersion.BOTH)); //$NON-NLS-1$
				if (root == null)
				{
					return Collections.emptyList();
				}
				final String theConstantName = constantName;
				List<Node> decls = new ScopedNodeLocator().find(root, new INodeAcceptor()
				{
					public boolean accepts(Node node)
					{
						if (!(node instanceof ConstDeclNode))
						{
							return false;
						}
						ConstDeclNode declNode = (ConstDeclNode) node;
						return declNode.getName().equals(theConstantName);
					}
				});
				if (decls == null || decls.isEmpty())
				{
					return Collections.emptyList();
				}
				return infer(root, decls.iterator().next());
			}
			catch (SyntaxException e)
			{
				// ignore if syntax is busted.
			}
			catch (CoreException e)
			{
				RubyCorePlugin.log(e.getStatus());
			}
		}

		// It appears to be a type and not a constant, so just return the actual text as the resulting Type inferred
		return createSet(fullName);
	}

	private Collection<ITypeGuess> inferLocal(Node rootNode, LocalVarNode toInfer)
	{
		final String varName = toInfer.getName();
		Node precedingAssignment = new FirstPrecursorNodeLocator().find(rootNode, toInfer.getPosition()
				.getStartOffset() - 1, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node.getNodeType() == NodeType.LOCALASGNNODE && ((INameNode) node).getName().equals(varName);
			}
		});
		if (precedingAssignment != null)
		{
			LocalAsgnNode assign = (LocalAsgnNode) precedingAssignment;
			return infer(rootNode, assign.getValueNode());
		}
		return createSet("Object");
	}

	private Collection<ITypeGuess> inferMethod(Node rootNode, INameNode toInfer)
	{
		final String methodName = toInfer.getName();
		if (methodName.endsWith("?"))
		{
			return createSet("TrueClass", "FalseClass");
		}
		Collection<ITypeGuess> guesses = TYPICAL_METHOD_RETURN_TYPE_NAMES.get(methodName);
		if (guesses == null)
		{
			if (toInfer instanceof CallNode)
			{
				if ("new".equals(methodName))
				{
					Node receiver = ((CallNode) toInfer).getReceiverNode();
					return infer(rootNode, receiver);
				}
				// else
				// {
				// FIXME We need to gather the return type of the method if receiver is a method
				// }
			}
			else
			{
				Node enclosingType = enclosingType(rootNode, ((Node) toInfer).getPosition().getStartOffset());
				List<Node> methods = new ScopedNodeLocator().find(enclosingType, new INodeAcceptor()
				{

					public boolean accepts(Node node)
					{
						return NodeType.DEFNNODE == node.getNodeType()
								&& methodName.equals(((DefnNode) node).getName());
					}
				});
				if (!methods.isEmpty())
				{
					List<Node> returnNodes = new ScopedNodeLocator().find(enclosingType, new INodeAcceptor()
					{

						public boolean accepts(Node node)
						{
							return NodeType.RETURNNODE == node.getNodeType();
						}
					});
					if (!returnNodes.isEmpty())
					{
						for (Node returnNode : returnNodes)
						{
							ReturnNode blah = (ReturnNode) returnNode;
							guesses.addAll(infer(rootNode, blah.getValueNode()));
						}
					}
					// # Get method body as a BlockNode, grab last child, that's the implicit return.
					// implicit_return = last_statement(methods.first.body_node)
					// if implicit_return
					// case implicit_return.node_type
					// when org.jrubyparser.ast.NodeType::IFNODE
					// types << infer(last_statement(implicit_return.then_body)) if implicit_return.then_body
					// types << infer(last_statement(implicit_return.else_body)) if implicit_return.else_body
					// when org.jrubyparser.ast.NodeType::CASENODE
					// implicit_return.cases.child_nodes.each do |c|
					// types << infer(last_statement(c.body_node)) if c
					// end
					// types << infer(last_statement(implicit_return.else_node)) if implicit_return.else_node
				}
			}
			return Collections.emptySet();
		}
		return guesses;
	}

}
