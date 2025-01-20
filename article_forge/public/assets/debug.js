function android() {
  /**
   * @param {string} src
   */
  function openImage(src, caption = null) {
    window.open(`/image?src=${btoa(src)}&caption=${caption || ""}`);
  }

  return {
    openImage,
  };
}

function mercuryStub() {
  return {
    addExtractor: (extractor) => console.log(`[DEBUG] Add extractor for ${extractor.domain}...`)
  }
}

window.Mercury = mercuryStub();
window.Android = android();
