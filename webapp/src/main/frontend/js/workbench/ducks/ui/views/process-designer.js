import _ from 'lodash';

import {
  EnumInputType,
  EnumResourceType,
  EnumTool,
  EnumDesignerView,
} from '../../../model/process-designer';

/*
 * Action types
 */
import * as Types from './process-designer/types';

/*
 * State initializers
 */

import {
  initializeProcess,
  initializeGroups,
  initializeExecution,
} from './process-designer/state';

/*
 * Initial state
 */

const initialState = {
  // Configuration
  view: {
    type: EnumDesignerView.Designer,
  },
  // Counters for creating unique identifiers for designer elements
  counters: {
    step: 0,
    resource: 0,
  },
  // Designer active item
  active: {
    type: null,
    step: null,
    item: null,
  },
  // Resource bag items
  resources: [],
  // Current process instance
  readOnly: false,
  process: initializeProcess(),
  // Workflow step groups
  groups: initializeGroups(),
  // Process steps
  steps: [],
  // Execution
  execution: initializeExecution(),
  // Undo/Redo state
  undo: [{
    groups: [],
    steps: [],
    resources: [],
  }],
  redo: [],
  // Filtering
  filters: {
    resource: null,
  },
};

/*
 * Partial state reducers
 */
import {
  activeReducer,
} from './process-designer/reducers/active';

import {
  counterReducer,
} from './process-designer/reducers/counter';

import {
  addResourceToBagReducer,
  resourceReducer,
} from './process-designer/reducers/resource';

import {
  addStepReducer,
  setStepPropertyReducer,
  moveStepReducer,
  stepReducer,
} from './process-designer/reducers/step';

import {
  groupReducer,
} from './process-designer/reducers/group';

import {
  processReducer,
} from './process-designer/reducers/process';

import {
  executionReducer,
} from './process-designer/reducers/execution';

import {
  undoReducer,
  redoReducer,
} from './process-designer/reducers/undo-redo';

import {
  filterReducer,
} from './process-designer/reducers/filter';

/*
 * Reducer
 */

