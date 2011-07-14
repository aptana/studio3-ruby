package com.aptana.editor.ruby.internal.contentassist;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.aptana.editor.ruby.RubySourceEditor;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexManager;
import com.aptana.ruby.core.index.IRubyIndexConstants;

public class RubyContentAssistProcessorTest extends RubyContentAssistTestCase
{
	private List<Index> indicesforTesting;

	protected RubyContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor)
	{
		return new RubyContentAssistProcessor(editor)
		{
			@Override
			protected Collection<? extends ICompletionProposal> suggestWordCompletions(ITextViewer viewer, int offset)
			{
				// For test purposes, ignore word completions!
				return new ArrayList<ICompletionProposal>();
			}

			@Override
			protected Collection<Index> allIndicesForProject()
			{
				return indicesforTesting;
			}
		};
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		indicesforTesting = new ArrayList<Index>();
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{
			if (indicesforTesting == null)
			{
				indicesforTesting.clear();
				indicesforTesting = null;
			}
		}
		finally
		{
			super.tearDown();
		}
	}

	public void testDefKeyword() throws Exception
	{
		assertCompletionCorrect("de", 2, "def", "def"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testClassKeyword() throws Exception
	{
		assertCompletionCorrect("cla", 3, "class", "class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testModuleKeyword() throws Exception
	{
		assertCompletionCorrect("modu", 4, "module", "module"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testAliasKeyword() throws Exception
	{
		assertCompletionCorrect("alia", 4, "alias", "alias"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testEndKeyword() throws Exception
	{
		assertCompletionCorrect("def chris; en", 13, "end", "def chris; end"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testAndKeyword() throws Exception
	{
		assertCompletionCorrect("true an false", 7, "and", "true and false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testOrKeyword() throws Exception
	{
		assertCompletionCorrect("true o false", 6, "or", "true or false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testIfKeyword() throws Exception
	{
		assertCompletionCorrect("puts 'hello' i", 14, "if", "puts 'hello' if"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testUnlessKeyword() throws Exception
	{
		assertCompletionCorrect("puts 'hello' un", 15, "unless", "puts 'hello' unless"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testFalseKeyword() throws Exception
	{
		assertCompletionCorrect("puts 'hello' unless fa", 22, "false", "puts 'hello' unless false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testTrueKeyword() throws Exception
	{
		assertCompletionCorrect("puts 'hello' unless tr", 22, "true", "puts 'hello' unless true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSuperKeyword() throws Exception
	{
		assertCompletionCorrect(
				"class Chris\n  def initialize\n    su\n  end\nend", 35, "super", "class Chris\n  def initialize\n    super\n  end\nend"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	// TODO Add tests for the rest of the keywords!
	// BEGIN
	// begin
	// break
	// case
	// defined
	// do
	// else
	// elsif
	// END
	// ensure
	// for
	// in
	// next
	// nil
	// not
	// or
	// redo
	// rescue
	// retry
	// return
	// self
	// then
	// undef
	// until
	// when
	// while
	// yield

	// public void testExplicitKernelPuts() throws Exception
	// {
	//		assertCompletionCorrect("Kernel.pu", 9, "puts", "Kernel.puts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	// public void testImplicitKernelPuts() throws Exception
	// {
	// FIXME This assumes that the core index stuff is all hooked up!
	//		assertCompletionCorrect("pu", 2, "puts", "puts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	// public void testKernelMethodsImmediatelyAfterPeriod() throws Exception
	// {
	//		assertCompletionCorrect("Kernel.", 7, "puts", "Kernel.puts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	// public void testEvenQueryMethodOnFixnumLiteral() throws Exception
	// {
	//		assertCompletionCorrect("1.eve", 5, "even?", "1.even?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }
	//
	// public void testEvenQueryMethodOnFixnumVariable() throws Exception
	// {
	//		assertCompletionCorrect("var = 1\nvar.eve", 15, "even?", "var = 1\nvar.even?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	public void testInstanceVariablePreviouslyDeclaredNoSyntaxErrors() throws Exception
	{
		assertCompletionCorrect(
				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @c\n  end\nend", 74, "@counter", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @counter\n  end\nend"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testInstanceVariablePreviouslyDeclaredJustAtSigilPrefix() throws Exception
	{
		assertCompletionCorrect(
				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @\n  end\nend", 73, "@counter", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @counter\n  end\nend"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testAPSTUD2903() throws Exception
	{
		String src = "class Shapes\n" + //
				"  @@shapes = [ :triangle, :rectangle, :circle ]\n" + //
				"  @shape = :tri\n" + //
				"end\n"; //
		assertCompletionCorrect(src, 76, ":triangle", //
				"class Shapes\n" + //
						"  @@shapes = [ :triangle, :rectangle, :circle ]\n" + //
						"  @shape = :triangle\n" + //
						"end\n");
	}

	public void testSymbolsAfterSingleColonBreakingSyntax() throws Exception
	{
		String src = "class Shapes\n" + //
				"  @@shapes = [ :triangle, :rectangle, :circle ]\n" + //
				"  @shape = :\n" + //
				"end\n"; //
		assertCompletionCorrect(src, 73, ":triangle", //
				"class Shapes\n" + //
						"  @@shapes = [ :triangle, :rectangle, :circle ]\n" + //
						"  @shape = :triangle\n" + //
						"end\n");
	}

	public void testSymbolsAfterSingleColonBreakingSyntaxInsideHash() throws Exception
	{
		String src = "class Shapes\n" + //
				"  @@shapes = [ :triangle, :rectangle, :circle ]\n" + //
				"  @shape = {:}\n" + //
				"end\n"; //
		assertCompletionCorrect(src, 74, ":triangle", //
				"class Shapes\n" + //
						"  @@shapes = [ :triangle, :rectangle, :circle ]\n" + //
						"  @shape = {:triangle}\n" + //
						"end\n");
	}

	public void testDoesntSuggestLocalsNotMatchingPrefix() throws Exception
	{
		String src = "def run(test)\n" + //
				"  if test.nil?\n" + //
				"    pu\n" + //
				"  end\n" + //
				"end\n"; //

		ICompletionProposal[] proposals = computeProposals(src, 35);
		assertDoesntContain(proposals, "run", "test");
	}

	public void testSuggestsKernelMethodsInTopLevel() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add a fake entry for Kernel.puts
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "puts/Kernel//P/S/1", new URI("kernel.rb"));
		indicesforTesting.add(testIndex);

		String src = "pu";

		assertCompletionCorrect(src, 2, "puts", "puts");
	}

	protected Index getTestIndex()
	{
		// generate a tmp dir URI location for our fake index...
		String tmpDir = System.getProperty("java.io.tmpdir");
		File caIndexDir = new File(tmpDir, "ruby_ca_core" + System.currentTimeMillis());
		caIndexDir.deleteOnExit();
		return IndexManager.getInstance().getIndex(caIndexDir.toURI());
	}

	public void testDoesntSuggestMethodsDefinedInTypesScopeWhenInTopLevel() throws Exception
	{
		String src = "module Chris\n" + //
				"  def chris_method\n" + //
				"  end\n" + //
				"end\n" + //
				"ch"; //

		ICompletionProposal[] proposals = computeProposals(src, src.length() - 1);
		assertDoesntContain(proposals, "chris_method");
	}

	public void testDoesSuggestMethodsDefinedInTopLevelWhenInTopLevel() throws Exception
	{
		String src = "def chris_method\n" + //
				"end\n" + //
				"ch"; //

		assertCompletionCorrect(src, 23, "chris_method", "def chris_method\n" + //
				"end\n" + //
				"chris_method");
	}

	public void testSuggestSingletonMethodsOnClassName() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add a fake entry for File.expand_path
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "expand_path/File//P/S/1", new URI("file.rb"));
		indicesforTesting.add(testIndex);

		String src = "File.";

		assertCompletionCorrect(src, 5, "expand_path", "File.expand_path");
	}

	public void testSymbolsInSameFile() throws Exception
	{
		String src = "SYMBOLS = [:triangle, :circle, :rectangle]\n" + //
				"symbol = :";

		ICompletionProposal[] proposals = computeProposals(src, 53);
		assertEquals(3, proposals.length);
		assertContains(proposals, ":triangle", ":circle", ":rectangle");
	}

	// FIXME Implement suggesting instance variables when there's a syntax error, so no AST!
	// public void testInstanceVariablePreviouslyDeclaredJustAtSigilPrefixWithUnclosedBlockSyntaxErrors() throws
	// Exception
	// {
	// assertCompletionCorrect(
	//				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @", 73, "@counter", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @counter"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	public void testEvenQueryMethodProposalOnFixnumInstanceVariableRightAfterPeriod() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add a fake entry for Fixnum#even?
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "even?/Fixnum//P/I/0", new URI("fixnum.rb"));
		indicesforTesting.add(testIndex);

		assertCompletionCorrect("class Chris\n" + //
				"  def initialize\n" + //
				"    @counter = 1\n" + //
				"  end\n" + //
				"  def to_s\n" + //
				"    @counter.\n" + //
				"  end\n" + //
				"end", //
				76, "even?", //
				"class Chris\n" + //
						"  def initialize\n" + //
						"    @counter = 1\n" + //
						"  end\n" + //
						"  def to_s\n" + //
						"    @counter.even?\n" + //
						"  end\n" + //
						"end");
	}

	// TODO Test pre-defined globals
	// TODO Test class variables
}
