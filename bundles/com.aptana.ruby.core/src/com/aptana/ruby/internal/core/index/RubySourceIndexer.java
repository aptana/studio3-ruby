/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.index;

import java.net.URI;
import java.util.Stack;

import com.aptana.core.util.ArrayUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.index.core.Index;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.IRubyMethod.Visibility;
import com.aptana.ruby.core.ISourceElementRequestor;
import com.aptana.ruby.core.index.IRubyIndexConstants;

// TODO Also index symbols?
class RubySourceIndexer implements ISourceElementRequestor
{

	private static final String NAMESPACE_DELIMETER = IRubyConstants.NAMESPACE_DELIMETER;

	protected static final String VERSION_KEY = "index_version"; //$NON-NLS-1$
	protected static final int CURRENT_VERSION = 5;

	private Stack<TypeInfo> typeStack = new Stack<TypeInfo>();
	private Index index;
	private URI documentPath;

	RubySourceIndexer(Index index, URI documentPath)
	{
		this.index = index;
		this.documentPath = documentPath;
	}

	private void addIndex(String category, String word)
	{
		index.addEntry(category, word, documentPath);
	}

	public void exitType(int endOffset)
	{
		typeStack.pop();
	}

	public void exitScript(int endOffset)
	{
		typeStack.clear();
	}

	public void exitMethod(int endOffset)
	{
	}

	public void exitField(int endOffset)
	{
	}

	public void exitConstructor(int endOffset)
	{
	}

	public void enterType(TypeInfo type)
	{
		String simpleName = getSimpleName(type.name);
		String[] enclosingTypes = getEnclosingTypeNames(type.name);
		addClassDeclaration(type.isModule, simpleName, enclosingTypes, type.superclass, type.modules, type.secondary);
		typeStack.push(type);
	}

	private void addClassDeclaration(boolean isModule, String simpleName, String[] enclosingTypes, String superclass,
			String[] modules, boolean secondary)
	{
		String indexKey = createTypeDeclarationKey(isModule, simpleName, enclosingTypes, secondary);
		addIndex(IRubyIndexConstants.TYPE_DECL, indexKey);

		if (superclass != null && !IRubyConstants.OBJECT.equals(superclass))
		{
			addTypeReference(superclass);
		}

		if (!isModule)
		{
			// We know that both class and superclass must be classes because Modules can't have subclasses
			if (superclass != null && !IRubyConstants.OBJECT.equals(superclass))
			{
				addIndex(
						IRubyIndexConstants.SUPER_REF,
						createSuperTypeReferenceKey(simpleName, enclosingTypes, IRubyIndexConstants.CLASS_SUFFIX,
								superclass, IRubyIndexConstants.CLASS_SUFFIX));
			}
		}
		if (modules != null)
		{
			for (String module : modules)
			{
				addTypeReference(module);
				addIncludedModuleReference(simpleName, enclosingTypes, module);
			}
		}
	}

	/**
	 * Generates a key of the form: TypeName/namespace/(M|C)/S i.e. "Base/ActiveRecord/C"
	 * 
	 * @param isModule
	 * @param typeName
	 * @param packageName
	 * @param enclosingTypeNames
	 * @param secondary
	 * @return
	 */
	private String createTypeDeclarationKey(boolean isModule, String typeName, String[] enclosingTypeNames,
			boolean secondary)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(typeName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		if (enclosingTypeNames != null && enclosingTypeNames.length > 0)
		{
			for (String enclosingName : enclosingTypeNames)
			{
				builder.append(enclosingName);
				builder.append(NAMESPACE_DELIMETER);
			}
			builder.delete(builder.length() - 2, builder.length());
		}
		builder.append(IRubyIndexConstants.SEPARATOR);

		builder.append(isModule ? IRubyIndexConstants.MODULE_SUFFIX : IRubyIndexConstants.CLASS_SUFFIX);

		if (secondary)
		{
			builder.append(IRubyIndexConstants.SEPARATOR);
			builder.append('S');
		}
		return builder.toString();
	}

	private String[] getEnclosingTypeNames(String typeName)
	{
		String[] parts = typeName.split(NAMESPACE_DELIMETER);

		String[] names = new String[typeStack.size() + parts.length - 1];
		int i = 0;
		for (TypeInfo info : typeStack)
		{
			names[i++] = info.name;
		}
		for (int j = 0; j < parts.length - 1; j++)
		{
			names[i++] = parts[j];
		}
		return names;
	}

	public void enterScript()
	{
	}

	public void enterConstructor(MethodInfo constructor)
	{
		// FIXME Create a special key for the constructor
		addIndex(
				IRubyIndexConstants.CONSTRUCTOR_DECL,
				createMethodDefKey("initialize", getSimpleName(constructor.name), //$NON-NLS-1$
						new String[] { getNamespace(constructor.name) }, Visibility.PUBLIC, true,
						constructor.parameterNames.length));
	}

