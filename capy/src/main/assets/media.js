function configureVideoTags() {
  [...document.getElementsByTagName("video")].forEach((v) => {
    v.setAttribute("preload", "auto");
    v.setAttribute("playsinline", "true");
    v.setAttribute("controls", "true");
    v.setAttribute("controlslist", "nofullscreen nodownload noremoteplayback");

    if (v.classList.contains("article__video-autoplay--looped")) {
      v.setAttribute("loop", "true");
      v.play();
    }
  });
}

function addImageClickListeners() {
  const images = [...document.getElementsByTagName("img")].filter(
    (img) => !img.classList.contains("iframe-embed__image")
  );

  /** @type {MediaItem[]} */
  const galleryImages = images.map((i) => ({
    url: i.src,
    altText: i.alt || null,
  }));

  images.forEach((img, index) => {
    img.addEventListener("click", (e) => {
      e.preventDefault();
      Android.openImageGallery(JSON.stringify(galleryImages), index);
    });

    longPress(img, (e) => {
      e.preventDefault();
      Android.showImageDialog(img.src);
    });
  });
}

/**
 * @param {HTMLImageElement} img
 */
function setupImageLoadHandler(img) {
  if (img.classList.contains("loaded")) {
    return;
  }

  img.onload = () => img.classList.add("loaded");
  img.onerror = () => img.classList.add("loaded");

  // Check after attaching - catches race condition
  if (img.complete) {
    img.classList.add("loaded");
  }
}

function addImageLoadListeners() {
  [...document.getElementsByTagName("img")].forEach(setupImageLoadHandler);
}

function observeImages() {
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      mutation.addedNodes.forEach((node) => {
        if (node.nodeName === "IMG") {
          setupImageLoadHandler(/** @type {HTMLImageElement} */ (node));
        } else if (/** @type {Element} */ (node).querySelectorAll) {
          /** @type {Element} */
          (node).querySelectorAll("img").forEach(setupImageLoadHandler);
        }
      });
    });
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });
}

function addEmbedListeners() {
  [...document.querySelectorAll("div.iframe-embed")].forEach((div) => {
    div.addEventListener("click", () => {
      const iframe = document.createElement("iframe");
      iframe.src = div.getAttribute("data-iframe-src") || "";
      div.replaceWith(iframe);
    });
  });

  [...document.querySelectorAll("a")].forEach((anchor) => {
    longPress(anchor, () => {
      Android.showLinkDialog(anchor.href, anchor.text);
    });
  });
}

/**
 * @param {HTMLDivElement | Document} element
 * @returns boolean
 */
function cleanEmbeds(element = document) {
  const imgs = element.querySelectorAll("img");

  for (const img of imgs) {
    if (!img.src) {
      img.remove();
    }
  }

  const embeds = element.querySelectorAll("iframe");

  for (const embed of embeds) {
    const src = embed.getAttribute("src");
    if (!src) {
      continue;
    }

    const youtubeID = findYouTubeMatch(src);

    if (youtubeID !== null) {
      swapPlaceholder(embed, src, youtubeID);
    }
  }

  addEmbedListeners();
}

/**
 * @param {string} src
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

  const placeholder = document.createElement("a");
  placeholder.classList.add("iframe-embed");
  placeholder.setAttribute(
    "href",
    `https://www.youtube.com/watch?v=${youtubeID}`
  );
  placeholder.appendChild(placeholderImage);
  placeholder.appendChild(playButton);

  embed.replaceWith(placeholder);
}

/** @param {string} id */
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
  /.*?\/\/youtu\.be\/(.*?)(\?|$)/,
];

/**
 * Post-process article content: clean styles, resolve image URLs, wrap tables
 * @param {string} baseUrl
 * @param {boolean} hideImages
 */
