package com.aptana.ruby.internal.rake;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

@SuppressWarnings("nls")
public class RakeFileFinderTest
{

	private RakeFileFinder finder;

	@Before
	public void setUp() throws Exception
	{
//		super.setUp();
		finder = new RakeFileFinder();
	}

	@After
	public void tearDown() throws Exception
	{
		finder = null;
//		super.tearDown();
	}

	@Test
	public void testVisitRakefile() throws Exception
	{
		final String filename = "Rakefile";
		final String parentPath = "path/to/file";

		IResourceProxy proxy = makeProxy(IResource.FILE, filename, parentPath);

		assertFalse("Shouldn't continue to traverse into members, since this is Rakefile", finder.visit(proxy));
		assertEquals("Should have used parent path of Rakefile as working dir", Path.fromPortableString(parentPath),
				finder.getWorkingDirectory());
	}

	@Test
	public void testVisitNonRakefile() throws Exception
	{
		final String filename = "file.rb";
		final String parentPath = "path/to/file";

		IResourceProxy proxy = makeProxy(IResource.FILE, filename, parentPath);

		assertFalse("Shouldn't continue to traverse into members, since this is a file", finder.visit(proxy));
		assertNull("Shouldn't have found a Rakefile, so null working directory", finder.getWorkingDirectory());
	}

	@Test
	public void testVisitFolder() throws Exception
	{
		final String filename = "folder";
		final String parentPath = "path/to/file";

		IResourceProxy proxy = makeProxy(IResource.FOLDER, filename, parentPath);

		assertTrue("Should continue to traverse into members, since this is a folder", finder.visit(proxy));
		assertNull("Shouldn't have found a Rakefile, so null working directory", finder.getWorkingDirectory());
	}

	@Test
	public void testVisitProject() throws Exception
	{
		final String filename = "project";
		final String parentPath = "";

		IResourceProxy proxy = makeProxy(IResource.PROJECT, filename, parentPath);

		assertTrue("Should continue to traverse into members, since this is a project", finder.visit(proxy));
		assertNull("Shouldn't have found a Rakefile, so null working directory", finder.getWorkingDirectory());
	}

	@Test
	public void testVisitRoot() throws Exception
	{
		final String filename = "";
		final String parentPath = "";

		IResourceProxy proxy = makeProxy(IResource.ROOT, filename, parentPath);

		assertTrue("Should continue to traverse into members, since this is the workspace root", finder.visit(proxy));
		assertNull("Shouldn't have found a Rakefile, so null working directory", finder.getWorkingDirectory());
	}

