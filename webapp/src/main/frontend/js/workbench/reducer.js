const Redux = require('redux');

const { reduceMeta } = require('./ducks/meta');
const { reduceConfig } = require('./ducks/config');
const { reduceUser } = require('./ducks/user');
const { reduceI18n } = require('./ducks/i18n');

module.exports = Redux.combineReducers({
  meta: reduceMeta,
  i18n: reduceI18n,
  config: reduceConfig,
  user: reduceUser,
});
