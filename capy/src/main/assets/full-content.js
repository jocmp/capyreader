/**
 * @param {Object} article
 * @param {string} article.html
 * @param {string | null} article.url
 * @param {boolean} article.hideImages
 */
async function displayFullContent(article) {
  const { url, html, hideImages } = article;

  try {
    const result = await Mercury.parse(url, { html });
    const extracted = document.createElement("div");

    extracted.id = "article-body-content"
    extracted.innerHTML = result.content;

    const hasEmbed = cleanEmbeds(extracted);

    const shouldAddImage = result.lead_image_url &&
      !hideImages &&
      !hasEmbed &&
      !extracted.querySelectorAll("img").length;

    if (shouldAddImage) {
      const leadImage = document.createElement("img");
      leadImage.src = result.lead_image_url;
      extracted.prepend(leadImage);
    }

    const content = document.getElementById("article-body-content");
    content.replaceWith(extracted);
  } catch {
    // continue
  }
}
