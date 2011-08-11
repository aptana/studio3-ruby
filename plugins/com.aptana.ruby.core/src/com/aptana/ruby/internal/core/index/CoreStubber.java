/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.index;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.EclipseUtil;
import com.aptana.core.util.ProcessUtil;
import com.aptana.core.util.ResourceUtil;
import com.aptana.index.core.IFileStoreIndexingParticipant;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexContainerJob;
import com.aptana.index.core.IndexManager;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.RubyProjectNature;
import com.aptana.ruby.launching.RubyLaunchingPlugin;

public class CoreStubber extends Job
{

	private static final String CORE_STUBBER_PATH = "ruby/core_stubber.rb"; //$NON-NLS-1$
	private static final String FINISH_MARKER_FILENAME = "finish_marker"; //$NON-NLS-1$

	/**
	 * A way to version the core stubs. If the core stubber script changes, be sure to bump this so new core stubs are
	 * created!
	 */
	private static final String CORE_STUBBER_VERSION = "3"; //$NON-NLS-1$

	protected static boolean fgOutOfDate = false;

	public CoreStubber()
	{
		super("Generating stubs for Ruby Core"); //$NON-NLS-1$
		setPriority(Job.LONG);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		// TODO This also needs to listen for new projects added and make sure we do the core stubbing stuff if it's
		// tied to a new interpreter!
		SubMonitor sub = SubMonitor.convert(monitor, 100);

		// Bail out early if there are no ruby files in the user's workspace
		if (!isRubyFileInWorkspace())
		{
			IResourceChangeListener fResourceListener = new IResourceChangeListener()
			{

				public void resourceChanged(IResourceChangeEvent event)
				{
					// listen for addition of ruby files/opening of projects (traverse them and look for ruby
					// files)
					IResourceDelta delta = event.getDelta();
					if (delta == null)
					{
						return;
					}
					try
					{
						final boolean[] found = new boolean[1];
						delta.accept(new IResourceDeltaVisitor()
						{

							public boolean visit(IResourceDelta delta) throws CoreException
							{
								if (found[0])
									return false;
								IResource resource = delta.getResource();
								if (resource.getType() == IResource.FILE)
								{
									if (isRubyFile(resource.getProject(), resource.getName()))
									{
										found[0] = true;
									}
									return false;
								}
								if (resource.getType() == IResource.ROOT || resource.getType() == IResource.FOLDER)
								{
									return true;
								}
								if (resource.getType() == IResource.PROJECT)
								{
									// a project was added or opened
									if (delta.getKind() == IResourceDelta.ADDED
											|| (delta.getKind() == IResourceDelta.CHANGED
													&& (delta.getFlags() & IResourceDelta.OPEN) != 0 && resource
														.isAccessible()))
									{
										// Check if project contains ruby files!
										IProject project = resource.getProject();
										RubyFileDetectingVisitor visitor = new RubyFileDetectingVisitor(project);
										project.accept(visitor, IResource.NONE);
										if (visitor.found())
										{
											found[0] = true;
											return false;
										}
									}
									else
									{
										return true;
									}
								}
								return false;
							}
						});
						if (found[0])
						{
							ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
							schedule();
						}
					}
					catch (CoreException e)
					{
						RubyCorePlugin.log(e.getStatus());
					}
				}
			};
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener,
					IResourceChangeEvent.POST_CHANGE);
			return Status.CANCEL_STATUS;
		}

