import {
  EnumInputType,
  EnumTool,
  EnumStepFileType,
} from '../../../../model/process-designer';

const colors = [
  '#1B5E20',
  '#B71C1C',
  '#607D8B',
  '#FF6F00',
  '#9E9E9E',
  '#1A237E',
  '#212121',
];

export function resourceToLayers(steps, resources, execution) {
  const layers = [];

  // All input resources (exclude registered step output)
  const allInputResources = steps.reduce((agg, value) =>
    (value.tool === EnumTool.CATALOG ? agg : agg.concat(value.resources)), []);
  // All output resources created either by TripleGeo or the workflow final step
  const stepsWithOutput = steps.reduce((agg, value) =>
    (((value.outputKey) && ((value.tool === EnumTool.TripleGeo) || (allInputResources.indexOf(value.outputKey) === -1))) ? agg.concat([value.key]) : agg), []);
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
        icon: '\uf041',
        iconClass: 'fa fa-map-marker',
        color: colors[layers.length % colors.length],
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

    runtime.files
      .filter((f) => f.type === EnumStepFileType.OUTPUT && !!f.tableName)
      .forEach((f) => {
        layers.push({
          title: step.name,
          hidden: false,
          icon: '\uf041',
          iconClass: 'fa fa-map-marker',
          color: colors[layers.length % colors.length],
          tableName: f.tableName,
          boundingBox: f.boundingBox,
          inputType: EnumInputType.OUTPUT,
          step: key,
          resource: null,
          file: f.id,
        });
      });
  });

  return layers;
}
