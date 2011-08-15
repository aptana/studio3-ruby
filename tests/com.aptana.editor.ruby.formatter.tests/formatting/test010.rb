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
require 'rubygems'

#require 'bunder/setup'

require 'ntlm/mechanize'
require 'active_record'
require 'yaml'
class SharePointList
    def initialize(sharepoint_list_name)
      conf = open('config.yml') {|f| YAML.load(f) }

      #conf.each{ |k, v| puts "#{k} => #{v}" }
    @hostname = conf["hostname"]
    @domain = conf["domain"]
      @username = conf["username"]
      @password = conf["password"]
    @sp_list_name = sharepoint_list_name
  end

    def list_data
        mech = Mechanize.new
        mech.auth(@domain + '\\' + @username, @password)
        page = mech.get('http://' + @hostname + '/_vti_bin/owssvr.dll?Cmd=Display&List={' + @sp_list_name + '}&XMLDATA=TRUE')
        page.body
  end
end
==FORMATTED==
require 'rubygems'

#require 'bunder/setup'

require 'ntlm/mechanize'
require 'active_record'
require 'yaml'

class SharePointList
  def initialize(sharepoint_list_name)
    conf = open('config.yml') {|f| YAML.load(f) }

    #conf.each{ |k, v| puts "#{k} => #{v}" }
    @hostname = conf["hostname"]
    @domain = conf["domain"]
    @username = conf["username"]
    @password = conf["password"]
    @sp_list_name = sharepoint_list_name
  end

  def list_data
    mech = Mechanize.new
    mech.auth(@domain + '\\' + @username, @password)
    page = mech.get('http://' + @hostname + '/_vti_bin/owssvr.dll?Cmd=Display&List={' + @sp_list_name + '}&XMLDATA=TRUE')
    page.body
  end
end