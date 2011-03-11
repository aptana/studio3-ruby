package com.aptana.editor.sass;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public class SassFoldingComputerTest extends TestCase
{

	public void testFolding() throws Exception
	{
		IDocument document = new Document("blue: #3bbfce\n" +
			"$margin: 16px\n" +
			"\n" +
			".content-navigation\n" +
			"  border-color: $blue\n" +
			"  color: darken($blue, 9%)\n" +
			"\n" +
			".border\n" +
			"  padding: $margin / 2\n" +
			"  margin: $margin / 2\n" +
			"  border-color: $blue");
		SassFoldingComputer sfc = new SassFoldingComputer(document);
		List<Position> positions = sfc.emitFoldingRegions(null);

		assertNotNull(positions);
		assertEquals(2, positions.size());

		Position p = positions.get(0);
		assertEquals(29, p.getOffset());
		assertEquals(69, p.getLength());

		p = positions.get(1);
		assertEquals(99, p.getOffset());
		assertEquals(74, p.getLength());
	}
}
