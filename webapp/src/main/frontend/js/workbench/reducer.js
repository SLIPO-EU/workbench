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
  accountReducer,
  dashboardReducer,
  eventReducer,
  harvesterDataExplorerReducer,
  processDesignerReducer,
  processExecutionReducer,
  processExplorerReducer,
  processTemplateExplorerReducer,
  resourceExplorerReducer,
  resourceExportReducer,
  resourceRegistrationReducer,
} from './ducks/ui/views';

export default Redux.combineReducers({
  config,
  i18n,
  meta,
  ui: Redux.combineReducers({
    menu,
    viewport,
    views: Redux.combineReducers({
      account: accountReducer,
      dashboard: dashboardReducer,
      event: eventReducer,
      harvester: harvesterDataExplorerReducer,
      resources: Redux.combineReducers({
        explorer: resourceExplorerReducer,
        export: resourceExportReducer,
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
