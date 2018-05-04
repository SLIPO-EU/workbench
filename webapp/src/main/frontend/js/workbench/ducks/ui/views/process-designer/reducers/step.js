import * as Types from '../types';

import {
  defaultTripleGeoValues,
  EnumDataFormat,
  EnumInputType,
  EnumResourceType,
  EnumStepProperty,
  EnumTool,
  ResourceTypeIcons,
  ToolTitles,
} from '../../../../../model/process-designer';

import {
  validator as tripleGeoValidator,
} from '../../../../../service/triplegeo';

function createTripleGeoDefaultConfiguration(appConfiguration, effectiveVersion) {
  // TODO : Create enumerations
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

function createDefaultConfiguration(steps, tool, appConfiguration) {
  const effectiveVersion = steps.reduce((version, step) => version ? version : step.configuration ? step.configuration.version : null, null) || null;

  switch (tool) {
    case EnumTool.TripleGeo:
      return createTripleGeoDefaultConfiguration(appConfiguration, effectiveVersion);

    default:
      return {
        configuration: null,
        errors: {},
      };
  }
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
  if (action.type == Types.ADD_STEP) {
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
      key: stepKey,
      ...createDefaultConfiguration(state.steps.filter(s => s.tool === action.step.tool), action.step.tool, action.appConfiguration),
    };
    if (step.tool !== EnumTool.CATALOG) {
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
export function setStepPropertyReducer(state, action) {
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
            resources: step.resources.filter((key) => {
              return (key !== action.step.outputKey);
            })
          };
        });

    case Types.ADD_STEP_INPUT:
      return state.map((step) => {
        if (step.key === action.step.key) {
          return {
            ...step,
            resources: [...step.resources, action.resource.key],
          };
        }
        return step;
      });

    case Types.REMOVE_STEP_INPUT:
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
          resources: step.resources.filter((key) => {
            return (key !== action.resource.key);
          })
        };
      });

    default:
      return state;
  }
}
