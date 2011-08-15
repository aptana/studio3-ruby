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
require 'test_helper'

class ProductTest < ActiveSupport::TestCase
  test "product attributes must not be empty" do
    product = Product.new
    assert product.invalid?
    assert product.errors[:title].any?
    assert product.errors[:description].any?
    assert product.errors[:price].any?
    assert product.errors[:image_url].any?
  end

  test "product price must be positive" do
    product = Product.new(:title => "My Book Title",
    :description => "yyy",
    :image_url => "zzz.jpg")
    product.price = -1
    assert product.invalid?
    assert_equal "must be greater than or equal to 0.01",
product.errors[:price].join('; ')
    product.price = 0
    assert product.invalid?
    assert_equal "must be greater than or equal to 0.01",
product.errors[:price].join('; ')
    product.price = 1
    assert product.valid?
  end

  def new_product(image_url)
    Product.new(:title => "My Book Title",
    :description => "yyy",
    :price => 1,
    :image_url => image_url)
  end
    test "image url" do
    ok = %w{ fred.gif fred.jpg fred.png FRED.JPG FRED.Jpg
     http://a.b.c/x/y/z/fred.gif }
    bad = %w{ fred.doc fred.gif/more fred.gif.more }
       ok.each do |name|
         assert new_product(name).valid?, "#{name} shouldn't be invalid"
    end
      bad.each do |name|
       assert new_product(name).invalid?, "#{name} shouldn't be valid"
     end
  end
end
==FORMATTED==
require 'test_helper'



class ProductTest < ActiveSupport::TestCase
  test "product attributes must not be empty" do
    product = Product.new
    assert product.invalid?
    assert product.errors[:title].any?
    assert product.errors[:description].any?
    assert product.errors[:price].any?
    assert product.errors[:image_url].any?
  end

  test "product price must be positive" do
    product = Product.new(:title => "My Book Title",
    :description => "yyy",
    :image_url => "zzz.jpg")
    product.price = -1
    assert product.invalid?
    assert_equal "must be greater than or equal to 0.01",
    product.errors[:price].join('; ')
    product.price = 0
    assert product.invalid?
    assert_equal "must be greater than or equal to 0.01",
    product.errors[:price].join('; ')
    product.price = 1
    assert product.valid?
  end

  def new_product(image_url)
    Product.new(:title => "My Book Title",
    :description => "yyy",
    :price => 1,
    :image_url => image_url)
  end
  test "image url" do
    ok = %w{ fred.gif fred.jpg fred.png FRED.JPG FRED.Jpg
    http://a.b.c/x/y/z/fred.gif }
    bad = %w{ fred.doc fred.gif/more fred.gif.more }
    ok.each do |name|
      assert new_product(name).valid?, "#{name} shouldn't be invalid"
    end
    bad.each do |name|
      assert new_product(name).invalid?, "#{name} shouldn't be valid"
    end
  end
end