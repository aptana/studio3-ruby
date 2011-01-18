/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.parsing.lexer;

import java.util.HashMap;
import java.util.Map;

public class ERBTokens
{
	public static final short UNKNOWN = -1;
	public static final short EOF = 0;
	public static final short RUBY = 101;
	public static final short RUBY_END = 102;

	private static final short MAXIMUM = 2;
	private static final short OFFSET = 100;

	@SuppressWarnings("nls")
	private static final String[] NAMES = { "EOF", "RUBY", "RUBY_END" };
	private static final String NAME_UNKNOWN = "UNKNOWN"; //$NON-NLS-1$

	private static Map<String, Short> nameIndexMap;

	public static String getTokenName(short token)
	{
		init();
		token -= OFFSET;
		if (token < 0 || token > MAXIMUM)
		{
			return NAME_UNKNOWN;
		}
		return NAMES[token];
	}

	public static short getToken(String tokenName)
	{
		init();
		Short token = nameIndexMap.get(tokenName);
		return (token == null) ? UNKNOWN : token;
	}

	private static void init()
	{
		if (nameIndexMap == null)
		{
			nameIndexMap = new HashMap<String, Short>();
			short index = OFFSET;
			for (String name : NAMES)
			{
				nameIndexMap.put(name, index++);
			}
		}
	}

	private ERBTokens()
	{
	}
}
