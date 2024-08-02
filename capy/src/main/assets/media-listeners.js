function addMediaListeners() {
  [...document.getElementsByTagName('video')].forEach(v => {
    v.addEventListener('click', () => {
      const sources = v.getElementsByTagName("source")

      if (!sources.length) {
        return;
      }

      Android.openVideo(sources[0].src);
    })
  });
}

window.onload = () => {
  addMediaListeners();
};
