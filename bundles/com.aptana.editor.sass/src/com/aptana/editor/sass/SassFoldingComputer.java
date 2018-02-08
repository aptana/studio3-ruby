/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.parsing.ast.IParseRootNode;

public class SassFoldingComputer implements IFoldingComputer
{

	private IDocument document;

	public SassFoldingComputer(IDocument document)
	{
		this.document = document;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.text.reconciler.IFoldingComputer#emitFoldingRegions(boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Map<ProjectionAnnotation, Position> emitFoldingRegions(boolean initialReconcile, IProgressMonitor monitor,
			IParseRootNode ast)
	{
		int lineCount = getDocument().getNumberOfLines();
		if (lineCount <= 1)
		{
			return Collections.emptyMap();
		}

		Map<ProjectionAnnotation, Position> positions = new HashMap<ProjectionAnnotation, Position>();
		SubMonitor sub = SubMonitor.convert(monitor, lineCount);
		try
		{
			int lineNum = 0;
			int fLastIndent = -1;
			IRegion fLastLineRegion = new Region(0, 0);
			Map<Integer, Integer> starts = new HashMap<Integer, Integer>();

			// Iterate over lines of the document
			String src = getDocument().get();
			String[] lines = src.split("\r?\n|\r"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
			for (String line : lines)
			{
				if (sub.isCanceled())
				{
					return positions;
				}
				int indent = getIndentLevel(line);
				IRegion lineRegion = getDocument().getLineInformation(lineNum);
				if (fLastIndent == -1)
				{
					starts.put(0, 0);
				}
				// If the indent increased here, then last line was start of a folding block
				else if (indent > fLastIndent)
				{
					starts.put(fLastIndent, fLastLineRegion.getOffset());
				}
				// Indent shrank, so last line was end of folding block
				else if (indent < fLastIndent)
				{
					List<Integer> toRemove = new ArrayList<Integer>();
					// Any start with an indent greater than "indent" is now closed
					for (Map.Entry<Integer, Integer> entry : starts.entrySet())
					{
						if (entry.getKey() >= indent)
						{
							positions.put(
									new ProjectionAnnotation(),
									new Position(entry.getValue(), (fLastLineRegion.getOffset()
											+ fLastLineRegion.getLength() + 1)
											- entry.getValue()));
							toRemove.add(entry.getKey());
						}
					}
					for (Integer item : toRemove)
					{
						starts.remove(item);
					}
				}
				fLastIndent = indent;
				fLastLineRegion = lineRegion;
				lineNum++;
				sub.worked(1);
			}
			// Do we have any leftover opens? Close them!
			for (Map.Entry<Integer, Integer> entry : starts.entrySet())
			{
				positions.put(new ProjectionAnnotation(), new Position(entry.getValue(), getDocument().getLength()
						- entry.getValue()));
			}
		}
		catch (BadLocationException e)
		{
			SassPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, SassPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		finally
		{
			if (sub != null)
			{
				sub.done();
			}
		}
		return positions;
	}

	private int getIndentLevel(String line)
	{
		int size = 0;
		if (line == null)
		{
			return size;
		}
		int spaces = 0;
		for (int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if (c == ' ')
			{
				spaces++;
			}
			else if (c == '\t')
			{
				size++;
			}
			else
			{
				break;
			}
		}
		// TODO check prefs for determining width of indent. Assume 2 for now.
		return size + (spaces >> 1); // shift operator to divide by 2
	}

	protected IDocument getDocument()
	{
		return document;
	}

}
