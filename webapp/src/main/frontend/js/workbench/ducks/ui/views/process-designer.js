import _ from 'lodash';

import {
  EnumInputType,
  EnumResourceType,
  EnumSelection,
  EnumStepProperty,
  EnumTool,
  EnumToolboxItem,
  EnumViews,
  ResourceTypeIcons,
  ToolTitles,
} from '../../../components/views/process/designer';

import * as processService from '../../../service/process';

/*
 * Actions
 */

import { LOGOUT } from '../../user';
import { EnumProcessSaveAction } from '../../../model/constants';

const RESET = 'ui/process-designer/RESET';
const RESET_SELECTION = 'ui/process-designer/RESET_SELECTION';

const SAVE_SEND_REQUEST = 'ui/process-designer/SAVE_SEND_REQUEST';
const SAVE_RECEIVE_RESPONSE = 'ui/process-designer/SAVE_RECEIVE_RESPONSE';

const LOAD_SEND_REQUEST = 'ui/process-designer/LOAD_SEND_REQUEST';
const LOAD_RECEIVE_RESPONSE = 'ui/process-designer/LOAD_RECEIVE_RESPONSE';

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
const CONFIGURE_DATA_SOURCE_BEGIN = 'ui/process-designer/CONFIGURE_DATA_SOURCE_BEGIN';
const CONFIGURE_DATA_SOURCE_VALIDATE = 'ui/process-designer/CONFIGURE_DATA_SOURCE_VALIDATE';
const CONFIGURE_DATA_SOURCE_UPDATE = 'ui/process-designer/CONFIGURE_DATA_SOURCE_UPDATE';
const CONFIGURE_DATA_SOURCE_END = 'ui/process-designer/CONFIGURE_DATA_SOURCE_END';
const SELECT_STEP_DATA_SOURCE = 'ui/process-designer/SELECT_STEP_DATA_SOURCE';

const ADD_RESOURCE_TO_BAG = 'ui/process-designer/ADD_RESOURCE_TO_BAG';
const REMOVE_RESOURCE_FROM_BAG = 'ui/process-designer/REMOVE_RESOURCE_FROM_BAG';
const SELECT_RESOURCE = 'ui/process-designer/SELECT_RESOURCE';
const SET_RESOURCE_FILTER = 'ui/process-designer/SET_RESOURCE_FILTER';

const SELECT_PROCESS = 'ui/process-designer/SELECT_PROCESS';
const PROCESS_VALIDATE = 'ui/process-designer/PROCESS_VALIDATE';
const PROCESS_UPDATE = 'ui/process-designer/PROCESS_UPDATE';

const UNDO = 'ui/process-designer/UNDO';
const REDO = 'ui/process-designer/REDO';

/*
 * Initial state
 */

function initializeGroups() {
  return [{
    key: 0,
    steps: [],
  }, {
    key: 1,
    steps: [],
  }];
}

function initializeProcess() {
  return {
    properties: {
      id: null,
      name: '',
      description: '',
    },
    errors: {},
  };
}