	public void enterField(FieldInfo field)
	{
		if (field == null || field.name == null || field.name.length() == 0)
		{
			return;
		}
		if (field.name.startsWith("@@")) //$NON-NLS-1$
		{
			addIndex(IRubyIndexConstants.FIELD_DECL, createdNamespacedFieldKey(field.name));
			return;
		}
		else if (field.name.length() > 0 && field.name.charAt(0) == '@')
		{
			addIndex(IRubyIndexConstants.FIELD_DECL, createdNamespacedFieldKey(field.name));
			return;
		}
		else if (field.name.length() > 0 && field.name.charAt(0) == '$')
		{
			addIndex(IRubyIndexConstants.GLOBAL_DECL, field.name);
			return;
		}
		else if (Character.isUpperCase(field.name.charAt(0)))
		{
			addIndex(IRubyIndexConstants.CONSTANT_DECL, createdNamespacedFieldKey(field.name));
			return;
		}
		addIndex(IRubyIndexConstants.LOCAL_DECL, field.name);
	}

	private String createdNamespacedFieldKey(String name)
	{
		// TODO Use Toplevel, not Object?
		String simpleName = IRubyConstants.OBJECT;
		String[] enclosingTypes = ArrayUtil.NO_STRINGS;
		if (!typeStack.isEmpty())
		{
			TypeInfo info = typeStack.pop();
			simpleName = getSimpleName(info.name);
			enclosingTypes = getEnclosingTypeNames(info.name);
			typeStack.push(info);
		}

		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Defining type simple name
		builder.append(simpleName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Defining type namespace
		if (enclosingTypes != null && enclosingTypes.length > 0)
		{
			for (String enclosingName : enclosingTypes)
			{
				builder.append(enclosingName);
				builder.append(NAMESPACE_DELIMETER);
			}
			builder.delete(builder.length() - 2, builder.length());
		}
		return builder.toString();
	}

	public void enterMethod(MethodInfo method)
	{
		// TODO Use Toplevel, not Object?
		String simpleName = IRubyConstants.OBJECT;
		String[] enclosingTypes = ArrayUtil.NO_STRINGS;
		if (!typeStack.isEmpty())
		{
			TypeInfo info = typeStack.pop();
			simpleName = getSimpleName(info.name);
			enclosingTypes = getEnclosingTypeNames(info.name);
			typeStack.push(info);
		}
		addIndex(
				IRubyIndexConstants.METHOD_DECL,
				createMethodDefKey(method.name, simpleName, enclosingTypes, method.visibility, method.isClassLevel,
						method.parameterNames.length));
	}

	public void acceptYield(String name)
	{
	}

	public void acceptTypeReference(String name, int startOffset, int endOffset)
	{
		addTypeReference(name);
	}

	private void addTypeReference(String name)
	{
		addIndex(IRubyIndexConstants.REF, getSimpleName(name));
	}

	private String lastSegment(String name, String delimeter)
	{
		if (name == null)
		{
			return null;
		}
		int index = name.lastIndexOf(delimeter);
		if (index != -1)
		{
			return name.substring(index + delimeter.length());
		}
		return name;
	}

	public void acceptModuleFunction(String function)
	{
	}

	public void acceptMixin(String moduleName)
	{
		addIndex(IRubyIndexConstants.REF, getSimpleName(moduleName));

		if (typeStack != null && !typeStack.isEmpty())
		{
			// We need to pop and then push after grabbing namespace because the method assumes the current type info
			// isn't on the stack yet!
			TypeInfo info = typeStack.pop();
			String[] enclosingTypes = getEnclosingTypeNames(info.name);
			typeStack.push(info);
			addIncludedModuleReference(getSimpleName(info.name), enclosingTypes, moduleName);
		}
	}

	private void addIncludedModuleReference(String simpleName, String[] enclosingTypes, String moduleName)
	{
		addIndex(
				IRubyIndexConstants.SUPER_REF,
				createSuperTypeReferenceKey(simpleName, enclosingTypes, IRubyIndexConstants.CLASS_SUFFIX, moduleName,
						IRubyIndexConstants.MODULE_SUFFIX));
	}

	/**
	 * SuperTypeName(Simple)/SuperTypeNamespace/SimpleName/EnclosingTypeName/SuperIsClassOrModule(M|C)
	 * isClassorModule(M|C)
	 * 
	 * @param typeName
	 * @param enclosingTypeNames
	 * @param classOrModule
	 * @param superTypeName
	 * @param superClassOrModule
	 * @return
	 */
	private String createSuperTypeReferenceKey(String typeName, String[] enclosingTypeNames, char classOrModule,
			String superTypeName, char superClassOrModule)
	{
		if (superTypeName == null)
		{
			superTypeName = IRubyConstants.OBJECT;
		}
		String superSimpleName = lastSegment(superTypeName, NAMESPACE_DELIMETER);
		char[] superQualification = null;
		if (!superTypeName.equals(superSimpleName))
		{
			int length = superTypeName.length() - superSimpleName.length() - 1;
			superQualification = new char[length - 1];
			System.arraycopy(superTypeName.toCharArray(), 0, superQualification, 0, length - 1);
		}

		// if the supertype name contains a $, then split it into: source name and append the $
		// prefix to the qualification
		// e.g. p.A$B ---> p.A$ + B
		String superTypeSourceName = lastSegment(superSimpleName, NAMESPACE_DELIMETER);
		if (superSimpleName != null && !superSimpleName.equals(superTypeSourceName))
		{
			int start = (superQualification == null) ? 0 : superQualification.length + 1;
			int prefixLength = superSimpleName.length() - superTypeSourceName.length();
			char[] mangledQualification = new char[start + prefixLength];
			if (superQualification != null)
			{
				System.arraycopy(superQualification, 0, mangledQualification, 0, start - 1);
				mangledQualification[start - 1] = '.';
			}
			System.arraycopy(superSimpleName.toCharArray(), 0, mangledQualification, start, prefixLength);
			superQualification = mangledQualification;
			superSimpleName = superTypeSourceName;
		}

		String simpleName = lastSegment(typeName, NAMESPACE_DELIMETER);
		String enclosingTypeName = StringUtil.join(NAMESPACE_DELIMETER, enclosingTypeNames);

		StringBuilder builder = new StringBuilder();
		builder.append(superSimpleName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		if (superQualification != null)
		{
			builder.append(superQualification);
		}
		builder.append(IRubyIndexConstants.SEPARATOR);
		builder.append(simpleName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		builder.append(enclosingTypeName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		builder.append(superClassOrModule);
		builder.append(classOrModule);

		return builder.toString();
	}

	private String getNamespace(String name)
	{
		if (name == null)
		{
			return null;
		}
		int index = name.lastIndexOf(NAMESPACE_DELIMETER);
		if (index != -1)
		{
			return name.substring(0, index);
		}
		return StringUtil.EMPTY;
	}

	private String getSimpleName(String name)
	{
		return lastSegment(name, NAMESPACE_DELIMETER);
	}

	public void acceptMethodVisibilityChange(String methodName, Visibility visibility)
	{
	}

	public void acceptMethodReference(String name, int argCount, int offset)
	{
		addIndex(IRubyIndexConstants.METHOD_REF, createMethodRefKey(name, argCount));
	}

	private String createMethodRefKey(String name, int argCount)
	{
		return name + IRubyIndexConstants.SEPARATOR + String.valueOf(argCount);
	}

	private String createMethodDefKey(String methodName, String definingTypeSimpleName, String[] definingTypeNamespace,
			Visibility visibility, boolean isSingleton, int argCount)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(methodName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Defining type simple name
		builder.append(definingTypeSimpleName);
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Defining type namespace
		if (definingTypeNamespace != null && definingTypeNamespace.length > 0)
		{
			for (String enclosingName : definingTypeNamespace)
			{
				builder.append(enclosingName);
				builder.append(NAMESPACE_DELIMETER);
			}
			builder.delete(builder.length() - 2, builder.length());
		}
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Visibility
		builder.append(getVisibilityChar(visibility));
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Singleton or instance
		builder.append(isSingleton ? 'S' : 'I');
		builder.append(IRubyIndexConstants.SEPARATOR);
		// Arg count
		builder.append(String.valueOf(argCount));
		return builder.toString();
	}

	private char getVisibilityChar(Visibility visibility)
	{
		switch (visibility)
		{
			case PRIVATE:
				return 'V';
			case PROTECTED:
				return 'R';
			case PUBLIC:
				return 'P';
		}
		return 'X';
	}

	public void acceptImport(String value, int startOffset, int endOffset)
	{
		// FIXME This is really, really bad. requires are relative to loadpaths, which are dynamic.
		// IFile requireFile = file.getParent().getFile(new Path(value));
		// if (requireFile.exists())
		// {
		// addIndex(IRubyIndexConstants.REQUIRE, requireFile.getProjectRelativePath().toPortableString());
		// }
		addIndex(IRubyIndexConstants.REQUIRE, value);
	}

	public void acceptFieldReference(String name, int offset)
	{
		addIndex(IRubyIndexConstants.REF, name);
	}

	public void acceptConstructorReference(String name, int argCount, int offset)
	{
		String simpleTypeName = getSimpleName(name);
		addIndex(IRubyIndexConstants.REF, simpleTypeName);
		addIndex(IRubyIndexConstants.CONSTRUCTOR_REF, createMethodRefKey(simpleTypeName, argCount));
	}

	public void enterBlock(int startOffset, int endOffset)
	{
	}

	public void exitBlock(int endOffset)
	{
		// no-op
	}
}
