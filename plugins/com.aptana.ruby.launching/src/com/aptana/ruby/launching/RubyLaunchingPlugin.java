/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.launching;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.contentobjects.jnotify.IJNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.aptana.core.ShellExecutable;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.ExecutableUtil;
import com.aptana.core.util.PlatformUtil;
import com.aptana.core.util.ProcessUtil;
import com.aptana.filewatcher.FileWatcher;

public class RubyLaunchingPlugin extends Plugin
{
	public static final String PLUGIN_ID = "com.aptana.ruby.launching"; //$NON-NLS-1$

	private static final String GEM_COMMAND = "gem"; //$NON-NLS-1$
	private static final String RUBYW = "rubyw"; //$NON-NLS-1$
	private static final String RBENV = "rbenv"; //$NON-NLS-1$
	public static final String RUBY = "ruby"; //$NON-NLS-1$
	public static final String RAKE = "rake"; //$NON-NLS-1$
	public static final String RAKEFILE = "Rakefile"; //$NON-NLS-1$

	/**
	 * Map from project to ruby version. FIXME make use of the workingDirToRubyExe map?
	 */
	private static Map<IProject, String> projectToVersion = new HashMap<IProject, String>();
	/**
	 * map of working directories to the corresponding Ruby interpreter found on PATH there.
	 */
	private static Map<IPath, IPath> workingDirToRubyExe = new HashMap<IPath, IPath>();
	/**
	 * Map from ruby interpreter to loadpaths
	 */
	private static Map<String, Set<IPath>> rubyToLoadPaths = new HashMap<String, Set<IPath>>();
	/**
	 * Map from ruby interpreter to set of gem paths. FIXME I'm not sure this is 100% RVM friendly since they could use
	 * gemsets...
	 */
	private static Map<String, Set<IPath>> rubyToGemPaths = new HashMap<String, Set<IPath>>();
	/**
	 * Cache from ruby interpreter path to version string
	 */
	private static Map<IPath, String> pathToVersion = new HashMap<IPath, String>();

	protected static RubyLaunchingPlugin plugin;

	private static RbenvVersionListener rbEnvVersionListener;

	private static Map<IPath, Integer> filewatcherIds = new HashMap<IPath, Integer>();

	public static IPath getRakePath(IPath workingDir)
	{
		return getBinaryScriptPath(RAKE, workingDir);
	}

	public static IPath resolveRBENVShimPath(IPath rbenvShimPath, IPath workingDir)
	{
		if (rbenvShimPath == null)
		{
			return null;
		}

		// if we're using rbenv, resolve to the underlying install/script we're targeting.
		// we can't chain the rbenv shims together on the command line or it all blows up.
		if (!Platform.OS_WIN32.equals(Platform.getOS()) && rbenvShimPath.toOSString().contains("/.rbenv/")) //$NON-NLS-1$
		{
			IPath rbEnvPath = ExecutableUtil.find(RBENV, false, null, workingDir);
			if (rbEnvPath != null)
			{
				IStatus status = ProcessUtil.runInBackground(rbEnvPath.toOSString(), workingDir,
						"which", rbenvShimPath.lastSegment()); //$NON-NLS-1$
				if (status.isOK())
				{
					return Path.fromOSString(status.getMessage());
				}
			}
		}
		return rbenvShimPath;
	}

	/**
	 * Search for the applicable ruby executable for the working dir. If no working dir is set, we won't take rvmrc into
	 * account (we'll use global PATH).
	 * 
	 * @param workingDir
	 * @return
	 */
	public static IPath rubyExecutablePath(IPath workingDir)
	{
		// Use Path.ROOT in place of null working dir
		IPath pathKey = (workingDir == null ? Path.ROOT : workingDir);
		if (!workingDirToRubyExe.containsKey(pathKey))
		{
			IPath path = null;
			if (Platform.OS_WIN32.equals(Platform.getOS()))
			{
				path = ExecutableUtil.find(RUBYW, true, getCommonRubyBinaryLocations(RUBYW), workingDir);
			}
			if (path == null)
			{
				path = ExecutableUtil.find(RUBY, true, getCommonRubyBinaryLocations(RUBY), workingDir);
				IPath resolved = resolveRBENVShimPath(path, workingDir);
				if (resolved != null)
				{
					if (workingDir != null && !resolved.equals(path))
					{
						// We had to dereference an rbenv shim. We need to hook up some listener here to handle
						// .rbenv-version file changes
						try
						{
							if (rbEnvVersionListener == null)
							{
								rbEnvVersionListener = new RbenvVersionListener();
							}
							Integer watchId = filewatcherIds.get(pathKey);
							if (watchId == null)
							{
								watchId = FileWatcher.addWatch(workingDir.toOSString(), IJNotify.FILE_ANY, false,
										rbEnvVersionListener);
								filewatcherIds.put(pathKey, watchId);
							}
						}
						catch (JNotifyException e)
						{
							IdeLog.logError(getDefault(), e);
						}
					}
					path = resolved;
				}
			}

			if (IdeLog.isInfoEnabled(getDefault(), IDebugScopes.DEBUG))
			{
				IdeLog.logInfo(getDefault(),
						MessageFormat.format("Found ruby executable ''{0}'' for working directory: {1}", path, pathKey)); //$NON-NLS-1$
			}
			workingDirToRubyExe.put(pathKey, path);
		}
		return workingDirToRubyExe.get(pathKey);
	}

