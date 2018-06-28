import _ from 'lodash';
import actions from './api/fetch-actions';

import {
  flatten,
} from 'flat';

import {
  EnumDesignerSaveAction,
} from '../model/process-designer';

import {
  EnumDataFormat,
  EnumDataSource,
  EnumInputType,
  EnumResourceType,
  EnumTool,
  EnumToolboxItem,
  DataSourceIcons,
  DataSourceTitles,
  ResourceTypeIcons,
  ToolIcons,
  ToolConfigurationSettings,
} from '../model/process-designer';

import {
  readConfigurationTripleGeo,
  writeConfigurationTripleGeo,
} from './triplegeo';

function buildProcessRequest(action, designer) {
  const allInputOutputResources = designer.steps
    .reduce((result, step) => {
      const stepInputKeys = step.input.map(i => i.inputKey);
      return (step.outputKey !== null ? result.concat(stepInputKeys, [step.outputKey]) : result.concat(stepInputKeys));
    }, [])
    .map((r) => {
      // Server expects all step input/output keys to be strings
      return r !== null ? r.toString() : null;
    });

  const model = {
    action,
    definition: {
      name: designer.process.properties.name,
      description: designer.process.properties.description,
      resources: designer.resources
        .map((r) => {
          switch (r.inputType) {
            case EnumInputType.CATALOG:
              return {
                key: r.key !== null ? r.key.toString() : null,
                inputType: EnumInputType.CATALOG,
                resourceType: r.resourceType,
                name: r.name,
                description: r.description,
                resource: {
                  id: r.id,
                  version: r.version,
                },
              };
            case EnumInputType.OUTPUT:
              return {
                key: r.key !== null ? r.key.toString() : null,
                inputType: EnumInputType.OUTPUT,
                resourceType: r.resourceType,
                name: r.name,
                tool: r.tool,
                stepKey: r.stepKey,
              };
            default:
              return null;
          }
        }).filter((r) => {
          // Require input resources used in a step definition and output resources
          return ((r !== null) && (allInputOutputResources.includes(r.key)));
        }),
      steps: designer.steps.map((s) => {
        return {
          key: s.key,
          group: s.group,
          name: s.name,
          tool: s.tool,
          operation: s.operation,
          input: [...s.input],
          sources: buildDataSource(s),
          configuration: buildConfiguration(s),
          outputKey: s.outputKey !== null ? s.outputKey.toString() : null,
          outputFormat: (s.tool === EnumTool.CATALOG ? null : EnumDataFormat.N_TRIPLES),
        };
      }).filter((s) => {
        return (s.configuration !== null);
      }),
    }
  };

  return model;
}

function buildConfiguration(step) {
  const config = step.configuration || null;

  switch (step.tool) {
    case EnumTool.TripleGeo:
      return writeConfigurationTripleGeo(config);

    case EnumTool.CATALOG:
      return {
        metadata: config,
      };

    case EnumTool.LIMES:
    case EnumTool.FAGI:
    case EnumTool.DEER:
      return config;

    default:
      return null;
  }
}

function buildDataSource(step) {
  if (step.dataSources.length !== 1) {
    return [];
  }

  const dataSource = step.dataSources[0];
  switch (dataSource.source) {
    case EnumDataSource.FILESYSTEM:
      if (dataSource.configuration && dataSource.configuration.resource) {
        return [{
          type: dataSource.source,
          path: dataSource.configuration.resource.path,
        }];
      }
      break;
    case EnumDataSource.URL:
      if (dataSource.configuration && dataSource.configuration.url) {
        return [{
          type: dataSource.source,
          url: dataSource.configuration.url,
        }];
      }
  }

  return null;
}

function readProcessResponse(result) {
  const { id, version, template, definition } = result;
  return {
    ...definition,
    id,
    version,
    template,
    resources: definition.resources
      .map((r) => {
        switch (r.inputType) {
          case EnumInputType.CATALOG:
            return {
              key: parseInt(r.key),
              inputType: r.inputType,
              resourceType: r.resourceType,
              name: r.name,
              iconClass: ResourceTypeIcons[r.resourceType],
              id: r.resource.id,
              version: r.resource.version,
              description: r.description,
              boundingBox: r.boundingBox,
              tableName: r.tableName,
            };
          case EnumInputType.OUTPUT:
            return {
              key: parseInt(r.key),
              inputType: r.inputType,
              resourceType: r.resourceType,
              name: r.name,
              iconClass: ResourceTypeIcons[r.resourceType],
              tool: r.tool,
              stepKey: r.stepKey,
            };
          default:
            return null;
        }
      }).filter((r) => {
        return (r != null);
      }),
    steps: definition.steps.map((s, index) => {
      return {
        key: s.key,
        group: s.group,
        type: EnumToolboxItem.Operation,
        tool: s.tool,
        operation: s.operation,
        order: index,
        name: s.name,
        iconClass: ToolIcons[s.tool],
        input: s.input.map((i) => ({ inputKey: parseInt(i.inputKey), partKey: i.partKey })),
        dataSources: readDataSource(s),
        configuration: readConfiguration(s),
        errors: {},
        outputKey: s.outputKey !== null ? parseInt(s.outputKey) : null,
        outputFormat: EnumDataFormat.N_TRIPLES,
      };
    }),
  };
}

