package com.aptana.editor.ruby.internal.contentassist;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.aptana.editor.ruby.RubySourceEditor;
import com.aptana.index.core.FileStoreBuildContext;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexManager;
import com.aptana.ruby.core.codeassist.CompletionContext;
import com.aptana.ruby.core.index.IRubyIndexConstants;
import com.aptana.ruby.core.inference.ITypeInferrer;
import com.aptana.ruby.internal.core.index.RubyFileIndexingParticipant;
import com.aptana.ruby.internal.core.inference.TypeInferrer;

@SuppressWarnings("restriction")
public class RubyContentAssistProcessorTest extends RubyContentAssistTestCase
{
	private List<Index> indicesforTesting;

	/**
	 * ugly hack, but we enforce that we use the test indices for CA in the unit tests, and that we ignore word
	 * proposals
	 */
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

			protected CompletionContext createCompletionContext(ITextViewer viewer, int offset)
			{
				return new CompletionContext(getProject(), viewer.getDocument().get(), offset - 1)
				{
					@Override
					protected ITypeInferrer getTypeInferrer()
					{
						return new TypeInferrer(null)
						{
							@Override
							protected Collection<Index> getAllIndicesForProject()
							{
								return indicesforTesting;
							}
						};
					}
				};
			}

			@Override
			protected Index getIndex()
			{
				if (indicesforTesting != null)
				{
					return indicesforTesting.get(0);
				}
				return null;
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
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "puts/Kernel//P/I/1", new URI("kernel.rb"));
		indicesforTesting.add(testIndex);

		assertCompletionCorrect("pu", 2, "puts", "puts");
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

	public void testEmptyPrefixSuggestsClassInstanceLocalVarsAndMethodsInType() throws Exception
	{
		ICompletionProposal[] proposals = computeProposals("class Chris\n" + //
				"  @@class_var = 123\n" + //
				"  def initialize\n" + //
				"    @counter = 1\n" + //
				"  end\n" + //
				"  def method\n" + //
				"  end\n" + //
				"  def run(arg)\n" + //
				"    local = 'string'\n" + //
				"    \n" + //
				"  end\n" + //
				"end", //
				131);

		assertNotNull(proposals);
		assertContains(proposals, "@@class_var", "@counter", "arg", "local", "method");
	}

	public void testSuggestGlobalsInsideIndexAfterDollarSign() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add fake global entries
		testIndex.addEntry(IRubyIndexConstants.GLOBAL_DECL, "$global", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.GLOBAL_DECL, "$stdout", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.GLOBAL_DECL, "$stderr", new URI("fake.rb"));
		indicesforTesting.add(testIndex);

		ICompletionProposal[] proposals = computeProposals("$", 1);