	/**
	 * Return an ordered list of common locations that you'd find a ruby binary.
	 * 
	 * @return
	 */
	private static List<IPath> getCommonRubyBinaryLocations(String binaryName)
	{
		List<IPath> locations = new ArrayList<IPath>();
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			locations.add(Path.fromOSString("C:\\ruby\\bin").append(binaryName).addFileExtension("exe")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			locations.add(Path.fromOSString(PlatformUtil.expandEnvironmentStrings("~/.rvm/bin/" + binaryName))); //$NON-NLS-1$
			locations.add(Path.fromOSString("/opt/local/bin/").append(binaryName)); //$NON-NLS-1$
			locations.add(Path.fromOSString("/usr/local/bin/").append(binaryName)); //$NON-NLS-1$
			locations.add(Path.fromOSString("/usr/bin/").append(binaryName)); //$NON-NLS-1$
		}
		if (Platform.OS_MACOSX.equals(Platform.getOS()))
		{
			locations.add(Path.fromOSString("/System/Library/Frameworks/Ruby.framework/Versions/Current/usr/bin/") //$NON-NLS-1$
					.append(binaryName));
		}
		return locations;
	}

	/**
	 * Return the version string for the ruby interpreter set up for a given project.
	 * 
	 * @param project
	 * @return
	 */
	public static synchronized String getRubyVersionForProject(IProject project)
	{
		if (project == null)
		{
			return null;
		}
		// This seems expensive, so we're caching the version per-project
		if (projectToVersion.containsKey(project))
		{
			return projectToVersion.get(project);
		}

		IPath rubyExe = rubyExecutablePath(project.getLocation());
		String version = getRubyVersion(rubyExe);
		projectToVersion.put(project, version);
		return version;
	}

	public synchronized static Set<IPath> getLoadpaths(IProject project)
	{
		IPath workingDir = (project == null ? null : project.getLocation());
		IPath rubyPath = rubyExecutablePath(workingDir);
		String rubyExe = (rubyPath == null ? RUBY : rubyPath.toOSString());
		if (!rubyToLoadPaths.containsKey(rubyExe))
		{
			Map<String, String> env = null;
			if (!Platform.OS_WIN32.equals(Platform.getOS()))
			{
				env = ShellExecutable.getEnvironment(workingDir);
			}

			String rawLoadPathOutput = ProcessUtil.outputForCommand(rubyExe, null, env, "-e", "puts $:"); //$NON-NLS-1$ //$NON-NLS-2$
			if (rawLoadPathOutput == null)
			{
				rubyToLoadPaths.put(rubyExe, null);
			}
			else
			{
				Set<IPath> paths = new HashSet<IPath>();
				String[] loadpaths = rawLoadPathOutput.split("\r\n|\r|\n"); //$NON-NLS-1$
				if (loadpaths != null)
				{
					for (String loadpath : loadpaths)
					{
						if (".".equals(loadpath)) //$NON-NLS-1$
						{
							continue;
						}
						paths.add(new Path(loadpath));
					}
				}
				rubyToLoadPaths.put(rubyExe, paths);
			}
		}
		Set<IPath> result = rubyToLoadPaths.get(rubyExe);
		if (result == null)
		{
			return Collections.emptySet();
		}
		return result;
	}

