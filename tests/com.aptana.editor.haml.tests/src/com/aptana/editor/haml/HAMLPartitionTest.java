package com.aptana.editor.haml;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.IExtendedPartitioner;
import com.aptana.editor.common.NullPartitionerSwitchStrategy;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.text.rules.NullSubPartitionScanner;
import com.aptana.editor.ruby.RubySourceConfiguration;

@SuppressWarnings("nls")
public class HAMLPartitionTest
{

	@Before
	public void setUp() throws Exception
	{
//		super.setUp();
	}

	@After
	public void tearDown() throws Exception
	{
//		super.tearDown();
	}

	@Test
	public void testPlainText() throws Exception
	{
		String src = "%gee\n" + //
				"  %whiz\n" + //
				"    Wow this is cool!\n";

		ITypedRegion[] partitions = partition(src);
		assertEquals(4, partitions.length);

		assertPartition(partitions[0], 0, 4, HAMLSourceConfiguration.ELEMENT);
		assertPartition(partitions[1], 4, 1, HAMLSourceConfiguration.DEFAULT);
		assertPartition(partitions[2], 5, 7, HAMLSourceConfiguration.ELEMENT);
		assertPartition(partitions[3], 12, 23, HAMLSourceConfiguration.DEFAULT);
	}

	@Test
	public void testInsertingRubyIndented() throws Exception
	{
		String src = "%p\n" + //
				"  = \"yo\"\n";

		ITypedRegion[] partitions = partition(src);
		assertEquals(6, partitions.length);

		// %p
		assertPartition(partitions[0], 0, 2, HAMLSourceConfiguration.ELEMENT);
		// \n
		assertPartition(partitions[1], 2, 1, HAMLSourceConfiguration.DEFAULT);
		// =
		assertPartition(partitions[2], 3, 3, HAMLSourceConfiguration.RUBY_EVALUATION);
		//
		assertPartition(partitions[3], 6, 1, RubySourceConfiguration.DEFAULT);
		// "yo"
		assertPartition(partitions[4], 7, 4, RubySourceConfiguration.STRING_DOUBLE);
		assertPartition(partitions[5], 11, 1, HAMLSourceConfiguration.DEFAULT);
	}

	@Test
	public void testInsertingRubyAtEndOfTag() throws Exception
	{
		String src = "%p= \"hello\"\n";

		ITypedRegion[] partitions = partition(src);
		assertEquals(5, partitions.length);

		// %p
		assertPartition(partitions[0], 0, 2, HAMLSourceConfiguration.ELEMENT);
		// =
		assertPartition(partitions[1], 2, 1, HAMLSourceConfiguration.RUBY_EVALUATION);
		//
		assertPartition(partitions[2], 3, 1, RubySourceConfiguration.DEFAULT);
		// "hello"
		assertPartition(partitions[3], 4, 7, RubySourceConfiguration.STRING_DOUBLE);
		assertPartition(partitions[4], 11, 1, HAMLSourceConfiguration.DEFAULT);
	}

	@Test
	public void testInsertingRubyAcrossMultipleLines() throws Exception
	{
		String src = "= link_to_remote \"Add to cart\",\n" + //
				"    :url => { :action => \"add\", :id => product.id },\n" + //
				"    :update => { :success => \"cart\", :failure => \"error\" }";

		ITypedRegion[] partitions = partition(src);
		assertEquals(10, partitions.length);

		// =
		assertPartition(partitions[0], 0, 1, HAMLSourceConfiguration.RUBY_EVALUATION);
		// link_to_remote
		assertPartition(partitions[1], 1, 16, RubySourceConfiguration.DEFAULT);
		// "Add to cart"
		assertPartition(partitions[2], 17, 13, RubySourceConfiguration.STRING_DOUBLE);
		// , :url => ...
		assertPartition(partitions[3], 30, 27, RubySourceConfiguration.DEFAULT);
		// "add"
		assertPartition(partitions[4], 57, 5, RubySourceConfiguration.STRING_DOUBLE);
		// , :id => product.id...
		assertPartition(partitions[5], 62, 52, RubySourceConfiguration.DEFAULT);
		// "cart"
		assertPartition(partitions[6], 114, 6, RubySourceConfiguration.STRING_DOUBLE);
		// , :failure =>
		assertPartition(partitions[7], 120, 14, RubySourceConfiguration.DEFAULT);
		// "error"
		assertPartition(partitions[8], 134, 7, RubySourceConfiguration.STRING_DOUBLE);
		// }
		assertPartition(partitions[9], 141, 2, RubySourceConfiguration.DEFAULT);
	}

	protected ITypedRegion[] partition(String src) throws BadLocationException
	{
		return partition(src, 0, src.length());
	}

	protected ITypedRegion[] partition(String src, int offset, int length) throws BadLocationException
	{
		IDocument document = new Document(src);
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HAMLSourceConfiguration.getDefault()
				.createSubPartitionScanner(), new NullSubPartitionScanner(), new NullPartitionerSwitchStrategy());
		IDocumentPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner, HAMLSourceConfiguration
				.getDefault().getContentTypes());
		partitionScanner.setPartitioner((IExtendedPartitioner) partitioner);
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);

		return document.computePartitioning(offset, length);
	}

	protected void assertPartition(ITypedRegion partition, int offset, int length, String type)
	{
		assertEquals("Offset doesn't match", offset, partition.getOffset());
		assertEquals("Length doesn't match", length, partition.getLength());
		assertEquals("Type doesn't match", type, partition.getType());
	}
}
