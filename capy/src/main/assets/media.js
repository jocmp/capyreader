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
    if (img.classList.contains("iframe-embed__image")) {
      return;
    }

    img.addEventListener("click", () => {
      Android.openImage(img.src, img.alt);
    });
  });
}

function addEmbedListeners() {
  [...document.querySelectorAll("div.iframe-embed")].forEach((div) => {
    div.addEventListener("click", () => {
      const iframe = document.createElement("iframe");
      iframe.src = div.getAttribute("data-iframe-src");
      div.replaceWith(iframe);
    });
  });
}

window.onload = () => {
  addImageClickListeners();
  addEmbedListeners();
  configureVideoTags();
};