	/**
	 * Handles resolving RBENV shims
	 * 
	 * @param binary
	 * @param pathsToSearch
	 * @param workingDirectory
	 * @return
	 */
	public static IPath getBinaryScriptPath(String binary, List<IPath> pathsToSearch, IPath workingDirectory)
	{
		IPath path = ExecutableUtil.find(binary, false, pathsToSearch, workingDirectory);
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			// No RBENV on Windows!
			return path;
		}
		return resolveRBENVShimPath(path, workingDirectory);
	}

	/**
	 * Handles resolving RBENV shims
	 * 
	 * @param binary
	 * @param workingDirectory
	 * @return
	 */
	public static IPath getBinaryScriptPath(String binary, IPath workingDirectory)
	{
		return getBinaryScriptPath(binary, null, workingDirectory);
	}

	public synchronized static Set<IPath> getGemPaths(IProject project)
	{
		// FIXME this is including every single gem! We should narrow the list down based on Gemfile in project root if
		// we can!
		IPath wd = (project == null ? null : project.getLocation());
		IPath rubyPath = rubyExecutablePath(wd);
		String rubyPathString = rubyPath == null ? RUBY : rubyPath.toOSString();

		if (!rubyToGemPaths.containsKey(rubyPathString))
		{
			IPath gemBinPath = getBinaryScriptPath(GEM_COMMAND, wd);
			String gemCommand = gemBinPath == null ? GEM_COMMAND : gemBinPath.toOSString();
			// FIXME Will this actually behave properly with RVM?
			// FIXME Not finding my user gem path on Windows...

			Map<String, String> env = null;
			if (!Platform.OS_WIN32.equals(Platform.getOS()))
			{
				env = ShellExecutable.getEnvironment(wd);
			}
			IStatus status = ProcessUtil.runInBackground(rubyPathString, wd, env, gemCommand, "env", "gempath"); //$NON-NLS-1$ //$NON-NLS-2$
			String gemEnvOutput = null;
			if (status.isOK())
			{
				gemEnvOutput = status.getMessage();
			}

			if (gemEnvOutput == null)
			{
				rubyToGemPaths.put(rubyPathString, null);
			}
			else
			{
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
				rubyToGemPaths.put(rubyPathString, paths);
			}
		}
		Set<IPath> result = rubyToGemPaths.get(rubyPathString);
		if (result == null)
		{
			return Collections.emptySet();
		}
		return result;
	}

	public RubyLaunchingPlugin()
	{
		super();
	}

	public static Plugin getDefault()
	{
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			for (Map.Entry<IPath, Integer> entry : filewatcherIds.entrySet())
			{
				try
				{
					FileWatcher.removeWatch(entry.getValue());
				}
				catch (Exception e)
				{
					IdeLog.logError(getDefault(), e);
				}
			}
		}
		finally
		{
			filewatcherIds = null;
			plugin = null;
			projectToVersion = null;
			workingDirToRubyExe = null;
			rubyToLoadPaths = null;
			pathToVersion = null;
			super.stop(context);
		}
	}

	public static String getPluginIdentifier()
	{
		return PLUGIN_ID;
	}

	public synchronized static String getRubyVersion(IPath rubyExe)
	{
		if (!pathToVersion.containsKey(rubyExe))
		{
			String rubyPath = (rubyExe == null ? RUBY : rubyExe.toOSString());

			Map<String, String> env = null;
			if (!Platform.OS_WIN32.equals(Platform.getOS()))
			{
				env = ShellExecutable.getEnvironment();
			}
			String version = ProcessUtil.outputForCommand(rubyPath, null, env, "-v"); //$NON-NLS-1$
			pathToVersion.put(rubyExe, version);
		}
		return pathToVersion.get(rubyExe);
	}

	/**
	 * Listens for rbenv version changes to projects. When the special file changes, we wipe our in-memory cache of ruby
	 * executable/version so we re-calculate the next time we need them.
	 * 
	 * @author cwilliams
	 */
	private static class RbenvVersionListener implements JNotifyListener
	{
		private static final String RBENV_VERSION_FILENAME = ".rbenv-version"; //$NON-NLS-1$

		public void fileRenamed(int wd, String rootPath, String oldName, String newName)
		{
			if (newName.equals(RBENV_VERSION_FILENAME))
			{
				fileCreated(wd, rootPath, newName);
			}
			else if (oldName.equals(RBENV_VERSION_FILENAME))
			{
				fileDeleted(wd, rootPath, oldName);
			}
		}

		public void fileModified(int wd, String rootPath, String name)
		{
			if (name.equals(RBENV_VERSION_FILENAME))
			{
				// Wipe out the cached version since it's been changed
				IPath path = Path.fromOSString(rootPath);
				workingDirToRubyExe.remove(path);
				pathToVersion.remove(path);
			}
		}

		public void fileDeleted(int wd, String rootPath, String name)
		{
			fileModified(wd, rootPath, name);
		}

		public void fileCreated(int wd, String rootPath, String name)
		{
			fileModified(wd, rootPath, name);
		}
	}
}
