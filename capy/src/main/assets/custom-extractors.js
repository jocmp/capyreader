const tagesschauExtractor = {
  domain: 'www.tagesschau.de',
  title: {
    selectors: ['.seitenkopf__headline'],
  },
  author: {
    selectors: ['.authorline__author'],
  },
  content: {
    selectors: ['article'],
    clean: [
      '[data-config]',
      '.seitenkopf__headline',
      '.authorline__author',
      '.metatextline'
    ],
  },
  date_published: {
    selectors: ['.metatextline'],
  },
};

Mercury.addExtractor(tagesschauExtractor)
