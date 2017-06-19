const store = require('./store');
const {renderRoot} = require('./root');
const actions = require('./actions/index');

var rootSelector = document.currentScript.getAttribute('data-root') || '#root';

// Bind top-level event handlers

document.addEventListener("DOMContentLoaded", function () {
  var rootEl = document.querySelector(rootSelector);
  var _renderRoot = renderRoot.bind(window, rootEl);
  
  var token = document.querySelector("meta[name=_csrf]")
    .getAttribute('content');

  // Chain preliminary actions before initial rendering
  store.dispatch(actions.meta.setCsrfToken(token));
  store.dispatch(actions.user.refreshProfile())
    .then(undefined, (err) => console.info('Cannot refresh user profile'))
    .then(_renderRoot);
});


// Provide development shortcuts

/* global process */
if (process.env.NODE_ENV != 'production') {
  global.$a = {
    store: store,
    api: require('./service/api/index'),
  };
}
