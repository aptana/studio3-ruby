/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.codeassist;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Colon2MethodNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;

import com.aptana.core.util.IOUtil;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexManager;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.lexer.IRange;
import com.aptana.parsing.lexer.Range;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.ast.ASTUtils;
import com.aptana.ruby.core.ast.FirstPrecursorNodeLocator;
import com.aptana.ruby.core.ast.INodeAcceptor;
import com.aptana.ruby.core.ast.NamespaceVisitor;
import com.aptana.ruby.core.codeassist.CodeResolver;
import com.aptana.ruby.core.codeassist.ResolutionTarget;
import com.aptana.ruby.core.codeassist.ResolveContext;
import com.aptana.ruby.core.index.IRubyIndexConstants;
import com.aptana.ruby.core.inference.ITypeGuess;
import com.aptana.ruby.internal.core.NamedMember;
import com.aptana.ruby.internal.core.RubyScript;
import com.aptana.ruby.internal.core.index.CoreStubber;
import com.aptana.ruby.internal.core.inference.TypeInferrer;

public class RubyCodeResolver extends CodeResolver
{

	private static final String NAMESPACE_DELIMITER = "::"; //$NON-NLS-1$
	private ResolveContext context;

	@Override
	public void resolve(ResolveContext context)
	{
		this.context = context;
		try
		{
			Node atOffset = context.getSelectedNode();
			if (atOffset == null)
			{
				return;
			}
			switch (atOffset.getNodeType())
			{
				case VCALLNODE:
				case FCALLNODE:
					addAll(noReceiverMethodCallLink((INameNode) atOffset));
					break;
				case CALLNODE:
					addAll(methodCallLink((CallNode) atOffset));
					break;
				case COLON3NODE:
				case CONSTNODE:
					addAll(constNode(atOffset));
					break;
				case COLON2NODE:
					addAll(typeName((Colon2Node) atOffset));
					break;
				case LOCALVARNODE:
					addAll(localVariableDeclaration(atOffset));
					break;
				case INSTVARNODE:
					addAll(instanceVariableDeclaration(atOffset));
					break;
				case CLASSVARNODE:
					addAll(classVariableDeclaration(atOffset));
					break;
				default:
					// System.out.println(atOffset);
					break;
			}
		}
		finally
		{
			this.context = null;
		}
	}

	private void addAll(Collection<ResolutionTarget> targets)
	{
		if (targets != null)
		{
			context.addResolved(targets);
		}
	}

	private Collection<ResolutionTarget> noReceiverMethodCallLink(INameNode atOffset)
	{
		String methodName = atOffset.getName();
		// TODO Try and infer the type of "self" and search up the hierarchy for matching methods first before we do a
		// global search!
		return findMethods(methodName);
	}

	/**
	 * Generate a hyperlink to the first preceding assignment to the same local variable.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<ResolutionTarget> localVariableDeclaration(Node atOffset)
	{
		return variableDeclaration(atOffset, NodeType.LOCALASGNNODE);
	}

	/**
	 * Generate a hyperlink to the first preceding assignment to the same instance variable.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<ResolutionTarget> instanceVariableDeclaration(Node atOffset)
	{
		return variableDeclaration(atOffset, NodeType.INSTASGNNODE);
	}

	/**
	 * Generate a hyperlink to the first preceding assignment to the same class variable.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<ResolutionTarget> classVariableDeclaration(Node atOffset)
	{
		return variableDeclaration(atOffset, NodeType.CLASSVARASGNNODE);
	}

	/**
	 * Common code for finding first preceding assignment to a variable.
	 * 
	 * @param atOffset
	 * @param nodeType
	 * @return
	 */
	private Collection<ResolutionTarget> variableDeclaration(Node atOffset, final NodeType nodeType)
	{
		Node decl = new FirstPrecursorNodeLocator().find(getRoot(), atOffset.getPosition().getStartOffset() - 1,
				new INodeAcceptor()
				{

					public boolean accepts(Node node)
					{
						return node.getNodeType() == nodeType;
					}
				});
		if (decl != null)
		{
			List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
			links.add(new ResolutionTarget(this.context.getURI(), new Range(decl.getPosition().getStartOffset(), decl
					.getPosition().getEndOffset())));
			return links;
		}
		return Collections.emptyList();
	}

