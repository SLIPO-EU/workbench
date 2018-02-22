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
    definition: {
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
          inputKeys: [...s.resources],
          sources: buildDataSource(s),
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
  const config = step.configuration || null;

  switch (step.tool) {
    case EnumTool.TripleGeo:
      return config;

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
    case EnumDataSource.EXTERNAL_URL:
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
  const { id, version, definition } = result;

  return {
    ...definition,
    id,
    version,
    resources: definition.resources
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
        resources: [...s.inputKeys],
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
  switch (step.tool) {
    case EnumTool.TripleGeo:
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

    case EnumDataSource.EXTERNAL_URL:
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
  if ((!designer.process.properties.name) || (!designer.process.properties.description)) {
    return false;
  }
  if (designer.steps.length === 0) {
    return false;
  } else {
    if (designer.steps.find((s) => s.configuration === null)) {
      return false;
    }
  }
  if (designer.resources.length === 0) {
    return false;
  }
  return true;
}

export function save(action, designer, token) {
  const id = (action === EnumProcessSaveAction.SaveAsTemplate ? null : designer.process.id);
  const data = buildProcessRequest(action, designer);

  if (id) {
    return actions.post(`/action/process/${id}`, token, data);
  } else {
    return actions.post('/action/process', token, data);
  }
}
