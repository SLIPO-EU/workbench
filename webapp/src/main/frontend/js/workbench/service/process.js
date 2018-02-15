import actions from './api/fetch-actions';

import {
  EnumProcessSaveAction,
} from '../model';

import {
  EnumInputType,
  EnumTool,
  EnumDataSource,
  EnumToolboxItem
} from '../components/views/process/designer/constants';

import {
  DataSourceIcons,
  DataSourceTitles,
  ResourceTypeIcons,
  ToolIcons,
} from '../components/views/process/designer/config';

function buildProcessRequest(action, designer) {
  const allInputResources = designer.steps.reduce((agg, value) => {
    return agg.concat(value.resources);
  }, []);

  const model = {
    action,
    process: {
      name: designer.process.properties.name,
      description: designer.process.properties.description,
      resources: designer.resources
        .map((r) => {
          switch (r.inputType) {
            case EnumInputType.CATALOG:
              return {
                key: r.key,
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
                key: r.key,
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
          // Require resources only used in a step definition
          return ((r !== null) && (allInputResources.includes(r.key)));
        }),
      steps: designer.steps.map((s) => {
        return {
          key: s.key,
          group: s.group,
          name: s.name,
          tool: s.tool,
          operation: s.operation,
          input: [...s.resources],
          configuration: buildConfiguration(s),
          outputKey: s.outputKey,
        };
      }).filter((s) => {
        return (s.configuration !== null);
      }),
    }
  };

  return model;
}

function buildConfiguration(step) {
  const config = step.configuration;

  switch (step.tool) {
    case EnumTool.TripleGeo:
      return {
        settings: config,
        dataSource: buildDataSource(step),
      };

    case EnumTool.CATALOG:
      return {
        metadata: config,
      };

    case EnumTool.LIMES:
    case EnumTool.FAGI:
    case EnumTool.DEER:
      return {
        resources: [...step.resources],
      };

    default:
      return null;
  }
}

function buildDataSource(step) {
  if (step.dataSources.length !== 1) {
    return null;
  }

  const dataSource = step.dataSources[0];
  switch (dataSource.source) {
    case EnumDataSource.FILESYSTEM:
      if (dataSource.configuration && dataSource.configuration.resource) {
        return {
          type: dataSource.source,
          path: dataSource.configuration.resource.path,
        };
      }
      break;
    case EnumDataSource.EXTERNAL_URL:
      if (dataSource.configuration && dataSource.configuration.url) {
        return {
          type: dataSource.source,
          url: dataSource.configuration.url,
        };
      }
  }

  return null;
}

function readProcessResponse(process) {
  return {
    ...process,
    resources: process.resources
      .map((r) => {
        switch (r.inputType) {
          case EnumInputType.CATALOG:
            return {
              key: r.key,
              inputType: r.inputType,
              resourceType: r.resourceType,
              name: r.name,
              iconClass: ResourceTypeIcons[r.resourceType],
              id: r.resource.id,
              version: r.resource.version,
              description: r.description,
            };
          case EnumInputType.OUTPUT:
            return {
              key: r.key,
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
    steps: process.steps.map((s, index) => {
      return {
        key: s.key,
        group: s.group,
        type: EnumToolboxItem.Operation,
        tool: s.tool,
        operation: s.operation,
        order: index,
        name: s.name,
        iconClass: ToolIcons[s.tool],
        resources: s.configuration.resources,
        dataSources: readDataSource(s),
        configuration: readConfiguration(s),
        errors: {},
        outputKey: s.outputKey,
      };
    }),
  };
}

function readConfiguration(step) {
  const config = step.configuration;
  if (!config) {
    return null;
  }
  switch (config.tool) {
    case EnumTool.TripleGeo:
      return config.settings;
    case EnumTool.CATALOG:
      return config.metadata;
    default:
      return {};
  }
}

function readDataSource(step) {
  if (step.tool != EnumTool.TripleGeo) {
    return [];
  }

  const config = step.configuration;
  if ((!config) || (!config.dataSource)) {
    return [];
  }

  const dataSource = config.dataSource;
  switch (dataSource.type) {
    case EnumDataSource.FILESYSTEM:
      return [{
        key: 0,
        type: EnumToolboxItem.DataSource,
        source: dataSource.type,
        iconClass: DataSourceIcons[dataSource.type],
        name: DataSourceTitles[dataSource.type],
        configuration: {
          resource: {
            path: dataSource.path,
          }
        },
        errors: {},
      }];
    case EnumDataSource.EXTERNAL_URL:
      return [{
        key: 0,
        type: EnumToolboxItem.DataSource,
        source: dataSource.type,
        iconClass: DataSourceIcons[dataSource.type],
        name: DataSourceTitles[dataSource.type],
        configuration: {
          url: dataSource.url,
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
      return readProcessResponse(result.process);
    });
}

export function fetchProcessRevision(id, version, token) {
  return actions
    .get(`/action/process/${id}/${version}`, token)
    .then((result) => {
      return readProcessResponse(result.process);
    });
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
  return actions.get(`/action/process/${process}/${version}/execution/${execution}`, token);
}

export function fetchExecutionKpiData(process, version, execution, file, token) {
  //return actions.get(`/action/process/${process}/${version}/execution/${execution}/kpi/${file}`, token);
  return Promise.resolve({
    values: [{
      key: 'Key 1',
      value: 100,
    }, {
      key: 'Key 2',
      value: 200,
      description: 'Value 2 description',
    }, {
      key: 'Key 3',
      value: 15,
    }, {
      key: 'Key 4',
      value: 50,
    }],
  });
}

export function validate(action, designer) {
  const request = buildProcessRequest(action, designer);

  // Validate process properties
  if ((!request.process.name) || (!request.process.description)) {
    return false;
  }
  if (request.process.steps.length === 0) {
    return false;
  }
  if (request.process.resources.length === 0) {
    return false;
  }
  return true;
}

export function save(action, designer, token) {
  const id = (action === EnumProcessSaveAction.SaveAsTemplate ? null : designer.process.properties.id);
  const data = buildProcessRequest(action, designer);

  if (id) {
    return actions.post(`/action/process/${id}`, token, data);
  } else {
    return actions.post('/action/process', token, data);
  }
}
