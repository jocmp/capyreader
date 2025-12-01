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

    if (shouldAddImage) {
      const leadImage = document.createElement("img");
      leadImage.src = result.image;
      extracted.prepend(leadImage);
    }

    const content = document.getElementById("article-body-content");
    content.replaceWith(extracted);

    postProcessContent(article.url, hideImages);
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
    table.parentNode.insertBefore(wrapper, table);
    wrapper.appendChild(table);
  });
}
