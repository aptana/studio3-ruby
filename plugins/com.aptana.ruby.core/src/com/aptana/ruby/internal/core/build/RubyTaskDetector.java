/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.aptana.core.build.AbstractBuildParticipant;
import com.aptana.core.build.IProblem;
import com.aptana.core.logging.IdeLog;
import com.aptana.index.core.build.BuildContext;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.internal.core.RubyComment;

/**
 * Detects task markers in ruby comments.
 * 
 * @author cwilliams
 */
public class RubyTaskDetector extends AbstractBuildParticipant
{

	public void clean(IProject project, IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
	}

	public void buildFile(BuildContext context, IProgressMonitor monitor)
	{
		Collection<IProblem> tasks = detectTasks(context, monitor);
		context.putProblems(IMarker.TASK, tasks);
	}

	public void deleteFile(BuildContext context, IProgressMonitor monitor)
	{
		context.removeProblems(IMarker.TASK);
	}

	private Collection<IProblem> detectTasks(BuildContext context, IProgressMonitor monitor)
	{
		Collection<IProblem> tasks = new ArrayList<IProblem>();

		try
		{
			IParseRootNode rootNode = context.getAST();
			IParseNode[] comments = rootNode.getCommentNodes();
			if (comments == null || comments.length == 0)
			{
				return Collections.emptyList();
			}

			SubMonitor sub = SubMonitor.convert(monitor, comments.length);
			String source = context.getContents();
			String filePath = context.getURI().toString();
			for (IParseNode commentNode : comments)
			{
				if (commentNode instanceof RubyComment)
				{
					tasks.addAll(processCommentNode(filePath, source, 0, commentNode, "=end")); //$NON-NLS-1$
				}
				sub.worked(1);
			}
			sub.done();
		}
		catch (CoreException e)
		{
			IdeLog.logError(RubyCorePlugin.getDefault(), e);
		}
		return tasks;
	}
}
