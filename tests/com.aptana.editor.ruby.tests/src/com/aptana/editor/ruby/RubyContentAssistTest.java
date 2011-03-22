package com.aptana.editor.ruby;

import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;

import junit.framework.TestCase;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.scripting.ScriptLogListener;
import com.aptana.scripting.ScriptLogger;
import com.aptana.scripting.model.BundleManager;
import com.aptana.ui.util.UIUtils;

public class RubyContentAssistTest extends TestCase
{
	private static class LogListener implements ScriptLogListener
	{

		public void logError(String error)
		{
			System.err.println(error);
		}

		public void logInfo(String info)
		{
			System.out.println(info);
		}

		public void logWarning(String warning)
		{
			System.out.println(warning);
		}

		public void trace(String message)
		{
			System.out.println(message);
		}

		public void print(String message)
		{
			logInfo(message);
		}

		public void printError(String message)
		{
			logError(message);
		}

	}

	private CommonContentAssistProcessor fProcessor;
	private RubySourceEditor fEditor;
	private LogListener fListener;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		fListener = new LogListener();
		ScriptLogger.getInstance().addLogListener(fListener);

		// make sure the ruby ruble is loaded!
		BundleManager.getInstance().loadBundles();
		// Make sure that the translation layer for partitions/scopes is set up
		RubySourceConfiguration.getDefault();
	}

	@Override
	protected void tearDown() throws Exception
	{
		ScriptLogger.getInstance().removeLogListener(fListener);
		fListener = null;
		fEditor.close(false);
		fEditor = null;
		BundleManager.getInstance().reset();
		fProcessor = null;
		super.tearDown();
	}

	private ICompletionProposal findProposal(String string, ICompletionProposal[] proposals)
	{
		for (ICompletionProposal proposal : proposals)
		{
			if (proposal.getDisplayString().equals(string))
			{
				return proposal;
			}
		}
		return null;
	}

	private void assertCompletionCorrect(String document, int offset, String proposalToSelect, String postCompletion)
			throws Exception
	{
		char trigger = '\t';
		IWorkbenchPage page = UIUtils.getActivePage();
		File file = File.createTempFile("ruby", ".rb");
		FileWriter writer = new FileWriter(file);
		writer.write(document);
		writer.close();
		fEditor = (RubySourceEditor) IDE.openEditor(page, file.toURI(), "com.aptana.editor.ruby", true);
		fEditor.selectAndReveal(offset, 0);

		fProcessor = new CommonContentAssistProcessor(fEditor);

		ICompletionProposal[] proposals = fProcessor
				.computeCompletionProposals(getTextViewer(), offset, trigger, false);
		ICompletionProposal selectedProposal = findProposal(proposalToSelect, proposals);
		assertNotNull(
				MessageFormat.format("Tried to select proposal {0}, but it didn't exist in list!", proposalToSelect),
				selectedProposal);
		assertTrue(((ICompletionProposalExtension2) selectedProposal).validate(getDocument(), offset, null));
		((ICompletionProposalExtension2) selectedProposal).apply(getTextViewer(), trigger, SWT.NONE, offset);
		assertEquals(postCompletion, getDocument().get());
	}

	private ITextViewer getTextViewer()
	{
		return fEditor.getISourceViewer();
	}

	private IDocument getDocument()
	{
		return getTextViewer().getDocument();
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

	public void testExplicitKernelPuts() throws Exception
	{
		assertCompletionCorrect("Kernel.pu", 9, "puts", "Kernel.puts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testImplicitKernelPuts() throws Exception
	{
		assertCompletionCorrect("pu", 2, "puts", "puts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testKernelMethodsImmediatelyAfterPeriod() throws Exception
	{
		assertCompletionCorrect("Kernel.", 7, "puts", "Kernel.puts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testEvenQueryMethodOnFixnumLiteral() throws Exception
	{
		assertCompletionCorrect("1.eve", 5, "even?", "1.even?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testEvenQueryMethodOnFixnumVariable() throws Exception
	{
		assertCompletionCorrect("var = 1\nvar.eve", 15, "even?", "var = 1\nvar.even?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

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

	// FIXME Implement suggesting instance variables when there's a syntax error, so no AST!
	// public void testInstanceVariablePreviouslyDeclaredJustAtSigilPrefixWithUnclosedBlockSyntaxErrors() throws
	// Exception
	// {
	// assertCompletionCorrect(
	//				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @", 73, "@counter", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    puts @counter"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	public void testEvenQueryMethodProposalOnFixnumInstanceVariableWithNoSyntaxErrors() throws Exception
	{
		assertCompletionCorrect(
				"class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    @counter.\n  end\nend", 76, "even?", "class Chris\n  def initialize\n    @counter = 1\n  end\n  def to_s\n    @counter.even?\n  end\nend"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	// TODO Test pre-defined globals
	// TODO Test class variables
	// TODO Test core classes/modules
	// TODO Test classes in files inside the same project
	// TODO Test classes inside gems

}
