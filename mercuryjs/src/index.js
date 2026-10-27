import './globals';

globalThis.capyMercury = {
  parse(url, html) {
    return globalThis.Mercury.parse(url, { html, fetchAllPages: false }).then(
      result =>
        JSON.stringify({
          content: result.content,
          lead_image_url: result.lead_image_url,
          error: result.error === true,
          message: result.message,
        })
    );
  },
};
