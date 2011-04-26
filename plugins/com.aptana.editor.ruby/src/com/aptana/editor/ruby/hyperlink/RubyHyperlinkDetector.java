package com.aptana.editor.ruby.hyperlink;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.parser.ParserConfiguration;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.common.text.hyperlink.IndexQueryingHyperlinkDetector;
import com.aptana.editor.ruby.CoreStubber;
import com.aptana.editor.ruby.IRubyConstants;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.core.IRubyElement;
import com.aptana.editor.ruby.index.IRubyIndexConstants;
import com.aptana.editor.ruby.parsing.ast.FirstPrecursorNodeLocator;
import com.aptana.editor.ruby.parsing.ast.INodeAcceptor;
import com.aptana.editor.ruby.parsing.ast.NamedMember;
import com.aptana.editor.ruby.parsing.ast.OffsetNodeLocator;
import com.aptana.editor.ruby.parsing.ast.RubyScript;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexManager;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.lexer.IRange;

public class RubyHyperlinkDetector extends IndexQueryingHyperlinkDetector
{

	private IRegion srcRegion;
	private Node root;

	public RubyHyperlinkDetector()
	{
		super();
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks)
	{
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();
		try
		{
			// Can we grab an already parsed version from FileService?
			IDocument doc = textViewer.getDocument();
			Parser parser = new Parser();
			// TODO Handle fixing common syntax errors as we do in ruble for CA!
			root = parser.parse("", new StringReader(doc.get()), new ParserConfiguration(0, CompatVersion.BOTH));

			if (root == null)
			{
				return null;
			}
			// Expand hyperlink region to the node bounds
			srcRegion = region;
			Node atOffset = new OffsetNodeLocator().find(root, region.getOffset());
			if (atOffset == null)
			{
				return null;
			}
			srcRegion = new Region(atOffset.getPosition().getStartOffset(), atOffset.getPosition().getEndOffset()
					- atOffset.getPosition().getStartOffset());
			switch (atOffset.getNodeType())
			{
				case CALLNODE:
					hyperlinks.addAll(methodCallLink((CallNode) atOffset));
					break;
				case COLON3NODE:
				case CONSTNODE:
					hyperlinks.addAll(constNode(atOffset));
					break;
				case COLON2NODE:
					hyperlinks.addAll(typeName((Colon2Node) atOffset));
					break;
				case LOCALVARNODE:
					hyperlinks.addAll(localVariableDeclaration(atOffset));
					break;
				case INSTVARNODE:
					hyperlinks.addAll(instanceVariableDeclaration(atOffset));
					break;
				case CLASSVARNODE:
					hyperlinks.addAll(classVariableDeclaration(atOffset));
					break;
				default:
					System.out.println(atOffset);
					break;
			}
		}
		catch (Exception e)
		{
			RubyEditorPlugin.log(e);
		}
		try
		{
			if (hyperlinks.isEmpty())
			{
				return null;
			}
			return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
		}
		finally
		{
			root = null;
			srcRegion = null;
		}
	}

