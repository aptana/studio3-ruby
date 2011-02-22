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

import com.aptana.editor.common.text.reconciler.IFoldingComputer;

public class SassFoldingComputer implements IFoldingComputer
{

	private IDocument document;

	public SassFoldingComputer(IDocument document)
	{
		this.document = document;
	}

	public List<Position> emitFoldingRegions(IProgressMonitor monitor)
	{
		int lineCount = getDocument().getNumberOfLines();
		if (lineCount <= 1)
		{
			return Collections.emptyList();
		}

		List<Position> positions = new ArrayList<Position>();
		SubMonitor sub = SubMonitor.convert(monitor, lineCount);
		try
		{
			int lineNum = 0;
			int fLastIndent = -1;
			IRegion fLastLineRegion = new Region(0, 0);
			Map<Integer, Integer> starts = new HashMap<Integer, Integer>();

			// Iterate over lines of the document
			String src = getDocument().get();
			String[] lines = src.split("(\r)?\n|\r"); //$NON-NLS-1$
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
							positions.add(new Position(entry.getValue(), (fLastLineRegion.getOffset()
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
				positions.add(new Position(entry.getValue(), getDocument().getLength() - entry.getValue()));
			}
		}
		catch (BadLocationException e)
		{
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
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
		for (int i = 0; i < line.length(); i++)
		{
			// FIXME Handle tabs as being one indent, X spaces as one indent!
			if (Character.isWhitespace(line.charAt(i)))
			{
				size++;
			}
			else
			{
				break;
			}
		}
		return size;
	}

	protected IDocument getDocument()
	{
		return document;
	}

}
