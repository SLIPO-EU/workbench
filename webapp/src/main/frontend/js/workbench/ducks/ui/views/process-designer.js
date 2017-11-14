import {
  EnumTool,
  EnumProcessInput,
  EnumResourceType,
  EnumViews,
} from '../../../components/views/process/designer/constants';

/*
 * Actions
 */

const RESET = 'ui/process-designer/RESET';

const ADD_STEP = 'ui/process-designer/ADD_STEP';
const REMOVE_STEP = 'ui/process-designer/REMOVE_STEP';
const MOVE_STEP = 'ui/process-designer/MOVE_STEP';
const CONFIGURE_STEP_BEGIN = 'ui/process-designer/CONFIGURE_STEP_BEGIN';
const CONFIGURE_STEP_END = 'ui/process-designer/CONFIGURE_STEP_END';
const SELECT_STEP = 'ui/process-designer/SELECT_STEP';

const ADD_STEP_INPUT = 'ui/process-designer/ADD_STEP_INPUT';
const REMOVE_STEP_INPUT = 'ui/process-designer/REMOVE_STEP_INPUT';
const SELECT_STEP_INPUT = 'ui/process-designer/SELECT_STEP_INPUT';

const ADD_STEP_DATA_SOURCE = 'ui/process-designer/ADD_STEP_DATA_SOURCE';
const REMOVE_STEP_DATA_SOURCE = 'ui/process-designer/REMOVE_STEP_DATA_SOURCE';
const SELECT_STEP_DATA_SOURCE = 'ui/process-designer/SELECT_STEP_DATA_SOURCE';
const CONFIGURE_DATA_SOURCE_BEGIN = 'ui/process-designer/CONFIGURE_DATA_SOURCE_BEGIN';
const CONFIGURE_DATA_SOURCE_END = 'ui/process-designer/CONFIGURE_DATA_SOURCE_END';

const ADD_RESOURCE_TO_BAG = 'ui/process-designer/ADD_RESOURCE_TO_BAG';
const REMOVE_RESOURCE_FROM_BAG = 'ui/process-designer/REMOVE_RESOURCE_FROM_BAG';
const SELECT_RESOURCE = 'ui/process-designer/SELECT_RESOURCE';

/*
 * Initial state
 */

const initialState = {
  // View configuration
  view: {
    type: EnumViews.Designer,
  },
  // Counters for creating unique identifiers of designer elements
  counters: {
    step: 0,
    resource: 0,
  },
  // Selected designer item status
  active: {
    step: null,
    stepInput: null,
    stepDataSource: null,
    resource: null,
  },
  // Resource bag items
  resources: [],
  // Process steps
  steps: []
};

/*
 * Reducers
 */

function counterReducer(state, action) {
  switch (action.type) {
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
    case RESET:
    case REMOVE_STEP:
    case REMOVE_STEP_INPUT:
    case REMOVE_RESOURCE_FROM_BAG:
      return {
        step: null,
        stepInput: null,
        stepDataSource: null,
        resource: null,
      };

    case SELECT_STEP:
      return {
        ...state,
        step: action.step.index,
        stepInput: null,
        stepDataSource: null,
        resource: null,
      };

    case SELECT_STEP_INPUT:
      return {
        ...state,
        step: action.step.index,
        stepInput: action.resource.index,
        stepDataSource: null,
        resource: null,
      };

    case SELECT_STEP_DATA_SOURCE:
      return {
        ...state,
        step: action.step.index,
        stepInput: null,
        stepDataSource: action.dataSource.index,
        resource: null,
      };

    case SELECT_RESOURCE:
      return {
        ...state,
        step: null,
        stepInput: null,
        stepDataSource: null,
        resource: action.resource.index,
      };

    default:
      return state;
  }
}

function stepReducer(state, action) {
  switch (action.type) {
    case REMOVE_STEP:
      return state
        .filter((s) => {
          return (s.index != action.step.index);
        })
        .map((s) => {
          return {
            ...s,
            resources: s.resources.filter((r) => {
              return ((r.inputType != EnumProcessInput.OUTPUT) || (r.stepIndex != action.step.index));
            })
          };
        });

    case ADD_STEP_INPUT:
      return state.map((step) => {
        if (step.index == action.step.index) {
          const resources = [...step.resources];
          if ((step.tool != EnumTool.CATALOG) && (action.resource.dependencies.length > 0)) {
            resources.splice(0, resources.length);
            for (let d of action.resource.dependencies) {
              resources.push(d);
            }
          }
          resources.push(action.resource);
          return {
            ...step,
            resources: resources,
          };
        }
        return step;
      });

    case REMOVE_STEP_INPUT:
      return state.map((step) => {
        if (step.index == action.step.index) {
          return {
            ...step,
            resources: step.resources.filter((r) => {
              return (r.index != action.resource.index);
            })
          };
        }
        return step;
      });

    case ADD_STEP_DATA_SOURCE:
      return state.map((step) => {
        if (step.index == action.step.index) {
          return {
            ...step,
            dataSources: [...step.dataSources, {
              ...action.dataSource,
              configuration: null,
              index: step.dataSources.length,
            }],
          };
        }
        return step;
      });

    case REMOVE_STEP_DATA_SOURCE:
      return state.map((step) => {
        if (step.index == action.step.index) {
          return {
            ...step,
            dataSources: step.dataSources.filter((s) => {
              return (s.index != action.dataSource.index);
            })
          };
        }
        return step;
      });

    case REMOVE_RESOURCE_FROM_BAG:
      return state.map((step) => {
        return {
          ...step,
          resources: step.resources.filter((r) => {
            return (r.index != action.resource.index);
          })
        };
      });
    default:
      return state;
  }
}

