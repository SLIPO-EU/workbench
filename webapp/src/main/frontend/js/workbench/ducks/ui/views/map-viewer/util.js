import _ from 'lodash';
import GeoJSON from 'ol/format/geojson';

import {
  Colors,
} from '../../../../model/constants';

import {
  ATTRIBUTE_GEOMETRY,
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
  FEATURE_GEOMETRY,
  FEATURE_URI,
} from '../../../../components/helpers/map/model/constants';

function undefinedToNull(value) {
  if (typeof value === 'undefined') {
    return null;
  }
  return value;
}

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

function projectProperty(feature, property, current, updates = [], index = 0) {
  if (property === ATTRIBUTE_GEOMETRY) {
    // Set current value to the current geometry
    current = feature[FEATURE_GEOMETRY];
  }

  for (let i = updates.length - 1; i >= index; i--) {
    if (property === ATTRIBUTE_GEOMETRY) {
      current = updates[i].geometry;
    } else if (updates[i].properties.hasOwnProperty(property)) {
      current = updates[i].properties[property];
    }
  }

  return current;
}

function createEnrichedCells(step, property, features, updates = []) {
  const initial = features.filter((f) => f.properties[FEATURE_URI] === step.uri && f.source !== step.name).pop();
  const enriched = features.find((f) => f.properties[FEATURE_URI] === step.uri && f.source === step.name);

  const updated = updates && updates.some(u => u.properties.hasOwnProperty(property));
  const firstUpdate = updated ? updates.filter(u => u.properties.hasOwnProperty(property))[0] : null;

  const result = [
    // Initial
    {
      value: initial ? property === ATTRIBUTE_GEOMETRY ? null : initial.properties[property] : '-',
      output: false,
      updatedBy: null,
      updatedOn: null,
    },
    // Enriched
    {
      value: projectProperty(enriched, property, enriched.properties[property], updates, 0),
      output: enriched && initial ? firstUpdate ?
        firstUpdate.properties[property] !== initial.properties[property] :
        enriched.properties[property] !== initial.properties[property] : false,
      updatedBy: null,
      updatedOn: null,
    },
    // Updates
    ...updates.map((item, index) => ({
      value: projectProperty(enriched, property, enriched.properties[property], updates, index + 1),
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
  let fused = features.find((f) => f.properties[FEATURE_URI] === step.selectedUri && f.source === step.name);
  if (!fused) {
    // Fused POI is not imported in the database and should be reconstructed by provenance data
    fused = {
      type: 'Feature',
      source: step.name,
      geometry: null,
      properties: {
        [FEATURE_URI]: step.selectedUri,
      },
    };
    features.push(fused);
  }
  // Check this is the last operation (property should exist)
  const isLast = fused.properties.hasOwnProperty(property);

  // TODO: Check default action
  const value = action ? action.value : step.selectedUri === step.left.uri ? left.properties[property] : right.properties[property];
  // Add properties incrementally
  if (!isLast) {
    fused.properties[property] = value;
  }

  const result = [
    // Left input
    {
      value: property === ATTRIBUTE_GEOMETRY ? null : left.properties[property] || null,
    },
    // Right input
    {
      value: property === ATTRIBUTE_GEOMETRY ? null : right.properties[property] || null,
    },
    // Operation
    {
      value: property === ATTRIBUTE_GEOMETRY ? null : action ? action.operation : null,
    },
    // Fused value
    {
      value: isLast ? projectProperty(fused, property, fused.properties[property], updates, 0) : undefinedToNull(value),
      output: !!action,
    },
    // Include updates only if this is the last operation
    ...(isLast ? updates : []).map((item, index) => ({
      value: projectProperty(fused, property, fused.properties[property], updates, index + 1),
      updatedBy: item.updatedBy.name,
      updatedOn: item.updatedOn,
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
  const operations = provenance.operations || [];

  if (operations.length === 0) {
    return null;
  }

  const inputRow = [];
  const dataRows = [];

  // Properties
  const properties = features
    .reduce((result, feature) => {
      const keys = Object.keys(feature.properties);
      return _.uniq([...result, ...keys]);
    }, [])
    .sort();
  // Add property for geometry updates
  if (updates.length !== 0) {
    properties.push(ATTRIBUTE_GEOMETRY);
  }

  // Steps
  const steps = operations
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
      index: operations.length + index + 1,
      name: '',
      tool: null,
      updatedOn: update.updatedOn,
      updatedBy: update.updatedBy.name,
    }))
  );

  // Actual step count
  const stepCount = steps.filter(s => s.tool !== null).length;

  // Inputs
  steps
    // Ignore pseudo-steps
    .filter(s => s.tool !== null)
    .map((step, index) => {
      switch (step.tool) {
        case EnumTool.DEER:
          if (stepCount === 1) {
            // If only a single enrichment step is present, add input column (ignore update steps)
            inputRow.push({
              value: step.input,
              step: index + 1,
            });
          }
          inputRow.push({
            value: stepCount === 1 ? 'Output' : step.input,
            step: index + 1,
          });
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
      step: stepCount + index + 1,
      value: '',
      updatedOn: update.updatedOn,
      updatedBy: update.updatedBy.name,
    }))
  );

  // Data rows
  properties.forEach((property) => {
    let columnIndex = 0;

    // Attribute columns
    const values = [{
      value: property,
      index: columnIndex,
    }];

    steps.forEach((step, index) => {
      switch (step.tool) {
        case EnumTool.DEER: {
          const cells = createEnrichedCells(step, property, features, updates);
          values.push(
            // If only a single operation exists, include initial value
            ...(operations.length === 1 ? cells : cells.slice(1)).map(cell => ({
              ...cell,
              index: ++columnIndex,
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
              index: ++columnIndex,
              property,
            })),
          );
          break;
        }
        default:
        // Do nothing
      }
    });

    dataRows.push(values);
  });

  const result = {
    layer: provenance.stepName,
    featureId: provenance.featureId,
    featureUri: provenance.featureUri,
    steps,
    properties,
    features,
    inputRow,
    dataRows,
    updates,
    geometrySnapshotIndex: dataRows[0].length - 1,
  };
  return result;
}

export function compareGeometry(geom1, geom2) {
  const format = new GeoJSON();

  const geom1AsText = format.writeGeometry(geom1, {
    featureProjection: 'EPSG:3857',
    dataProjection: 'EPSG:4326',
  });
  const geom2AsText = format.writeGeometry(geom2, {
    featureProjection: 'EPSG:3857',
    dataProjection: 'EPSG:4326',
  });

  return geom1AsText === geom2AsText;
}

export function geometryFromObject(geometry) {
  const format = new GeoJSON();
  return format.readGeometry(geometry, {
    featureProjection: 'EPSG:3857',
    dataProjection: 'EPSG:4326',
  });
}
