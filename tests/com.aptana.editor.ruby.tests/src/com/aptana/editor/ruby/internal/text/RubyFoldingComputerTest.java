package com.aptana.editor.ruby.internal.text;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.preferences.IPreferenceConstants;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ParseState;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.ruby.core.RubyParser;

public class RubyFoldingComputerTest extends TestCase
{

	private RubyFoldingComputer folder;

	@Override
	protected void tearDown() throws Exception
	{
		folder = null;
		super.tearDown();
	}

	protected void createFolder(String src)
	{
		folder = new RubyFoldingComputer(null, new Document(src))
		{
			protected IParseNode getAST()
			{
				IParseState parseState = new ParseState(getDocument().get(), 0);
				try
				{
					return parse(parseState);
				}
				catch (Exception e)
				{
					fail(e.getMessage());
				}
				return null;
			};
		};
	}

	public void testMultilineCommentFolding() throws Exception
	{
		// @formatter:off
		String src = 
				"=begin\n" +
				"  This is a comment.\n" +
				"=end\n";
		// @formatter:on
		createFolder(src);
		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		Collection<Position> positions = annotations.values();
		assertEquals("Incorrect number of folding points", 1, positions.size());
		assertTrue(positions.contains(new Position(0, src.length()))); // eats whole line at end
	}

	// public void testContiguousSinglelineCommentFolding() throws Exception
	// {
//		// @formatter:off
//		String src = 
//				"# First comment line.\n" +
//				"# Second comment line.\n" +
//				"# Third!\n";
//		// @formatter:on
	// createFolder(src);
	// Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
	// Collection<Position> positions = annotations.values();
	// assertEquals("Incorrect number of folding points", 1, positions.size());
	// assertTrue(positions.contains(new Position(0, src.length()))); // eats whole line at end
	// }

	public void testModuleFolding() throws Exception
	{
		// @formatter:off
		String src = 
				"module MyModule\n" +
				"  # Comment here\n" +
				"end\n";
		// @formatter:on
		createFolder(src);
		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		Collection<Position> positions = annotations.values();
		assertEquals("Incorrect number of folding points", 1, positions.size());
		assertTrue(positions.contains(new Position(0, src.length()))); // eats whole line at end
	}

	public void testClassFolding() throws Exception
	{
		// @formatter:off
		String src = 
				"class MyClass\n" +
				"  # This is a comment.\n" +
				"end\n";
		// @formatter:on
		createFolder(src);
		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		Collection<Position> positions = annotations.values();
		assertEquals("Incorrect number of folding points", 1, positions.size());
		assertTrue(positions.contains(new Position(0, src.length()))); // eats whole line at end
	}

	public void testMethodFolding() throws Exception
	{
		// @formatter:off
		String src = 
				"def method_name(arg)\n" +
				"  return 1\n" +
				"end\n";
		// @formatter:on
		createFolder(src);
		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		Collection<Position> positions = annotations.values();
		assertEquals("Incorrect number of folding points", 1, positions.size());
		assertTrue(positions.contains(new Position(0, src.length()))); // eats whole line at end
	}

	public void testCommentInitiallyFolded() throws Exception
	{
		// @formatter:off
		String src = 
				"=begin\n" +
				"  This is a comment.\n" +
				"=end\n" +
				"puts 'hello world'\n";
		// @formatter:on

		createFolder(src);
		// Turn on initially folding comments
		EclipseUtil.instanceScope().getNode(RubyEditorPlugin.PLUGIN_ID)
				.putBoolean(IPreferenceConstants.INITIALLY_FOLD_COMMENTS, true);

		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(true, new NullProgressMonitor());
		assertEquals("Wrong number of folding annotations", 1, annotations.size());
		assertTrue(annotations.keySet().iterator().next().isCollapsed());

		// After initial reconcile, don't mark any collapsed
		annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		assertFalse(annotations.keySet().iterator().next().isCollapsed());
	}

	public void testMethodInitiallyFolded() throws Exception
	{
		// @formatter:off
		String src = "def method(arg)\n" +
				"  puts 'hello'\n" +
				"end\n";
		// @formatter:on

		createFolder(src);
		// Turn on initially folding functions
		EclipseUtil.instanceScope().getNode(RubyEditorPlugin.PLUGIN_ID)
				.putBoolean(IPreferenceConstants.INITIALLY_FOLD_METHODS, true);

		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(true, new NullProgressMonitor());
		assertTrue(annotations.keySet().iterator().next().isCollapsed());

		// After initial reconcile, don't mark any collapsed
		annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		assertFalse(annotations.keySet().iterator().next().isCollapsed());
	}

	public void testInnerTypeInitiallyFolded() throws Exception
	{
		// @formatter:off
		String src =
				"module Namespace\n" +
				"  class Inner\n" +
				"  end\n" +
				"end\n";
		// @formatter:on

		createFolder(src);
		// Turn on initially folding arrays
		EclipseUtil.instanceScope().getNode(RubyEditorPlugin.PLUGIN_ID)
				.putBoolean(IPreferenceConstants.INITIALLY_FOLD_INNER_TYPES, true);

		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(true, new NullProgressMonitor());
		assertEquals("Wrong number of folding annotations", 2, annotations.size());
		// Grab the inner type folding...
		ProjectionAnnotation annotation = getByPosition(annotations, new Position(19, 18));
		assertTrue(annotation.isCollapsed());

		// After initial reconcile, don't mark any collapsed
		annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		annotation = getByPosition(annotations, new Position(19, 18));
		assertFalse(annotation.isCollapsed());
	}

	public void testBlockInitiallyFolded() throws Exception
	{
		// @formatter:off
		String src = 
				"[1, 2, 3].each do |i|\n" +
				"  puts i\n" +
				"end\n";
		// @formatter:on

		createFolder(src);
		// Turn on initially folding objects
		EclipseUtil.instanceScope().getNode(RubyEditorPlugin.PLUGIN_ID)
				.putBoolean(IPreferenceConstants.INITIALLY_FOLD_BLOCKS, true);

		Map<ProjectionAnnotation, Position> annotations = folder.emitFoldingRegions(true, new NullProgressMonitor());
		assertTrue(annotations.keySet().iterator().next().isCollapsed());

		// After initial reconcile, don't mark any collapsed
		annotations = folder.emitFoldingRegions(false, new NullProgressMonitor());
		assertFalse(annotations.keySet().iterator().next().isCollapsed());
	}

	private ProjectionAnnotation getByPosition(Map<ProjectionAnnotation, Position> annotations, Position position)
	{
		for (Map.Entry<ProjectionAnnotation, Position> entry : annotations.entrySet())
		{
			if (entry.getValue().equals(position))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	private IParseRootNode parse(IParseState parseState) throws Exception
	{
		return new RubyParser().parse(parseState).getRootNode();
	}
}
