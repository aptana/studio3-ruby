package com.aptana.editor.ruby.internal.text.rules;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.ruby.RubyCodeScanner;
import com.aptana.editor.ruby.RubyDocumentProvider;
import com.aptana.editor.ruby.internal.text.LineContinuationDamagerRepairer;

public class LineContinuationDamagerRepairerTest extends TestCase
{

	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testBug676() throws Exception
	{

		String code = "\"Just an example: %s %d\" \\\n% [1, 9000]";

		IDocument document = new Document(code);
		RubyDocumentProvider docProvider = new RubyDocumentProvider();
		IPartitioningConfiguration configuration = docProvider.getPartitioningConfiguration();
		IDocumentPartitioner partitioner = new FastPartitioner(docProvider.createPartitionScanner(),
				configuration.getContentTypes());
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		CommonEditorPlugin.getDefault().getDocumentScopeManager().registerConfiguration(document, configuration);

		LineContinuationDamagerRepairer dr = new LineContinuationDamagerRepairer(new RubyCodeScanner());
		dr.setDocument(document);
		DocumentEvent e = new DocumentEvent();
		e.fLength = 0;
		e.fOffset = 31;
		e.fDocument = document;
		e.fText = "2";
		e.fModificationStamp = System.currentTimeMillis();

		ITypedRegion partition = document.getPartition(31);
		IRegion damage = dr.getDamageRegion(partition, e, false);

		assertEquals(24, damage.getOffset());
		assertEquals(14, damage.getLength());
	}

}
