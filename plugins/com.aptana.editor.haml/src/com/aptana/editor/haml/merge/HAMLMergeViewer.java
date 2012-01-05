/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml.merge;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.IExtendedPartitioner;
import com.aptana.editor.common.NullPartitionerSwitchStrategy;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.text.rules.NullSubPartitionScanner;
import com.aptana.editor.haml.HAMLEditor;
import com.aptana.editor.haml.HAMLSourceConfiguration;
import com.aptana.editor.haml.HAMLSourceViewerConfiguration;

/**
 * @author cwilliams
 */
class HAMLMergeViewer extends TextMergeViewer
{
	HAMLMergeViewer(Composite parent, CompareConfiguration configuration)
	{
		super(parent, configuration);
	}

	@Override
	protected IDocumentPartitioner getDocumentPartitioner()
	{
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HAMLSourceConfiguration.getDefault()
				.createSubPartitionScanner(), new NullSubPartitionScanner(), new NullPartitionerSwitchStrategy());
		IDocumentPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner, HAMLSourceConfiguration
				.getDefault().getContentTypes());
		partitionScanner.setPartitioner((IExtendedPartitioner) partitioner);
		return partitioner;
	}

	@Override
	protected String getDocumentPartitioning()
	{
		return IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer)
	{
		if (textViewer instanceof SourceViewer)
		{
			SourceViewer sourceViewer = (SourceViewer) textViewer;
			sourceViewer.unconfigure();
			IPreferenceStore preferences = HAMLEditor.getChainedPreferenceStore();
			HAMLSourceViewerConfiguration config = new HAMLSourceViewerConfiguration(preferences, null);
			sourceViewer.configure(config);
		}
	}
}
