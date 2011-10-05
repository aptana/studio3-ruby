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
class AdImpressionRollup < ActiveRecord::Base

        named_scope :limit, lambda { |num| { :limit => num} }
  named_scope :for_day, lambda { |day| {:conditions => ['created_at <= ? and created_at >= ?' , day.end_of_day, day.midnight] } }
          def self.counts_by_ad_id
    sum :count, :order => 'sum_count DESC', :group => 'ad_id'
  end

  def self.counts_for_day(day)
    @after_each_ran

    
                  for_day(day).sum :count, :order => 'LOWER(ad_id) ASC', :group => 'ad_id'
  end

end
==FORMATTED==
class AdImpressionRollup < ActiveRecord::Base

  named_scope :limit, lambda { |num| { :limit => num} }
  named_scope :for_day, lambda { |day| {:conditions => ['created_at <= ? and created_at >= ?' , day.end_of_day, day.midnight] } }
  def self.counts_by_ad_id
    sum :count, :order => 'sum_count DESC', :group => 'ad_id'
  end

  def self.counts_for_day(day)
    @after_each_ran

    for_day(day).sum :count, :order => 'LOWER(ad_id) ASC', :group => 'ad_id'
  end

end