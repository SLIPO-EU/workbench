import {
  Colors,
  EnumInputType,
  EnumTool,
  EnumStepFileType,
} from '../../../../model/process-designer';

export function resourceToLayers(steps, resources, execution) {
  const layers = [];

  // All input resources (exclude registered step output)
  const allInputResources = steps.reduce((agg, value) =>
    (value.tool === EnumTool.CATALOG ? agg : agg.concat(value.resources)), []);
  // All output resources created either by TripleGeo or the workflow final step
  const stepsWithOutput = steps.reduce((keys, step) =>
    (((step.outputKey) && ((step.tool === EnumTool.TripleGeo) || (allInputResources.indexOf(step.outputKey) === -1))) ? keys.concat([step.key]) : keys), []);
  // All catalog resources used by any step
  const catalogInputResources = resources.filter((r) =>
    ((r.inputType === EnumInputType.CATALOG) && (allInputResources.indexOf(r.key !== -1))));

  // Input resource layers
  catalogInputResources
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
  stepsWithOutput.forEach((key) => {
    const step = steps.find((s) => s.key === key);
    const runtime = execution.steps.find((s) => s.key === key);

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
            inputType: EnumInputType.OUTPUT,
            step: key,
            resource: null,
            file: f.id,
          });
        });
    }
  });

  return layers;
}