function readConfiguration(step) {
  const config = step.configuration;

  if (!config) {
    return null;
  }
  switch (step.tool) {
    case EnumTool.TripleGeo:
      return readConfigurationTripleGeo(config);

    case EnumTool.LIMES:
      return config;

    case EnumTool.CATALOG:
      return config.metadata;

    default:
      return {};
  }
}

function readDataSource(step) {
  if (step.tool !== EnumTool.TripleGeo) {
    return [];
  }

  const sources = step.sources;
  if ((!sources) || (sources.length !== 1)) {
    return [];
  }

  const source = sources[0];
  switch (source.type) {
    case EnumDataSource.FILESYSTEM:
      return [{
        key: 0,
        type: EnumToolboxItem.DataSource,
        source: source.type,
        iconClass: DataSourceIcons[source.type],
        name: DataSourceTitles[source.type],
        configuration: {
          resource: {
            path: source.path,
          }
        },
        errors: {},
      }];

    case EnumDataSource.URL:
      return [{
        key: 0,
        type: EnumToolboxItem.DataSource,
        source: source.type,
        iconClass: DataSourceIcons[source.type],
        name: DataSourceTitles[source.type],
        configuration: {
          url: source.url,
        },
        errors: {},
      }];

    default:
      return [];
  }
}

export function fetchProcess(id, token) {
  return actions
    .get(`/action/process/${id}`, token)
    .then((result) => {
      return readProcessResponse(result);
    });
}

export function fetchProcessRevision(id, version, token) {
  return actions
    .get(`/action/process/${id}/${version}`, token)
    .then((result) => {
      return readProcessResponse(result);
    });
}

export function fetchTemplates(query, token) {
  return actions.post('/action/process/template/query', token, query);
}

export function fetchProcesses(query, token) {
  return actions.post('/action/process/query', token, query);
}

export function fetchExecutions(query, token) {
  return actions.post('/action/process/execution/query', token, query);
}

export function fetchProcessExecutions(process, version, token) {
  return actions.get(`/action/process/${process}/${version}/execution`, token);
}

export function fetchExecutionDetails(process, version, execution, token) {
  return actions
    .get(`/action/process/${process}/${version}/execution/${execution}`, token)
    .then((result) => {
      return {
        process: readProcessResponse(result.process),
        execution: result.execution,
      };
    });
}

export function fetchExecutionKpiData(process, version, execution, file, token) {
  return actions.get(`/action/process/${process}/${version}/execution/${execution}/kpi/${file}`, token)
    .then(data => {
      // Flatten data
      data = flatten(data);
      // Remove empty objects
      return Object.keys(data).reduce((result, key) => {
        if (!_.isObject(data[key])) {
          result.values.push({
            key,
            value: data[key],
            description: null,
          });
        }
        return result;
      }, { values: [] });
    });
}

export function getStepDataSourceRequirements(step) {
  let { source } = ToolConfigurationSettings[step.tool];

  return {
    source: source - step.dataSources.length,
  };
}

export function getStepInputRequirements(step, resources) {
  let { poi, linked, any } = ToolConfigurationSettings[step.tool];

  let counters = resources.reduce((counters, resource) => {
    switch (resource.resourceType) {
      case EnumResourceType.POI:
        counters.poi++;
        break;
      case EnumResourceType.LINKED:
        counters.linked++;
        break;
    }

    return counters;
  }, { poi: 0, linked: 0 });

  return {
    poi: poi - counters.poi,
    linked: linked - counters.linked,
    any: any - counters.poi - counters.linked,
  };
}

function validateProcess(action, model, isTemplate, errors) {
  const { process, steps, resources, ...rest } = model;

  if ((!process.properties.name) || (!process.properties.description)) {
    errors.push({ code: 1, text: 'One or more workflow property values are missing' });
  }
}

