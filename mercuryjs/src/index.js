import './globals';
// The package's "browser" field maps its main entry to the web build, so
// import the Node build by path and let rollup resolve its dependencies.
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
