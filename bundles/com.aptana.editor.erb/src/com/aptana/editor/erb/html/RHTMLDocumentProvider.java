/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.html;

import com.aptana.editor.common.CompositeDocumentProvider;
import com.aptana.editor.erb.ERBPartitionerSwitchStrategy;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.ruby.RubySourceConfiguration;

/**
 * @author Max Stepanov
 */
public class RHTMLDocumentProvider extends CompositeDocumentProvider
{

	public RHTMLDocumentProvider()
	{
		super(IERBConstants.CONTENT_TYPE_HTML_ERB, HTMLSourceConfiguration.getDefault(), RubySourceConfiguration
				.getDefault(), ERBPartitionerSwitchStrategy.getDefault());
	}

}
