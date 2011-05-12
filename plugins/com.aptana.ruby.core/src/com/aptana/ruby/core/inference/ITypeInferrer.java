/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.inference;

import java.util.Collection;

import org.jrubyparser.ast.Node;

public interface ITypeInferrer
{

	/**
	 * Given raw Ruby source code and an offset into that code, does a best estimate of type information and returns a
	 * list of ITypeGuess objects.
	 * 
	 * @param source
	 *            The raw source to be parsed
	 * @param offset
	 *            the position in the source where we want to infer type information.
	 * @return A List of ITypeGuess objects giving us the best available information for inferred type.
	 */
	public Collection<ITypeGuess> infer(String source, int offset);

	public Collection<ITypeGuess> infer(Node rootNode, Node toInfer);

}
