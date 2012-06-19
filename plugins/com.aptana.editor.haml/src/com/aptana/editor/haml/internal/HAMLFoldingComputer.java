/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.editor.common.text.reconciler.Messages;
import com.aptana.parsing.ast.IParseRootNode;

public class HAMLFoldingComputer implements IFoldingComputer
{

	private IDocument fDocument;
	private AbstractThemeableEditor fEditor;

	public HAMLFoldingComputer(AbstractThemeableEditor editor, IDocument document)
	{
		this.fDocument = document;
		this.fEditor = editor;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.text.reconciler.IFoldingComputer#emitFoldingRegions(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	public Map<ProjectionAnnotation, Position> emitFoldingRegions(boolean initialReconcile, IProgressMonitor monitor,
			IParseRootNode ast) throws BadLocationException
	{
		int lineCount = fDocument.getNumberOfLines();
		SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.CommonReconcilingStrategy_FoldingTaskName,
				lineCount);
		if (lineCount <= 1) // Quick hack fix for minified files. We need at least two lines to have folding!
		{
			return Collections.emptyMap();
		}

		// using shift operator to do a faster "divide by 4"
		Map<ProjectionAnnotation, Position> newPositions = new HashMap<ProjectionAnnotation, Position>(lineCount >> 2);
		Stack<Integer> indentLevels = new Stack<Integer>();
		indentLevels.push(0);
		Map<Integer, Integer> starts = new HashMap<Integer, Integer>(3);
		for (int currentLine = 0; currentLine < lineCount; currentLine++)
		{
			// Check for cancellation
			if (subMonitor.isCanceled())
			{
				return newPositions;
			}

			IRegion lineRegion = fDocument.getLineInformation(currentLine);
			int offset = lineRegion.getOffset();
			String line = fDocument.get(offset, lineRegion.getLength());
			if (line.trim().length() == 0)
			{
				// ignore blank lines?
				continue;
			}

			// Every new indent level is a possible folding start
			int indent = findIndent(line);
			if (!indentLevels.isEmpty())
			{
				int peekedIndent = indentLevels.peek();
				if (indent > peekedIndent)
				{
					// indent increased, might be a new folding start, add it
					indentLevels.push(indent);
					starts.put(indent, offset);
				}
				else if (indent == peekedIndent)
				{
					// same indent level, update folding offset for this indent level (multiple lines at same level)
					starts.put(indent, offset);
				}
				else if (indent < peekedIndent)
				{
					// indent level decreased, close all levels greater than current indent...
					while (!indentLevels.isEmpty() && indent <= indentLevels.peek())
					{
						int toPop = indentLevels.pop();
						if (!starts.containsKey(toPop))
						{
							continue;
						}
						int startingOffset = starts.remove(toPop);
						IRegion startLine = fDocument.getLineInformationOfOffset(startingOffset);
						IRegion endLine = fDocument.getLineInformation(currentLine - 1);
						if (startLine.getOffset() == endLine.getOffset())
						{
							continue;
						}
						int end = endLine.getOffset() + endLine.getLength() + 1;
						int posLength = end - startingOffset;
						if (posLength > 0)
						{
							Position position = new Position(startingOffset, posLength);
							newPositions.put(new ProjectionAnnotation(), position);
						}
					}
					starts.put(indent, offset);
				}
			}

			subMonitor.worked(1);
		}

		// Close all open starts at the end of the document!
		int documentEnd = fDocument.getLength();
		IRegion endLine = fDocument.getLineInformationOfOffset(documentEnd);
		for (Integer startingOffset : starts.values())
		{
			IRegion startLine = fDocument.getLineInformationOfOffset(startingOffset);
			if (startLine.getOffset() == endLine.getOffset())
			{
				continue;
			}
			int posLength = documentEnd - startingOffset;
			if (posLength > 0)
			{
				Position position = new Position(startingOffset, posLength);
				newPositions.put(new ProjectionAnnotation(), position);
			}
		}

		subMonitor.done();
		return newPositions;
	}

	private int findIndent(String text)
	{
		int indent = 0;
		while (indent < text.length())
		{
			char c = text.charAt(indent);
			if (c == '\t')
			{
				indent += getTabSize();
				continue;
			}
			if (!Character.isWhitespace(c))
				break;
			indent++;
		}

		return indent;
	}

	protected int getTabSize()
	{
		return fEditor.getTabSize();
	}

}
