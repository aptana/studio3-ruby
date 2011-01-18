/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.xml;

import com.aptana.editor.erb.common.ERBContentDescriber;

public class RXMLContentDescriber extends ERBContentDescriber {

    private static final String XML_PREFIX = "<?xml "; //$NON-NLS-1$

    @Override
    protected String getPrefix() {
        return XML_PREFIX;
    }
}
