import _ from 'lodash';

import {
  Colors,
} from '../../../../model/constants';

import {
  EnumLayerType,
  Symbols,
} from '../../../../model/map-viewer';

import {
  EnumInputType,
  EnumResourceType,
  EnumStepFileType,
  EnumTool,
  ToolIcons,
} from '../../../../model/process-designer';

import {
  FEATURE_URI,
} from '../../../../components/helpers/map/model/constants';

function createStyle(index) {
  return {
    symbol: Symbols[index % Symbols.length].value,
    fill: {
      color: Colors[index % Colors.length],
    },
    stroke: {
      color: Colors[index % Colors.length],
      width: 2,
    },
    size: 15,
    opacity: 50,
  };
}

function projectProperty(property, current, updates = [], index = 0) {
  for (let i = updates.length - 1; i >= index; i--) {
    if (updates[i].properties.hasOwnProperty(property)) {
      current = updates[i].properties[property];
    }
  }
  return current;
}

function createEnrichedCells(step, property, features, updates = []) {
  const initial = features.filter((f) => f.properties[FEATURE_URI] === step.uri && f.source !== step.name).pop();
  const current = features.find((f) => f.properties[FEATURE_URI] === step.uri && f.source === step.name);

  const updated = updates && updates.some(u => u.properties.hasOwnProperty(property));
  const firstUpdate = updated ? updates.filter(u => u.properties.hasOwnProperty(property))[0] : null;

  const result = [
    // Initial
    {
      value: initial ? initial.properties[property] : '-',
      output: false,
      updatedBy: null,
      updatedOn: null,
    },
    // Enriched
    {
      value: projectProperty(property, current.properties[property], updates, 0),
      output: current && initial ? firstUpdate ?
        firstUpdate.properties[property] !== initial.properties[property] :
        current.properties[property] !== initial.properties[property] : false,
      updatedBy: null,
      updatedOn: null,
    },
    // Updates
    ...updates.map((item, index) => ({
      value: projectProperty(property, current.properties[property], updates, index + 1),
      output: false,
      updatedBy: item.updatedBy.name,
      updatedOn: item.updatedOn,
    })),
  ];

  result.forEach((cell, index) => {
    if (index > 1) {
      cell.modified = cell.value !== result[index - 1].value;
    }
  });

  return result;
}

function createFusedCells(step, property, features, updates) {
  const left = features.find((f) => f.properties[FEATURE_URI] === step.left.uri && f.source === step.left.input);
  const right = features.find((f) => f.properties[FEATURE_URI] === step.right.uri && f.source === step.right.input);
  const action = step.actions.find((a) => a.property === property);

  // Add feature for fused POI if not already exists.
  // A fused POI may not exist if fusion is not the last step in the workflow.
  let current = features.find((f) => f.properties[FEATURE_URI] === step.selectedUri && f.source === step.name);
  if (!current) {
    // Fused POI is not imported in the database and should be reconstructed by provenance data
    current = {
      type: 'Feature',
      source: step.name,
      geometry: null,
      properties: {
        [FEATURE_URI]: step.selectedUri,
      },
    };
    features.push(current);
  }
  // Check this is the last operation (property should exist)
  const isLast = current.properties.hasOwnProperty(property);

  // TODO: Check default action
  const value = action ? action.value : step.selectedUri === step.left.uri ? left.properties[property] : right.properties[property];
  // Add properties incrementally
  if (!isLast) {
    current.properties[property] = value;
  }

  const result = [
    // Left input
    {
      value: left.properties[property] || null,
    },
    // Right input
    {
      value: right.properties[property] || null,
    },
    // Operation
    {
      value: action ? action.operation : null,
    },
    // Fused value
    {
      value: isLast ? projectProperty(property, current.properties[property], updates, 0) : value,
      output: !!action,
    },
    // Include updates only if this is the last operation
    ...(isLast ? updates : []).map((item, index) => ({
      value: isLast ? projectProperty(property, current.properties[property], updates, index + 1) : value,
      updatedBy: isLast ? item.updatedBy.name : null,
      updatedOn: isLast ? item.updatedOn : null,
    })),
  ];

  result.forEach((cell, index) => {
    if ((isLast) && (index > 3)) {
      cell.modified = cell.value !== result[index - 1].value;
    }
  });

  return result;
}

