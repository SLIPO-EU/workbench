const Redux = require('redux');

const reduceMeta = require('./meta');
const reduceConfig = require('./config');
const reduceUser = require('./user');
const reduceLocale = require('./locale');
const reduceI18n = require('./i18n');

module.exports = Redux.combineReducers({
  meta: reduceMeta,
  locale: reduceLocale,
  i18n: reduceI18n,
  config: reduceConfig,
  user: reduceUser,
});
