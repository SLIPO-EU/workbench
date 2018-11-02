import _ from 'lodash';
import * as Types from '../types';

import {
  DEFAULT_OUTPUT_PART,
  defaultTripleGeoValues,
  defaultLimesValues,
  defaultFagiValues,
  defaultDeerValues,
  defaultReverseTripleGeoValues,
  EnumDataFormat,
  EnumInputType,
  EnumResourceType,
  EnumStepProperty,
  EnumTool,
  ResourceTypeIcons,
  ToolConfigurationSettings,
  ToolTitles,
} from '../../../../../model/process-designer';

import {
  validateConfiguration as tripleGeoValidator,
} from '../../../../../service/toolkit/triplegeo';
import {
  validateConfiguration as limesValidator,
} from '../../../../../service/toolkit/limes';
import {
  validateConfiguration as fagiValidator,
} from '../../../../../service/toolkit/fagi';
import {
  validateConfiguration as deerValidator,
} from '../../../../../service/toolkit/deer';
import {
  validateConfiguration as reverseTripleGeoValidator,
} from '../../../../../service/toolkit/triplegeo-reverse';

function createTripleGeoDefaultConfiguration(appConfiguration, effectiveVersion) {
  const configuration = {
    ...defaultTripleGeoValues,
    version: effectiveVersion || appConfiguration.tripleGeo.version,
  };

  try {
    tripleGeoValidator(configuration);
  } catch (errors) {
    return {
      configuration,
      errors,
    };
  }

  return {
    configuration,
    errors: {},
  };
}

function createLimesDefaultConfiguration(appConfiguration, effectiveVersion) {
  const configuration = {
    ...defaultLimesValues,
    version: effectiveVersion || appConfiguration.limes.version,
  };

  try {
    limesValidator(configuration);
  } catch (errors) {
    return {
      configuration,
      errors,
    };
  }

  return {
    configuration,
    errors: {},
  };
}

function createFagiDefaultConfiguration(appConfiguration, effectiveVersion) {
  const configuration = {
    ...defaultFagiValues,
    version: effectiveVersion || appConfiguration.fagi.version,
  };

  try {
    fagiValidator(configuration);
  } catch (errors) {
    return {
      configuration,
      errors,
    };
  }

  return {
    configuration,
    errors: {},
  };
}

function createDeerDefaultConfiguration(appConfiguration, effectiveVersion) {
  const configuration = {
    ...defaultDeerValues,
    version: effectiveVersion || appConfiguration.deer.version,
  };

  try {
    deerValidator(configuration);
  } catch (errors) {
    return {
      configuration,
      errors,
    };
  }

  return {
    configuration,
    errors: {},
  };
}

function createReverseTripleGeoDefaultConfiguration(appConfiguration, effectiveVersion) {
  const configuration = {
    ...defaultReverseTripleGeoValues,
    version: effectiveVersion || appConfiguration.tripleGeo.version,
  };

  try {
    reverseTripleGeoValidator(configuration);
  } catch (errors) {
    return {
      configuration,
      errors,
    };
  }

  return {
    configuration,
    errors: {},
  };
}

function createDefaultConfiguration(steps, tool, appConfiguration) {
  const effectiveVersion = steps.reduce((version, step) => version ? version : step.configuration ? step.configuration.version : null, null) || null;

  switch (tool) {
    case EnumTool.TripleGeo:
      return createTripleGeoDefaultConfiguration(appConfiguration, effectiveVersion);

    case EnumTool.LIMES:
      return createLimesDefaultConfiguration(appConfiguration, effectiveVersion);

    case EnumTool.FAGI:
      return createFagiDefaultConfiguration(appConfiguration, effectiveVersion);

    case EnumTool.DEER:
      return createDeerDefaultConfiguration(appConfiguration, effectiveVersion);

    case EnumTool.ReverseTripleGeo:
      return createReverseTripleGeoDefaultConfiguration(appConfiguration, effectiveVersion);

    default:
      return {
        configuration: null,
        errors: {},
      };
  }
}

