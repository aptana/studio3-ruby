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
begin
  require 'mechanize'
rescue LoadError
  puts "To use the crawler plugin you must install the 'mechanize' gem."
  puts "Simply enter the command 'gem install mechanize'"
end

module Watobo
  module Plugins
    module Crawler
      module Constants
      CRAWL_NONE = 0x00
      CRAWL_RUNNING = 0x01
      CRAWL_PAUSED = 0x02
       
      end
    end
  end
end


path = File.expand_path(File.dirname(__FILE__))

%w( bags grabber engine uri_mp).each do |l|
  require File.join(path, "lib", l)
end
 
if $0 == __FILE__
  module Watobo
    module Conf
      class Interceptor
        def self.port
 #         8081
        nil
        end
      end
    end
  end
  
  require 'yaml'
  if ARGV.length > 0
    url = ARGV[0]
  end

  hook = lambda{ |agent, request|
    begin
      puts request.class
      puts request.method
      puts request.methods.sort
     
      exit
      clean_jar = Mechanize::CookieJar.new
      agent.cookie_jar.each{|cookie|
        puts "Cookie: #{cookie.name}"
        clean_jar.add! cookie unless cookie.name =~ /^box/i
      }
      exit unless agent.cookie_jar.empty?(request.url)
      agent.cookie_jar = clean_jar
    rescue => bang
      puts bang
      puts bang.backtrace
    end

  }
  hook = nil
 
  crawler = Watobo::Crawler::Engine.new
  crawler.run(url, :pre_connect_hook => hook, :num_grabbers => 1 )
end
==FORMATTED==
begin
  require 'mechanize'
rescue LoadError
  puts "To use the crawler plugin you must install the 'mechanize' gem."
  puts "Simply enter the command 'gem install mechanize'"
end

module Watobo
  module Plugins
    module Crawler
      module Constants
        CRAWL_NONE = 0x00
        CRAWL_RUNNING = 0x01
        CRAWL_PAUSED = 0x02

      end
    end
  end
end

path = File.expand_path(File.dirname(__FILE__))

%w( bags grabber engine uri_mp).each do |l|
  require File.join(path, "lib", l)
end

if $0 == __FILE__
  module Watobo
    module Conf
      class Interceptor
        def self.port
          #         8081
          nil
        end
      end
    end
  end

  require 'yaml'
  if ARGV.length > 0
    url = ARGV[0]
  end

  hook = lambda{ |agent, request|
    begin
      puts request.class
      puts request.method
      puts request.methods.sort

      exit
      clean_jar = Mechanize::CookieJar.new
      agent.cookie_jar.each{|cookie|
        puts "Cookie: #{cookie.name}"
        clean_jar.add! cookie unless cookie.name =~ /^box/i
      }
      exit unless agent.cookie_jar.empty?(request.url)
      agent.cookie_jar = clean_jar
    rescue => bang
      puts bang
      puts bang.backtrace
    end

  }
  hook = nil

  crawler = Watobo::Crawler::Engine.new
  crawler.run(url, :pre_connect_hook => hook, :num_grabbers => 1 )
end