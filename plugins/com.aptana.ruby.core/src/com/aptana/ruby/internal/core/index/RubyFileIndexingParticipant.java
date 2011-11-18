/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.index;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserResult;

import com.aptana.index.core.AbstractFileIndexingParticipant;
import com.aptana.index.core.Index;
import com.aptana.index.core.build.BuildContext;
import com.aptana.ruby.core.ISourceElementRequestor;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.RubySourceParser;
import com.aptana.ruby.core.ast.SourceElementVisitor;

public class RubyFileIndexingParticipant extends AbstractFileIndexingParticipant
{
	public void index(BuildContext context, Index index, IProgressMonitor monitor) throws CoreException
	{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		try
		{
			sub.subTask(getIndexingMessage(index, context.getURI()));

			indexSource(index, context, context.getContents(), sub.newChild(100));
		}
		catch (Throwable e)
		{
			RubyCorePlugin.log(e);
		}
		finally
		{
			sub.done();
		}
	}

	public void indexSource(final Index index, BuildContext context, String source, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, 60);
		try
		{
			// FIXME Can we take the AST from the context and traverse that for indexing purposes?
			// Otherwise we're not re-using the parse!

			RubySourceParser sourceParser = new RubySourceParser(CompatVersion.BOTH);
			ParserResult result = sourceParser.parse(context.getName(), source);
			sub.worked(40);

			indexAST(index, context.getURI(), result.getAST(), sub.newChild(20));
		}
		finally
		{
			sub.done();
		}
	}

	private void indexAST(final Index index, URI uri, Node root, IProgressMonitor monitor)
	{
		ISourceElementRequestor builder = new RubySourceIndexer(index, uri);
		SourceElementVisitor visitor = new SourceElementVisitor(builder);
		visitor.acceptNode(root);
	}
}
