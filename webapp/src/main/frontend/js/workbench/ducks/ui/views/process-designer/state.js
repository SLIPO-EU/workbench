export const initializeProcess = () => ({
  id: null,
  version: null,
  template: false,
  clone: false,
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
  selectedKpi: null,
  selectedLog: null,
  selectedRow: null,
});
