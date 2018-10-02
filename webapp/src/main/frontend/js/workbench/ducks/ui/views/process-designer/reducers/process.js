import * as Types from '../types';

import {
  EnumInputType,
  EnumSelection,
  EnumTaskType,
  EnumTool,
} from '../../../../../model/process-designer';

function resolveVersion(tool, version, appConfiguration) {
  if (version) {
    return version;
  }
  switch (tool) {
    case EnumTool.TripleGeo:
    case EnumTool.ReverseTripleGeo:
      return appConfiguration.tripleGeo.baselineVersion;
    case EnumTool.LIMES:
      return appConfiguration.limes.baselineVersion;
    case EnumTool.FAGI:
      return appConfiguration.fagi.baselineVersion;
    case EnumTool.DEER:
      return appConfiguration.deer.baselineVersion;
    case EnumTool.CATALOG:
      return null;
    default:
      console.error(`Cannot resolve version. SLIPO Toolkit component ${tool} is not supported.`);
      return null;
  }
}

function load(state, action) {
  const data = action.process;

  // Create process
  const process = action.clone ?
    {
      id: null,
      version: null,
      template: false,
      clone: true,
      properties: {
        name: '',
        description: '',
      },
      errors: {},
      taskType: EnumTaskType.DATA_INTEGRATION,
    } : {
      id: data.id,
      version: data.version,
      template: data.template,
      clone: false,
      properties: {
        name: data.name,
        description: data.description,
      },
      errors: {},
      taskType: data.taskType,
    };


  // Create groups
  const groupCounter = data.steps.reduce((result, current) => {
    return Math.max(current.group, result);
  }, 0) + 2;

  const groups = [];
  for (let key = 0; key < groupCounter; key++) {
    groups.push({
      key,
      steps: data.steps.filter((step) => step.group === key).map((step) => step.key),
    });
  }

  // Create steps
  const steps = data.steps.map((step) => {
    return {
      ...step,
      // Add baseline version if not set
      configuration: {
        ...step.configuration,
        version: resolveVersion(step.tool, step.configuration.version, action.appConfiguration),
      }
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
        existing.key = parseInt(r.key);
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
      groups,
      steps,
      resources,
    }],
    redo: [],
  };
}

export function processReducer(state, action) {
  switch (action.type) {
    case Types.LOAD_RECEIVE_RESPONSE:
      return load(state, action);
    default:
      return state;
  }
}
