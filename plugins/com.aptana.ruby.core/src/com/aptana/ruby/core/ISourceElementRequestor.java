/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import com.aptana.ruby.core.IRubyMethod.Visibility;

public interface ISourceElementRequestor
{

	public static class TypeInfo
	{
		public int declarationStart;
		public boolean isModule;
		public String name;
		public int nameSourceStart;
		public int nameSourceEnd;
		public String superclass;
		public String[] modules;
		public boolean secondary;
	}

	public static class MethodInfo
	{
		public boolean isConstructor;
		public boolean isClassLevel;
		public Visibility visibility;
		public int declarationStart;
		public String name;
		public int nameSourceStart;
		public int nameSourceEnd;
		public String[] parameterNames;
		public String[] blockVars;
	}

	public static class FieldInfo
	{
		public int declarationStart;
		public String name;
		public boolean isDynamic;
		public int nameSourceStart;
		public int nameSourceEnd;
	}

	public void enterMethod(MethodInfo methodInfo);

	public void enterConstructor(MethodInfo constructor);

	public void enterField(FieldInfo fieldInfo);

	public void enterType(TypeInfo typeInfo);

	public void enterScript();

	public void exitMethod(int endOffset);

	public void exitConstructor(int endOffset);

	public void exitField(int endOffset);

	public void exitType(int endOffset);

	public void exitScript(int endOffset);

	public void acceptMethodReference(String name, int argCount, int offset);

	public void acceptConstructorReference(String name, int argCount, int offset);

	public void acceptFieldReference(String name, int offset);

	public void acceptTypeReference(String name, int startOffset, int endOffset);

	public void acceptImport(String value, int startOffset, int endOffset);

	public void acceptMixin(String string);

	public void acceptModuleFunction(String function);

	public void acceptMethodVisibilityChange(String methodName, Visibility visibility);

	public void acceptYield(String name);

	public void enterBlock(int startOffset, int endOffset);

	public void exitBlock(int endOffset);
}