const initialState = {
  // Configuration
  view: {
    type: EnumViews.Designer,
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
 * Reducers
 */

function counterReducer(state, action) {
  switch (action.type) {
    case LOGOUT:
    case RESET:
      // Do not reset resource counter since resource bag items
      // of type EnumInputType.CATALOG are not deleted
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

    case SELECT_PROCESS:
      return {
        type: EnumSelection.Process,
        step: null,
        item: action.process,
      };

    case SELECT_STEP:
      return {
        type: EnumSelection.Step,
        step: action.step.key,
        item: null,
      };

    case SELECT_STEP_INPUT:
      return {
        type: EnumSelection.Input,
        step: action.step.key,
        item: action.resource.key,
      };

    case SELECT_STEP_DATA_SOURCE:
      return {
        type: EnumSelection.DataSource,
        step: action.step.key,
        item: action.dataSource.key,
      };

    case SELECT_RESOURCE:
      return {
        type: EnumSelection.Resource,
        step: null,
        item: action.resource.key,
      };

    default:
      return state;
  }
}

function groupReducer(state, action) {
  switch (action.type) {
    case REMOVE_STEP:
      return state
        .map((group) => {
          // Remove step from group
          return {
            ...group,
            steps: group.steps.filter((key) => {
              return (key !== action.step.key);
            })
          };
        }).filter((group, index, array) => {
          // Remove empty groups except for the last one
          return !((index > 0) && (group.steps.length === 0) && (group !== array[array.length - 1]));
        }).map((group, index) => {
          // Update indexes to avoid duplicate group keys
          return {
            ...group,
            key: index,
          };
        });

    default:
      return state;
  }
}

function stepReducer(state, action) {
  switch (action.type) {
    case REMOVE_STEP:
      return state
        .filter((step) => {
          return (step.key !== action.step.key);
        })
        .map((step) => {
          return {
            ...step,
            resources: step.resources.filter((key) => {
              return (key !== action.step.outputKey);
            })
          };
        });

    case ADD_STEP_INPUT:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            resources: [...step.resources, action.resource.key],
          };
        }
        return step;
      });

    case REMOVE_STEP_INPUT:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            resources: step.resources.filter((key) => {
              return (key !== action.resource.key);
            })
          };
        }
        return step;
      });

    case ADD_STEP_DATA_SOURCE:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            dataSources: [...step.dataSources, {
              ...action.dataSource,
              configuration: null,
              errors: {},
              key: step.dataSources.length,
            }],
          };
        }
        return step;
      });

    case REMOVE_STEP_DATA_SOURCE:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            dataSources: step.dataSources.filter((s) => {
              return (s.key !== action.dataSource.key);
            })
          };
        }
        return step;
      });

    case REMOVE_RESOURCE_FROM_BAG:
      return state.map((step) => {
        return {
          ...step,
          resources: step.resources.filter((key) => {
            return (key !== action.resource.key);
          })
        };
      });

    default:
      return state;
  }
}

