/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.codeassist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.Node;
import org.jrubyparser.lexer.SyntaxException;
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

	public synchronized Node getAST() throws SyntaxException
	{
		if (root == null)
		{
			Parser parser = new Parser();
			// TODO Handle fixing common syntax errors as we do in ruble for CA!
			Reader reader = new BufferedReader(new StringReader(source));
			try
			{
				root = parser.parse(getFileName(), reader, new ParserConfiguration(0, CompatVersion.BOTH));
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
		}
		return root;
	}

	public synchronized Node getSelectedNode() throws SyntaxException
	{
		if (atOffset == null)
		{
			try
			{
				atOffset = new OffsetNodeLocator().find(getAST(), offset);
			}
			catch (SyntaxException se)
			{
				// ignore
			}
		}
		return atOffset;
	}

	public URI getURI()
	{
		return this.uri;
	}

	private String getFileName()
	{
		return uri.getPath();
	}

	public List<ResolutionTarget> getResolved()
	{
		if (resolved == null || resolved.isEmpty())
		{
			return Collections.emptyList();
		}
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
