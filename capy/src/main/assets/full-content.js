/**
 * @param {Object} article
 * @param {string} article.html
 * @param {string | null} article.url
 * @param {boolean} article.hideImages
 * @param {string} article.parserType
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

    if (shouldAddImage) {
      const leadImage = document.createElement("img");
      leadImage.src = result.image;
      extracted.prepend(leadImage);
    }

    const content = document.getElementById("article-body-content");
    content.replaceWith(extracted);
  } catch (e) {
    console.error(e);
  }
}

/**
 * @param {Object} article
 * @param {string} article.html
 * @param {string | null} article.url
 * @param {string} article.parserType
 */
async function parseWithParser(article) {
  if (article.parserType === "DEFUDDLE") {
    const parser = new DOMParser();
    const doc = parser.parseFromString(article.html, 'text/html');

    const defuddle = new Defuddle(doc, {
      url: article.url,
      debug: true,
      markdown: false,
    })

    return defuddle.parse();
  }

  const result = await Mercury.parse(article.url, { html: article.html });

  return {
    image: result.lead_image_url,
    content: result.content,
  };
}
