package com.aptana.editor.ruby.internal.contentassist;

import com.aptana.editor.ruby.RubySourceEditor;

public class RubyContentAssistProcessorTest extends RubyContentAssistTestCase
{
	protected RubyContentAssistProcessor createContentAssistProcessor(RubySourceEditor editor)
	{
		return new RubyContentAssistProcessor(editor);
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

	// FIXME Implement suggesting instance variables when there's a syntax error, so no AST!
	// public void testInstanceVariablePreviouslyDeclaredJustAtSigilPrefixWithUnclosedBlockSyntaxErrors() throws
	// Exception
	// {
	// assertCompletionCorrect(
	//				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @", 73, "@counter", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @counter"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	// public void testEvenQueryMethodProposalOnFixnumInstanceVariableWithNoSyntaxErrors() throws Exception
	// {
	// assertCompletionCorrect(
	//				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    @counter.\n  end\nend", 76, "even?", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    @counter.even?\n  end\nend"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	// TODO Test pre-defined globals
	// TODO Test class variables
	// TODO Test core classes/modules
	// TODO Test classes in files inside the same project
	// TODO Test classes inside gems
}