		assertNotNull(proposals);
		assertContains(proposals, "$global", "$stdout", "$stderr");
	}

	public void testSuggestsTypeNamesAndConstantsWhenPrefixIsUppercase() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add fake global entries
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "ClassName//C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "CModule//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "CONSTANT/Object/", new URI("fake.rb"));
		indicesforTesting.add(testIndex);

		ICompletionProposal[] proposals = computeProposals("C", 1);

		assertNotNull(proposals);
		assertContains(proposals, "CONSTANT", "ClassName", "CModule");
	}

	public void testClassVariablePreviouslyDeclaredNoSyntaxErrors() throws Exception
	{
		assertCompletionCorrect("class Chris\n" + //
				"  @@counter = 1\n" + //
				"  def to_s\n" + //
				"    puts @@c\n" + //
				"  end\n" + //
				"end", 51, "@@counter", //
				"class Chris\n" + //
						"  @@counter = 1\n" + //
						"  def to_s\n" + //
						"    puts @@counter\n" + //
						"  end\n" + //
						"end");
	}

	public void testClassVariablePreviouslyDeclaredJustDoubleAtSigilPrefix() throws Exception
	{
		assertCompletionCorrect("class Chris\n" + //
				"  @@counter = 1\n" + //
				"  def to_s\n" + //
				"    puts @@\n" + //
				"  end\n" + //
				"end", 50, "@@counter", //
				"class Chris\n" + //
						"  @@counter = 1\n" + //
						"  def to_s\n" + //
						"    puts @@counter\n" + //
						"  end\n" + //
						"end"); //
	}

	public void testClassVariablePreviouslyDeclaredJustSingleAtSigilPrefix() throws Exception
	{
		assertCompletionCorrect("class Chris\n" + //
				"  @@counter = 1\n" + //
				"  def to_s\n" + //
				"    puts @\n" + //
				"  end\n" + //
				"end", 49, "@@counter", //
				"class Chris\n" + //
						"  @@counter = 1\n" + //
						"  def to_s\n" + //
						"    puts @@counter\n" + //
						"  end\n" + //
						"end"); //
	}

	public void testDoesntSuggestClassVariablesOutsideCurrentType() throws Exception
	{
		String src = "class Outside\n" + //
				"  @@outside = 1\n" + //
				"end\n" + //
				"class Chris\n" + //
				"  @@counter = 1\n" + //
				"  def to_s\n" + //
				"    puts @\n" + //
				"  end\n" + //
				"end"; //

		ICompletionProposal[] proposals = computeProposals(src, 83);

		assertNotNull(proposals);
		assertContains(proposals, "@@counter");
		assertDoesntContain(proposals, "@@outside");
	}

	public void testDoesntSuggestInstanceVariablesOutsideCurrentType() throws Exception
	{
		String src = "class Outside\n" + //
				"  def initialize\n" + //
				"    @outside = 1\n" + //
				"  end\n" + //
				"end\n" + //
				"class Chris\n" + //
				"  def initialize\n" + //
				"    @counter = 1\n" + //
				"  end\n" + //
				"  def to_s\n" + //
				"    puts @\n" + //
				"  end\n" + //
				"end"; //

		ICompletionProposal[] proposals = computeProposals(src, 131);

		assertNotNull(proposals);
		assertContains(proposals, "@counter");
		assertDoesntContain(proposals, "@outside");
	}

	public void testSuggestsConstantsAndTypesInNamespaceAfterDoubleColon() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add fake global entries
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "TopClass//C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "SubClass/Namespace/C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "TopModule//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "SubModule/Namespace/M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "Namespace//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "TOP_CONSTANT/Object/", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "SUB_CONSTANT/Namespace/", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "method_out_of_namespace/TopClass//P/I/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "public_method_in_namespace/Namespace//P/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "protected_method_in_namespace/Namespace//R/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "private_method_in_namespace/Namespace//V/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "public_instance_method_in_namespace/Namespace//P/I/0",
				new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "protected_instance_method_in_namespace/Namespace//R/I/0",
				new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "private_instance_method_in_namespace/Namespace//V/I/0",
				new URI("fake.rb"));
		indicesforTesting.add(testIndex);

		ICompletionProposal[] proposals = computeProposals("Namespace::", 11);

		assertNotNull(proposals);
		// Proposals should include any singleton methods for Namespace module, constants defined in Namespace, types
		// defined in namespace
		assertContains(proposals, "SUB_CONSTANT", "SubClass", "SubModule", "public_method_in_namespace",
				"protected_method_in_namespace", "private_method_in_namespace");
		// Don't include toplevel types, constants; or instance methods in module
		assertDoesntContain(proposals, "TOP_CONSTANT", "TopClass", "TopModule", "method_out_of_namespace",
				"public_instance_method_in_namespace", "protected_instance_method_in_namespace",
				"private_instance_method_in_namespace");
	}

	public void testSuggestsConstantsAndTypesInExplicitTopLevelNamespaceAfterDoubleColon() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add fake global entries
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "TopClass//C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "SubClass/Namespace/C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "TopModule//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "SubModule/Namespace/M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "Namespace//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "TOP_CONSTANT/Object/", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "SUB_CONSTANT/Namespace/", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "method_out_of_namespace/TopClass//P/I/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "public_method_in_namespace/Namespace//P/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "protected_method_in_namespace/Namespace//R/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "private_method_in_namespace/Namespace//V/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "public_instance_method_in_namespace/Namespace//P/I/0",
				new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "protected_instance_method_in_namespace/Namespace//R/I/0",
				new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "private_instance_method_in_namespace/Namespace//V/I/0",
				new URI("fake.rb"));
		indicesforTesting.add(testIndex);

		ICompletionProposal[] proposals = computeProposals("module Namespace\n" + //
				"  ::\n" + //
				"end\n", 21);

		assertNotNull(proposals);
		assertContains(proposals, "TopClass", "TopModule", "TOP_CONSTANT");
		assertDoesntContain(proposals, "SubClass", "SubModule", "SUB_CONSTANT");
	}

	public void testAfterNamespacedDoubleColonInsideImplicitNamespace() throws Exception
	{
		Index testIndex = getTestIndex();
		// Add fake global entries
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "TopClass//C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "SubClass/Namespace/C", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "TopModule//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "SubModule/Namespace/M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.TYPE_DECL, "Namespace//M", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "TOP_CONSTANT/Object/", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.CONSTANT_DECL, "SUB_CONSTANT/Namespace/", new URI("fake.rb"));
		testIndex
				.addEntry(IRubyIndexConstants.CONSTANT_DECL, "SUB_SUB_CONSTANT/SubClass/Namespace", new URI("fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "method_out_of_namespace/TopClass//P/S/0", new URI(
				"fake.rb"));
		testIndex.addEntry(IRubyIndexConstants.METHOD_DECL, "public_method_in_namespace/SubClass/Namespace/P/S/0",
				new URI("fake.rb"));
		indicesforTesting.add(testIndex);

		ICompletionProposal[] proposals = computeProposals("module Namespace\n" + //
				"  SubClass::\n" + //
				"end\n", 29);

		assertNotNull(proposals);
		assertContains(proposals, "SUB_SUB_CONSTANT", "public_method_in_namespace");
		assertDoesntContain(proposals, "TopClass", "SubClass", "TopModule", "SubModule", "Namespace", "TOP_CONSTANT",
				"SUB_CONSTANT", "method_out_of_namespace");
	}

	public void testMethodsUpTheHierarchyonInstance() throws Exception
	{
		setupHierarchyCA("ruby_ca_methods_on_instance");

		ICompletionProposal[] proposals = computeProposals("chris = Chris.new\nchris.", 24);

		assertNotNull(proposals);
		// Propose public instance methods on type, supertype and included modules up the chain
		assertContains(proposals, "chris_public_instance", "super_public_instance", "other_public_instance");
		// Do not propose protected, private or siingleton methods.
		assertDoesntContain(proposals, "chris_private_instance", "chris_protected_instance", "super_private_instance",
				"super_protected_instance", "chris_public_singleton", "chris_protected_singleton",
				"chris_private_singleton", "super_public_singleton", "super_protected_singleton",
				"super_private_singleton", "other_public_singleton", "other_protected_singleton",
				"other_private_singleton", "other_protected_instance", "other_private_instance");
	}

	public void testMethodsUpTheHierarchyOnSingleton() throws Exception
	{
		setupHierarchyCA("ruby_ca_methods_on_singleton");

		ICompletionProposal[] proposals = computeProposals("Chris.", 6);

		assertNotNull(proposals);
		// Proposals contain singleton methods from classes up hierarchy
		assertContains(proposals, "chris_public_singleton", "chris_protected_singleton", "chris_private_singleton",
				"super_public_singleton", "super_protected_singleton", "super_private_singleton");
		// Don't contain instance methods, or singletons on included Modules
		assertDoesntContain(proposals, "chris_public_instance", "chris_private_instance", "chris_protected_instance",
				"super_public_instance", "super_private_instance", "super_protected_instance", "other_public_instance",
				"other_protected_instance", "other_private_instance", "other_public_singleton",
				"other_protected_singleton", "other_private_singleton");
	}

	public void testMethodsUpTheHierarchyInsideClassDefinitionSingletonMethod() throws Exception
	{
		setupHierarchyCA("ruby_ca_methods_inside_class_singleton_method");

		ICompletionProposal[] proposals = computeProposals(
				"class Chris < Super\n  def self.singleton\n    \n  end\nend\n", 45);

		assertNotNull(proposals);
		assertContains(proposals, "super_public_singleton", "super_protected_singleton", "super_private_singleton",
				"chris_public_singleton", "chris_protected_singleton", "chris_private_singleton");
		// Doesn't contains instance methods or singletons on included modules
		assertDoesntContain(proposals, "other_public_singleton", "other_protected_singleton",
				"other_private_singleton", "other_public_instance", "other_protected_instance",
				"other_private_instance", "super_public_instance", "super_protected_instance",
				"super_private_instance", "chris_protected_instance", "chris_public_instance", "chris_private_instance");
	}

	public void testMethodsUpTheHierarchyInsideClassDefinitionInstanceMethod() throws Exception
	{
		setupHierarchyCA("ruby_ca_methods_inside_class_instance_method");

		ICompletionProposal[] proposals = computeProposals("class Chris < Super\n  def instance\n    \n  end\nend\n",
				39);

		assertNotNull(proposals);
		// All instance methods up hierarchy
		assertContains(proposals, "other_public_instance", "other_protected_instance", "other_private_instance",
				"super_public_instance", "super_protected_instance", "super_private_instance",
				"chris_protected_instance", "chris_public_instance", "chris_private_instance");
		// No singletons
		assertDoesntContain(proposals, "other_public_singleton", "other_protected_singleton",
				"other_private_singleton", "super_public_singleton", "super_protected_singleton",
				"super_private_singleton", "chris_public_singleton", "chris_protected_singleton",
				"chris_private_singleton");
	}

	public void testMethodsUpTheHierarchyInsideClassDefinitionOutsideMethod() throws Exception
	{
		setupHierarchyCA("ruby_ca_methods_inside_class_toplevel");

		ICompletionProposal[] proposals = computeProposals("class Chris < Super\n  \nend\n", 22);

		assertNotNull(proposals);
		assertContains(proposals, "super_public_singleton", "super_protected_singleton", "super_private_singleton",
				"chris_public_singleton", "chris_protected_singleton", "chris_private_singleton");
		assertDoesntContain(proposals, "super_public_instance", "super_protected_instance", "super_private_instance",
				"chris_protected_instance", "chris_public_instance", "chris_private_instance",
				"other_public_singleton", "other_protected_singleton", "other_private_singleton",
				"other_public_instance", "other_protected_instance", "other_private_instance");
	}

	// TODO Test invocation inside class definition Chris with "self" as receiver
	// TODO Test "include" versus "extend"
	// TODO Test methods available in top-level: various visibilities, singleton/instance

	protected void setupHierarchyCA(String tmpFilePrefix) throws IOException, CoreException
	{
		String indexFileSrc = "module Other\n" + //
				"  def self.other_public_singleton\n" + //
				"  end\n" + //
				"  def other_public_instance\n" + //
				"  end\n" + //
				"  protected\n" + //
				"  def self.other_protected_singleton\n" + //
				"  end\n" + //
				"  def other_protected_instance\n" + //
				"  end\n" + //
				"  private\n" + //
				"  def self.other_private_singleton\n" + //
				"  end\n" + //
				"  def other_private_instance\n" + //
				"  end\n" + //
				"end\n" + //
				"class Super\n" + //
				"  include Other\n" + //
				"  def self.super_public_singleton\n" + //
				"  end\n" + //
				"  def super_public_instance\n" + //
				"  end\n" + //
				"  protected\n" + //
				"  def self.super_protected_singleton\n" + //
				"  end\n" + //
				"  def super_protected_instance\n" + //
				"  end\n" + //
				"  private\n" + //
				"  def self.super_private_singleton\n" + //
				"  end\n" + //
				"  def super_private_instance\n" + //
				"  end\n" + //
				"end\n" + //
				"class Chris < Super\n" + //
				"  def self.chris_public_singleton\n" + //
				"  end\n" + //
				"  def chris_public_instance\n" + //
				"  end\n" + //
				"  protected\n" + //
				"  def self.chris_protected_singleton\n" + //
				"  end\n" + //
				"  def chris_protected_instance\n" + //
				"  end\n" + //
				"  private\n" + //
				"  def self.chris_private_singleton\n" + //
				"  end\n" + //
				"  def chris_private_instance\n" + //
				"  end\n" + //
				"end\n"; //
		RubyFileIndexingParticipant rfip = new RubyFileIndexingParticipant();
		Set<IFileStore> files = new HashSet<IFileStore>();
		File file = File.createTempFile(tmpFilePrefix, ".rb");
		FileWriter writer = new FileWriter(file);
		writer.write(indexFileSrc);
		writer.close();
		IFileStore fileStore = EFS.getStore(file.toURI());
		files.add(fileStore);
		Index testIndex = getTestIndex();		
		rfip.indexSource(testIndex, new FileStoreBuildContext(fileStore), indexFileSrc, new NullProgressMonitor());
		indicesforTesting.add(testIndex);
	}
}
