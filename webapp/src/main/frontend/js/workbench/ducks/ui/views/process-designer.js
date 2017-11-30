import _ from 'lodash';

import {
  EnumTool,
  EnumToolboxItem,
  EnumStepProperty,
  EnumProcessInput,
  EnumResourceType,
  EnumSelection,
  EnumViews,
  ResourceTypeIcons,
  ToolTitles,
} from '../../../components/views/process/designer';

/*
 * Actions
 */

import { LOGOUT } from '../../user';

const RESET = 'ui/process-designer/RESET';
const RESET_SELECTION = 'ui/process-designer/RESET_SELECTION';

const ADD_STEP = 'ui/process-designer/ADD_STEP';
const REMOVE_STEP = 'ui/process-designer/REMOVE_STEP';
const MOVE_STEP = 'ui/process-designer/MOVE_STEP';
const CONFIGURE_STEP_BEGIN = 'ui/process-designer/CONFIGURE_STEP_BEGIN';
const CONFIGURE_STEP_VALIDATE = 'ui/process-designer/CONFIGURE_STEP_VALIDATE';
const CONFIGURE_STEP_UPDATE = 'ui/process-designer/CONFIGURE_STEP_UPDATE';
const CONFIGURE_STEP_END = 'ui/process-designer/CONFIGURE_STEP_END';
const SELECT_STEP = 'ui/process-designer/SELECT_STEP';
const SET_STEP_PROPERTY = 'ui/process-designer/SET_STEP_PROPERTY';

const ADD_STEP_INPUT = 'ui/process-designer/ADD_STEP_INPUT';
const REMOVE_STEP_INPUT = 'ui/process-designer/REMOVE_STEP_INPUT';
const SELECT_STEP_INPUT = 'ui/process-designer/SELECT_STEP_INPUT';

const ADD_STEP_DATA_SOURCE = 'ui/process-designer/ADD_STEP_DATA_SOURCE';
const REMOVE_STEP_DATA_SOURCE = 'ui/process-designer/REMOVE_STEP_DATA_SOURCE';
const SELECT_STEP_DATA_SOURCE = 'ui/process-designer/SELECT_STEP_DATA_SOURCE';
const CONFIGURE_DATA_SOURCE_BEGIN = 'ui/process-designer/CONFIGURE_DATA_SOURCE_BEGIN';
const CONFIGURE_DATA_SOURCE_VALIDATE = 'ui/process-designer/CONFIGURE_DATA_SOURCE_VALIDATE';
const CONFIGURE_DATA_SOURCE_UPDATE = 'ui/process-designer/CONFIGURE_DATA_SOURCE_UPDATE';
const CONFIGURE_DATA_SOURCE_END = 'ui/process-designer/CONFIGURE_DATA_SOURCE_END';

const ADD_RESOURCE_TO_BAG = 'ui/process-designer/ADD_RESOURCE_TO_BAG';
const REMOVE_RESOURCE_FROM_BAG = 'ui/process-designer/REMOVE_RESOURCE_FROM_BAG';
const SELECT_RESOURCE = 'ui/process-designer/SELECT_RESOURCE';
const SET_RESOURCE_FILTER = 'ui/process-designer/SET_RESOURCE_FILTER';

const UNDO = 'ui/process-designer/UNDO';
const REDO = 'ui/process-designer/REDO';

/*
 * Initial state
 */

