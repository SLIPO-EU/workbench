import * as Redux from 'redux';

import {
  config,
  i18n,
  meta,
  user
} from './ducks';

import {
  menu,
  viewport,
} from './ducks/ui/';

import {
  dashboardReducer,
  processDesignerReducer,
  processExecutionReducer,
  processExplorerReducer,
  processTemplateExplorerReducer,
  resourceExplorerReducer,
  resourceRegistrationReducer,
} from './ducks/ui/views';

module.exports = Redux.combineReducers({
  config,
  i18n,
  meta,
  ui: Redux.combineReducers({
    menu,
    viewport,
    views: Redux.combineReducers({
      dashboard: dashboardReducer,
      resources: Redux.combineReducers({
        explorer: resourceExplorerReducer,
        registration: resourceRegistrationReducer,
      }),
      process: Redux.combineReducers({
        designer: processDesignerReducer,
        explorer: processExplorerReducer,
      }),
      template: Redux.combineReducers({
        explorer: processTemplateExplorerReducer,
      }),
      execution: Redux.combineReducers({
        explorer: processExecutionReducer,
      }),
    }),
  }),
  user,
});
