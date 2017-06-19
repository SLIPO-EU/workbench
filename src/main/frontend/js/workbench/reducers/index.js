const Redux = require('redux');

const reduceMeta = require('./meta');
const reduceConfig = require('./config');
const reduceUser = require('./user');

module.exports = Redux.combineReducers({
  meta: reduceMeta,
  config: reduceConfig,
  user: reduceUser,
});
