const Redux = require('redux');

import { config, i18n, meta, user, forms, resources, viewport } from './ducks';

module.exports = Redux.combineReducers({
  config,
  i18n,
  meta,
  user,
  forms,
  ui: Redux.combineReducers({
    viewport,
    resources,
  }),
});
