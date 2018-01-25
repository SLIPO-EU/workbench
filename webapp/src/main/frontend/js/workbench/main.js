const store = require('./store');
const { renderRoot } = require('./root');

import { setCsrfToken } from './ducks/meta';
import { changeLocale } from './ducks/i18n';
import { refreshProfile } from './ducks/user';


var rootSelector = document.currentScript.getAttribute('data-root') || '#root';

// Bind top-level event handlers

document.addEventListener("DOMContentLoaded", function () {
  var rootEl = document.querySelector(rootSelector);

  // Todo read from non-httponly "language" cookie
  var language = "en-GB";

  var token = document.querySelector("meta[name=_csrf]")
    .getAttribute('content');

  // Chain preliminary actions before initial rendering

  Promise.resolve()
    .then(() => store.dispatch(setCsrfToken(token)))
    .then(() => store.dispatch(changeLocale(language)))
    .then(() => store.dispatch(refreshProfile())
      // recover from an "Unauthorized" error
      .catch(() => console.error('Cannot refresh user profile')))
    .then(() => renderRoot(rootEl));
});


// Provide development shortcuts

/* global process */
if (process.env.NODE_ENV != 'production') {
  global.$a = {
    store: store,
    api: require('./service/api/index'),
  };
}
