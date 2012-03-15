package com.aptana.ruby.internal.core.inference;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import com.aptana.index.core.Index;
import com.aptana.index.core.IndexManager;
import com.aptana.index.core.IndexPlugin;
import com.aptana.ruby.core.index.IRubyIndexConstants;
import com.aptana.ruby.core.inference.ITypeGuess;

@SuppressWarnings("nls")
public class TypeInferrerTest extends TestCase
{
	private List<Index> indicesforTesting;
	private TypeInferrer inferrer;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		indicesforTesting = new ArrayList<Index>();
		inferrer = new TypeInferrer(null)
		{
			@Override
			protected Collection<Index> getAllIndicesForProject()
			{
				return indicesforTesting;
			}
		};
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{
			inferrer = null;
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

	protected Index getTestIndex()
	{
		// generate a tmp dir URI location for our fake index...
		String tmpDir = System.getProperty("java.io.tmpdir");
		File caIndexDir = new File(tmpDir, "ruby_ti_test" + System.currentTimeMillis());
		caIndexDir.deleteOnExit();
		return getIndexManager().getIndex(caIndexDir.toURI());
	}

	public void testAmbiguousTypeInImplicitNamespaceMatchesFullNameBeforeToplevelName() throws Exception
	{
		Index index = getTestIndex();
		index.addEntry(IRubyIndexConstants.TYPE_DECL, "Subclass//C", new URI("fake.rb"));
		index.addEntry(IRubyIndexConstants.TYPE_DECL, "Namespace//M", new URI("fake.rb"));
		index.addEntry(IRubyIndexConstants.TYPE_DECL, "Subclass/Namespace/C", new URI("fake.rb"));
		indicesforTesting.add(index);

		String src = "module Namespace\n" + //
				"  Subclass\n" + //
				"end\n"; //
		int offset = 22;
		Collection<ITypeGuess> typeGuesses = inferrer.infer(src, offset);
		assertEquals(1, typeGuesses.size());
		ITypeGuess guess = typeGuesses.iterator().next();
		assertEquals("Namespace::Subclass", guess.getType());
	}

	protected IndexManager getIndexManager()
	{
		return IndexPlugin.getDefault().getIndexManager();
	}

}
