const Redux = require('redux');

const reduceConfig = require('./config');
const reduceUser = require('./user');

module.exports = Redux.combineReducers({
  config: reduceConfig,
  user: reduceUser,
});
