#! /usr/bin/ruby

# Default to comparing from master to HEAD
since = ARGV[0] || 'master'
until_sha = ARGV[1] || 'HEAD'

# set up common locations
plugins_dir = File.join(Dir.pwd, 'plugins')
features_dir = File.join(Dir.pwd, 'features')

feature_bump = 3 # default to only bugfix jump
# TODO Do some sanity check to ensure that there's no uncommitted changes in working copy!

Dir.foreach(plugins_dir) do |dir|
  next if dir == '.' || dir == '..' || dir == '.DS_Store'

  changes = 0
  command = "git diff --name-only #{since}...#{until_sha} -- plugins/#{dir}"
  IO.popen(command) do |io|
    output = io.read
    changes = output.split(/\r?\n|\r/).length if !output.empty?
  end
  
  if changes == 0
    puts "#{dir}: No changes!"
  else
    puts "#{dir}: #{changes} change#{changes > 1 ? 's' : ''}"
    
    # FIXME This looks at the current version of the manifest. We should use the "since" version as base for determination.
    manifest = File.join(plugins_dir, dir, "META-INF", "MANIFEST.MF")
    current_version = nil
    qualifier = nil
    open(manifest, 'r') do |manifest_io|
      contents = manifest_io.read
      match = /Bundle-Version: (\d+\.\d+\.\d+)(\.\w+)?/.match(contents)
      current_version = match[1]
      qualifier = match[2]
    end
    # Prompt user if they want to see diff to try and determine what version part should change
    print "View diff of changes? (y/N): "
    answer = gets.chomp
    if answer == 'y' || answer == 'Y'
      system "git diff #{since}...#{until_sha} -- plugins/#{dir}"
    end
    
    # FIXME Is there any easier way to prompt the user about this stuff?
    bump = :bugfix
    # TODO Given the exported package list in the MANIFEST, can we tell if there have been any breaking/new API changes?
    # We can definitely tell if a new package has been added, and force bump of :minor
    # We can tell if a package has been removed from the list, and force :major
    print "Are there breaking public API changes (removed/modified classes, methods, packages)? (y/N): "
    answer = gets.chomp
    if answer == 'y' || answer == 'Y'
      bump = :major
    else
      # Ask about new public classes/methods/etc
      print "Are there new public APIs (new classes, new methods/constants on accessible classes, new packages)? (y/N): "
      answer = gets.chomp
      bump = :minor if answer == 'y' || answer == 'Y'
    end
    
    # Bump the plugin version
    new_version = current_version.split('.')
    index = case bump
            when :major
              0
            when :minor
              1
            when :bugfix
              2
            end
    new_version[index] = new_version[index].to_i + 1
    new_version = new_version.join('.') + qualifier
    
    feature_bump = [feature_bump, index].min # feature must be bumped at lowest index that any given plugin is!
    
    # TODO Check the "until_sha" version value and compare versus what we determined here. if they don't match, let user know?
    
    # TODO Update the MANIFEST.MF for the user
    puts "-> Should bump version from #{current_version + qualifier} to #{new_version}!\n"
  end
end

# TODO Loop through the features and update their versions for user based on feature_bump
# feature is stored in feature.xml as an attribute on a root "feature" tag
