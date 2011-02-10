package org.radrails.rails.internal.ui.hyperlink;

import junit.framework.TestCase;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class RenderPathHyperlinkDetectorTest extends TestCase
{

	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	// TODO Add tests for the render path hyperlinks!

	public void testGeneratorCreatedWithModeOutput() throws Exception
	{
		RenderPathHyperlinkDetector detector = new RenderPathHyperlinkDetector();
		IHyperlink[] links = detector.detectHyperlinks(" create mode 100644 active_support/test/file_watcher_test.rb");
		assertNotNull(links);
		assertEquals(1, links.length);
		IRegion region = links[0].getHyperlinkRegion();
		assertEquals(20, region.getOffset());
		assertEquals(40, region.getLength());
	}

	public void testGeneratorCreateOutput() throws Exception
	{
		RenderPathHyperlinkDetector detector = new RenderPathHyperlinkDetector();
		IHyperlink[] links = detector.detectHyperlinks("	create  lib/generators/initializer/USAGE");
		assertNotNull(links);
		assertEquals(1, links.length);
		IRegion region = links[0].getHyperlinkRegion();
		assertEquals(9, region.getOffset());
		assertEquals(32, region.getLength());
	}

	public void testGeneratorExistsOutput() throws Exception
	{
		RenderPathHyperlinkDetector detector = new RenderPathHyperlinkDetector();
		IHyperlink[] links = detector.detectHyperlinks("      exists  db/migrate");
		assertNotNull(links);
		assertEquals(1, links.length);
		IRegion region = links[0].getHyperlinkRegion();
		assertEquals(14, region.getOffset());
		assertEquals(10, region.getLength());
	}

	public void testGeneratorIdenticalOutput() throws Exception
	{
		RenderPathHyperlinkDetector detector = new RenderPathHyperlinkDetector();
		IHyperlink[] links = detector.detectHyperlinks("      identical  lib/generators/initializer/USAGE");
		assertNotNull(links);
		assertEquals(1, links.length);
		IRegion region = links[0].getHyperlinkRegion();
		assertEquals(17, region.getOffset());
		assertEquals(32, region.getLength());
	}

}