		try
		{
			Set<IPath> rubyExes = new HashSet<IPath>();
			Set<IPath> pathsToIndex = new HashSet<IPath>();
			// We need to do this for every single project tied to ruby, set up it's own ruby core stub dir, std
			// lib, gems, etc.
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			{
				IPath wd = null;
				if (project != null)
				{
					wd = project.getLocation();
				}
				IPath rubyExe = RubyLaunchingPlugin.rubyExecutablePath(wd);
				rubyExes.add(rubyExe);
				pathsToIndex.addAll(getUniqueLoadpaths(project));
				pathsToIndex.addAll(RubyLaunchingPlugin.getGemPaths(project));
			}
			// Add "global" ruby
			rubyExes.add(RubyLaunchingPlugin.rubyExecutablePath(null));

			Set<File> stubDirs = new HashSet<File>();
			int unit = 50 / rubyExes.size();
			for (IPath rubyExe : rubyExes)
			{
				String rubyVersion = RubyLaunchingPlugin.getRubyVersion(rubyExe);
				File outputDir = getRubyCoreStubDir(rubyVersion);
				if (outputDir == null)
				{
					continue;
				}
				stubDirs.add(outputDir);
				File finishMarker = new File(outputDir, FINISH_MARKER_FILENAME);
				// Skip if we already generated core stubs for this ruby...
				if (!finishMarker.exists())
				{
					generateCoreStubs(rubyExe, outputDir, finishMarker);
				}
				sub.worked(unit);
			}
			sub.setWorkRemaining(50);

			final IProgressMonitor pm = Job.getJobManager().createProgressGroup();
			final List<Job> jobs = new ArrayList<Job>();

			// Check index version. if out of date, force re-index of everything!
			int currentVersion = Platform.getPreferencesService().getInt(RubyCorePlugin.PLUGIN_ID,
					RubySourceIndexer.VERSION_KEY, -1, null);
			if (currentVersion != RubySourceIndexer.CURRENT_VERSION)
			{
				fgOutOfDate = true;
			}
			for (File stubDir : stubDirs)
			{
				jobs.add(indexCoreStubs(stubDir));
			}
			for (IPath pathToIndex : pathsToIndex)
			{
				Job job = indexFiles(pathToIndex.toFile().toURI());
				if (job != null)
				{
					jobs.add(job);
				}
			}
			pm.beginTask(Messages.CoreStubber_IndexingRuby, jobs.size() * 1000);
			for (Job job : jobs)
			{
				if (job == null)
				{
					continue;
				}
				job.setProgressGroup(pm, 1000);
				job.schedule();
			}
			// Use a thread to report back to progress monitor when all the jobs are done.
			Thread t = new Thread(new Runnable()
			{

				public void run()
				{
					for (Job job : jobs)
					{
						if (job == null)
						{
							continue;
						}
						try
						{
							job.join();
						}
						catch (InterruptedException e)
						{
							// ignore
						}
					}
					pm.done();
					// Store current version of index in prefs so we can force re-index if indexer changes
					IEclipsePreferences prefs = EclipseUtil.instanceScope().getNode(RubyCorePlugin.PLUGIN_ID);
					prefs.putInt(RubySourceIndexer.VERSION_KEY, RubySourceIndexer.CURRENT_VERSION);
					try
					{
						prefs.flush();
					}
					catch (BackingStoreException e)
					{
						RubyCorePlugin.log(e);
					}
				}
			});
			t.start();
		}
		catch (Exception e)
		{
			return new Status(IStatus.ERROR, RubyCorePlugin.PLUGIN_ID, e.getMessage(), e);
		}
		finally
		{
			sub.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Traverses the workspace until we find a file that matches the ruby content type. If one is found, returns true
	 * early. Otherwise we search everything and ultimately return false.
	 * 
	 * @return
	 */
	private boolean isRubyFileInWorkspace()
	{
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (final IProject project : projects)
		{
			try
			{
				if (!project.isAccessible())
				{
					continue;
				}
				RubyFileDetectingVisitor visitor = new RubyFileDetectingVisitor(project);
				project.accept(visitor, IResource.NONE);
				if (visitor.found())
				{
					RubyProjectNature.add(project, new NullProgressMonitor());
					return true;
				}
			}
			catch (CoreException e)
			{
				// ignore
			}
		}
		return false;
	}

	class RubyFileDetectingVisitor implements IResourceProxyVisitor
	{

		private IProject fProject;
		private boolean fFound;

		RubyFileDetectingVisitor(IProject project)
		{
			this.fProject = project;
			this.fFound = false;
		}

		public boolean visit(IResourceProxy proxy)
		{
			if (fFound)
			{
				return false;
			}

			if (proxy.getType() == IResource.FILE && isRubyFile(fProject, proxy.getName()))
			{
				fFound = true;
				return false;
			}

			return true;
		}

		public boolean found()
		{
			return fFound;
		}
	}

	private boolean isRubyFile(IProject project, String filename)
	{
		try
		{
			IContentType[] types = project.getContentTypeMatcher().findContentTypesFor(filename);
			for (IContentType type : types)
			{
				if (IRubyConstants.CONTENT_TYPE_RUBY.equals(type.getId()))
				{
					return true;
				}
			}
		}
		catch (CoreException e)
		{
			// ignore
		}
		return false;
	}

	/**
	 * @deprecated Use getRubyCoreIndex(IProject)
	 * @return
	 */
	public static Index getRubyCoreIndex()
	{
		return getRubyCoreIndex(null);
	}

	protected static File getRubyCoreStubDir(IProject project)
	{
		String rubyVersion = RubyLaunchingPlugin.getRubyVersionForProject(project);
		if (rubyVersion == null)
		{
			return null;
		}
		return getRubyCoreStubDir(rubyVersion);
	}

	protected static File getRubyCoreStubDir(String rubyVersion)
	{
		// TODO Maybe convert ruby version string into a more readable string, not integer hash code!
		// Store core stubs based on ruby version string...
		IPath outputPath = RubyCorePlugin.getDefault().getStateLocation()
				.append(Integer.toString(rubyVersion.hashCode())).append(CORE_STUBBER_VERSION);
		return outputPath.toFile();
	}

	protected List<Job> indexGems(IProject project)
	{
		List<Job> jobs = new ArrayList<Job>();
		for (IPath gemPath : RubyLaunchingPlugin.getGemPaths(project))
		{
			jobs.add(indexFiles(gemPath.toFile().toURI()));
		}
		return jobs;
	}

	/**
	 * @deprecated use getGemPaths(IProject)
	 * @return
	 */
	public static Set<IPath> getGemPaths()
	{
		return RubyLaunchingPlugin.getGemPaths(null);
	}

	protected List<Job> indexStdLib(Set<IPath> uniqueLoadPaths)
	{
		List<Job> jobs = new ArrayList<Job>();
		for (IPath loadpath : uniqueLoadPaths)
		{
			Job job = indexFiles(loadpath.toFile().toURI());
			if (job != null)
			{
				jobs.add(job);
			}
		}
		return jobs;
	}

	/**
	 * @deprecated use getLoadPaths(IProject)
	 * @return
	 */
	public static Set<IPath> getLoadpaths()
	{
		return RubyLaunchingPlugin.getLoadpaths(null);
	}

	protected Job indexCoreStubs(File outputDir)
	{
		return indexFiles(Messages.CoreStubber_IndexingRubyCore, outputDir.toURI());
	}

	protected void generateCoreStubs(IPath rubyExe, File outputDir, File finishMarker) throws IOException
	{
		// FIXME Need to be able to version this file? This file has changed...
		URL url = FileLocator.find(RubyCorePlugin.getDefault().getBundle(), new Path(CORE_STUBBER_PATH), null);
		File stubberScript = ResourceUtil.resourcePathToFile(url);

		IStatus stubberResult = ProcessUtil.runInBackground((rubyExe == null) ? "ruby" : rubyExe.toOSString(), null, //$NON-NLS-1$
				ShellExecutable.getEnvironment(), stubberScript.getAbsolutePath(), outputDir.getAbsolutePath());
		if (stubberResult == null || !stubberResult.isOK())
		{
			RubyCorePlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, RubyCorePlugin.PLUGIN_ID, (stubberResult == null) ? "" //$NON-NLS-1$
							: stubberResult.getMessage(), null));
		}
		else
		{
			// Now write empty file as a marker that core stubs were generated to completion...
			finishMarker.createNewFile();
		}
	}

	protected Job indexFiles(String message, URI outputDir)
	{
		return new IndexRubyContainerJob(message, outputDir);
	}

	protected Job indexFiles(URI outputDir)
	{
		return new IndexRubyContainerJob(outputDir);
	}

	private static class IndexRubyContainerJob extends IndexContainerJob
	{
		private IndexRubyContainerJob(URI outputDir)
		{
			super(outputDir);
		}

		private IndexRubyContainerJob(String message, URI outputDir)
		{
			super(message, outputDir);
		}

		protected List<Map.Entry<IFileStoreIndexingParticipant, Set<IFileStore>>> mapParticipantsToFiles(
				Set<IFileStore> fileStores)
		{
			Map<IFileStoreIndexingParticipant, Set<IFileStore>> map = new HashMap<IFileStoreIndexingParticipant, Set<IFileStore>>();
			map.put(new RubyFileIndexingParticipant(), fileStores);
			return new ArrayList<Map.Entry<IFileStoreIndexingParticipant, Set<IFileStore>>>(map.entrySet());
		}

		@Override
		protected Set<IFileStore> filterFiles(long indexLastModified, Set<IFileStore> files)
		{
			Set<IFileStore> firstPass;
			if (fgOutOfDate)
			{
				firstPass = files;
			}
			else
			{
				firstPass = super.filterFiles(indexLastModified, files);
			}
			if (firstPass == null || firstPass.isEmpty())
			{
				return firstPass;
			}
			// OK, now limit to only files that are ruby type!
			IContentTypeManager manager = Platform.getContentTypeManager();
			Set<IContentType> types = new HashSet<IContentType>();
			types.add(manager.getContentType(IRubyConstants.CONTENT_TYPE_RUBY));
			types.add(manager.getContentType(IRubyConstants.CONTENT_TYPE_RUBY_AMBIGUOUS));
			Set<IFileStore> filtered = new HashSet<IFileStore>();
			for (IFileStore store : firstPass)
			{
				if (hasType(store, types))
				{
					filtered.add(store);
				}
			}
			return filtered;
		}
	}

	public static Collection<Index> getStdLibIndices(IProject iProject)
	{
		Collection<Index> indices = new ArrayList<Index>();
		for (IPath path : getUniqueLoadpaths(iProject))
		{
			indices.add(IndexManager.getInstance().getIndex(path.toFile().toURI()));
		}
		return indices;
	}

	/**
	 * Takes the loadpaths and removes any paths that are subpaths of another entry. i.e. If we have /usr/local/ruby/1.8
	 * and /usr/local/ruby/1.8/site_ruby, the latter will be removed. This is primarily used to avoid indexing subdirs
	 * multiple times.
	 * 
	 * @return
	 */
	private static Collection<IPath> getUniqueLoadpaths(IProject project)
	{
		List<IPath> dupe = new ArrayList<IPath>(RubyLaunchingPlugin.getLoadpaths(project));
		Collections.sort(dupe, new Comparator<IPath>()
		{
			public int compare(IPath p1, IPath p2)
			{
				return p1.segmentCount() - p2.segmentCount();
			}
		});
		Set<IPath> uniques = new HashSet<IPath>();
		for (IPath current : dupe)
		{
			boolean add = true;
			if (!uniques.isEmpty())
			{
				for (IPath unique : uniques)
				{
					if (unique.isPrefixOf(current))
					{
						add = false;
						break;
					}
				}
			}
			if (add)
			{
				uniques.add(current);
			}
		}
		return uniques;
	}

	public static Index getRubyCoreIndex(IProject project)
	{
		File stubDir = getRubyCoreStubDir(project);
		if (stubDir == null)
		{
			return null;
		}
		return IndexManager.getInstance().getIndex(stubDir.toURI());
	}

}
