import 'core-js/stable/url';
import 'core-js/stable/url-search-params';

// browser-request reads XMLHttpRequest when Mercury loads. Requests never
// happen since Capy always passes HTML and disables fetchAllPages.
if (typeof globalThis.XMLHttpRequest === 'undefined') {
  globalThis.XMLHttpRequest = function XMLHttpRequest() {
    throw new Error('Network access is disabled in the headless parser');
  };
}