/**
 * Reorders steps
 *
 * @param {any} state
 * @param {any} action
 * @returns the new state
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
      if (r.inputType === EnumInputType.CATALOG) {
        resources.push(r);
      }
    }
    for (let s of steps) {
      if (s.tool !== EnumTool.CATALOG) {
        resources.push(state.resources.find((r) => r.key === s.outputKey));
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
    const stepKey = ++state.counters.step;
    const resourceKey = ++state.counters.resource;

    // Create step
    const step = {
      ...action.step,
      group: action.group.key,
      name: `${ToolTitles[action.step.tool]} ${stepKey}`,
      resources: [],
      dataSources: [],
      configuration: null,
      errors: {},
      key: stepKey,
    };
    if (step.tool !== EnumTool.CATALOG) {
      step.outputKey = resourceKey;
    }

    // Create output resource
    const outputResource = {
      key: resourceKey,
      inputType: EnumInputType.OUTPUT,
      resourceType: (action.step.tool == EnumTool.LIMES ? EnumResourceType.LINKED : EnumResourceType.POI),
      name: `${step.name} : Output`,
      iconClass: (action.step.tool == EnumTool.LIMES ? ResourceTypeIcons[EnumResourceType.LINKED] : ResourceTypeIcons[EnumResourceType.POI]),
      tool: action.step.tool,
      stepKey,
    };

    // Update steps array
    let steps = [...state.steps, step];

    // Reset ordering
    steps = steps.map((value, index) => {
      return {
        ...value,
        order: index,
      };
    });

    // Update groups array
    const groups = state.groups.map((group) => {
      if (group.key == action.group.key) {
        return {
          ...group,
          steps: [...group.steps, step.key],
        };
      }
      return group;
    });
    // If the last step group is not empty, add a new empty step group
    if (groups[groups.length - 1].steps.length !== 0) {
      groups.push({
        key: groups.length,
        steps: [],
      });
    }

    // 1. Move TripleGeo output after catalog resources
    // 2. Move output from other tools to the end of the array
    const resources = [...state.resources];
    const outputResourceIndex = resources.reduce((result, value, index) => {
      if ((value.inputType === EnumInputType.CATALOG) || (value.tool === EnumTool.TripleGeo)) {
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
        step: stepKey,
        resource: resourceKey,
      },
      groups,
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
        if (step.key === action.key) {
          switch (action.property) {
            case EnumStepProperty.Title:
              return {
                ...step,
                name: action.value,
              };
          }
        }
        return step;
      }),
      resources: state.resources.map((resource) => {
        if ((resource.inputType === EnumInputType.OUTPUT) && (resource.stepKey === action.key)) {
          switch (action.property) {
            case EnumStepProperty.Title:
              return {
                ...resource,
                name: `${action.value} : Output`,
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
          return (resource.inputType === EnumInputType.CATALOG);
        });

    case REMOVE_STEP:
      return state
        .filter((resource) => {
          if (resource.inputType == EnumInputType.OUTPUT) {
            return (resource.stepKey != action.step.key);
          }
          return true;
        });

    case REMOVE_RESOURCE_FROM_BAG:
      return state
        .filter((resource) => {
          if (resource.inputType == action.resource.inputType) {
            switch (resource.inputType) {
              case EnumInputType.CATALOG:
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
      .find((resource) => {
        if (resource.inputType === action.resource.inputType) {
          switch (resource.inputType) {
            case EnumInputType.CATALOG:
              return ((resource.id === action.resource.id) && (resource.version === action.resource.version));
          }
        }
        return false;
      });

    if (existing) {
      return state;
    }

    const resourceKey = ++state.counters.resource;
    return {
      ...state,
      counters: {
        ...state.counters,
        resource: resourceKey,
      },
      resources: [...state.resources, {
        ...action.resource,
        key: resourceKey,
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
    groups: snapshot.groups.length === 0 ? initializeGroups() : [...snapshot.groups],
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
    groups: [...snapshot.groups],
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

function loadReducer(state, action) {
  const data = action.process;

  // Create process
  const process = {
    properties: {
      id: data.id,
      version: data.version,
      name: data.name,
      description: data.description,
    },
    errors: {},
  };

  // Create groups
  const groupCounter = data.steps.reduce((result, current) => {
    return Math.max(current.group, result);
  }, 0) + 2;

  const groups = [];
  for (let key = 0; key < groupCounter; key++) {
    groups.push({
      key,
      steps: data.steps.filter((step) => step.group == key).map((step) => step.key),
    });
  }
  // Create steps
  const steps = data.steps.map((step, index) => {
    return {
      ...step,
    };
  });
  // Create resources
  let resources = state.resources
    .filter((r) => {
      // Remove any output resources from the designer
      return (r.inputType === EnumInputType.CATALOG);
    }).map((r) => {
      // Reset key for existing catalog resources
      return {
        ...r,
        key: null,
      };
    });
  data.resources.forEach((r) => {
    if (r.inputType === EnumInputType.CATALOG) {
      const existing = resources.find((value) => value.id === r.id && value.version === r.version);
      if (existing) {
        // Keep existing key
        existing.key = r.key;
      } else {
        resources.push(r);
      }
    }
    if (r.inputType === EnumInputType.OUTPUT) {
      resources.push(r);
    }
  });
  let maxResourceKey = resources.filter((r) => r.key != null).reduce((result, current) => Math.max(result, current.key), 0);
  resources = resources.map((r) => {
    if (!r.key) {
      r.key = ++maxResourceKey;
    }
    return r;
  });

  return {
    ...state,
    // Counters for creating unique identifiers for designer elements
    counters: {
      step: steps.reduce((result, current) => Math.max(result, current.key), 0),
      resource: resources.reduce((result, current) => Math.max(result, current.key), 0),
    },
    // Designer active item
    active: {
      type: EnumSelection.Process,
      step: null,
      item: process,
    },
    // Resource bag items
    resources,
    // True if editing is disabled
    readOnly: action.readOnly,
    // Current process instance
    process,
    // Workflow step groups
    groups,
    // Process steps
    steps,
    // Undo/Redo state
    undo: [{
      groups: [],
      steps: [],
      resources: [],
    }],
    redo: [],
  };
}

export default (state = initialState, action) => {
  let newState = state;

  switch (action.type) {
    case LOGOUT:
    case RESET:
      return {
        ...state,
        active: activeReducer(state.active, action),
        counters: counterReducer(state.counters, action),
        readOnly: false,
        process: initializeProcess(),
        groups: initializeGroups(),
        steps: [],
        resources: resourceReducer(state.resources, action),
        undo: [{
          groups: [],
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
        groups: groupReducer(state.groups, action),
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

    case PROCESS_VALIDATE:
      return {
        ...state,
        process: {
          ...state.process,
          errors: { ...action.errors },
        },
      };

    case PROCESS_UPDATE:
      return {
        ...state,
        process: {
          properties: { ...action.properties },
        },
      };

    case ADD_RESOURCE_TO_BAG:
      newState = addResourceToBagReducer(state, action);
      break;

    case REMOVE_RESOURCE_FROM_BAG:
      newState = {
        ...state,
        active: activeReducer(state.active, action),
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
    case SELECT_PROCESS:
    case SELECT_STEP:
    case SELECT_STEP_INPUT:
    case SELECT_STEP_DATA_SOURCE:
    case SELECT_RESOURCE:
      return {
        ...state,
        active: activeReducer(state.active, action)
      };

    case UNDO:
      return undoReducer(state, action);

    case REDO:
      return redoReducer(state, action);

    case LOAD_RECEIVE_RESPONSE:
      return loadReducer(state, action);

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

const processLoaded = function (process, readOnly) {
  return {
    type: LOAD_RECEIVE_RESPONSE,
    process,
    readOnly,
  };
};

export const reset = function () {
  return {
    type: RESET,
  };
};

export const addStep = function (group, step) {
  return {
    type: ADD_STEP,
    group,
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

export const setStepProperty = function (key, property, value) {
  return {
    type: SET_STEP_PROPERTY,
    key,
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

export const setActiveProcess = function (process) {
  return {
    type: SELECT_PROCESS,
    process,
  };
};

export const processValidate = function (errors) {
  return {
    type: PROCESS_VALIDATE,
    errors,
  };
};

export const processUpdate = function (properties) {
  return {
    type: PROCESS_UPDATE,
    properties,
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

/**
 * Thunks
 */

export function fetchProcess(id) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcess(id, token).then((process) => {
      dispatch(processLoaded(process, false));
    });
  };
}

export function fetchProcessRevision(id, version) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }
    if (Number.isNaN(version)) {
      return Promise.reject(new Error('Invalid version. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcessRevision(id, version, token).then((process) => {
      dispatch(processLoaded(process, true));
    });
  };
}

export function edit(id) {
  return (dispatch, getState) => {
    if (Number.isNaN(id)) {
      return Promise.reject(new Error('Invalid id. Failed to load workflow instance'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.fetchProcessRevision(id, token).then((process) => {
      dispatch(processLoaded(process, false));
    });
  };
}

export function save(action, process) {
  return (dispatch, getState) => {
    if (action !== EnumProcessSaveAction.Save) {
      return Promise.reject(new Error('Not Implemented!'));
    }
    if (!processService.validate(action, process)) {
      return Promise.reject(new Error('Validation has failed'));
    }

    const { meta: { csrfToken: token } } = getState();
    return processService.save(action, process, token);
  };
}

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
