function android() {
  /**
   * @param {string} src
   */
  function openImage(src, caption = null) {
    window.open(`/image?src=${btoa(src)}&caption=${caption || ''}`)
  }

  return {
    openImage,
  };
}

window.Android = android();