export default (state = initialState, action) => {
  let newState = state;

  switch (action.type) {
    case Types.LOGOUT:
    case Types.RESET:
      return {
        ...state,
        active: activeReducer(state.active, action),
        counters: counterReducer(state.counters, action),
        readOnly: false,
        process: initializeProcess(),
        groups: initializeGroups(),
        steps: [],
        execution: initializeExecution(),
        resources: resourceReducer(state.resources, action),
        undo: [{
          groups: [],
          steps: [],
          resources: [],
        }],
        redo: [],
        view: {
          type: EnumDesignerView.Designer,
        },
      };

    case Types.ADD_STEP:
      newState = addStepReducer(state, action);
      break;

    case Types.SET_STEP_PROPERTY:
      newState = setStepPropertyReducer(state, action);
      break;

    case Types.REMOVE_STEP:
      newState = {
        ...state,
        groups: groupReducer(state.groups, action),
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };
      break;

    case Types.MOVE_STEP:
      return moveStepReducer(state, action);

    case Types.CONFIGURE_STEP_BEGIN:
      return {
        ...state,
        view: {
          type: EnumDesignerView.StepConfiguration,
          step: _.cloneDeep(action.step),
          configuration: _.cloneDeep(action.configuration),
          errors: {},
        },
      };

    case Types.CONFIGURE_STEP_UPDATE:
      return {
        ...state,
        view: {
          ...state.view,
          configuration: {
            ...state.view.configuration,
            ...action.configuration,
          }
        }
      };

    case Types.CONFIGURE_STEP_VALIDATE:
      return {
        ...state,
        view: {
          ...state.view,
          errors: { ...action.errors },
        }
      };

    case Types.CONFIGURE_STEP_END:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumDesignerView.Designer,
          },
        };
      }

      newState = {
        ...state,
        view: {
          type: EnumDesignerView.Designer,
        },
        steps: state.steps.map((step) => {
          if ((step.key === action.step.key) && (action.configuration)) {
            return {
              ...step,
              configuration: _.cloneDeep(action.configuration),
              errors: { ...action.errors },
            };
          }
          return step;
        })
      };
      break;

    case Types.ADD_STEP_INPUT:
    case Types.REMOVE_STEP_INPUT:
      newState = {
        ...state,
        steps: stepReducer(state.steps, action),
      };
      break;

    case Types.ADD_STEP_DATA_SOURCE:
    case Types.REMOVE_STEP_DATA_SOURCE:
      newState = {
        ...state,
        steps: stepReducer(state.steps, action),
      };
      break;

    case Types.CONFIGURE_DATA_SOURCE_BEGIN:
      return {
        ...state,
        view: {
          type: EnumDesignerView.DataSourceConfiguration,
          step: _.cloneDeep(action.step),
          dataSource: _.cloneDeep(action.dataSource),
          configuration: _.cloneDeep(action.configuration),
          errors: {},
        },
      };

    case Types.CONFIGURE_DATA_SOURCE_UPDATE:
      return {
        ...state,
        view: {
          ...state.view,
          configuration: {
            ...state.view.configuration,
            ...action.configuration,
          }
        }
      };

    case Types.CONFIGURE_DATA_SOURCE_VALIDATE:
      return {
        ...state,
        view: {
          ...state.view,
          errors: { ...action.errors },
        }
      };

    case Types.CONFIGURE_DATA_SOURCE_END:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumDesignerView.Designer,
          },
        };
      }

      newState = {
        ...state,
        view: {
          type: EnumDesignerView.Designer,
        },
        steps: state.steps.map((step) => {
          if ((step.key === action.step.key) && (step.tool === EnumTool.TripleGeo)) {
            return {
              ...step,
              dataSources: step.dataSources.map((ds) => {
                if ((ds.key === action.dataSource.key) && (action.configuration)) {
                  return {
                    ...ds,
                    configuration: _.cloneDeep(action.configuration),
                    errors: { ...action.errors },
                  };
                }
                return ds;
              })
            };
          }
          return step;
        })
      };
      break;

    case Types.PROCESS_VALIDATE:
      return {
        ...state,
        process: {
          ...state.process,
          errors: { ...action.errors },
        },
      };

    case Types.PROCESS_UPDATE:
      return {
        ...state,
        process: {
          ...state.process,
          properties: {
            ...action.properties
          },
        },
      };

    case Types.ADD_RESOURCE_TO_BAG:
      newState = addResourceToBagReducer(state, action);
      break;

    case Types.REMOVE_RESOURCE_FROM_BAG:
      newState = {
        ...state,
        active: activeReducer(state.active, action),
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };
      break;

    case Types.SET_RESOURCE_FILTER:
      return {
        ...state,
        filters: filterReducer(state.filters, action),
        resources: resourceReducer(state.resources, action),
      };

    case Types.RESET_SELECTION:
    case Types.SELECT_PROCESS:
    case Types.SELECT_STEP:
    case Types.SELECT_STEP_INPUT:
    case Types.SELECT_STEP_DATA_SOURCE:
    case Types.SELECT_RESOURCE:
      return {
        ...state,
        active: activeReducer(state.active, action)
      };

    case Types.UNDO:
      return undoReducer(state, action);

    case Types.REDO:
      return redoReducer(state, action);

    case Types.LOAD_RECEIVE_RESPONSE:
      return processReducer(state, action);

    case Types.SHOW_STEP_EXECUTION:
      return {
        ...state,
        view: {
          type: EnumDesignerView.StepExecution,
        },
      };

    case Types.HIDE_STEP_EXECUTION:
      return {
        ...state,
        view: {
          type: EnumDesignerView.Designer,
        },
      };

    case Types.REQUEST_EXECUTION_DATA:
    case Types.RECEIVE_EXECUTION_DATA:
    case Types.SET_SELECTED_FILE:
    case Types.REQUEST_EXECUTION_KPI_DATA:
    case Types.RECEIVE_EXECUTION_KPI_DATA:
    case Types.RESET_SELECTED_FILE:
    case Types.RESET_SELECTED_KPI:
    case Types.SELECT_LAYER:
    case Types.TOGGLE_LAYER:
    case Types.SET_BASE_LAYER:
    case Types.SET_SELECTED_FEATURES:
      return {
        ...state,
        execution: executionReducer(state, action),
      };

    default:
      return state;
  }


  return {
    ...newState,
    undo: [...newState.undo, {
      groups: newState.groups,
      steps: newState.steps,
      resources: newState.resources
    }],
    redo: [],
  };
};

/*
 * Action creators
 */

export {
  reset,
  addStep,
  moveStep,
  removeStep,
  resetActive,
  setActiveStep,
  setStepProperty,
  configureStepBegin,
  configureStepValidate,
  configureStepUpdate,
  configureStepEnd,
  addStepInput,
  removeStepInput,
  setActiveStepInput,
  addStepDataSource,
  removeStepDataSource,
  setActiveStepDataSource,
  configureStepDataSourceBegin,
  configureStepDataSourceValidate,
  configureStepDataSourceUpdate,
  configureStepDataSourceEnd,
  addResourceToBag,
  removeResourceFromBag,
  filterResource,
  setActiveResource,
  setActiveProcess,
  processValidate,
  processUpdate,
  undo,
  redo,
  showStepExecutionDetails,
  hideStepExecutionDetails,
  selectFile,
  resetSelectedFile,
  resetSelectedKpi,
  selectLayer,
  toggleLayer,
  setBaseLayer,
  selectFeatures,
} from './process-designer/actions';

/*
 * Thunk actions
 */

export {
  fetchExecutionDetails,
  fetchExecutionKpiData,
  fetchProcess,
  fetchProcessRevision,
  save,
} from './process-designer/thunks';

/*
 * Selectors
 */

export function filteredResources(state) {
  if (state.filters.resource) {
    switch (state.filters.resource) {
      case EnumInputType.CATALOG:
        return state.resources.filter((r) => r.inputType === EnumInputType.CATALOG);
      case EnumInputType.OUTPUT:
        return state.resources.filter((r) => r.inputType === EnumInputType.OUTPUT);
      case EnumResourceType.POI:
        return state.resources.filter((r) => r.resourceType === EnumResourceType.POI);
      case EnumResourceType.LINKED:
        return state.resources.filter((r) => r.resourceType === EnumResourceType.LINKED);
    }
  }
  return state.resources;
}
