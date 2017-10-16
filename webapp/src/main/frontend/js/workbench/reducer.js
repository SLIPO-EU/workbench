const Redux = require('redux');

import { config, i18n, meta, user } from './ducks';
import { viewport, menu } from './ducks/ui/';
import { resourceRegistration } from './ducks/ui/views';

module.exports = Redux.combineReducers({
  config,
  i18n,
  meta,
  user,
  ui: Redux.combineReducers({
    viewport,
    menu,
    views: Redux.combineReducers({
      resources: Redux.combineReducers({
        registration: resourceRegistration,
      }),
    }),
  }),
});