export function processExecutionToLayers(process, execution) {
  const { steps, resources } = process;
  const layers = [];

  // All input resource keys (exclude input for CATALOG or EXPORT operations)
  const input = steps.reduce(
    (all, step) =>
      (step.tool === EnumTool.CATALOG || step.tool === EnumTool.ReverseTripleGeo ?
        all : all.concat(step.input)),
    []);

  // All catalog input resources
  const catalogResources = resources
    .filter((r) =>
      r.inputType === EnumInputType.CATALOG &&
      r.resourceType === EnumResourceType.POI &&
      !!input.find((i) => i.inputKey === r.key)
    );

  // All TripleGeo input resources
  const transformResources = resources
    .filter((r) => r.tool === EnumTool.TripleGeo);

  // All output resources
  const output = resources
    .filter((r) =>
      r.inputType === EnumInputType.OUTPUT &&
      r.resourceType === EnumResourceType.POI &&
      !input.find((i) => i.inputKey === r.key)
    );

  // Create layers
  catalogResources
    .filter((r) => r.tableName)
    .forEach((r) => {
      layers.push({
        title: r.name,
        hidden: false,
        icon: '\uf08d',
        iconClass: 'fa fa-map-marker',
        color: Colors[layers.length % Colors.length],
        tableName: r.tableName,
        boundingBox: r.boundingBox,
        inputType: r.inputType,
        step: null,
        resource: {
          id: r.id,
          version: r.version,
          name: r.name,
        },
        file: null,
        type: EnumLayerType.Input,
        style: r.style || createStyle(layers.length),
      });
    });

  // Output resource layers
  [...transformResources, ...output].forEach((r) => {
    const step = steps.find((s) => s.key === r.stepKey);
    const runtime = execution.steps.find((s) => s.key === step.key);
    const isOutput = output.indexOf(r) !== -1;

    if (runtime) {
      runtime.files
        .filter((f) => f.type === EnumStepFileType.OUTPUT && !!f.tableName)
        .forEach((f) => {
          if (layers.find((l) => l.tableName == f.tableName)) {
            // Ignore duplicates, e.g. in a registration task, a single
            // TripleGeo step will be the only output
            return;
          }
          layers.push({
            title: step.name,
            hidden: false,
            icon: '\uf08d',
            iconClass: 'fa fa-map-marker',
            color: Colors[layers.length % Colors.length],
            tableName: f.tableName,
            boundingBox: f.boundingBox,
            inputType: r.inputType,
            step: {
              key: step.key,
              name: step.name,
              outputKey: isOutput ? step.outputKey : null,
            },
            resource: null,
            file: f.id,
            type: isOutput ? EnumLayerType.Output : EnumLayerType.Input,
            style: f.style || createStyle(layers.length),
          });
        });
    }
  });

  return layers;
}

