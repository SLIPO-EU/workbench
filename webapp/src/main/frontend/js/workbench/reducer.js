const Redux = require('redux');

import { config, i18n, meta, user } from './ducks';
import { viewport, menu } from './ducks/ui/';
import { resourceRegistration, resourceExplorer, dashboardReducer, processReducer } from './ducks/ui/views';

import { resources } from './ducks/data';

module.exports = Redux.combineReducers({
  config,
  i18n,
  meta,
  user,
  data: Redux.combineReducers({
    resources,
  }),
  ui: Redux.combineReducers({
    viewport,
    menu,
    views: Redux.combineReducers({
      dashboard: dashboardReducer,
      resources: Redux.combineReducers({
        registration: resourceRegistration,
        explorer: resourceExplorer,
      }),
      processes: processReducer,
    }),
  }),
});
