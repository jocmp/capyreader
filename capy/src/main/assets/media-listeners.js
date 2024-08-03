function addMediaListeners() {
  [...document.getElementsByTagName("video")].forEach((v) => {
    v.setAttribute("preload", "auto");
    v.setAttribute("controls", true);
    v.setAttribute("controlslist", "nofullscreen nodownload noremoteplayback");
  });
}

window.onload = () => {
  addMediaListeners();
};