	/**
	 * Generate a fake IResourceProxy for testing purposes.
	 * 
	 * @param type
	 * @param filename
	 * @param parentPath
	 * @return
	 */
	protected IResourceProxy makeProxy(final int type, final String filename, final String parentPath)
	{
		return new IResourceProxy()
		{

			public IResource requestResource()
			{
				return new IResource()
				{

					public boolean isConflicting(ISchedulingRule rule)
					{
						return false;
					}

					public boolean contains(ISchedulingRule rule)
					{
						return false;
					}

					@SuppressWarnings("rawtypes")
					public Object getAdapter(Class adapter)
					{
						return null;
					}

					public void touch(IProgressMonitor monitor) throws CoreException
					{
					}

					public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException
					{
					}

					public void setSessionProperty(QualifiedName key, Object value) throws CoreException
					{
					}

					public void setResourceAttributes(ResourceAttributes attributes) throws CoreException
					{
					}

					public void setReadOnly(boolean readOnly)
					{
					}

					public void setPersistentProperty(QualifiedName key, String value) throws CoreException
					{
					}

					public long setLocalTimeStamp(long value) throws CoreException
					{
						return 0;
					}

					public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException
					{
					}

					public void setHidden(boolean isHidden) throws CoreException
					{
					}

					public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException
					{
					}

					public void setDerived(boolean isDerived) throws CoreException
					{
					}

					public void revertModificationStamp(long value) throws CoreException
					{
					}

					public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException
					{
					}

					public void move(IProjectDescription description, boolean force, boolean keepHistory,
							IProgressMonitor monitor) throws CoreException
					{
					}

					public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
							throws CoreException
					{
					}

					public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException
					{
					}

					public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException
					{
					}

					public boolean isVirtual()
					{
						return false;
					}

					public boolean isTeamPrivateMember(int options)
					{
						return false;
					}

					public boolean isTeamPrivateMember()
					{
						return false;
					}

					public boolean isSynchronized(int depth)
					{
						return false;
					}

					public boolean isReadOnly()
					{
						return false;
					}

					public boolean isPhantom()
					{
						return false;
					}

					public boolean isLocal(int depth)
					{
						return false;
					}

					public boolean isLinked(int options)
					{
						return false;
					}

					public boolean isLinked()
					{
						return false;
					}

					public boolean isHidden(int options)
					{
						return false;
					}

					public boolean isHidden()
					{
						return false;
					}

					public boolean isDerived(int options)
					{
						return false;
					}

					public boolean isDerived()
					{
						return false;
					}

					public boolean isAccessible()
					{
						return false;
					}

					public IWorkspace getWorkspace()
					{
						return null;
					}

					public int getType()
					{
						return type;
					}

					public Object getSessionProperty(QualifiedName key) throws CoreException
					{
						return null;
					}

					public Map<QualifiedName, Object> getSessionProperties() throws CoreException
					{
						return null;
					}

					public ResourceAttributes getResourceAttributes()
					{
						return null;
					}

					public URI getRawLocationURI()
					{
						return null;
					}

					public IPath getRawLocation()
					{
						return null;
					}

					public IPath getProjectRelativePath()
					{
						return Path.fromPortableString(parentPath + "/" + filename); //$NON-NLS-1$
					}

					public IProject getProject()
					{
						return null;
					}

					public String getPersistentProperty(QualifiedName key) throws CoreException
					{
						return null;
					}

					public Map<QualifiedName, String> getPersistentProperties() throws CoreException
					{
						return null;
					}

					public IPathVariableManager getPathVariableManager()
					{
						return null;
					}

					public IContainer getParent()
					{
						return null;
					}

					public String getName()
					{
						return filename;
					}

					public long getModificationStamp()
					{
						return 0;
					}

					public IMarker getMarker(long id)
					{
						return null;
					}

					public URI getLocationURI()
					{
						return null;
					}

					public IPath getLocation()
					{
						return null;
					}

					public long getLocalTimeStamp()
					{
						return 0;
					}

					public IPath getFullPath()
					{
						return null;
					}

					public String getFileExtension()
					{
						return null;
					}

					public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth)
							throws CoreException
					{
						return 0;
					}

					public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException
					{
						return null;
					}

					public IMarker findMarker(long id) throws CoreException
					{
						return null;
					}

					public boolean exists()
					{
						return false;
					}

					public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException
					{
					}

					public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException
					{
					}

					public void delete(boolean force, IProgressMonitor monitor) throws CoreException
					{
					}

					public IResourceProxy createProxy()
					{
						return null;
					}

					public IMarker createMarker(String type) throws CoreException
					{
						return null;
					}

					public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
							throws CoreException
					{
					}

					public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor)
							throws CoreException
					{
					}

					public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException
					{
					}

					public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException
					{
					}

					public void clearHistory(IProgressMonitor monitor) throws CoreException
					{
					}

					public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException
					{
					}

					public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms)
							throws CoreException
					{
					}

					public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException
					{
					}

					public void accept(IResourceVisitor visitor) throws CoreException
					{
					}

					public void accept(IResourceProxyVisitor visitor,
							int depth, int memberFlags) throws CoreException 
					{
					}
				};
			}

			public IPath requestFullPath()
			{
				return null;
			}

			public boolean isTeamPrivateMember()
			{
				return false;
			}

			public boolean isPhantom()
			{
				return false;
			}

			public boolean isLinked()
			{
				return false;
			}

			public boolean isHidden()
			{
				return false;
			}

			public boolean isDerived()
			{
				return false;
			}

			public boolean isAccessible()
			{
				return false;
			}

			public int getType()
			{
				return type;
			}

			public Object getSessionProperty(QualifiedName key)
			{
				return null;
			}

			public String getName()
			{
				return filename;
			}

			public long getModificationStamp()
			{
				return 0;
			}
		};
	}
}
