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
class Account < ActiveRecord::Base

  # Attribute Modifiers
  # -------------------

  attr_readonly :apps_count
  # we expose the user_id as accessible, however, can_be_updated_by? verifies that only an owner can change ownership
  attr_accessible :name, :owner, :user_id

  # Validations
  # -----------

  validates_length_of   :name, :maximum => 255, :allow_blank => true
  validates_presence_of :name

  validates_presence_of :owner

  # Associations
  # ------------

  belongs_to :owner, :class_name => 'User', :foreign_key => 'user_id'

  has_many :memberships, :dependent => :destroy
  has_many :invites, :dependent => :destroy
  has_many :users, :through => :memberships
  has_many :cloud_provider_accounts, :dependent => :destroy
  has_many :certificates

  has_many :app_instances, :dependent => :destroy, :conditions => {:hidden => false}

  has_many :platforms, :dependent => :destroy, :conditions => {:hidden => false}

  has_many :repositories, :dependent => :destroy, :conditions => {:hidden => false}

  accepts_nested_attributes_for :cloud_provider_accounts
  attr_accessible :cloud_provider_accounts_attributes

  # Callbacks
  # ---------

  after_create :add_membership_for_owner
  after_create :create_default_platforms

  before_update :ensure_owner_is_admin

  # Finders
  # -------
  named_scope :with_users, :include => :users
  # Used for amazon
  def ssh_key_name
    "Aptana-Account-#{self.id.to_s}"
  end

  def ensure_key_exists_on_amazon
    transaction do
      provider = self.cloud_provider_accounts.first
      provider.lock!
      # Check again, while we are locked, if we have a key name and private-key, and the provider
      # actually has the key.
      if provider.key_name.blank? || provider.private_key.blank? || !provider.key_exists_on_amazon?
      provider.generate_amazon_key
      end
    end
  end

  # Permissions
  # -----------

  acts_as_authorized

  # Any user can create an account
  def can_be_created_by?(user)
    true
  end

  # Only admins can update accounts
  # TODO - Add spec for that!
  def can_be_updated_by?(user)
    user.staff? || user.admin_of?(self) &&
    (!self.user_id_changed? || self.user_id_was == user.id) # only allow the account owner to change the owner
  end

  # Only the owner of an account can destroy it
  def can_be_destroyed_by?(user)
    user.staff? || self.owner == user
  end

  def can_be_viewed_by?(user)
    user.staff? || self.user_id == user.id || user.member_of?(self)
  end

  # Event logging
  # -------------

  acts_as_eventable do |account, event|

    event.user    = ActsAsAuthorized.current_user
    event.account = account
  end

  acts_as_realtime do |account|
    account.id
  end

  protected

  def create_default_cloud_provider_account
    cloud_provider_account = self.cloud_provider_accounts.build
    cloud_provider_account.provider = CloudProviderAccount::Provider::Amazon
    
    
    
    cloud_provider_account.access_key_id = "16D5M5ZHA1NW4FNY8JR2"
    cloud_provider_account.secret_access_key = "KSSB/NtGe1KF83yJV41WGQlKvzHh3miL0GLcRSwy"
    cloud_provider_account.save!
  end

  # TODO: add spec for this
  # This is only on create
  def add_membership_for_owner
    # We do it like that, instead of using create!, to avoid exposing the
    # membership's attributes with attr_accessible (as this is not really
        # needed to be exposed)
    owner_membership = self.memberships.new
         owner_membership.admin = true
    owner_membership.user = self.owner
           owner_membership.save!
  end

  # Give administrative privileges to an owner (in case he just got the
  # ownership and is still defined as a 'member')
  def ensure_owner_is_admin
    account_owner = self.memberships.find_by_user_id(self.owner)
    if !account_owner.admin?
      account_owner.update_attribute(:admin, true)
    end
  end

  def create_default_platforms
    self.platforms.create({:name => "Staging"})
    self.platforms.create({:name => "Production"})
  end
