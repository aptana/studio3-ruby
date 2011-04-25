package com.aptana.editor.ruby;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserConfiguration;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.common.text.hyperlink.IndexQueryingHyperlinkDetector;
import com.aptana.editor.ruby.core.IRubyElement;
import com.aptana.editor.ruby.core.IRubyField;
import com.aptana.editor.ruby.core.IRubyMethod;
import com.aptana.editor.ruby.core.IRubyType;
import com.aptana.editor.ruby.index.IRubyIndexConstants;
import com.aptana.editor.ruby.parsing.ast.OffsetNodeLocator;
import com.aptana.editor.ruby.parsing.ast.RubyScript;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;
import com.aptana.parsing.ParserPoolFactory;

public class RubyHyperlinkDetector extends IndexQueryingHyperlinkDetector
{

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
			Node root = parser.parse("", new StringReader(doc.get()), new ParserConfiguration(0, CompatVersion.BOTH));
			if (root == null)
			{
				return null;
			}

			Node atOffset = new OffsetNodeLocator().find(root, region.getOffset());
			if (atOffset == null)
			{
				return null;
			}
			switch (atOffset.getNodeType())
			{
				case CALLNODE:
					return methodCallLink((CallNode) atOffset);
				case COLON3NODE:
				case CONSTNODE:
					return constNode(atOffset);
				default:
					break;
			}

			// Index index = getIndex();
			// if (index == null)
			// {
			// return null;
			// }

			// String htmlId = m.group().substring(1);
			// List<QueryResult> results = index.query(new String[] { CSSIndexConstants.IDENTIFIER }, htmlId,
			// SearchPattern.EXACT_MATCH | SearchPattern.CASE_SENSITIVE);
			// if (results == null || results.isEmpty())
			// {
			// return null;
			// }
			// int start = m.start() + lineRegion.getOffset();
			// int length = m.end() - m.start();
			// IRegion linkRegion = new Region(start, length);
			// for (QueryResult result : results)
			// {
			// Set<String> documents = result.getDocuments();
			// if (documents == null || documents.isEmpty())
			// {
			// continue;
			// }
			//
			// for (String filepath : documents)
			// {
			// // FIXME Don't suggest current file/occurrence
			// // FIXME Don't suggest usages in embedded CSS inside HTML
			// hyperlinks.add(new EditorSearchHyperlink(linkRegion, htmlId, new URI(filepath)));
			//
			// }
			// }
		}
		catch (Exception e)
		{
			RubyEditorPlugin.log(e);
		}
		if (hyperlinks.isEmpty())
		{
			return null;
		}
		return hyperlinks.toArray(new IHyperlink[hyperlinks.size()]);
	}

	private IHyperlink[] constNode(Node atOffset)
	{
		String constantName = ((INameNode) atOffset).getName();
		List<IRubyField> constants = findConstant(constantName);
		List<IRubyType> types = findType(constantName);
		// TODO Now generate hyperlinks pointing at them!
		return null;
	}

	private List<IRubyField> findConstant(String constantName)
	{
		List<IRubyField> constants = new ArrayList<IRubyField>();
		try
		{
			List<QueryResult> results = getIndex().query(new String[] { IRubyIndexConstants.CONSTANT_DECL },
					constantName, SearchPattern.EXACT_MATCH | SearchPattern.CASE_SENSITIVE);
			for (QueryResult result : results)
			{
				Set<String> docs = result.getDocuments();
				for (String doc : docs)
				{
					RubyScript root = parseURI(doc);
					if (root != null)
					{
						// FIXME Do this recursively!
						IRubyElement[] possible = root.getChildrenOfType(IRubyElement.CONSTANT);
						for (IRubyElement p : possible)
						{
							if (constantName.equals(p.getName()))
							{
								constants.add((IRubyField) p);
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

	private IHyperlink[] methodCallLink(CallNode callNode)
	{
		String methodName = callNode.getName();
		if ("new".equals(methodName)) //$NON-NLS-1$
		{
			Node receiver = callNode.getReceiverNode();
			// TODO Infer the type of the receiver...
			String typeName = null;
			List<IRubyType> types = findType(typeName);
			// TODO Now generate hyperlinks pointing at them!
		}
		else
		{
			List<IRubyMethod> methods = findMethods(methodName);
			// TODO Now generate hyperlinks pointing at them!
		}
		return null;
	}

	private List<IRubyMethod> findMethods(String methodName)
	{
		List<IRubyMethod> methods = new ArrayList<IRubyMethod>();
		try
		{
			List<QueryResult> results = getIndex().query(new String[] { IRubyIndexConstants.METHOD_DECL },
					methodName + IRubyIndexConstants.SEPARATOR,
					SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
			for (QueryResult result : results)
			{
				Set<String> docs = result.getDocuments();
				for (String doc : docs)
				{
					// TODO doc is an URI, parse it and traverse the AST to find the method!
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

	protected List<IRubyType> findType(String typeName)
	{
		List<IRubyType> types = new ArrayList<IRubyType>();
		try
		{
			List<QueryResult> results = getIndex().query(new String[] { IRubyIndexConstants.TYPE_DECL },
					typeName + IRubyIndexConstants.SEPARATOR + IRubyIndexConstants.SEPARATOR,
					SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
			for (QueryResult result : results)
			{
				Set<String> docs = result.getDocuments();
				for (String doc : docs)
				{
					// TODO doc is an URI, parse it and traverse the AST to find the type!
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
