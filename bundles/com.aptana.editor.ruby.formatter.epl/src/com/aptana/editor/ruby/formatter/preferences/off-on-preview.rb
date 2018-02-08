#
# Off/On formatter preview...
#
module Alpha
  class Beta
    def main(a)
      if a % 2 == 0
        print "even"
      end
      # @formatter:off
              case a % 2
              when 1
                puts "odd"
              when 0
                puts "even"
              end
    # @formatter:on
    end
  end
end