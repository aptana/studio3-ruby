package com.aptana.ruby.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.aptana.ruby.core.RubyParserTest;
import com.aptana.ruby.core.ast.NamespaceVisitorTest;
import com.aptana.ruby.core.ast.SourceElementVisitorTest;
import com.aptana.ruby.core.codeassist.CompletionContextTest;
import com.aptana.ruby.internal.core.inference.TypeInferrerTest;

@RunWith(Suite.class)
@SuiteClasses({ CompletionContextTest.class, NamespaceVisitorTest.class, RubyParserTest.class,
		SourceElementVisitorTest.class, TypeInferrerTest.class, })
public class AllTests
{

}
