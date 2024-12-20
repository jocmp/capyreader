function configureVideoTags() {
  [...document.getElementsByTagName("video")].forEach((v) => {
    v.setAttribute("preload", "auto");
    v.setAttribute("playsinline", true);
    v.setAttribute("controls", true);
    v.setAttribute("controlslist", "nofullscreen nodownload noremoteplayback");

    if (v.classList.contains("article__video-autoplay--looped")) {
      v.setAttribute("loop", true);
      v.play();
    }
  });
}

function addImageClickListeners() {
  [...document.getElementsByTagName("img")].forEach((img) => {
    img.addEventListener("click", () => {
      Android.openImage(img.src, img.alt);
    });
  });
}

window.onload = () => {
  addImageClickListeners();
  configureVideoTags();
};
