import {
  Colors,
} from '../../../../model/constants';

import {
  EnumInputType,
  EnumResourceType,
  EnumStepFileType,
  EnumTool,
} from '../../../../model/process-designer';

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
    .filter((r) => r.tool === EnumTool.TripleGeo && !!input.find((i) => i.inputKey === r.key));

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
        },
        file: null,
      });
    });

  // Output resource layers
  [...transformResources, ...output].forEach((r) => {
    const step = steps.find((s) => s.key === r.stepKey);
    const runtime = execution.steps.find((s) => s.key === step.key);

    if (runtime) {
      runtime.files
        .filter((f) => f.type === EnumStepFileType.OUTPUT && !!f.tableName)
        .forEach((f) => {
          layers.push({
            title: step.name,
            hidden: false,
            icon: '\uf08d',
            iconClass: 'fa fa-map-marker',
            color: Colors[layers.length % Colors.length],
            tableName: f.tableName,
            boundingBox: f.boundingBox,
            inputType: r.inputType,
            step: step.key,
            resource: null,
            file: f.id,
          });
        });
    }
  });

  return layers;
}
