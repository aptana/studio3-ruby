/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.index;

import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserResult;

import com.aptana.core.resources.TaskTag;
import com.aptana.core.util.IOUtil;
import com.aptana.index.core.AbstractFileIndexingParticipant;
import com.aptana.index.core.Index;
import com.aptana.parsing.IParserPool;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.ISourceElementRequestor;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.RubyParser;
import com.aptana.ruby.core.RubySourceParser;
import com.aptana.ruby.core.ast.SourceElementVisitor;

public class RubyFileIndexingParticipant extends AbstractFileIndexingParticipant
{
	/*
	 * (non-Javadoc)
	 * @see com.aptana.index.core.AbstractFileIndexingParticipant#indexFileStore(com.aptana.index.core.Index,
	 * org.eclipse.core.filesystem.IFileStore, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void indexFileStore(final Index index, IFileStore store, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		try
		{
			if (store == null)
			{
				return;
			}

			sub.subTask(getIndexingMessage(index, store));

			removeTasks(store, sub.newChild(10));

			// grab the source of the file we're going to parse
			String source = IOUtil.read(store.openInputStream(EFS.NONE, sub.newChild(20)));

			// minor optimization when creating a new empty file
			if (source == null || source.length() <= 0)
			{
				return;
			}

			indexSource(index, source, store, sub.newChild(70));
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

	public void indexSource(final Index index, String source, IFileStore store, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, 70);
		try
		{
			// create parser and associated parse state
			IParserPool pool = ParserPoolFactory.getInstance().getParserPool(IRubyConstants.CONTENT_TYPE_RUBY);
			if (pool == null)
			{
				return;
			}
			RubyParser parser = (RubyParser) pool.checkOut();

			RubySourceParser sourceParser = parser.getSourceParser(CompatVersion.BOTH);
			ParserResult result = sourceParser.parse(store.getName(), source);

			pool.checkIn(parser);
			sub.worked(40);

			Node root = result.getAST();
			ISourceElementRequestor builder = new RubySourceIndexer(index, store.toURI());
			SourceElementVisitor visitor = new SourceElementVisitor(builder);
			visitor.acceptNode(root);
			sub.worked(20);
			detectTasks(store, result.getCommentNodes(), sub.newChild(10));
		}
		finally
		{
			sub.done();
		}
	}

	private void detectTasks(IFileStore store, List<CommentNode> comments, IProgressMonitor monitor)
	{
		if (comments == null || comments.isEmpty())
		{
			return;
		}

		SubMonitor sub = SubMonitor.convert(monitor, comments.size());
		for (CommentNode commentNode : comments)
		{
			String text = commentNode.getContent();
			int offset = 0;
			int lineOffset = 0;
			if (!TaskTag.isCaseSensitive())
			{
				text = text.toLowerCase();
			}
			String[] lines = text.split("\r\n|\r|\n"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
			for (String line : lines)
			{
				for (TaskTag entry : TaskTag.getTaskTags())
				{
					String tag = entry.getName();
					if (!TaskTag.isCaseSensitive())
					{
						tag = tag.toLowerCase();
					}
					int index = line.indexOf(tag);
					if (index == -1)
					{
						continue;
					}

					String message = line.substring(index).trim();
					int start = commentNode.getPosition().getStartOffset() + offset + index;
					createTask(store, message, entry.getPriority(), commentNode.getPosition().getStartLine()
							+ lineOffset, start, start + message.length());
				}
				// FIXME This doesn't take the newline into account from split!
				offset += line.length();
				lineOffset++;
			}

			sub.worked(1);
		}
		sub.done();
	}
}