const initialState = {
  // Configuration
  view: {
    type: EnumViews.Designer,
  },
  // Counters for creating unique identifiers of designer elements
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
  // Process steps
  steps: [],
  // Undo/Redo state
  undo: [{
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
 * Reducers
 */

function counterReducer(state, action) {
  switch (action.type) {
    case LOGOUT:
    case RESET:
      return {
        ...state,
        step: 0,
      };

    default:
      return state;
  }
}

function activeReducer(state, action) {
  switch (action.type) {
    case LOGOUT:
    case RESET:
    case REMOVE_STEP:
    case REMOVE_STEP_INPUT:
    case REMOVE_RESOURCE_FROM_BAG:
    case RESET_SELECTION:
      return {
        type: null,
        step: null,
        item: null,
      };

    case SELECT_STEP:
      return {
        type: EnumSelection.Step,
        step: action.step.index,
        item: null,
      };

    case SELECT_STEP_INPUT:
      return {
        type: EnumSelection.Input,
        step: action.step.index,
        item: action.resource.index,
      };

    case SELECT_STEP_DATA_SOURCE:
      return {
        type: EnumSelection.DataSource,
        step: action.step.index,
        item: action.dataSource.index,
      };

    case SELECT_RESOURCE:
      return {
        type: EnumSelection.Resource,
        step: null,
        item: action.resource.index,
      };

    default:
      return state;
  }
}

function stepReducer(state, action) {
  switch (action.type) {
    case REMOVE_STEP:
      return state
        .filter((step) => {
          return (step.index !== action.step.index);
        })
        .map((step) => {
          return {
            ...step,
            resources: step.resources.filter((index) => {
              return (index !== action.step.outputResourceIndex);
            })
          };
        });

    case ADD_STEP_INPUT:
      return state.map((step) => {
        if (step.index === action.step.index) {
          return {
            ...step,
            resources: [...step.resources, action.resource.index],
          };
        }
        return step;
      });

    case REMOVE_STEP_INPUT:
      return state.map((step) => {
        if (step.index === action.step.index) {
          return {
            ...step,
            resources: step.resources.filter((index) => {
              return (index !== action.resource.index);
            })
          };
        }
        return step;
      });

    case ADD_STEP_DATA_SOURCE:
      return state.map((step) => {
        if (step.index === action.step.index) {
          return {
            ...step,
            dataSources: [...step.dataSources, {
              ...action.dataSource,
              configuration: null,
              errors: {},
              index: step.dataSources.length,
            }],
          };
        }
        return step;
      });

    case REMOVE_STEP_DATA_SOURCE:
      return state.map((step) => {
        if (step.index === action.step.index) {
          return {
            ...step,
            dataSources: step.dataSources.filter((s) => {
              return (s.index !== action.dataSource.index);
            })
          };
        }
        return step;
      });

    case REMOVE_RESOURCE_FROM_BAG:
      return state.map((step) => {
        return {
          ...step,
          resources: step.resources.filter((index) => {
            return (index !== action.resource.index);
          })
        };
      });

    case UNDO:
      return undoReducer(state, action);

    case REDO:
      return redoReducer(state, action);

    default:
      return state;
  }
}

/**
 * Reorders steps
 *
 * @param {any} state
 * @param {any} action
 */
function moveStepReducer(state, action) {
  if (action.type === MOVE_STEP) {
    // Reorder steps
    let steps = [...state.steps];
    const step = steps.splice(action.dragOrder, 1);
    steps.splice(action.hoverOrder, 0, step[0]);

    // Reset step ordering
    steps = steps.map((value, index) => {
      return {
        ...value,
        order: index,
      };
    });

    // Reorder resources
    const resources = [];
    for (let r of state.resources) {
      if (r.inputType === EnumProcessInput.CATALOG) {
        resources.push(r);
      }
    }
    for (let s of steps) {
      if (s.tool !== EnumTool.CATALOG) {
        resources.push(state.resources.find((r) => r.index === s.outputResourceIndex));
      }
    }
    return {
      ...state,
      steps,
      resources
    };
  }
  return state;
}

/**
 * Handles {@link ADD_STEP} action. This action updates several parts of the
 * state
 *
 * @param {any} state
 * @param {any} action
 * @returns the new state
 */
function addStepReducer(state, action) {
  if (action.type == ADD_STEP) {
    // Update counters
    const stepIndex = ++state.counters.step;
    const resourceIndex = ++state.counters.resource;

    // Create step
    const step = {
      ...action.step,
      title: `${ToolTitles[action.step.tool]} ${stepIndex}`,
      resources: [],
      dataSources: [],
      configuration: null,
      errors: {},
      index: stepIndex,
    };
    if (step.tool !== EnumTool.CATALOG) {
      step.outputResourceIndex = resourceIndex;
    }

    // Create output resource
    const outputResource = {
      index: resourceIndex,
      inputType: EnumProcessInput.OUTPUT,
      resourceType: (action.step.tool == EnumTool.LIMES ? EnumResourceType.LINKED : EnumResourceType.POI),
      title: `${step.title} : Output`,
      iconClass: (action.step.tool == EnumTool.LIMES ? ResourceTypeIcons[EnumResourceType.LINKED] : ResourceTypeIcons[EnumResourceType.POI]),
      tool: action.step.tool,
      stepIndex: stepIndex,
    };

    // 1. Move TripleGeo steps to the start of the array
    // 2. Move Catalog step to the end of the array
    let steps = [...state.steps];
    const tripleGeoIndex = state.steps.reduce((result, value, index) => {
      if (value.tool === EnumTool.TripleGeo) {
        return index;
      }
      return result;
    }, -1);
    const catalogIndex = state.steps.reduce((result, value, index) => {
      if (value.tool === EnumTool.CATALOG) {
        return index;
      }
      return -1;
    }, -1);

    switch (action.step.tool) {
      case EnumTool.TripleGeo:
        steps.splice(tripleGeoIndex + 1, 0, step);
        break;
      case EnumTool.CATALOG:
        steps.push(step);
        break;
      default:
        if (catalogIndex < 0) {
          steps.push(step);
        } else {
          steps.splice(catalogIndex, 0, step);
        }
    }

    // Reset ordering
    steps = steps.map((value, index) => {
      return {
        ...value,
        order: index,
      };
    });

    // 1. Move TripleGeo output after catalog resources
    // 2. Move output from other tools to the end of the array
    const resources = [...state.resources];
    const outputResourceIndex = resources.reduce((result, value, index) => {
      if ((value.inputType === EnumProcessInput.CATALOG) || (value.tool === EnumTool.TripleGeo)) {
        return index;
      }
      return result;
    }, -1);

    switch (action.step.tool) {
      case EnumTool.CATALOG:
        // Ignore
        break;
      case EnumTool.TripleGeo:
        resources.splice(outputResourceIndex + 1, 0, outputResource);
        break;
      default:
        resources.push(outputResource);
        break;
    }

    // Compose new state
    return {
      ...state,
      counters: {
        ...state.counters,
        step: stepIndex,
        resource: resourceIndex,
      },
      steps,
      resources,
    };
  }
  return state;
}

/**
 * Handles {@link SET_STEP_PROPERTY} action. This action updates several parts of the
 * state
 *
 * @param {any} state
 * @param {any} action
 */
function setStepPropertyReducer(state, action) {
  if (action.type === SET_STEP_PROPERTY) {
    return {
      ...state,
      steps: state.steps.map((step) => {
        if (step.index === action.index) {
          switch (action.property) {
            case EnumStepProperty.Title:
              return {
                ...step,
                title: action.value,
              };
          }
        }
        return step;
      }),
      resources: state.resources.map((resource) => {
        if ((resource.inputType === EnumProcessInput.OUTPUT) && (resource.stepIndex === action.index)) {
          switch (action.property) {
            case EnumStepProperty.Title:
              return {
                ...resource,
                title: `${action.value} : Output`,
              };
          }
        }
        return resource;
      }),
    };
  }

  return state;
}

function resourceReducer(state, action) {
  switch (action.type) {
    case LOGOUT:
    case RESET:
      return state
        .filter((resource) => {
          return (resource.inputType === EnumProcessInput.CATALOG);
        });

    case REMOVE_STEP:
      return state
        .filter((resource) => {
          if (resource.inputType == EnumProcessInput.OUTPUT) {
            return (resource.stepIndex != action.step.index);
          }
          return true;
        });

    case REMOVE_RESOURCE_FROM_BAG:
      return state
        .filter((resource) => {
          if (resource.inputType == action.resource.inputType) {
            switch (resource.inputType) {
              case EnumProcessInput.CATALOG:
                return ((resource.id !== action.resource.id) || (resource.version != action.resource.version));
            }
          }
          return true;
        });
    default:
      return state;
  }
}

/**
 * Handles {@link ADD_RESOURCE_TO_BAG} action. This action updates several
 * parts of the state
 *
 * @param {any} state
 * @param {any} action
 * @returns the new state
 */
function addResourceToBagReducer(state, action) {
  if (action.type === ADD_RESOURCE_TO_BAG) {
    // Check if already exists
    const existing = state.resources
      .filter((resource) => {
        if (resource.inputType === action.resource.inputType) {
          switch (resource.inputType) {
            case EnumProcessInput.CATALOG:
              return ((resource.id === action.resource.id) && (resource.version === action.resource.version));
          }
        }
        return false;
      });

    if (existing.length != 0) {
      return state;
    }

    const resourceIndex = ++state.counters.resource;
    return {
      ...state,
      counters: {
        ...state.counters,
        resource: resourceIndex,
      },
      resources: [...state.resources, {
        ...action.resource,
        index: resourceIndex,
      }]
    };
  }
  return state;
}

function undoReducer(state, action) {
  if (state.undo.length === 1) {
    return state;
  }
  const undo = [...state.undo];
  const current = undo.splice(undo.length - 1, 1);
  const redo = [current[0], ...state.redo];
  const snapshot = undo[undo.length - 1];

  return {
    ...state,
    steps: [...snapshot.steps],
    resources: [...snapshot.resources],
    undo,
    redo,
  };
}

function redoReducer(state, action) {
  if (state.undo.length === 0) {
    return state;
  }
  const undo = [...state.undo];
  const redo = [...state.redo];

  const current = redo.splice(0, 1);
  undo.push(current[0]);

  const snapshot = undo[undo.length - 1];

  return {
    ...state,
    steps: [...snapshot.steps],
    resources: [...snapshot.resources],
    undo,
    redo,
  };
}

function filterReducer(state, action) {
  if (action.type === SET_RESOURCE_FILTER) {
    if (state.resource === action.filter) {
      return {
        ...state,
        resource: null,
      };
    } else {
      return {
        ...state,
        resource: action.filter,
      };
    }
  }
  return state;
}

export default (state = initialState, action) => {
  let newState = state;

  switch (action.type) {
    case LOGOUT:
    case RESET:
      return {
        ...state,
        counters: counterReducer(state.counters, action),
        steps: [],
        resources: resourceReducer(state.resources, action),
        undo: [{
          steps: [],
          resources: [],
        }],
        redo: [],
      };

    case ADD_STEP:
      newState = addStepReducer(state, action);
      break;

    case SET_STEP_PROPERTY:
      newState = setStepPropertyReducer(state, action);
      break;

    case REMOVE_STEP:
      newState = {
        ...state,
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };
      break;

    case MOVE_STEP:
      return moveStepReducer(state, action);

    case CONFIGURE_STEP_BEGIN:
      return {
        ...state,
        view: {
          type: EnumViews.StepConfiguration,
          step: _.cloneDeep(action.step),
          configuration: _.cloneDeep(action.configuration),
          errors: {},
        },
      };

    case CONFIGURE_STEP_UPDATE:
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

    case CONFIGURE_STEP_VALIDATE:
      return {
        ...state,
        view: {
          ...state.view,
          errors: { ...action.errors },
        }
      };

    case CONFIGURE_STEP_END:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumViews.Designer,
          },
        };
      }

      newState = {
        ...state,
        view: {
          type: EnumViews.Designer,
        },
        steps: state.steps.map((step) => {
          if ((step.index === action.step.index) && (action.configuration)) {
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

    case ADD_STEP_INPUT:
    case REMOVE_STEP_INPUT:
      newState = {
        ...state,
        steps: stepReducer(state.steps, action),
      };
      break;

    case ADD_STEP_DATA_SOURCE:
    case REMOVE_STEP_DATA_SOURCE:
      newState = {
        ...state,
        steps: stepReducer(state.steps, action),
      };
      break;

    case CONFIGURE_DATA_SOURCE_BEGIN:
      return {
        ...state,
        view: {
          type: EnumViews.DataSourceConfiguration,
          step: _.cloneDeep(action.step),
          dataSource: _.cloneDeep(action.dataSource),
          configuration: _.cloneDeep(action.configuration),
          errors: {},
        },
      };

    case CONFIGURE_DATA_SOURCE_UPDATE:
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

    case CONFIGURE_DATA_SOURCE_VALIDATE:
      return {
        ...state,
        view: {
          ...state.view,
          errors: { ...action.errors },
        }
      };

    case CONFIGURE_DATA_SOURCE_END:
      if (!action.configuration) {
        return {
          ...state,
          view: {
            type: EnumViews.Designer,
          },
        };
      }

      newState = {
        ...state,
        view: {
          type: EnumViews.Designer,
        },
        steps: state.steps.map((step) => {
          if ((step.index === action.step.index) && (step.tool === EnumTool.TripleGeo)) {
            return {
              ...step,
              dataSources: step.dataSources.map((ds) => {
                if ((ds.index === action.dataSource.index) && (action.configuration)) {
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

    case ADD_RESOURCE_TO_BAG:
      newState = addResourceToBagReducer(state, action);
      break;

    case REMOVE_RESOURCE_FROM_BAG:
      newState = {
        ...state,
        counters: counterReducer(state.counters, action),
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };
      break;

    case SET_RESOURCE_FILTER:
      return {
        ...state,
        filters: filterReducer(state.filters, action),
        resources: resourceReducer(state.resources, action),
      };

    case RESET_SELECTION:
    case SELECT_STEP:
    case SELECT_STEP_INPUT:
    case SELECT_STEP_DATA_SOURCE:
    case SELECT_RESOURCE:
      return {
        ...state,
        active: activeReducer(state.active, action)
      };

    case UNDO:
    case REDO:
      return stepReducer(state, action);

    default:
      return state;
  }

  return {
    ...newState,
    undo: [...newState.undo, {
      steps: newState.steps,
      resources: newState.resources
    }],
    redo: [],
  };
};

/*
 * Action creators
 */

export const reset = function () {
  return {
    type: RESET,
  };
};

export const addStep = function (step) {
  return {
    type: ADD_STEP,
    step,
  };
};

export const moveStep = function (dragOrder, hoverOrder) {
  return {
    type: MOVE_STEP,
    dragOrder,
    hoverOrder,
  };
};

export const removeStep = function (step) {
  return {
    type: REMOVE_STEP,
    step,
  };
};

export const resetActive = function () {
  return {
    type: RESET_SELECTION,
  };
};

export const setActiveStep = function (step) {
  return {
    type: SELECT_STEP,
    step,
  };
};

export const setStepProperty = function (index, property, value) {
  return {
    type: SET_STEP_PROPERTY,
    index,
    property,
    value,
  };
};

export const configureStepBegin = function (step, configuration) {
  return {
    type: CONFIGURE_STEP_BEGIN,
    step,
    configuration,
  };
};

export const configureStepValidate = function (step, errors) {
  return {
    type: CONFIGURE_STEP_VALIDATE,
    step,
    errors,
  };
};

export const configureStepUpdate = function (step, configuration) {
  return {
    type: CONFIGURE_STEP_UPDATE,
    step,
    configuration,
  };
};

export const configureStepEnd = function (step, configuration, errors) {
  return {
    type: CONFIGURE_STEP_END,
    step,
    configuration,
    errors,
  };
};

export const addStepInput = function (step, resource) {
  return {
    type: ADD_STEP_INPUT,
    step,
    resource,
  };
};

export const removeStepInput = function (step, resource) {
  return {
    type: REMOVE_STEP_INPUT,
    step,
    resource,
  };
};

export const setActiveStepInput = function (step, resource) {
  return {
    type: SELECT_STEP_INPUT,
    step,
    resource,
  };
};

export const addStepDataSource = function (step, dataSource) {
  return {
    type: ADD_STEP_DATA_SOURCE,
    step,
    dataSource,
  };
};

export const removeStepDataSource = function (step, dataSource) {
  return {
    type: REMOVE_STEP_DATA_SOURCE,
    step,
    dataSource,
  };
};

export const setActiveStepDataSource = function (step, dataSource) {
  return {
    type: SELECT_STEP_DATA_SOURCE,
    step,
    dataSource,
  };
};

export const configureStepDataSourceBegin = function (step, dataSource, configuration) {
  return {
    type: CONFIGURE_DATA_SOURCE_BEGIN,
    step,
    dataSource,
    configuration,
  };
};

export const configureStepDataSourceValidate = function (step, dataSource, errors) {
  return {
    type: CONFIGURE_STEP_VALIDATE,
    step,
    dataSource,
    errors,
  };
};

export const configureStepDataSourceUpdate = function (step, dataSource, configuration) {
  return {
    type: CONFIGURE_STEP_UPDATE,
    step,
    dataSource,
    configuration,
  };
};

export const configureStepDataSourceEnd = function (step, dataSource, configuration, errors) {
  return {
    type: CONFIGURE_DATA_SOURCE_END,
    step,
    dataSource,
    configuration,
    errors,
  };
};

export const addResourceToBag = function (resource) {
  return {
    type: ADD_RESOURCE_TO_BAG,
    resource,
  };
};

export const removeResourceFromBag = function (resource) {
  return {
    type: REMOVE_RESOURCE_FROM_BAG,
    resource,
  };
};

export const filterResource = function (filter) {
  return {
    type: SET_RESOURCE_FILTER,
    filter,
  };
};

export const setActiveResource = function (resource) {
  return {
    type: SELECT_RESOURCE,
    resource,
  };
};

export const undo = function () {
  return {
    type: UNDO,
  };
};

export const redo = function () {
  return {
    type: REDO,
  };
};

/*
 * Selectors
 */

export function filteredResources(state) {
  if (state.filters.resource) {
    switch (state.filters.resource) {
      case EnumProcessInput.CATALOG:
        return state.resources.filter((r) => r.inputType === EnumProcessInput.CATALOG);
      case EnumProcessInput.OUTPUT:
        return state.resources.filter((r) => r.inputType === EnumProcessInput.OUTPUT);
      case EnumResourceType.POI:
        return state.resources.filter((r) => r.resourceType === EnumResourceType.POI);
      case EnumResourceType.LINKED:
        return state.resources.filter((r) => r.resourceType === EnumResourceType.LINKED);
    }
  }
  return state.resources;
}
