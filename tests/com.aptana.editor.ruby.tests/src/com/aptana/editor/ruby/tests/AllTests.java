/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.aptana.editor.ruby.RubyEditorTests;
import com.aptana.editor.ruby.internal.contentassist.RubyCATests;
import com.aptana.editor.ruby.internal.text.InternalTextTests;
import com.aptana.editor.ruby.internal.text.rules.InternalTextRulesTests;
import com.aptana.editor.ruby.outline.RubyOutlineTest;

@RunWith(Suite.class)
@SuiteClasses({ RubyEditorTests.class, RubyCATests.class, InternalTextTests.class, InternalTextRulesTests.class,
		RubyOutlineTest.class, })
public class AllTests
{
}
