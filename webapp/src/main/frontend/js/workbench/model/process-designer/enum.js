/**
 * Process designer mode
 */
export const EnumDesignerMode = {
  CREATE: 'new',
  EDIT: 'edit',
  VIEW: 'view',
  EXECUTION: 'execution',
};

/*
 * Process designer views
 */
export const EnumDesignerView = {
  Designer: 'Designer',
  StepConfiguration: 'StepConfiguration',
  DataSourceConfiguration: 'DataSourceConfiguration',
  StepExecution: 'StepExecution',
  DockerLogViewer: 'DockerLogViewer',
};

/**
 * Toolbox item types
 */
export const EnumToolboxItem = {
  // A catalog or computed resource
  Resource: 'Resource',
  // A data source implementation
  DataSource: 'DataSource',
  // A harvester implementation used for loading a resource
  Harvester: 'Harvester',
  // A SLIPO Toolkit component operation
  Operation: 'Operation',
};

/**
 * Toolbox item groups
 */
export const EnumToolboxItemGroup = {
  All: 'All',
  Tools: 'Tools',
  DataSource: 'DataSource',
  Harvester: 'Harvester',
  Misc: 'Misc',
};

/*
 * Resource types
 */
export const EnumResourceType = {
  POI: 'POI_DATA',
  LINKED: 'POI_LINKED_DATA',
};

/**
 * Supported data sources
 */
export const EnumDataSource = {
  FILESYSTEM: 'FILESYSTEM',
  URL: 'URL',
  HARVESTER: 'HARVESTER',
};

/**
 * Supported harvesters
 */
export const EnumHarvester = {
  OSM: 'OSM',
};

/*
 * Process input resources
 */
export const EnumInputType = {
  // A registered resource
  CATALOG: 'CATALOG',
  // The output of a single process step/operation
  OUTPUT: 'OUTPUT',
};

/**
 * SLIPO Toolkit components
 */
export const EnumTool = {
  TripleGeo: 'TRIPLEGEO',
  LIMES: 'LIMES',
  FAGI: 'FAGI',
  DEER: 'DEER',
  CATALOG: 'REGISTER',
  ReverseTripleGeo: 'REVERSE_TRIPLEGEO',
};

/**
 * Designer operations
 */
export const EnumOperation = {
  Transform: 'TRANSFORM',
  Interlink: 'INTERLINK',
  Fusion: 'FUSION',
  Enrichment: 'ENRICHMENT',
  Registration: 'REGISTER',
};

/**
 * Execution step file types
 */
export const EnumStepFileType = {
  CONFIGURATION: 'CONFIGURATION',
  INPUT: 'INPUT',
  OUTPUT: 'OUTPUT',
  SAMPLE: 'SAMPLE',
  KPI: 'KPI',
  LOG: 'LOG',
  QA: 'QA',
};

export const EnumStepFileTypeValue = {
  [EnumStepFileType.CONFIGURATION]: 0,
  [EnumStepFileType.INPUT]: 1,
  [EnumStepFileType.OUTPUT]: 2,
  [EnumStepFileType.SAMPLE]: 3,
  [EnumStepFileType.KPI]: 4,
  [EnumStepFileType.QA]: 5,
};

/**
 * Drag sources
 */
export const EnumDragSource = {
  DataSource: 'DataSource',
  Operation: 'Operation',
  Resource: 'Resource',
  Step: 'Step',
  StepInput: 'StepInput',
};

/**
 * Step properties
 */
export const EnumStepProperty = {
  Title: 'Title',
};

/**
 * Selection type
 */
export const EnumSelection = {
  Process: 'Workflow',
  Resource: 'Resource',
  Step: 'Step',
  Input: 'Input',
  DataSource: 'DataSource',
};

/*
 * Supported data formats
 */
export const EnumDataFormat = {
  CSV: 'CSV',
  GPX: 'GPX',
  GEOJSON: 'GEOJSON',
  JSON: 'JSON',
  OSM_PBF: 'OSM_PBF',
  OSM_XML: 'OSM_XML',
  SHAPEFILE: 'SHAPEFILE',
  RDF_XML: 'RDF_XML',
  RDF_XML_ABBREV: 'RDF_XML_ABBREV',
  TURTLE: 'TURTLE',
  XML: 'XML',
  N_TRIPLES: 'N_TRIPLES',
  N3: 'N3',
};

/**
 * Process Save actions
 */
export const EnumDesignerSaveAction = {
  None: null,
  Save: 'SAVE',
  SaveAndExecute: 'SAVE_AND_EXECUTE',
  SaveAndExecuteAndMap: 'SAVE_AND_EXECUTE_AND_MAP',
  SaveAsTemplate: 'SAVE_TEMPLATE',
};

/**
 * KPI view mode
 */
export const EnumKpiViewMode = {
  GRID: 'Grid',
  CHART: 'Chart',
};

/**
 * Output partial keys for SLIPO Toolkit components
 */
export const DEFAULT_OUTPUT_PART = 'Default';

export const EnumTriplegeoOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  transformed: 'Transformed',
  classification: 'Classification',
};

export const EnumReverseTriplegeoOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  transformed: 'Transformed',
};

export const EnumLimesOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  accepted: 'Accepted',
  review: 'Review',
};

export const EnumFagiOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  fused: 'Fused',
  remaining: 'Remaining',
  review: 'Review',
};

export const EnumDeerOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  enriched: 'Enriched',
};

// Supported data integration tasks
export const EnumTaskType = {
  REGISTRATION: 'REGISTRATION',
  DATA_INTEGRATION: 'DATA_INTEGRATION',
  EXPORT: 'EXPORT',
  EXPORT_MAP: 'EXPORT_MAP',
};

// Map export status
export const EnumMapExportStatus = {
  NONE: 'NONE',
  PENDING: 'PENDING',
  RUNNING: 'RUNNING',
  FAILED: 'FAILED',
  COMPLETED: 'COMPLETED',
};
