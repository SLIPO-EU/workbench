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

function createEnrichedCell(step, property, features) {
  const initial = features.filter((f) => f.properties[FEATURE_URI] === step.uri && f.source !== step.name).pop();
  const enriched = features.find((f) => f.properties[FEATURE_URI] === step.uri && f.source === step.name);

  return {
    initial: initial ? initial.properties[property] : '-',
    value: enriched ? enriched.properties[property] : '-',
    modified: enriched && initial ? enriched.properties[property] !== initial.properties[property] : false,
  };
}

function createFusedCell(step, property, features) {
  const left = features.find((f) => f.properties[FEATURE_URI] === step.left.uri && f.source === step.left.input);
  const right = features.find((f) => f.properties[FEATURE_URI] === step.right.uri && f.source === step.right.input);
  const action = step.actions.find((a) => a.property === property);

  // Add feature for fused POI if not already exists
  let fusedFeature = features.find((f) => f.properties[FEATURE_URI] === step.selectedUri && f.source === step.name);
  if (!fusedFeature) {
    fusedFeature = {
      type: 'Feature',
      source: step.name,
      geometry: null,
      properties: {
        [FEATURE_URI]: step.selectedUri,
      },
    };
    features.push(fusedFeature);
  }
  // Add properties incrementally
  // TODO: Check default action
  const value = action ? action.value : step.selectedUri === step.left.uri ? left.properties[property] : right.properties[property];
  fusedFeature.properties[property] = value;

  return {
    left: left.properties[property] || null,
    right: right.properties[property] || null,
    operation: action ? action.operation : null,
    value,
    isDefault: !action,
  };
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

  const inputRow = [];
  const dataRows = [];

  // Properties
  const properties = provenance.features.features
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
              feature: provenance.features.features.find((f) => f.properties[FEATURE_URI] === o.leftUri) || null,
            },
            right: {
              uri: o.rightUri,
              input: o.rightInput,
              feature: provenance.features.features.find((f) => f.properties[FEATURE_URI] === o.rightUri) || null,
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
    .filter((l) => !!l);

  // Inputs
  steps
    .map((step, index) => {
      switch (step.tool) {
        case EnumTool.DEER:
          inputRow.push(
            // If only a single enrichment step is present, add input column
            steps.length === 1 ? {
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

  // Data rows
  properties.forEach((property) => {
    // Attribute columns
    const cells = [{
      value: property,
      step: 0,
    }];

    steps.forEach((step, index) => {
      switch (step.tool) {
        case EnumTool.DEER: {
          const cell = createEnrichedCell(step, property, features);
          cells.push(
            // If only a single enrichment step is present, add input column
            steps.length === 1 ? {
              value: cell.initial,
              step: index + 1,
            } : null,
            {
              value: cell.value,
              step: index + 1,
              property,
              modified: cell.modified,
            });
          break;
        }
        case EnumTool.FAGI: {
          const cell = createFusedCell(step, property, features);
          cells.push(
            {
              value: cell.left,
              step: index + 1,
              property,
            }, {
              value: cell.right,
              step: index + 1,
              property,
            }, {
              value: cell.operation,
              step: index + 1,
              property,
            }, {
              value: cell.value,
              selected: cell.operation && !!cell.value,
              step: index + 1,
              property,
            },
          );
          break;
        }
        default:
        // Do nothing
      }
    });
    // Filter out empty data cells (may occur due to enrichment operations)
    dataRows.push(cells.filter(c => c));
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