end
==FORMATTED==
class Account < ActiveRecord::Base

  # Attribute Modifiers
  # -------------------

  attr_readonly :apps_count
  # we expose the user_id as accessible, however, can_be_updated_by? verifies that only an owner can change ownership
  attr_accessible :name, :owner, :user_id

  # Validations
  # -----------

  validates_length_of   :name, :maximum => 255, :allow_blank => true
  validates_presence_of :name

  validates_presence_of :owner

  # Associations
  # ------------

  belongs_to :owner, :class_name => 'User', :foreign_key => 'user_id'

  has_many :memberships, :dependent => :destroy
  has_many :invites, :dependent => :destroy
  has_many :users, :through => :memberships
  has_many :cloud_provider_accounts, :dependent => :destroy
  has_many :certificates

  has_many :app_instances, :dependent => :destroy, :conditions => {:hidden => false}

  has_many :platforms, :dependent => :destroy, :conditions => {:hidden => false}

  has_many :repositories, :dependent => :destroy, :conditions => {:hidden => false}

  accepts_nested_attributes_for :cloud_provider_accounts
  attr_accessible :cloud_provider_accounts_attributes

  # Callbacks
  # ---------

  after_create :add_membership_for_owner
  after_create :create_default_platforms

  before_update :ensure_owner_is_admin

  # Finders
  # -------
  named_scope :with_users, :include => :users
  # Used for amazon
  def ssh_key_name
    "Aptana-Account-#{self.id.to_s}"
  end

  def ensure_key_exists_on_amazon
    transaction do
      provider = self.cloud_provider_accounts.first
      provider.lock!
      # Check again, while we are locked, if we have a key name and private-key, and the provider
      # actually has the key.
      if provider.key_name.blank? || provider.private_key.blank? || !provider.key_exists_on_amazon?
      provider.generate_amazon_key
      end
    end
  end

  # Permissions
  # -----------

  acts_as_authorized

  # Any user can create an account
  def can_be_created_by?(user)
    true
  end

  # Only admins can update accounts
  # TODO - Add spec for that!
  def can_be_updated_by?(user)
    user.staff? || user.admin_of?(self) &&
    (!self.user_id_changed? || self.user_id_was == user.id) # only allow the account owner to change the owner
  end

  # Only the owner of an account can destroy it
  def can_be_destroyed_by?(user)
    user.staff? || self.owner == user
  end

  def can_be_viewed_by?(user)
    user.staff? || self.user_id == user.id || user.member_of?(self)
  end

  # Event logging
  # -------------

  acts_as_eventable do |account, event|

    event.user    = ActsAsAuthorized.current_user
    event.account = account
  end

  acts_as_realtime do |account|
    account.id
  end

  protected

  def create_default_cloud_provider_account
    cloud_provider_account = self.cloud_provider_accounts.build
    cloud_provider_account.provider = CloudProviderAccount::Provider::Amazon

    cloud_provider_account.access_key_id = "16D5M5ZHA1NW4FNY8JR2"
    cloud_provider_account.secret_access_key = "KSSB/NtGe1KF83yJV41WGQlKvzHh3miL0GLcRSwy"
    cloud_provider_account.save!
  end

  # TODO: add spec for this
  # This is only on create
  def add_membership_for_owner
    # We do it like that, instead of using create!, to avoid exposing the
    # membership's attributes with attr_accessible (as this is not really
    # needed to be exposed)
    owner_membership = self.memberships.new
    owner_membership.admin = true
    owner_membership.user = self.owner
    owner_membership.save!
  end

  # Give administrative privileges to an owner (in case he just got the
  # ownership and is still defined as a 'member')
  def ensure_owner_is_admin
    account_owner = self.memberships.find_by_user_id(self.owner)
    if !account_owner.admin?
    account_owner.update_attribute(:admin, true)
    end
  end

  def create_default_platforms
    self.platforms.create({:name => "Staging"})
    self.platforms.create({:name => "Production"})
  end
end