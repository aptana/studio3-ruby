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
class Ball
def update(time_delta)
# Determine new x and y position of ball.
@x += @x_velocity * time_delta
@y += @y_velocity * time_delta
# Keep the ball on the window.
if @x < @image.width / 2
 @x = @image.width / 2
      @x_velocity = -@x_velocity
 end
if @x > (@window.width - (@image.width / 2))
@x = (@window.width - (@image.width / 2))
  @x_velocity = -@x_velocity
end
               if @y < @image.height / 2
      @y = @image.height / 2
      @y_velocity = -@y_velocity
end
    if @y > @window.height + (@image.height / 2)
reset
    end
end
end
==FORMATTED==
class Ball
  def update(time_delta)
    # Determine new x and y position of ball.
    @x += @x_velocity * time_delta
    @y += @y_velocity * time_delta
    # Keep the ball on the window.
    if @x < @image.width / 2
      @x = @image.width / 2
      @x_velocity = -@x_velocity
    end
    if @x > (@window.width - (@image.width / 2))
      @x = (@window.width - (@image.width / 2))
      @x_velocity = -@x_velocity
    end
    if @y < @image.height / 2
      @y = @image.height / 2
      @y_velocity = -@y_velocity
    end
    if @y > @window.height + (@image.height / 2)
      reset
    end
  end
end