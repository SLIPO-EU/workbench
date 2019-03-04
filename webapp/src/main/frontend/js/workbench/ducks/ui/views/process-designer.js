import _ from 'lodash';
import * as processService from '../../../service/process';

import {
  EnumInputType,
  EnumResourceType,
  EnumTool,
  EnumDesignerView,
  EnumDesignerSaveAction,
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
  // Errors
  errors: [],
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
  moveStepReducer,
  moveStepInputReducer,
  stepOutputPartReducer,
  stepPropertyReducer,
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

  let requireValidation = false;
  let supportUndo = false;

  switch (action.type) {
    case Types.LOGOUT:
      return initialState;

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
      requireValidation = true;
      supportUndo = true;

      newState = addStepReducer(state, action);
      break;

    case Types.CLONE_STEP:
      requireValidation = true;
      supportUndo = true;

      newState = addStepReducer(state, action);
      break;


    case Types.SET_STEP_PROPERTY:
      requireValidation = true;
      supportUndo = true;

      newState = stepPropertyReducer(state, action);
      break;

    case Types.REMOVE_STEP:
      requireValidation = true;
      supportUndo = true;

      newState = {
        ...state,
        groups: groupReducer(state.groups, action),
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };
      break;

    case Types.MOVE_STEP:
      return moveStepReducer(state, action);

    case Types.MOVE_STEP_INPUT:
      return moveStepInputReducer(state, action);

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
      requireValidation = true;

      newState = {
        ...state,
        view: {
          ...state.view,
          configuration: {
            ...state.view.configuration,
            ...action.configuration,
          }
        }
      };
      break;

    case Types.CONFIGURE_STEP_VALIDATE:
      requireValidation = true;

      newState = {
        ...state,
        view: {
          ...state.view,
          errors: { ...action.errors },
        }
      };
      break;

    case Types.CONFIGURE_STEP_END:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumDesignerView.Designer,
          },
        };
      }

      requireValidation = true;
      supportUndo = true;

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

    case Types.SET_STEP_CONFIGURATION:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumDesignerView.Designer,
          },
        };
      }

      requireValidation = true;
      supportUndo = true;

      newState = {
        ...state,
        view: {
          type: EnumDesignerView.Designer,
        },
        steps: state.steps.map((step) => {
          if (step.key === action.step.key) {
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
      requireValidation = true;
      supportUndo = true;

      newState = {
        ...state,
        steps: stepReducer(state.steps, action),
      };
      break;

    case Types.ADD_STEP_DATA_SOURCE:
    case Types.REMOVE_STEP_DATA_SOURCE:
      requireValidation = true;
      supportUndo = true;

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
      requireValidation = true;

      newState = {
        ...state,
        view: {
          ...state.view,
          configuration: {
            ...state.view.configuration,
            ...action.configuration,
          }
        }
      };
      break;

    case Types.CONFIGURE_DATA_SOURCE_VALIDATE:
      requireValidation = true;

      newState = {
        ...state,
        view: {
          ...state.view,
          errors: { ...action.errors },
        }
      };
      break;

    case Types.CONFIGURE_DATA_SOURCE_END:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumDesignerView.Designer,
          },
        };
      }

      requireValidation = true;
      supportUndo = true;

      newState = {
        ...state,
        view: {
          type: EnumDesignerView.Designer,
        },
        steps: state.steps.map((step) => {
          if ((step.key === action.step.key) && (step.tool === EnumTool.TripleGeo)) {
            // Update data sources
            const dataSources = step.dataSources.map((ds) => {
              if ((ds.key === action.dataSource.key) && (action.configuration)) {
                return {
                  ...ds,
                  configuration: _.cloneDeep(action.configuration),
                  errors: { ...action.errors },
                };
              }
              return ds;
            });
            // Reset mappings for TripleGeo
            switch (step.tool) {
              case EnumTool.TripleGeo:
                return {
                  ...step,
                  configuration: {
                    ...step.configuration,
                    autoMappings: null,
                    userMappings: null,
                  },
                  dataSources,
                };
              default:
                return {
                  ...step,
                  dataSources,
                };
            }
          }
          return step;
        })
      };
      break;

    case Types.PROCESS_VALIDATE:
      requireValidation = true;

      newState = {
        ...state,
        process: {
          ...state.process,
          errors: { ...action.errors },
        },
      };
      break;

    case Types.PROCESS_UPDATE:
      requireValidation = true;

      newState = {
        ...state,
        process: {
          ...state.process,
          properties: {
            ...action.properties
          },
        },
      };
      break;

    case Types.ADD_RESOURCE_TO_BAG:
      supportUndo = true;

      newState = addResourceToBagReducer(state, action);
      break;

    case Types.REMOVE_RESOURCE_FROM_BAG:
      requireValidation = true;
      supportUndo = true;

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
      requireValidation = true;

      newState = undoReducer(state, action);
      break;

    case Types.REDO:
      requireValidation = true;

      newState = redoReducer(state, action);
      break;

    case Types.LOAD_RECEIVE_RESPONSE:
      requireValidation = true;

      newState = processReducer(state, action);
      break;

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
      return {
        ...state,
        execution: executionReducer(state, action),
      };

    case Types.SET_STEP_INPUT_OUTPUT_PART:
      return {
        ...state,
        steps: stepOutputPartReducer(state.steps, action),
      };

    default:
      return state;
  }
  if (requireValidation) {
    newState.errors = processService.validate(EnumDesignerSaveAction.None, newState, false);
  }

  if (supportUndo) {
    return {
      ...newState,
      undo: [...newState.undo, {
        groups: newState.groups,
        steps: newState.steps,
        resources: newState.resources
      }],
      redo: [],
    };
  }
  return newState;
};

/*
 * Action creators
 */

export {
  reset,
  addStep,
  cloneStep,
  moveStep,
  moveStepInput,
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
  setConfiguration,
  processValidate,
  processUpdate,
  undo,
  redo,
  showStepExecutionDetails,
  hideStepExecutionDetails,
  selectFile,
  resetSelectedFile,
  resetSelectedKpi,
  selectOutputPart,
} from './process-designer/actions';

/*
 * Thunk actions
 */

export {
  cloneTemplate,
  checkFile,
  downloadFile,
  fetchExecutionDetails,
  fetchExecutionKpiData,
  fetchProcess,
  fetchProcessRevision,
  getTripleGeoMappings,
  getTripleGeoMappingFileAsText,
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