function validateSteps(action, model, isTemplate, errors, requireSingleOutput) {
  const { process, steps, resources, ...rest } = model;

  if (steps.length === 0) {
    errors.push({ code: 1, text: 'At least a single step is required' });
  }

  const countStepWithoutName = steps.reduce((count, step) => {
    return (step.name ? count : ++count);
  }, 0);
  if (countStepWithoutName > 0) {
    errors.push({ code: 1, text: `The name of one or more steps is not set` });
  }

  _(steps)
    .groupBy('name')
    .map(function (steps, name) {
      return { name, count: steps.length };
    })
    .value()
    .forEach((r) => {
      if (r.count > 1) {
        errors.push({ code: 1, text: `Step name ${r.name} is not unique.` });
      }
    });

  if (requireSingleOutput) {
    // All input resources (exclude registered step output)
    const allInputResources = steps.reduce((agg, value) =>
      (value.tool === EnumTool.CATALOG ? agg : agg.concat(value.input)), []);
    // All output resources that are not used as an input
    const stepOutputResources = steps.reduce((agg, value) =>
      (((value.outputKey !== null) && (!allInputResources.find((i) => i.inputKey === value.outputKey))) ? agg.concat([value.outputKey]) : agg), []);
    if (stepOutputResources.length !== 1) {
      errors.push({ code: 1, text: 'A workflow must generate a single output' });
    }
  }

  if (isTemplate) {
    return;
  }

  steps.forEach((s) => {
    if (s.configuration === null) {
      errors.push({ code: 1, text: `Configuration for step ${s.name} is not set` });
    } else if (Object.keys(s.errors).length !== 0) {
      errors.push({ code: 1, text: `Configuration for step ${s.name} is not valid`, items: { ...s.errors } });
    }

    s.dataSources.forEach((ds) => {
      if (ds.configuration === null) {
        errors.push({ code: 1, text: `Configuration for step ${s.name} data source is not set` });
      } else if (Object.keys(ds.errors).length !== 0) {
        errors.push({ code: 1, text: `Configuration for step ${s.name} data source is not valid`, items: { ...ds.errors } });
      }
    });
  });
}

function validateResources(action, model, isTemplate, errors) {
  const { process, steps, resources, ...rest } = model;

  if (isTemplate) {
    return;
  }

  steps.forEach((s) => {
    // Resources
    const stepResources = resources.filter(r => (!!s.input.find(i => i.inputKey === r.key)));
    const requiredResources = getStepInputRequirements(s, stepResources);

    if ((requiredResources.poi > 0) && (requiredResources.any <= 0)) {
      if (requiredResources.poi === 1) {
        errors.push({ code: 1, text: `Step ${s.name} requires a single POI dataset` });
      } else {
        errors.push({ code: 1, text: `Step ${s.name} requires ${requiredResources.poi} POI datasets` });
      }
    }

    if ((requiredResources.linked > 0) && (requiredResources.any <= 0)) {
      if (requiredResources.linked === 1) {
        errors.push({ code: 1, text: `Step ${s.name} requires a single Links dataset` });
      } else {
        errors.push({ code: 1, text: `Step ${s.name} requires ${requiredResources.linked} Links datasets` });
      }
    }
    if (requiredResources.any > 0) {
      if (requiredResources.any === 1) {
        errors.push({ code: 1, text: `Step ${s.name} requires a single POI or Links dataset` });
      } else {
        errors.push({ code: 1, text: `Step ${s.name} requires ${requiredResources.linked} POI or Links datasets` });
      }
    }

    // Data sources
    const requiredDataSources = getStepDataSourceRequirements(s);

    if (requiredDataSources.source > 0) {
      if (requiredDataSources.source === 1) {
        errors.push({ code: 1, text: `Step ${s.name} requires a single data source or harvester` });
      } else {
        errors.push({ code: 1, text: `Step ${s.name} requires ${requiredDataSources.source} data sources or harvesters` });
      }
    }
  });
}

export function validate(action, model, isTemplate) {
  const errors = [];

  // Properties
  validateProcess(action, model, isTemplate, errors);

  // Steps
  validateSteps(action, model, isTemplate, errors, false);

  // Resources
  validateResources(action, model, isTemplate, errors);

  return errors;
}

export function save(action, designer, token) {
  const id = (action === EnumDesignerSaveAction.SaveAsTemplate ? null : designer.process.id);
  const data = buildProcessRequest(action, designer);

  if (id) {
    return actions.post(`/action/process/${id}`, token, data);
  } else {
    return actions.post('/action/process', token, data);
  }
}

export function start(id, version, token) {
  return actions.post(`/action/process/${id}/${version}/start`, token);
}

export function stop(id, version, token) {
  return actions.post(`/action/process/${id}/${version}/stop`, token);
}
