/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.radrails.tests.all;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({
	org.radrails.rails.ui.tests.AllTests.class,
	com.aptana.editor.ruby.tests.AllTests.class,
	com.aptana.editor.ruby.formatter.tests.AllTests.class,
	com.aptana.editor.erb.tests.AllTests.class,
	com.aptana.editor.haml.tests.HAMLEditorTests.class,
	com.aptana.editor.sass.tests.AllTests.class,
})
// @formatter:on
public class UITests
{
}
