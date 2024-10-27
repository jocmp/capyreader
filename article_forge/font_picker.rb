class FontPicker
  def self.pick(family)
    family&.downcase || "default"
  end
end
