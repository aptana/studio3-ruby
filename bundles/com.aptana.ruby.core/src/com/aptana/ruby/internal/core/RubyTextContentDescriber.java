/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.internal.content.TextContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

@SuppressWarnings("restriction")
public class RubyTextContentDescriber extends TextContentDescriber
{

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException
	{
		return describe(new InputStreamReader(contents), description); // $codepro.audit.disable closeWhereCreated
	}

	@Override
	public int describe(Reader contents, IContentDescription description) throws IOException
	{
		int result = super.describe(contents, description);
		String firstLine = new BufferedReader(contents).readLine(); // $codepro.audit.disable closeWhereCreated
		// Verify that a shebang line is there
		if (firstLine.contains("#!") && firstLine.contains("ruby")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return VALID;
		}
		// TODO Now try passing a syntax check?!
		return result;
	}
}
