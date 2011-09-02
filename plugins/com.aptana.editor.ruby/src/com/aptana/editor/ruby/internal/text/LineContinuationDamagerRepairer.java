/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.internal.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.ITokenScanner;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.text.rules.ThemeingDamagerRepairer;
import com.aptana.editor.ruby.RubyEditorPlugin;

/**
 * Expands the damage region to the previous line if it ends with '\'.
 * 
 * @author cwilliams
 */
public class LineContinuationDamagerRepairer extends ThemeingDamagerRepairer
{

	public LineContinuationDamagerRepairer(ITokenScanner scanner)
	{
		super(scanner);
	}

	@Override
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged)
	{
		IRegion region = super.getDamageRegion(partition, e, documentPartitioningChanged);
		// We need to expand damage region based on end of previous line, since we may be continuing an
		// expression!
		if (!documentPartitioningChanged)
		{
			try
			{
				int line = fDocument.getLineOfOffset(e.getOffset());
				if (line >= 1)
				{
					// check to see if previous line ends with slash, if so, we need to expand back to include
					// previous line (or partition end).
					// FIXME Recursively expand back...
					IRegion info = fDocument.getLineInformation(line - 1);
					int start = Math.max(partition.getOffset(), info.getOffset());
					int end = info.getOffset() + info.getLength();
					int length = end - start;
					if (length > 0)
					{
						String previousLine = fDocument.get(start, length);
						if (previousLine.endsWith("\\")) //$NON-NLS-1$
						{
							return new Region(start, region.getLength() + (length + 1));
						}
					}
				}
			}
			catch (BadLocationException e1)
			{
				IdeLog.logError(RubyEditorPlugin.getDefault(),
						"Unable to check previous line for '\\' continuation, offset: " + e.getOffset(), e1); //$NON-NLS-1$
			}
		}
		return region;
	}

}
