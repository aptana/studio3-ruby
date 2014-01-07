package com.aptana.editor.sass;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

public class SassFoldingComputerTest
{

	@Test
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
		Map<ProjectionAnnotation, Position> annotations = sfc.emitFoldingRegions(false, null, null);
		assertNotNull(annotations);

		Collection<Position> positions = annotations.values();
		assertEquals(2, positions.size());

		assertTrue(positions.contains(new Position(29, 69)));
		assertTrue(positions.contains(new Position(99, 74)));
	}
}
