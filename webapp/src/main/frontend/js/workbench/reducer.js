const Redux = require('redux');

import * as reducers from './ducks';

module.exports = Redux.combineReducers(reducers);