function postProcessContent(baseUrl, hideImages) {
  const content = document.getElementById("article-body-content");
  if (!content) return;

  content.querySelectorAll("*").forEach((el) => {
    el.removeAttribute("style");
  });

  content.querySelectorAll("a[onclick]").forEach((anchor) => {
    anchor.removeAttribute("onclick");
  });

  content.querySelectorAll("img").forEach((img) => {
    if (hideImages) {
      img.remove();
    } else {
      img.loading = "lazy";
      if (baseUrl) {
        const src = img.getAttribute("src");
        if (src && !src.startsWith("http") && !src.startsWith("data:")) {
          try {
            img.src = new URL(src, baseUrl).href;
          } catch (e) {
            // continue
          }
        }
      }
    }
  });

  content.querySelectorAll("table").forEach((table) => {
    if (table.parentElement?.classList.contains("table__wrapper")) return;
    const wrapper = document.createElement("div");
    wrapper.className = "table__wrapper";
    table.parentNode?.insertBefore(wrapper, table);
    wrapper.appendChild(table);
  });
}

/**
 * @param {HTMLElement} element
 * @param {(event: Event) => void} callback
 */
function longPress(element, callback) {
  /** @type {number | undefined} */
  let timer;

  const start = (/** @type {Event} */ event) => {
    timer = setTimeout(() => {
      callback(event);
    }, 500);
  };

  const stop = () => {
    clearTimeout(timer);
  };

  element.addEventListener("mousedown", start);
  element.addEventListener("mouseup", stop);
  element.addEventListener("mouseleave", stop);

  element.addEventListener("touchstart", start);
  element.addEventListener("touchend", stop);
  element.addEventListener("touchcancel", stop);
}

window.addEventListener("DOMContentLoaded", () => {
  cleanEmbeds();
});

/**
 * @param {MessageEvent} event
 */
function handleBlueskyEmbedResize(event) {
  if (event.origin !== "https://embed.bsky.app") return;
  if (!event.data || typeof event.data.height !== "number") return;

  document.querySelectorAll("iframe").forEach((iframe) => {
    if (iframe.contentWindow === event.source) {
      iframe.style.height = event.data.height + "px";
    }
  });
}

window.addEventListener("message", handleBlueskyEmbedResize);

/** @type {string | null} */
let currentPlayingUrl = null;

/** @type {boolean} */
let isCurrentlyPlaying = false;

/**
 * Toggle audio playback - play if stopped, pause if playing
 * @param {string} url
 * @param {string} title
 * @param {string} feedName
 * @param {number | null} durationSeconds
 * @param {string} artworkUrl
 */
function playAudio(url, title, feedName, durationSeconds, artworkUrl) {
  if (currentPlayingUrl === url && isCurrentlyPlaying) {
    Android.pauseAudio();
  } else {
    const audioData = JSON.stringify({
      url: url,
      title: title,
      feedName: feedName,
      durationSeconds: durationSeconds,
      artworkUrl: artworkUrl || null
    });
    Android.openAudioPlayer(audioData);
  }
}

/**
 * Update the play/pause state for an audio enclosure
 * Called from Android when play state changes
 * @param {string} url
 * @param {boolean} isPlaying
 */
function updateAudioPlayState(url, isPlaying) {
  currentPlayingUrl = url;
  isCurrentlyPlaying = isPlaying;

  const enclosures = document.querySelectorAll('.audio-enclosure');
  enclosures.forEach((enclosure) => {
    const enclosureUrl = enclosure.getAttribute('data-url');
    const playButton = enclosure.querySelector('.audio-enclosure__play-button');
    if (!playButton) return;

    if (enclosureUrl === url) {
      playButton.classList.toggle('playing', isPlaying);
    } else {
      playButton.classList.remove('playing');
    }
  });
}

/**
 * Reset all audio enclosures to not playing state
 * Called from Android when audio is dismissed
 */
function resetAudioPlayState() {
  currentPlayingUrl = null;
  isCurrentlyPlaying = false;
  const playButtons = document.querySelectorAll('.audio-enclosure__play-button');
  playButtons.forEach((btn) => btn.classList.remove('playing'));
}

window.onload = () => {
  addImageClickListeners();
  addImageLoadListeners();
  observeImages();
  addEmbedListeners();
  configureVideoTags();
  Android.requestAudioState();
};
