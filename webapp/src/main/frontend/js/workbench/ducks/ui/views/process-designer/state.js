export const initializeProcess = () => ({
  id: null,
  version: null,
  properties: {
    name: '',
    description: '',
  },
  errors: {},
});

export const initializeGroups = () => ([{
  key: 0,
  steps: [],
}, {
  key: 1,
  steps: [],
}]);

export const initializeExecution = () => ({
  data: null,
  lastUpdate: null,
  layers: [],
  baseLayer: 'BingMaps-Road',
  selectedFile: null,
  selectedKpi: null,
  selectedLayer: null,
  selectedFeatures: [],
});