function addStep(state, action) {
  // Update counters
  const stepKey = ++state.counters.step;
  const resourceKey = ++state.counters.resource;

  // Create step
  const step = {
    ...action.step,
    group: action.group.key,
    name: `${ToolTitles[action.step.tool]} ${stepKey}`,
    input: [],
    dataSources: [],
    key: stepKey,
    outputKey: null,
    ...createDefaultConfiguration(state.steps.filter(s => s.tool === action.step.tool), action.step.tool, action.appConfiguration),
  };
  if ((step.tool !== EnumTool.CATALOG) && (step.tool !== EnumTool.ReverseTripleGeo)) {
    step.outputKey = resourceKey;
    step.outputFormat = EnumDataFormat.N_TRIPLES;
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
    case EnumTool.ReverseTripleGeo:
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

function cloneStep(state, action) {
  const { step: initialStep, step: { tool } } = action;

  if (!initialStep) {
    return state;
  }

  if (!ToolConfigurationSettings[tool].allowClone) {
    return state;
  }

  // Update counters
  const stepKey = ++state.counters.step;
  const resourceKey = ++state.counters.resource;

  // Create step
  const step = {
    ..._.cloneDeep(initialStep),
    name: `${initialStep.name} (Clone)`,
    input: [],
    dataSources: [],
    key: stepKey,
    outputKey: null,
  };
  if (step.tool !== EnumTool.CATALOG) {
    step.outputKey = resourceKey;
    step.outputFormat = EnumDataFormat.N_TRIPLES;
  }

  // Create output resource
  const outputResource = {
    key: resourceKey,
    inputType: EnumInputType.OUTPUT,
    resourceType: (tool === EnumTool.LIMES ? EnumResourceType.LINKED : EnumResourceType.POI),
    name: `${step.name} : Output`,
    iconClass: (tool === EnumTool.LIMES ? ResourceTypeIcons[EnumResourceType.LINKED] : ResourceTypeIcons[EnumResourceType.POI]),
    tool,
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
    if (group.key === step.group) {
      return {
        ...group,
        steps: [...group.steps, step.key],
      };
    }
    return group;
  });

  // 1. Move TripleGeo output after catalog resources
  // 2. Move output from other tools to the end of the array
  const resources = [...state.resources];
  const outputResourceIndex = resources.reduce((result, value, index) => {
    if ((value.inputType === EnumInputType.CATALOG) || (value.tool === EnumTool.TripleGeo)) {
      return index;
    }
    return result;
  }, -1);

  switch (tool) {
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

/**
 * Handles {@link ADD_STEP} action. This action updates several parts of the
 * state
 *
 * @export
 * @param {any} state
 * @param {any} action
 * @returns the new state
 */
export function addStepReducer(state, action) {
  switch (action.type) {
    case Types.ADD_STEP:
      return addStep(state, action);

    case Types.CLONE_STEP:
      return cloneStep(state, action);

    default:
      return state;
  }
}

/**
 * Handles {@link SET_STEP_PROPERTY} action. This action updates several parts of the
 * state
 *
 * @param {any} state
 * @param {any} action
 */
export function stepPropertyReducer(state, action) {
  if (action.type === Types.SET_STEP_PROPERTY) {
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

/**
 * Reorders steps
 *
 * @param {any} state
 * @param {any} action
 * @returns the new state
 */
export function moveStepReducer(state, action) {
  if (action.type === Types.MOVE_STEP) {
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
 * Reorders step inputs
 *
 * @param {any} state
 * @param {any} action
 * @returns the new state
 */
export function moveStepInputReducer(state, action) {
  if (action.type === Types.MOVE_STEP_INPUT) {
    // Find step input
    const steps = [...state.steps];
    const step = steps.find((s) => s.key === action.step.key);
    let input = step.input;
    // Reorder inputs
    const entry = input.splice(action.dragOrder, 1);
    input.splice(action.hoverOrder, 0, entry[0]);

    return {
      ...state,
      steps,
    };
  }
  return state;
}

export function stepOutputPartReducer(state, action) {
  switch (action.type) {
    case Types.SET_STEP_INPUT_OUTPUT_PART:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            input: step.input.map((input) => {
              if (input.inputKey === action.resource.key) {
                return {
                  ...input,
                  partKey: (action.partKey === DEFAULT_OUTPUT_PART ? null : action.partKey),
                };
              }
              return input;
            }),
          };
        }
        return step;
      });

    default:
      return state;
  }
}

export function stepReducer(state, action) {
  switch (action.type) {
    case Types.REMOVE_STEP:
      return state
        .filter((step) => {
          return (step.key !== action.step.key);
        })
        .map((step) => {
          return {
            ...step,
            input: step.input.filter((i) => {
              return (i.inputKey !== action.step.outputKey);
            })
          };
        });

    case Types.ADD_STEP_INPUT:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            input: [...step.input, { inputKey: action.resource.key, partKey: null }],
          };
        }
        return step;
      });

    case Types.REMOVE_STEP_INPUT:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            input: step.input.filter((i) => {
              return (i.inputKey !== action.resource.key);
            })
          };
        }
        return step;
      });

    case Types.ADD_STEP_DATA_SOURCE:
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

    case Types.REMOVE_STEP_DATA_SOURCE:
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

    case Types.REMOVE_RESOURCE_FROM_BAG:
      return state.map((step) => {
        return {
          ...step,
          input: step.input.filter((i) => {
            return (i.inputKey !== action.resource.key);
          })
        };
      });

    default:
      return state;
  }
}
