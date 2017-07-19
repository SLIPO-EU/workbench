const store = require('./store');
const {renderRoot} = require('./root');
const actions = require('./actions/index');

var rootSelector = document.currentScript.getAttribute('data-root') || '#root';

// Bind top-level event handlers

document.addEventListener("DOMContentLoaded", function () 
{
  var rootEl = document.querySelector(rootSelector);
  
  // Todo read from non-httponly "language" cookie  
  var language = "en"; 

  var token = document.querySelector("meta[name=_csrf]")
    .getAttribute('content');

  // Chain preliminary actions before initial rendering
  
  Promise.resolve()
    .then(() => store.dispatch(actions.meta.setCsrfToken(token)))
    .then(() => store.dispatch(actions.i18n.changeLocale(language)))
    .then(() => store.dispatch(actions.user.refreshProfile())
      // recover from an "Unauthorized" error
      .then(undefined, (err) => console.info('Cannot refresh user profile')))
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
