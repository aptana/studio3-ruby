package com.aptana.ruby.core.codeassist;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserConfiguration;

import com.aptana.ruby.core.ast.OffsetNodeLocator;

public class ResolveContext
{

	private URI uri;
	private String source;
	private int offset;
	private List<ResolutionTarget> resolved;
	private Node root;
	private Node atOffset;

	public ResolveContext(URI uri, String source, int offset)
	{
		this.uri = uri;
		this.source = source;
		this.offset = offset;
	}

	public synchronized Node getAST()
	{
		if (root == null)
		{
			Parser parser = new Parser();
			// TODO Handle fixing common syntax errors as we do in ruble for CA!
			root = parser
					.parse(getFileName(), new StringReader(source), new ParserConfiguration(0, CompatVersion.BOTH));
		}
		return root;
	}

	public synchronized Node getSelectedNode()
	{
		if (atOffset == null)
		{
			atOffset = new OffsetNodeLocator().find(getAST(), offset);
		}
		return atOffset;
	}

	public URI getURI()
	{
		return this.uri;
	}

	private String getFileName()
	{
		// TODO Return name from uri?
		return "";
	}

	public List<ResolutionTarget> getResolved()
	{
		// TODO Sort/prioritize these based on their location? confidence level?
		return Collections.unmodifiableList(resolved);
	}

	public synchronized void addResolved(Collection<ResolutionTarget> targets)
	{
		if (this.resolved == null)
		{
			this.resolved = new ArrayList<ResolutionTarget>(targets);
		}
		else
		{
			this.resolved.addAll(targets);
		}

	}
}
