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

/**
 * @param {HTMLDivElement | Document} element
 * @returns boolean
 */
function cleanEmbeds(element = document) {
  const embeds = element.querySelectorAll("iframe");

  for (const embed of embeds) {
    const src = embed.getAttribute("src");
    const youtubeID = findYouTubeMatch(src);

    if (youtubeID !== null) {
      swapPlaceholder(embed, src, youtubeID);
      addEmbedListeners();

      return true;
    }
  }

  return false;
}

/**
 * @param {string} src
 * @returns string | null
 */
function findYouTubeMatch(src) {
  for (const regex of YOUTUBE_DOMAINS) {
    const match = src.match(regex);
    if (match) {
      return match[1];
    }
  }
  return null;
}

/**
 * @param {HTMLIFrameElement} embed
 * @param {string} src
 * @param {string} youtubeID
 */
function swapPlaceholder(embed, src, youtubeID) {
  const placeholderImage = document.createElement("img");
  placeholderImage.classList.add("iframe-embed__image", "mercury-parser-keep");
  placeholderImage.setAttribute("src", imageURL(youtubeID));

  const playButton = document.createElement("div");
  playButton.classList.add("iframe-embed__play-button");

  const placeholder = document.createElement("div");
  placeholder.classList.add("iframe-embed");
  placeholder.setAttribute("data-iframe-src", autoplaySrc(src));
  placeholder.appendChild(placeholderImage);
  placeholder.appendChild(playButton);

  embed.replaceWith(placeholder);
}

function imageURL(id) {
  return `https://img.youtube.com/vi/${id}/hqdefault.jpg`;
}

/**
 * @param {string} src
 * @returns string
 */
function autoplaySrc(src) {
  try {
    const url = new URL(src);
    url.searchParams.set("autoplay", "1");
    return url.toString();
  } catch (e) {
    return src;
  }
}

const YOUTUBE_DOMAINS = [
  /.*?\/\/www\.youtube-nocookie\.com\/embed\/(.*?)(\?|$)/,
  /.*?\/\/www\.youtube\.com\/embed\/(.*?)(\?|$)/,
  /.*?\/\/www\.youtube\.com\/user\/.*?#\w\/\w\/\w\/\w\/(.+)\b/,
  /.*?\/\/www\.youtube\.com\/v\/(.*?)(#|\?|$)/,
  /.*?\/\/www\.youtube\.com\/watch\?(?:.*?&)?v=([^&#]*)(?:&|#|$)/,
  /.*?\/\/youtube-nocookie\.com\/embed\/(.*?)(\?|$)/,
  /.*?\/\/youtube\.com\/embed\/(.*?)(\?|$)/,
  /.*?\/\/youtu\.be\/(.*?)(\?|$)/
];

window.addEventListener("DOMContentLoaded", () => {
  cleanEmbeds()
});

window.onload = () => {
  addImageClickListeners();
  addEmbedListeners();
  configureVideoTags();
};
