/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aptana.editor.common.outline.CommonOutlineContentProvider;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyMethod;

public class RubyOutlineContentProvider extends CommonOutlineContentProvider
{

	@Override
	protected Object[] filter(IParseNode[] nodes)
	{
		List<IRubyElement> list = new ArrayList<IRubyElement>();
		for (IParseNode node : nodes)
		{
			if (!(node instanceof IRubyElement))
			{
				continue;
			}
			IRubyElement element = (IRubyElement) node;
			// filters out block elements
			if (element.getNodeType() == IRubyElement.BLOCK)
			{
				continue;
			}
			list.add(element);
		}
		// Sort within this level of the hierarchy
		Collections.sort(list, new Comparator<IRubyElement>()
		{
			public int compare(IRubyElement o1, IRubyElement o2)
			{
				return sortPriority(o1) - sortPriority(o2);
			}

			private int sortPriority(IRubyElement element)
			{
				switch (element.getNodeType())
				{
					case IRubyElement.SCRIPT:
						return -2;
					case IRubyElement.GLOBAL:
						return -1;
					case IRubyElement.IMPORT_CONTAINER:
						return 0;
					case IRubyElement.IMPORT_DECLARATION:
						return 1;
					case IRubyElement.TYPE:
						return 2;
					case IRubyElement.CONSTANT:
						return 3;
					case IRubyElement.CLASS_VAR:
						return 4;
					case IRubyElement.INSTANCE_VAR:
					case IRubyElement.FIELD:
						return 5;
					case IRubyElement.METHOD:
						IRubyMethod method = (IRubyMethod) element;
						if (method.isSingleton())
						{
							return 6;
						}
						if (method.isConstructor())
						{
							return 7;
						}
						return 8;
					case IRubyElement.LOCAL_VAR:
						return 9;
					case IRubyElement.BLOCK:
					case IRubyElement.DYNAMIC_VAR:
						return 10;
					default:
						return 5;
				}
			}
		});

		// Turn into outline items
		List<CommonOutlineItem> outlineItems = new ArrayList<CommonOutlineItem>(list.size());
		for (IRubyElement element : list)
		{
			outlineItems.add(getOutlineItem(element));
		}

		return outlineItems.toArray(new CommonOutlineItem[outlineItems.size()]);
	}
}
