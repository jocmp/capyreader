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
  const result = await Mercury.parse(article.url, { html: article.html });

  return {
    image: result.lead_image_url,
    content: result.content,
  };
}

