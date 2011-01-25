/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;

import com.aptana.core.ShellExecutable;
import com.aptana.core.util.ExecutableUtil;
import com.aptana.core.util.ProcessUtil;
import com.aptana.core.util.ResourceUtil;
import com.aptana.editor.ruby.index.RubyFileIndexingParticipant;
import com.aptana.index.core.IFileStoreIndexingParticipant;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexContainerJob;
import com.aptana.index.core.IndexManager;
import com.aptana.ruby.launching.RubyLaunchingPlugin;

// TODO Move this to com.aptana.ruby.core plugin!
public class CoreStubber extends Job
{

	private static final String GEM_COMMAND = "gem"; //$NON-NLS-1$
	private static final String RUBY_EXE = "ruby"; //$NON-NLS-1$
	private static final String VERSION_SWITCH = "-v"; //$NON-NLS-1$

	private static final String CORE_STUBBER_PATH = "ruby/core_stubber.rb"; //$NON-NLS-1$
	private static final String FINISH_MARKER_FILENAME = "finish_marker"; //$NON-NLS-1$

	public CoreStubber()
	{
		super("Generating stubs for Ruby Core"); //$NON-NLS-1$
		setPriority(Job.LONG);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
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
						RubyEditorPlugin.log(e.getStatus());
					}
				}
			};
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener,
					IResourceChangeEvent.POST_CHANGE);
			return Status.CANCEL_STATUS;
		}

		try
		{
			File outputDir = getRubyCoreStubDir();
			if (outputDir == null)
			{
				return Status.CANCEL_STATUS;
			}
			File finishMarker = new File(outputDir, FINISH_MARKER_FILENAME);
			// Skip if we already generated core stubs for this ruby...
			if (!finishMarker.exists())
			{
				generateCoreStubs(outputDir, finishMarker);
			}
			sub.setWorkRemaining(10);

			final IProgressMonitor pm = Job.getJobManager().createProgressGroup();
			final List<Job> jobs = new ArrayList<Job>();
			jobs.add(indexCoreStubs(outputDir));
			jobs.addAll(indexStdLib());
			jobs.addAll(indexGems());
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
				}
			});
			t.start();
		}
		catch (Exception e)
		{
			return new Status(IStatus.ERROR, RubyEditorPlugin.PLUGIN_ID, e.getMessage(), e);
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
					// TODO If this project doesn't have the ruby nature, add it!
//					RubyProjectNature.add(project, new NullProgressMonitor());
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

		public boolean visit(IResourceProxy proxy) throws CoreException
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

	public static Index getRubyCoreIndex()
	{
		File stubDir = getRubyCoreStubDir();
		if (stubDir == null)
		{
			return null;
		}
		return IndexManager.getInstance().getIndex(stubDir.toURI());
	}

	protected static File getRubyCoreStubDir()
	{
		String rubyVersion = ProcessUtil.outputForCommand(getRubyExecutable(), null, ShellExecutable.getEnvironment(),
				VERSION_SWITCH);
		if (rubyVersion == null)
		{
			return null;
		}
		// Store core stubs based on ruby version string...
		IPath outputPath = RubyEditorPlugin.getDefault().getStateLocation().append(Integer.toString(rubyVersion.hashCode()))
				.append(RUBY_EXE);
		return outputPath.toFile();
	}

	private static String getRubyExecutable()
	{
		IPath ruby = RubyLaunchingPlugin.rubyExecutablePath();
		if (ruby != null)
		{
			return ruby.toOSString();
		}
		return RUBY_EXE;
	}

	protected List<Job> indexGems()
	{
		List<Job> jobs = new ArrayList<Job>();
		for (IPath gemPath : getGemPaths())
		{
			jobs.add(indexFiles(gemPath.toFile().toURI()));
		}
		return jobs;
	}

	public static Set<IPath> getGemPaths()
	{
		IPath gemCommand = ExecutableUtil.find(GEM_COMMAND, true, null);
		String command = GEM_COMMAND;
		if (gemCommand != null)
		{
			command = gemCommand.toOSString();
		}
		// FIXME Not finding my user gem path on Windows...
		String gemEnvOutput = ProcessUtil.outputForCommand(command, null, ShellExecutable.getEnvironment(),
				"env", "gempath"); //$NON-NLS-1$ //$NON-NLS-2$
		if (gemEnvOutput == null)
		{
			return Collections.emptySet();
		}
		Set<IPath> paths = new HashSet<IPath>();
		String[] gemPaths = gemEnvOutput.split(File.pathSeparator);
		if (gemPaths != null)
		{
			for (String gemPath : gemPaths)
			{
				IPath gemsPath = new Path(gemPath).append("gems"); //$NON-NLS-1$
				paths.add(gemsPath);
			}
		}
		return paths;
	}

	protected List<Job> indexStdLib()
	{
		List<Job> jobs = new ArrayList<Job>();
		for (IPath loadpath : getLoadpaths())
		{
			Job job = indexFiles(loadpath.toFile().toURI());
			if (job != null)
			{
				jobs.add(job);
			}
		}
		return jobs;
	}

	public static Set<IPath> getLoadpaths()
	{
		String rawLoadPathOutput = ProcessUtil.outputForCommand(getRubyExecutable(), null, ShellExecutable.getEnvironment(),
				"-e", "puts $:"); //$NON-NLS-1$ //$NON-NLS-2$
		if (rawLoadPathOutput == null)
		{
			return Collections.emptySet();
		}
		Set<IPath> paths = new HashSet<IPath>();
		String[] loadpaths = rawLoadPathOutput.split("\r\n|\r|\n"); //$NON-NLS-1$
		if (loadpaths != null)
		{
			// TODO What about when one loadpath is a parent of another, just filter to parent?
			for (String loadpath : loadpaths)
			{
				if (loadpath.equals(".")) //$NON-NLS-1$
					continue;
				paths.add(new Path(loadpath));
			}
		}
		return paths;
	}

	protected Job indexCoreStubs(File outputDir)
	{
		return indexFiles(Messages.CoreStubber_IndexingRubyCore, outputDir.toURI());
	}

	protected void generateCoreStubs(File outputDir, File finishMarker) throws IOException
	{
		URL url = FileLocator.find(RubyEditorPlugin.getDefault().getBundle(), new Path(CORE_STUBBER_PATH), null);
		File stubberScript = ResourceUtil.resourcePathToFile(url);

		Map<Integer, String> stubberResult = ProcessUtil.runInBackground(getRubyExecutable(), null,
				ShellExecutable.getEnvironment(), stubberScript.getAbsolutePath(), outputDir.getAbsolutePath());
		int exitCode = stubberResult.keySet().iterator().next();
		if (exitCode != 0)
		{
			String stubberOutput = stubberResult.values().iterator().next();
			RubyEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, RubyEditorPlugin.PLUGIN_ID, stubberOutput, null));
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
		public IndexRubyContainerJob(URI outputDir)
		{
			super(outputDir);
		}

		public IndexRubyContainerJob(String message, URI outputDir)
		{
			super(message, outputDir);
		}

		protected Map<IFileStoreIndexingParticipant, Set<IFileStore>> mapParticipantsToFiles(Set<IFileStore> fileStores)
		{
			Map<IFileStoreIndexingParticipant, Set<IFileStore>> map = new HashMap<IFileStoreIndexingParticipant, Set<IFileStore>>();
			map.put(new RubyFileIndexingParticipant(), fileStores);
			return map;
		}

		@Override
		protected Set<IFileStore> filterFiles(long indexLastModified, Set<IFileStore> files)
		{
			Set<IFileStore> firstPass = super.filterFiles(indexLastModified, files);
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

}