	/**
	 * Generate a hyperlink to the first preceding assignment to the same local variable.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<? extends IHyperlink> localVariableDeclaration(Node atOffset)
	{
		return variableDeclaration(atOffset, NodeType.LOCALASGNNODE);
	}

	/**
	 * Generate a hyperlink to the first preceding assignment to the same instance variable.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<? extends IHyperlink> instanceVariableDeclaration(Node atOffset)
	{
		return variableDeclaration(atOffset, NodeType.INSTASGNNODE);
	}

	/**
	 * Generate a hyperlink to the first preceding assignment to the same class variable.
	 * 
	 * @param atOffset
	 * @return
	 */
	private Collection<? extends IHyperlink> classVariableDeclaration(Node atOffset)
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
	private Collection<? extends IHyperlink> variableDeclaration(Node atOffset, final NodeType nodeType)
	{
		Node decl = new FirstPrecursorNodeLocator().find(root, atOffset.getPosition().getStartOffset() - 1,
				new INodeAcceptor()
				{

					public boolean accepts(Node node)
					{
						return node.getNodeType() == nodeType;
					}
				});
		if (decl != null)
		{
			List<IHyperlink> links = new ArrayList<IHyperlink>();
			links.add(new EditorLineHyperlink(srcRegion, getURI(), new Region(decl.getPosition().getStartOffset(), decl
					.getPosition().getEndOffset() - decl.getPosition().getStartOffset())));
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
	private List<IHyperlink> constNode(Node atOffset)
	{
		String namespace = new NamespaceVisitor().getNamespace(root, atOffset.getPosition().getStartOffset());
		// FIXME Check the current namespace to determine full namespace of constant/type we're trying to resolve (see
		// ActionController::Base's implicit ref to Metal)
		List<IHyperlink> links = new ArrayList<IHyperlink>();
		String constantName = ((INameNode) atOffset).getName();
		links.addAll(findConstant(constantName));
		if (namespace.length() > 0)
		{
			constantName = namespace + "::" + constantName;
		}
		links.addAll(findType(constantName));
		return links;
	}

	private List<IHyperlink> typeName(Colon2Node atOffset)
	{
		// TODO Handle Colon2ConstNode vs Colon2MethodNode.
		List<IHyperlink> links = new ArrayList<IHyperlink>();
		String fullyQualifiedTypeName = getFullyQualifiedTypeName(atOffset);
		links.addAll(findType(fullyQualifiedTypeName));
		return links;
	}

	private String getFullyQualifiedTypeName(Colon2Node typeNode)
	{
		Node leftNode = typeNode.getLeftNode();
		if (leftNode != null)
		{
			if (leftNode instanceof Colon2Node)
			{
				return getFullyQualifiedTypeName((Colon2Node) leftNode) + "::" + typeNode.getName();
			}
			else if (leftNode instanceof INameNode)
			{
				INameNode namedNode = (INameNode) leftNode;
				return namedNode.getName() + "::" + typeNode.getName();
			}
		}

		return typeNode.getName();
	}

	/**
	 * Given a constant name, search the project index for declarations of constants matching that name and then
	 * generate hyperlinks to those declarations.
	 * 
	 * @param constantName
	 * @return
	 */
	private List<IHyperlink> findConstant(String constantName)
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
			RubyEditorPlugin.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the full set of indices that we may need to search. Project index, ruby core, ruby std libs, then gems.
	 * 
	 * @return
	 */
	protected List<Index> getAllIndices()
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
	private IRegion createRegion(IRubyElement p)
	{
		if (p instanceof NamedMember)
		{
			NamedMember nm = (NamedMember) p;
			IRange range = nm.getNameNode().getNameRange();
			return new Region(range.getStartingOffset(), range.getLength());
		}

		return new Region(p.getStartingOffset(), p.getEndingOffset() - p.getStartingOffset());
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
			RubyEditorPlugin.log(e);
		}
		return null;
	}

	private List<IHyperlink> methodCallLink(CallNode callNode)
	{
		List<IHyperlink> links = new ArrayList<IHyperlink>();
		String methodName = callNode.getName();
		if ("new".equals(methodName)) //$NON-NLS-1$
		{
			Node receiver = callNode.getReceiverNode();
			String typeName = inferType(receiver);
			links.addAll(findType(typeName)); // TODO Find the "initialize" sub-method of the type if it exists!
		}
		else
		{
			links.addAll(findMethods(methodName));
		}
		return links;
	}

	private String inferType(Node receiver)
	{
		// TODO Infer the type of the receiver...
		switch (receiver.getNodeType())
		{
			case CONSTNODE:
				return ((ConstNode) receiver).getName();
			default:
				break;
		}
		return null;
	}

	private List<IHyperlink> findMethods(String methodName)
	{
		// TODO Handle narrowing by type...
		List<IHyperlink> links = new ArrayList<IHyperlink>();
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
			RubyEditorPlugin.log(e);
		}
		return links;
	}

	private List<IHyperlink> findType(String typeName)
	{
		// Handle qualified type name!
		String namespace = "";
		int separatorIndex = typeName.indexOf("::");
		if (separatorIndex != -1)
		{
			namespace = typeName.substring(0, separatorIndex);
			typeName = typeName.substring(separatorIndex + 2);
		}
		List<IHyperlink> links = new ArrayList<IHyperlink>();
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
				links.addAll(getMatchingElementHyperlinks(results, typeName, IRubyElement.TYPE));
			}
		}
		catch (IOException e)
		{
			RubyEditorPlugin.log(e);
		}
		return links;
	}

	@Override
	protected Index getIndex()
	{
		Index index = super.getIndex();
		if (index != null)
		{
			return index;
		}
		// TODO Handle when we have an external file open, check it's path and try and find the correct index where this
		// is a file underneath it...
		return null;
	}

	private List<IHyperlink> getMatchingElementHyperlinks(List<QueryResult> results, String elementName, int elementType)
	{
		if (results == null)
		{
			return Collections.emptyList();
		}

		List<IHyperlink> links = new ArrayList<IHyperlink>();
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
							links.add(new EditorLineHyperlink(srcRegion, URI.create(doc), createRegion(p)));
						}
					}
				}
			}
		}
		return links;
	}

}
