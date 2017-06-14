const store = require('./store');
const {refreshProfile} = require('./actions/user');
const {renderRoot} = require('./root');

var rootSelector = document.currentScript.getAttribute('data-root') || '#root';

// Bind top-level event handlers

document.addEventListener("DOMContentLoaded", function () {
  var rootEl = document.querySelector(rootSelector);
  var _renderRoot = renderRoot.bind(window, rootEl);
  
  // Chain preliminary actions before initial rendering
  store.dispatch(refreshProfile())
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