	/**
	 * Generate hyperlinks for a constant reference. Could be referring to a constant that is declared, or to a type
	 * name.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<ResolutionTarget> constNode(Node atOffset)
	{
		String namespace = new NamespaceVisitor().getNamespace(getRoot(), atOffset.getPosition().getStartOffset());
		List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
		String constantName = ((INameNode) atOffset).getName();
		links.addAll(findConstant(constantName));
		if (namespace.length() > 0)
		{
			constantName = namespace + NAMESPACE_DELIMITER + constantName;
		}
		links.addAll(findType(constantName));
		return links;
	}

	private Collection<ResolutionTarget> typeName(Colon2Node atOffset)
	{
		if (atOffset instanceof Colon2MethodNode)
		{
			return noReceiverMethodCallLink(atOffset);
		}
		List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
		String fullyQualifiedTypeName = ASTUtils.getFullyQualifiedName(atOffset);
		links.addAll(findType(fullyQualifiedTypeName));
		return links;
	}

	/**
	 * Given a constant name, search the project index for declarations of constants matching that name and then
	 * generate hyperlinks to those declarations.
	 * 
	 * @param constantName
	 * @return
	 */
	private Collection<ResolutionTarget> findConstant(String constantName)
	{
		try
		{
			Index index = getIndex();
			if (index == null)
			{
				return Collections.emptyList();
			}
			// TODO Search AST in current file first?
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.CONSTANT_DECL }, constantName,
					SearchPattern.EXACT_MATCH | SearchPattern.CASE_SENSITIVE);
			return getMatchingElementHyperlinks(results, constantName, IRubyElement.CONSTANT);
		}
		catch (IOException e)
		{
			RubyCorePlugin.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the full set of indices that we may need to search. Project index, ruby core, ruby std libs, then gems.
	 * 
	 * @return
	 */
	private List<Index> getAllIndices()
	{
		List<Index> indices = new ArrayList<Index>();
		indices.add(getIndex());
		indices.add(CoreStubber.getRubyCoreIndex());
		for (IPath path : CoreStubber.getLoadpaths())
		{
			indices.add(IndexManager.getInstance().getIndex(path.toFile().toURI()));
		}
		for (IPath path : CoreStubber.getGemPaths())
		{
			indices.add(IndexManager.getInstance().getIndex(path.toFile().toURI()));
		}
		return indices;
	}

	/**
	 * Offset and length in destination file of hyperlink. Point to name region for type/method declarations.
	 * 
	 * @param p
	 * @return
	 */
	private IRange createRange(IRubyElement p)
	{
		if (p instanceof NamedMember)
		{
			NamedMember nm = (NamedMember) p;
			return nm.getNameNode().getNameRange();
		}
		return new Range(p.getStartingOffset(), p.getEndingOffset());
	}

	/*
	 * doc is an URI, parse it and traverse the AST to find the constant!
	 */
	private RubyScript parseURI(String doc)
	{
		try
		{
			IFileStore store = EFS.getStore(URI.create(doc));
			return (RubyScript) ParserPoolFactory.parse(IRubyConstants.CONTENT_TYPE_RUBY,
					IOUtil.read(store.openInputStream(EFS.NONE, new NullProgressMonitor())));
		}
		catch (CoreException e)
		{
			RubyCorePlugin.log(e);
		}
		return null;
	}

	private Collection<ResolutionTarget> methodCallLink(CallNode callNode)
	{
		List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
		String methodName = callNode.getName();
		if ("new".equals(methodName)) //$NON-NLS-1$
		{
			Node receiver = callNode.getReceiverNode();
			Collection<ITypeGuess> guesses = new TypeInferrer().infer(getRoot(), receiver);
			for (ITypeGuess guess : guesses)
			{
				// TODO Find the "initialize" sub-method of the type if it exists!
				links.addAll(findType(guess.getType()));
			}
		}
		else
		{
			links.addAll(findMethods(methodName));
		}
		return links;
	}

	private Node getRoot()
	{
		return context.getAST();
	}

	private Collection<ResolutionTarget> findMethods(String methodName)
	{
		// TODO Handle narrowing by type...
		List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
		try
		{
			// Search all indices
			for (Index index : getAllIndices())
			{
				if (index == null)
				{
					continue;
				}
				List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.METHOD_DECL }, methodName
						+ IRubyIndexConstants.SEPARATOR, SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
				links.addAll(getMatchingElementHyperlinks(results, methodName, IRubyElement.METHOD));
			}
		}
		catch (IOException e)
		{
			RubyCorePlugin.log(e);
		}
		return links;
	}

	private Collection<ResolutionTarget> findType(String typeName)
	{
		// Handle qualified type name!
		String namespace = ""; //$NON-NLS-1$
		int separatorIndex = typeName.indexOf(NAMESPACE_DELIMITER);
		if (separatorIndex != -1)
		{
			namespace = typeName.substring(0, separatorIndex);
			typeName = typeName.substring(separatorIndex + 2);
		}
		List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
		try
		{
			// Search all indices
			for (Index index : getAllIndices())
			{
				if (index == null)
				{
					continue;
				}
				List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.TYPE_DECL }, typeName
						+ IRubyIndexConstants.SEPARATOR + namespace + IRubyIndexConstants.SEPARATOR,
						SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
				// TODO Exit early if we find matches?
				// FIXME Sort by a priority. We should prefer filenames that match the type name, parent folders
				// matching parent namespaces.
				links.addAll(getMatchingElementHyperlinks(results, typeName, IRubyElement.TYPE));
			}
		}
		catch (IOException e)
		{
			RubyCorePlugin.log(e);
		}
		return links;
	}

	private Index getIndex()
	{
		// Grab the correct index for this context. try and resolve URI to workspace file, grab it's project, then
		// project index?
		URI uri = context.getURI();
		if ("file".equals(uri.getScheme())) //$NON-NLS-1$
		{
			IPath path = Path.fromOSString(uri.getPath());
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			if (file != null)
			{
				IProject project = file.getProject();
				return IndexManager.getInstance().getIndex(project.getLocationURI());
			}
		}

		return null;
	}

	private Collection<ResolutionTarget> getMatchingElementHyperlinks(List<QueryResult> results, String elementName,
			int elementType)
	{
		if (results == null)
		{
			return Collections.emptyList();
		}

		List<ResolutionTarget> links = new ArrayList<ResolutionTarget>();
		for (QueryResult result : results)
		{
			Set<String> docs = result.getDocuments();
			for (String doc : docs)
			{
				RubyScript root = parseURI(doc);
				if (root != null)
				{
					List<IRubyElement> possible = root.getChildrenOfTypeRecursive(elementType);
					for (IRubyElement p : possible)
					{
						if (elementName.equals(p.getName()))
						{
							links.add(new ResolutionTarget(URI.create(doc), createRange(p)));
						}
					}
				}
			}
		}
		return links;
	}

}
