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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.parser.ParserConfiguration;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.common.text.hyperlink.IndexQueryingHyperlinkDetector;
import com.aptana.editor.ruby.IRubyConstants;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.core.IRubyElement;
import com.aptana.editor.ruby.index.IRubyIndexConstants;
import com.aptana.editor.ruby.parsing.ast.FirstPrecursorNodeLocator;
import com.aptana.editor.ruby.parsing.ast.INodeAcceptor;
import com.aptana.editor.ruby.parsing.ast.NamedMember;
import com.aptana.editor.ruby.parsing.ast.OffsetNodeLocator;
import com.aptana.editor.ruby.parsing.ast.RubyScript;
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
			IDocument doc = textViewer.getDocument();
			Parser parser = new Parser();
			// TODO Handle fixing common syntax errors as we do in ruble for CA!
			root = parser.parse("", new StringReader(doc.get()), new ParserConfiguration(0, CompatVersion.BOTH));
			if (root == null)
			{
				return null;
			}
			// FIXME Need to expand to the node bounds!
			srcRegion = region;
			Node atOffset = new OffsetNodeLocator().find(root, region.getOffset());
			if (atOffset == null)
			{
				return null;
			}
			srcRegion = new Region(atOffset.getPosition().getStartOffset(), atOffset.getPosition().getEndOffset()
					- atOffset.getPosition().getStartOffset() + 1);
			switch (atOffset.getNodeType())
			{
				case CALLNODE:
					hyperlinks.addAll(methodCallLink((CallNode) atOffset));
					break;
				case COLON3NODE:
				case CONSTNODE:
					hyperlinks.addAll(constNode(atOffset));
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

	private Collection<? extends IHyperlink> localVariableDeclaration(Node atOffset)
	{
		// Check for matching local var declaration up the scope blocks!
		Node decl = new FirstPrecursorNodeLocator().find(root, atOffset.getPosition().getStartOffset() - 1,
				new INodeAcceptor()
				{

					public boolean accepts(Node node)
					{
						return node.getNodeType() == NodeType.LOCALASGNNODE;
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

	private Collection<? extends IHyperlink> instanceVariableDeclaration(Node atOffset)
	{
		// Check for matching instance var declaration up the scope blocks!
		Node decl = new FirstPrecursorNodeLocator().find(root, atOffset.getPosition().getStartOffset() - 1,
				new INodeAcceptor()
				{

					public boolean accepts(Node node)
					{
						return node.getNodeType() == NodeType.INSTASGNNODE;
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

	private Collection<? extends IHyperlink> classVariableDeclaration(Node atOffset)
	{
		// Check for matching class var declaration up the scope blocks!
		Node decl = new FirstPrecursorNodeLocator().find(root, atOffset.getPosition().getStartOffset() - 1,
				new INodeAcceptor()
				{

					public boolean accepts(Node node)
					{
						return node.getNodeType() == NodeType.CLASSVARASGNNODE;
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

	private List<IHyperlink> constNode(Node atOffset)
	{
		List<IHyperlink> links = new ArrayList<IHyperlink>();
		String constantName = ((INameNode) atOffset).getName();
		links.addAll(findConstant(constantName));
		links.addAll(findType(constantName));
		return links;
	}

	private List<IHyperlink> findConstant(String constantName)
	{
		List<IHyperlink> constants = new ArrayList<IHyperlink>();
		try
		{
			List<QueryResult> results = getIndex().query(new String[] { IRubyIndexConstants.CONSTANT_DECL },
					constantName, SearchPattern.EXACT_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				return Collections.emptyList();
			}
			for (QueryResult result : results)
			{
				Set<String> docs = result.getDocuments();
				for (String doc : docs)
				{
					RubyScript root = parseURI(doc);
					if (root != null)
					{
						List<IRubyElement> possible = root.getChildrenOfTypeRecursive(IRubyElement.CONSTANT);
						for (IRubyElement p : possible)
						{
							if (constantName.equals(p.getName()))
							{
								constants.add(new EditorLineHyperlink(srcRegion, URI.create(doc), createRegion(p)));
							}
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return constants;
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		List<IHyperlink> methods = new ArrayList<IHyperlink>();
		try
		{
			List<QueryResult> results = getIndex().query(new String[] { IRubyIndexConstants.METHOD_DECL },
					methodName + IRubyIndexConstants.SEPARATOR,
					SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				return Collections.emptyList();
			}
			for (QueryResult result : results)
			{
				Set<String> docs = result.getDocuments();
				for (String doc : docs)
				{
					RubyScript root = parseURI(doc);
					if (root != null)
					{
						List<IRubyElement> possible = root.getChildrenOfTypeRecursive(IRubyElement.METHOD);
						for (IRubyElement p : possible)
						{
							if (methodName.equals(p.getName()))
							{
								methods.add(new EditorLineHyperlink(srcRegion, URI.create(doc), createRegion(p)));
							}
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return methods;
	}

	private List<IHyperlink> findType(String typeName)
	{
		List<IHyperlink> types = new ArrayList<IHyperlink>();
		try
		{
			List<QueryResult> results = getIndex().query(new String[] { IRubyIndexConstants.TYPE_DECL },
					typeName + IRubyIndexConstants.SEPARATOR + IRubyIndexConstants.SEPARATOR,
					SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				return Collections.emptyList();
			}
			for (QueryResult result : results)
			{
				Set<String> docs = result.getDocuments();
				for (String doc : docs)
				{
					RubyScript root = parseURI(doc);
					if (root != null)
					{
						List<IRubyElement> possible = root.getChildrenOfTypeRecursive(IRubyElement.TYPE);
						for (IRubyElement p : possible)
						{
							if (typeName.equals(p.getName()))
							{
								// FIXME Find the name range!
								types.add(new EditorLineHyperlink(srcRegion, URI.create(doc), createRegion(p)));
							}
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return types;
	}

}
