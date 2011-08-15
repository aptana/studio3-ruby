==PREFS==
ruby.formatter.indent.class=true
ruby.formatter.indent.module=true
ruby.formatter.indent.method=true
ruby.formatter.indent.blocks=true
ruby.formatter.indent.case=false
ruby.formatter.indent.when=true
ruby.formatter.indent.if=true
ruby.formatter.line.file.require.after=1
ruby.formatter.line.file.module.between=1
ruby.formatter.line.file.class.between=1
ruby.formatter.line.file.method.between=1
ruby.formatter.line.first.before=0
ruby.formatter.line.module.before=1
ruby.formatter.line.class.before=1
ruby.formatter.line.method.before=1
ruby.formatter.lines.preserve=1
ruby.formatter.wrap.comments=false
ruby.formatter.wrap.comments.length=80
ruby.formatter.formatter.tabulation.char=editor
ruby.formatter.formatter.tabulation.size=2
ruby.formatter.formatter.indentation.size=2
ruby.formatter.formatter.on.off.enabled=false
ruby.formatter.formatter.on=@formatter:on
ruby.formatter.formatter.off=@formatter:off
==CONTENT==
# Don't change this file!
# Configure your app in config/environment.rb and config/environments/*.rb

RAILS_ROOT = "#{File.dirname(__FILE__)}/.." unless defined?(RAILS_ROOT)

module Rails
	class << self
		def boot!
			unless booted?
				preinitialize
				pick_boot.run
			end
		end

		def booted?
			defined? Rails::Initializer
		end

		def pick_boot
			(vendor_rails? ? VendorBoot : GemBoot).new
		end

		def vendor_rails?
			File.exist?("#{RAILS_ROOT}/vendor/rails")
		end

		def preinitialize
			load(preinitializer_path) if File.exist?(preinitializer_path)
		end

		def preinitializer_path
			"#{RAILS_ROOT}/config/preinitializer.rb"
		end
	end

	class Boot
		def run
			load_initializer
			Rails::Initializer.run(:set_load_path)
		end
	end

	class VendorBoot < Boot
		def load_initializer
			require "#{RAILS_ROOT}/vendor/rails/railties/lib/initializer"
			Rails::Initializer.run(:install_gem_spec_stubs)
			Rails::GemDependency.add_frozen_gem_path
		end
	end

	class GemBoot < Boot
		def load_initializer
			self.class.load_rubygems
			load_rails_gem
			require 'initializer'
		end

		def load_rails_gem
			if version = self.class.gem_version
				gem 'rails', version
			else
				gem 'rails'
			end
		rescue Gem::LoadError => load_error
			$stderr.puts %(Missing the Rails #{version} gem. Please `gem install -v=#{version} rails`, update your RAILS_GEM_VERSION setting in config/environment.rb for the Rails version you do have installed, or comment out RAILS_GEM_VERSION to use the latest version installed.)
			exit 1
			end

		class << self
			def rubygems_version
				Gem::RubyGemsVersion rescue nil
			end

			def gem_version
				if defined? RAILS_GEM_VERSION
				RAILS_GEM_VERSION
				elsif ENV.include?('RAILS_GEM_VERSION')
					ENV['RAILS_GEM_VERSION']
				else
					parse_gem_version(read_environment_rb)
				end
			end

			def load_rubygems
				min_version = '1.3.2'
				require 'rubygems'
				unless rubygems_version >= min_version
					$stderr.puts %Q(Rails requires RubyGems >= #{min_version} (you have #{rubygems_version}). Please `gem update --system` and try again.)
					exit 1
				end

			rescue LoadError
				$stderr.puts %Q(Rails requires RubyGems >= #{min_version}. Please install RubyGems and try again: http://rubygems.rubyforge.org)
				exit 1
				end

			def parse_gem_version(text)
				$1 if text =~ /^[^#]*RAILS_GEM_VERSION\s*=\s*["']([!~<>=]*\s*[\d.]+)["']/
			end

			private

			def read_environment_rb
				File.read("#{RAILS_ROOT}/config/environment.rb")
			end
		end
	end
end

# All that for this:
Rails.boot!
==FORMATTED==
# Don't change this file!
# Configure your app in config/environment.rb and config/environments/*.rb

RAILS_ROOT = "#{File.dirname(__FILE__)}/.." unless defined?(RAILS_ROOT)

module Rails
  class << self
    def boot!
      unless booted?
      preinitialize
      pick_boot.run
      end
    end

    def booted?
      defined? Rails::Initializer
    end

    def pick_boot
      (vendor_rails? ? VendorBoot : GemBoot).new
    end

    def vendor_rails?
      File.exist?("#{RAILS_ROOT}/vendor/rails")
    end

    def preinitialize
      load(preinitializer_path) if File.exist?(preinitializer_path)
    end

    def preinitializer_path
      "#{RAILS_ROOT}/config/preinitializer.rb"
    end
  end

  class Boot
    def run
      load_initializer
      Rails::Initializer.run(:set_load_path)
    end
  end

  class VendorBoot < Boot
    def load_initializer
      require "#{RAILS_ROOT}/vendor/rails/railties/lib/initializer"
      Rails::Initializer.run(:install_gem_spec_stubs)
      Rails::GemDependency.add_frozen_gem_path
    end
  end

  class GemBoot < Boot
    def load_initializer
      self.class.load_rubygems
      load_rails_gem
      require 'initializer'
    end

    def load_rails_gem
      if version = self.class.gem_version
      gem 'rails', version
      else
      gem 'rails'
      end
    rescue Gem::LoadError => load_error
      $stderr.puts %(Missing the Rails #{version} gem. Please `gem install -v=#{version} rails`, update your RAILS_GEM_VERSION setting in config/environment.rb for the Rails version you do have installed, or comment out RAILS_GEM_VERSION to use the latest version installed.)
      exit 1
      end

    class << self
      def rubygems_version
        Gem::RubyGemsVersion rescue nil
      end

      def gem_version
        if defined? RAILS_GEM_VERSION
        RAILS_GEM_VERSION
        elsif ENV.include?('RAILS_GEM_VERSION')
        ENV['RAILS_GEM_VERSION']
        else
        parse_gem_version(read_environment_rb)
        end
      end

      def load_rubygems
        min_version = '1.3.2'
        require 'rubygems'
        unless rubygems_version >= min_version
        $stderr.puts %Q(Rails requires RubyGems >= #{min_version} (you have #{rubygems_version}). Please `gem update --system` and try again.)
        exit 1
        end

      rescue LoadError
        $stderr.puts %Q(Rails requires RubyGems >= #{min_version}. Please install RubyGems and try again: http://rubygems.rubyforge.org)
        exit 1
        end

      def parse_gem_version(text)
        $1 if text =~ /^[^#]*RAILS_GEM_VERSION\s*=\s*["']([!~<>=]*\s*[\d.]+)["']/
      end

      private

      def read_environment_rb
        File.read("#{RAILS_ROOT}/config/environment.rb")
      end
    end
  end
end

# All that for this:
Rails.boot!