export function provenanceToTable(provenance) {
  const features = provenance.features.features;
  const updates = provenance.updates || [];

  const inputRow = [];
  const dataRows = [];

  // Properties
  const properties = features
    .reduce((result, feature) => {
      const keys = Object.keys(feature.properties);
      return _.uniq([...result, ...keys]);
    }, [])
    .sort();

  // Steps
  const steps = provenance.operations
    .map((o, index) => {
      switch (o.tool) {
        case EnumTool.DEER: {
          const step = {
            iconClass: ToolIcons[o.tool],
            index: index + 1,
            input: o.input,
            name: o.stepName,
            tool: o.tool,
            uri: o.uri,
          };
          return step;
        }
        case EnumTool.FAGI: {
          const step = {
            actions: o.actions,
            confidenceScore: o.confidenceScore,
            defaultAction: o.defaultAction,
            iconClass: ToolIcons[o.tool],
            index: index + 1,
            name: `${o.stepName} ${o.confidenceScore ? ` (Confidence Score : ${o.confidenceScore.toFixed(4)})` : ''}`,
            left: {
              uri: o.leftUri,
              input: o.leftInput,
              feature: features.find((f) => f.properties[FEATURE_URI] === o.leftUri) || null,
            },
            right: {
              uri: o.rightUri,
              input: o.rightInput,
              feature: features.find((f) => f.properties[FEATURE_URI] === o.rightUri) || null,
            },
            selectedUri: o.selectedUri,
            tool: o.tool,
          };
          return step;
        }
        default:
          return null;
      }
    })
    // Remove null records due to unsupported SLIPO Toolkit components
    .filter((l) => !!l);

  // Add pseudo-steps from updates
  steps.push(
    ...updates.map((update, index) => ({
      iconClass: 'fa fa-pencil',
      index: provenance.operations.length + index + 1,
      name: '',
      tool: null,
      updatedOn: update.updatedOn,
      updatedBy: update.updatedBy.name,
    }))
  );

  // Inputs
  steps
    // Ignore pseudo-steps
    .filter(s => s.tool !== null)
    .map((step, index) => {
      switch (step.tool) {
        case EnumTool.DEER:
          inputRow.push(
            // If only a single enrichment step is present, add input column (ignore update steps)
            steps.filter(s => s.tool !== null).length === 1 ? {
              value: step.input,
              step: index + 1,
            } : null,
            {
              value: steps.length === 1 ? 'Output' : step.input,
              step: index + 1,
            },
          );
          break;
        case EnumTool.FAGI:
          inputRow.push(
            {
              selected: step.left.uri === step.selectedUri,
              value: `Left Input: ${step.left.input}`,
              step: index + 1,
            }, {
              selected: step.right.uri === step.selectedUri,
              value: `Right Input : ${step.right.input}`,
              step: index + 1,
            }, {
              value: `Action (Default : ${step.defaultAction})`,
              step: index + 1,
            }, {
              value: 'Value',
              step: index + 1,
            },
          );
          break;
        default:
        // Do nothing
      }
    });

  // Add pseudo-input from updates
  inputRow.push(
    ...updates.map((update, index) => ({
      step: steps.filter(s => s.tool !== null).length + index + 1,
      value: '',
      updatedOn: update.updatedOn,
      updatedBy: update.updatedBy.name,
    }))
  );

  // Data rows
  properties.forEach((property) => {
    // Attribute columns
    const values = [{
      value: property,
      step: 0,
    }];

    steps.forEach((step, index) => {
      switch (step.tool) {
        case EnumTool.DEER: {
          const cells = createEnrichedCells(step, property, features, updates);
          values.push(
            // If only a single operation exists, include initial value
            ...(provenance.operations.length === 1 ? cells : cells.slice(1)).map(cell => ({
              ...cell,
              step: index + 1,
              property,
            })),
          );
          break;
        }
        case EnumTool.FAGI: {
          const cells = createFusedCells(step, property, features, updates);
          values.push(
            ...cells.map(cell => ({
              ...cell,
              step: index + 1,
              property,
            })),
          );
          break;
        }
        default:
        // Do nothing
      }
    });

    // Filter out empty data cells (may occur due to enrichment operations)
    dataRows.push(values.filter(c => c));
  });

  return {
    layer: provenance.stepName,
    featureId: provenance.featureId,
    featureUri: provenance.featureUri,
    steps,
    properties,
    features,
    // Filter out empty input cells (may occur due to enrichment operations)
    inputRow: inputRow.filter(i => i),
    dataRows,
  };
}
