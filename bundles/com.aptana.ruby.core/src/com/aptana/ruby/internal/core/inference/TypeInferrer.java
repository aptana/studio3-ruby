/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.inference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.index.core.Index;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.ast.ASTUtils;
import com.aptana.ruby.core.ast.ClosestSpanningNodeLocator;
import com.aptana.ruby.core.ast.FirstPrecursorNodeLocator;
import com.aptana.ruby.core.ast.INodeAcceptor;
import com.aptana.ruby.core.ast.NamespaceVisitor;
import com.aptana.ruby.core.ast.OffsetNodeLocator;
import com.aptana.ruby.core.ast.ScopedNodeLocator;
import com.aptana.ruby.core.index.IRubyIndexConstants;
import com.aptana.ruby.core.index.RubyIndexUtil;
import com.aptana.ruby.core.inference.ITypeGuess;
import com.aptana.ruby.core.inference.ITypeInferrer;

@SuppressWarnings("nls")
public class TypeInferrer implements ITypeInferrer
{

	/**
	 * Hard-coded mapping from common method names to their possible return types.
	 */
	private static final Map<String, Collection<ITypeGuess>> TYPICAL_METHOD_RETURN_TYPE_NAMES = new HashMap<String, Collection<ITypeGuess>>();
	static
	{
		// TODO Read this in from some config file/property file rather than hardcode it!
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("ceil", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("center", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("concat", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("count", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("crypt", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("dump", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("floor", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gets", createSet(IRubyConstants.STRING, IRubyConstants.NIL_CLASS));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("hash", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("index", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("inspect", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("intern", createSet(IRubyConstants.SYMBOL));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("length", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("now", createSet(IRubyConstants.TIME));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("round", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("size", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put(
				"slice",
				createSet(IRubyConstants.STRING, IRubyConstants.ARRAY, IRubyConstants.NIL_CLASS, IRubyConstants.OBJECT,
						IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put(
				"slice!",
				createSet(IRubyConstants.STRING, IRubyConstants.ARRAY, IRubyConstants.NIL_CLASS, IRubyConstants.OBJECT,
						IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase!", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_a", createSet(IRubyConstants.ARRAY));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_ary", createSet(IRubyConstants.ARRAY));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_i", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_int", createSet(IRubyConstants.FIXNUM));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_f", createSet(IRubyConstants.FLOAT));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_proc", createSet(IRubyConstants.PROC));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_s", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_str", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_string", createSet(IRubyConstants.STRING));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_sym", createSet(IRubyConstants.SYMBOL));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("unpack", createSet(IRubyConstants.ARRAY));
	}

	private IProject project;

	public TypeInferrer(IProject project)
	{
		this.project = project;
	}

	private static Set<ITypeGuess> createSet(String... strings)
	{
		if (strings == null || strings.length == 0)
		{
			return Collections.emptySet();
		}
		// TODO Allow for un-equal weighting of types!
		int weight = 100 / strings.length;
		Set<ITypeGuess> set = new HashSet<ITypeGuess>();
		for (String string : strings)
		{
			set.add(new BasicTypeGuess(string, weight, true));
		}
		return set;
	}

	private Collection<ITypeGuess> createSet(Map<String, Boolean> types)
	{
		if (types == null || types.isEmpty())
		{
			return Collections.emptySet();
		}
		int weight = 100 / types.size();
		Set<ITypeGuess> set = new HashSet<ITypeGuess>();
		for (Map.Entry<String, Boolean> entry : types.entrySet())
		{
			set.add(new BasicTypeGuess(entry.getKey(), weight, entry.getValue()));
		}
		return set;
	}

	public Collection<ITypeGuess> infer(String source, int offset)
	{
		Parser parser = new Parser();
		Reader reader = new BufferedReader(new StringReader(source));
		Node root = null;
		try
		{
			root = parser.parse(StringUtil.EMPTY, reader, new ParserConfiguration(0, CompatVersion.BOTH));
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}

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
		if (toInfer == null)
		{
			return createSet(IRubyConstants.OBJECT);
		}
		switch (toInfer.getNodeType())
		{
			case CONSTNODE:
				return inferConstant(rootNode, (ConstNode) toInfer);
			case CALLNODE:
			case FCALLNODE:
			case VCALLNODE:
				return inferMethod(rootNode, (INameNode) toInfer);
			case SYMBOLNODE:
			case DSYMBOLNODE:
				return createSet(IRubyConstants.SYMBOL);
			case ARRAYNODE:
			case ZARRAYNODE:
				return createSet(IRubyConstants.ARRAY);
			case BIGNUMNODE:
				return createSet(IRubyConstants.BIGNUM);
			case FIXNUMNODE:
				return createSet(IRubyConstants.FIXNUM);
			case FLOATNODE:
				return createSet(IRubyConstants.FLOAT);
			case HASHNODE:
				return createSet(IRubyConstants.HASH);
			case DREGEXPNODE:
			case REGEXPNODE:
				return createSet(IRubyConstants.REGEXP);
			case TRUENODE:
				return createSet(IRubyConstants.TRUE_CLASS);
			case FALSENODE:
				return createSet(IRubyConstants.FALSE_CLASS);
			case NILNODE:
				return createSet(IRubyConstants.NIL_CLASS);
			case DSTRNODE:
			case DXSTRNODE:
			case STRNODE:
			case XSTRNODE:
				return createSet(IRubyConstants.STRING);
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
		return createSet(IRubyConstants.OBJECT);
	}

	private Collection<ITypeGuess> inferConstant(Node rootNode, ConstNode toInfer)
	{
		NamespaceVisitor visitor = new NamespaceVisitor();
		String implicitNamespace = visitor.getNamespace(rootNode, toInfer.getPosition().getStartOffset());
		String constantName = toInfer.getName();
		// First search for types and constants in the implicit namespace
		// if no match, then look for them in toplevel
		Map<String, Boolean> types = matchingTypes(implicitNamespace + IRubyConstants.NAMESPACE_DELIMETER
				+ constantName);
		if (types.isEmpty())
		{
			// TODO If no matching types, search constants and then infer any matches!
			// types = inferMatchingConstants(implicitNamespace + IRubyConstants.NAMESPACE_DELIMETER +
			// constantName);
			// no matching types or constants, try without implicit namespace
			if (implicitNamespace.length() > 0)
			{
				types = matchingTypes(constantName);
				// TODO If no matching types, search constants!
				// if (types.isEmpty())
				// {
				// types = inferMatchingConstants(constantName);
				// }
			}
		}

		if (types.isEmpty())
		{
			// Fell all the way through, fall back and just assume constant text is a type
			// FIXME We're assuming this is a class and not a module, may want to do some verification?
			return createSet(constantName);
		}
		return createSet(types);
	}

	/**
	 * Returns a map from the type name to a boolean indicating if it's a class (true) or Module (false).
	 * 
	 * @param fullyQualifiedName
	 * @return
	 */
	private Map<String, Boolean> matchingTypes(String fullyQualifiedName)
	{
		Map<String, Boolean> matches = new HashMap<String, Boolean>();

		if (fullyQualifiedName.startsWith(IRubyConstants.NAMESPACE_DELIMETER))
		{
			fullyQualifiedName = fullyQualifiedName.substring(2);
		}
		String typeName = fullyQualifiedName;
		String namespace = StringUtil.EMPTY;
		int lastNS = typeName.lastIndexOf(IRubyConstants.NAMESPACE_DELIMETER);
		if (lastNS != -1)
		{
			namespace = typeName.substring(0, lastNS);
			typeName = typeName.substring(lastNS + 2);
		}
		// Build query key
		StringBuilder builder = new StringBuilder();
		builder.append('^'); // start matching at beginning of key
		builder.append(typeName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		builder.append(namespace);
		builder.append(IRubyIndexConstants.SEPARATOR);
		builder.append(".+$");
		String key = builder.toString();
		for (Index index : getAllIndicesForProject())
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.TYPE_DECL }, key,
					SearchPattern.REGEX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				String word = result.getWord();
				String[] parts = word.split(Character.toString(IRubyIndexConstants.SEPARATOR));
				StringBuilder fullName = new StringBuilder();
				if (parts[1].length() > 0)
				{
					fullName.append(parts[1]);
					fullName.append(IRubyConstants.NAMESPACE_DELIMETER);
				}
				fullName.append(parts[0]);
				boolean isClass = parts[2].equals(IRubyIndexConstants.CLASS_SUFFIX);
				matches.put(fullName.toString(), isClass);
			}
		}
		return matches;
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
			return createSet(IRubyConstants.OBJECT);
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
		int namespaceIndex = fullName.lastIndexOf(IRubyConstants.NAMESPACE_DELIMETER);
		if (namespaceIndex != -1)
		{
			typeName = fullName.substring(0, namespaceIndex);
			constantName = fullName.substring(namespaceIndex + 2);

			namespaceIndex = typeName.lastIndexOf(IRubyConstants.NAMESPACE_DELIMETER);
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
		for (Index index : getAllIndicesForProject())
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
			Reader reader = null;
			try
			{
				// TODO Move parsing code into one method, and try to use the parser pool
				IFileStore store = EFS.getStore(URI.create(matchingDocURI));
				InputStream stream = store.openInputStream(EFS.NONE, new NullProgressMonitor());
				reader = new BufferedReader(new InputStreamReader(stream));
				Parser parser = new Parser();
				Node root = parser.parse(StringUtil.EMPTY, reader, new ParserConfiguration(0, CompatVersion.BOTH));
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
			catch (SyntaxException e) // $codepro.audit.disable emptyCatchClause
			{
				// ignore if syntax is busted.
			}
			catch (CoreException e)
			{
				IdeLog.log(RubyCorePlugin.getDefault(), e.getStatus());
			}
			finally
			{
				if (reader != null)
				{
					try
					{
						reader.close();
					}
					catch (IOException e) // $codepro.audit.disable emptyCatchClause
					{
						// ignore
					}
				}
			}
		}

		// FIXME We're assuming this is a class, and not a module. May want to look up in indices and verify!
		// It appears to be a type and not a constant, so just return the actual text as the resulting Type inferred
		return createSet(fullName);
	}

	protected Collection<Index> getAllIndicesForProject()
	{
		return RubyIndexUtil.allIndices(project);
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
		return createSet(IRubyConstants.OBJECT);
	}

	private Collection<ITypeGuess> inferMethod(Node rootNode, INameNode toInfer)
	{
		final String methodName = toInfer.getName();
		if (methodName.endsWith("?"))
		{
			return createSet(IRubyConstants.TRUE_CLASS, IRubyConstants.FALSE_CLASS);
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
				guesses = new ArrayList<ITypeGuess>();
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
					for (Node methodNode : methods)
					{
						List<Node> returnNodes = new ScopedNodeLocator().find(methodNode, new INodeAcceptor()
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
			}
			return Collections.emptySet();
		}
		return guesses;
	}

}
