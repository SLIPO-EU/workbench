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
  apiUsageReducer,
  applicationKeyReducer,
  dashboardReducer,
  eventReducer,
  harvesterDataExplorerReducer,
  mapViewerReducer,
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
      admin: Redux.combineReducers({
        api: apiUsageReducer,
      }),
      account: accountReducer,
      applicationKey: applicationKeyReducer,
      dashboard: dashboardReducer,
      event: eventReducer,
      execution: Redux.combineReducers({
        explorer: processExecutionReducer,
      }),
      harvester: harvesterDataExplorerReducer,
      map: mapViewerReducer,
      process: Redux.combineReducers({
        designer: processDesignerReducer,
        explorer: processExplorerReducer,
      }),
      resources: Redux.combineReducers({
        explorer: resourceExplorerReducer,
        export: resourceExportReducer,
        registration: resourceRegistrationReducer,
      }),
      template: Redux.combineReducers({
        explorer: processTemplateExplorerReducer,
      }),
    }),
  }),
  user,
});
