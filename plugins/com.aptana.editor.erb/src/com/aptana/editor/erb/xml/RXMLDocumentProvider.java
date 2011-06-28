/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.xml;

import com.aptana.editor.common.CompositeDocumentProvider;
import com.aptana.editor.erb.ERBPartitionerSwitchStrategy;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.ruby.RubySourceConfiguration;
import com.aptana.editor.xml.XMLSourceConfiguration;

/**
 * @author Max Stepanov
 *
 */
public class RXMLDocumentProvider extends CompositeDocumentProvider {

	public RXMLDocumentProvider() {
		super(IERBConstants.CONTENT_TYPE_XML_ERB,
				XMLSourceConfiguration.getDefault(),
				RubySourceConfiguration.getDefault(),
				ERBPartitionerSwitchStrategy.getDefault());
	}
	
}
