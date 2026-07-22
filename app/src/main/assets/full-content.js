/**
 * @param {Object} article
 * @param {string} article.html
 * @param {string | null} article.url
 * @param {boolean} article.hideImages
 */
async function displayFullContent(article) {
  const { hideImages } = article;

  try {
    const result = await parseWithParser(article);
    const extracted = document.createElement("div");

    extracted.id = "article-body-content";
    extracted.innerHTML = result.content;

    cleanEmbeds(extracted);

    const shouldAddImage =
      result.image &&
      !hideImages &&
      !extracted.querySelectorAll("img:not(iframe img):not(.iframe-embed img)")
        .length;

    if (shouldAddImage && result.image) {
      const leadImage = document.createElement("img");
      leadImage.src = result.image;
      extracted.prepend(leadImage);
    }

    const content = document.getElementById("article-body-content");
    if (content) {
      content.replaceWith(extracted);
    }

    postProcessContent(article.url ?? "", hideImages);
  } catch (e) {
    console.error(e);
  }
}

/**
 * @param {Object} article
 * @param {string} article.html
 * @param {string | null} article.url
 */
async function parseWithParser(article) {
  await loadMercury();

  const result = await Mercury.parse(article.url, { html: article.html });

  return {
    image: result.lead_image_url,
    content: result.content,
  };
}

/** @type {Promise<void> | null} */
let mercuryLoading = null;

/**
 * Mercury is nearly 1MB of JavaScript. Loading it eagerly in the template head
 * taxes every article render with its parse and eval cost, so it's injected
 * on demand the first time full content is requested.
 */
function loadMercury() {
  if (typeof Mercury !== "undefined") {
    return Promise.resolve();
  }

  if (!mercuryLoading) {
    mercuryLoading = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src =
        "https://appassets.androidplatform.net/assets/mercury-parser.js";
      script.onload = () => resolve();
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }

  return mercuryLoading;
}