/**
 * Handles {@link ADD_STEP} action. This action updates several parts of the
 * state
 *
 * @param {any} state
 * @param {any} action
 * @returns
 */
function addStepReducer(state, action) {
  if (action.type == ADD_STEP) {
    const stepIndex = ++state.counters.step;
    const resourceIndex = ++state.counters.resource;

    const resources = [...state.resources];
    if (action.step.tool !== EnumTool.CATALOG) {
      resources.push({
        index: resourceIndex,
        inputType: EnumProcessInput.OUTPUT,
        resourceType: (action.step.tool == EnumTool.LIMES ? EnumResourceType.LINKED : EnumResourceType.POI),
        title: `${action.step.title} : Output`,
        iconClass: (action.step.tool == EnumTool.LIMES ? 'fa fa-random' : 'fa fa-database'),
        dependencies: [],
        stepIndex: stepIndex,
      });
    }

    return {
      ...state,
      counters: {
        ...state.counters,
        step: stepIndex,
        resource: resourceIndex,
      },
      steps: [...state.steps, {
        ...action.step,
        resources: [],
        dataSources: [],
        configuration: null,
        index: stepIndex,
      }],
      resources: resources,
    };
  }
  return state;
}

function resourceReducer(state, action) {
  switch (action.type) {
    case RESET:
      return state
        .filter((resource) => {
          return (resource.inputType == EnumProcessInput.CATALOG);
        })
        .map((resource, index) => {
          return {
            ...resource,
            index: index,
          };
        });

    case REMOVE_STEP:
      return state
        .filter((r) => {
          if (r.inputType == EnumProcessInput.OUTPUT) {
            return (r.stepIndex != action.step.index);
          }
          return true;
        });

    case REMOVE_RESOURCE_FROM_BAG:
      return state
        .filter((r) => {
          if (r.inputType == action.resource.inputType) {
            switch (r.inputType) {
              case EnumProcessInput.CATALOG:
                return ((r.id != action.resource.id) || (r.version != action.resource.version));
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
 * @returns
 */
function addResourceToBagReducer(state, action) {
  if (action.type == ADD_RESOURCE_TO_BAG) {
    // Check if already exists
    const existing = state.resources
      .filter((r) => {
        if (r.inputType == action.resource.inputType) {
          switch (r.inputType) {
            case EnumProcessInput.CATALOG:
              return ((r.id == action.resource.id) && (r.version == action.resource.version));
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
        dependencies: [],
        ...action.resource,
        index: resourceIndex,
      }]
    };
  }
  return state;
}

export default (state = initialState, action) => {
  switch (action.type) {
    case RESET:
      return {
        ...state,
        counters: counterReducer(state.counters, action),
        steps: [],
        resources: resourceReducer(state.resources, action),
      };
    case ADD_STEP:
      return addStepReducer(state, action);

    case REMOVE_STEP:
      return {
        ...state,
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };

    case CONFIGURE_STEP_BEGIN:
      return {
        ...state,
        view: {
          type: EnumViews.StepConfiguration,
          step: action.step,
          configuration: action.configuration,
        },
      };

    case CONFIGURE_STEP_END:
      return {
        ...state,
        view: {
          type: EnumViews.Designer,
        },
        steps: state.steps.map((step) => {
          if ((step.index === action.step.index) && (action.configuration)) {
            return {
              ...step,
              configuration: action.configuration,
            };
          }
          return step;
        })
      };

    case ADD_STEP_INPUT:
    case REMOVE_STEP_INPUT:
      return {
        ...state,
        steps: stepReducer(state.steps, action),
      };

    case ADD_STEP_DATA_SOURCE:
    case REMOVE_STEP_DATA_SOURCE:
      return {
        ...state,
        steps: stepReducer(state.steps, action),
      };

    case CONFIGURE_DATA_SOURCE_BEGIN:
      return {
        ...state,
        view: {
          type: EnumViews.DataSourceConfiguration,
          step: action.step,
          dataSource: action.dataSource,
          configuration: action.configuration,
        },
      };

    case CONFIGURE_DATA_SOURCE_END:
      return {
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
                    configuration: action.configuration,
                  };
                }
                return ds;
              })
            };
          }
          return step;
        })
      };

    case ADD_RESOURCE_TO_BAG:
      return addResourceToBagReducer(state, action);

    case REMOVE_RESOURCE_FROM_BAG:
      return {
        ...state,
        counters: counterReducer(state.counters, action),
        steps: stepReducer(state.steps, action),
        resources: resourceReducer(state.resources, action),
      };

    case SELECT_STEP:
    case SELECT_STEP_INPUT:
    case SELECT_STEP_DATA_SOURCE:
    case SELECT_RESOURCE:
      return {
        ...state,
        active: activeReducer(state.active, action)
      };

    default:
      return state;
  }
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

export const removeStep = function (step) {
  return {
    type: REMOVE_STEP,
    step,
  };
};

export const setActiveStep = function (step) {
  return {
    type: SELECT_STEP,
    step,
  };
};

export const configureStepBegin = function (step, configuration) {
  return {
    type: CONFIGURE_STEP_BEGIN,
    step,
    configuration,
  };
};

export const configureStepEnd = function (step, configuration) {
  return {
    type: CONFIGURE_STEP_END,
    step,
    configuration,
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

export const configureStepDataSourceEnd = function (step, dataSource, configuration) {
  return {
    type: CONFIGURE_DATA_SOURCE_END,
    step,
    dataSource,
    configuration,
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

export const setActiveResource = function (resource) {
  return {
    type: SELECT_RESOURCE,
    resource,
  };
};
