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
  OSM: 'OSM',
  SHAPEFILE: 'SHAPEFILE',
  RDF_XML: 'RDF_XML',
  RDF_XML_ABBREV: 'RDF_XML_ABBREV',
  TURTLE: 'TURTLE',
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
export const DEFAULT_OUTPUT_PART = 'DEFAULT';

export const EnumTriplegeoOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  TRANSFORMED: 'transformed',
  CLASSIFICATION: 'classification',
};

export const EnumLimesOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  ACCEPTED: 'accepted',
  REVIEW: 'review',
};

export const EnumFagiOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  FUSED: 'fused',
  REMAINING: 'remaining',
  REVIEW: 'review',
  STATS: 'stats',
};

export const EnumDeerOutputPart = {
  [DEFAULT_OUTPUT_PART]: null,
  ENRICHED: 'enriched',
};

