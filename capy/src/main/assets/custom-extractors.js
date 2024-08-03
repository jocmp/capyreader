/** @param {Record<string, any>[]} list */
function extractors(list) {
  list.forEach((extractor) => Mercury.addExtractor(extractor));
}

extractors([
  {
    domain: "www.tagesschau.de",
    title: {
      selectors: [".seitenkopf__headline"],
    },
    author: {
      selectors: [".authorline__author"],
    },
    content: {
      selectors: ["article"],
      clean: [
        "[data-config]",
        ".seitenkopf__headline",
        ".authorline__author",
        ".metatextline",
      ],
    },
    date_published: {
      selectors: [".metatextline"],
    },
  },
  {
    domain: "factorio.com",
    content: {
      selectors: [[".blog-post", "div:nth-child(2)"]],
      transforms: {
        "h3 author": (node) => {
          node.attr("class", "article__body--subheading");
          return "span";
        },
      },
    },
  },
]);
