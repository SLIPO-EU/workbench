const Redux = require('redux');

import { config, i18n, meta, user } from './ducks';
import { viewport, menu } from './ducks/ui/';
import {
  resourceRegistration,
  resourceExplorer,
  dashboardReducer,
  processReducer,
  processDesigner,
  processConfigStep,
  processExecutionReducer,
  processExecutionViewReducer,
} from './ducks/ui/views';

module.exports = Redux.combineReducers({
  config,
  i18n,
  meta,
  user,
  ui: Redux.combineReducers({
    viewport,
    menu,
    views: Redux.combineReducers({
      dashboard: dashboardReducer,
      resources: Redux.combineReducers({
        registration: resourceRegistration,
        explorer: resourceExplorer,
      }),
      process: Redux.combineReducers({
        explorer: processReducer,
        designer: processDesigner,
        configuration: processConfigStep,
      }),
      execution: Redux.combineReducers({
        explorer: processExecutionReducer,
        viewer: processExecutionViewReducer,
      }),
    }),
  }),
});
