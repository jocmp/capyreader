class ColorPicker
  DEFAULT = {
    "color_on_primary_container" => "#102000",
    "color_on_surface_variant" => "#44483D",
    "color_on_surface" => "#1A1C16",
    "color_primary_container" => "#CDEDA3",
    "color_primary" => "#4C662B",
    "color_surface_variant" => "#E1E4D5",
    "color_surface" => "#F9FAEF",
    "color_surface_container" => "#eeefe3",
  }.freeze

  DARK = {
    "color_primary" => "#B1D18A",
    "color_surface" => "#12140E",
    "color_on_surface" => "#E2E3D8",
    "color_on_surface_variant" => "#C5C8BA",
    "color_surface_variant" => "#44483D",
    "color_primary_container" => "#354E16",
    "color_on_primary_container" => "#CDEDA3"
  }.freeze

  DEVICE = {}.freeze

  THEMES = {
    "device" => DEVICE,
    "dark" => DARK,
    "default" => DEFAULT
  }.freeze

  def self.pick(theme)
    THEMES[theme] || DEFAULT
  end
end
