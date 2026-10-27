import './globals';
// Import the Node build by path: the package's "module" field points at a
// stale ESM build that bundles jQuery (removed in jocmp/mercury-parser#236).
import Mercury from '@jocmp/mercury-parser/dist/mercury.js';

globalThis.capyMercury = {
  parse(url, html) {
    return Mercury.parse(url, { html, fetchAllPages: false }).then(result =>
      JSON.stringify({
        content: result.content,
        lead_image_url: result.lead_image_url,
        error: result.error === true,
        message: result.message,
      })
    );
  },
};
