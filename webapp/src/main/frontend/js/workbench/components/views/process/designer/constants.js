/*
 * Process designer views
 */
export const EnumViews = {
  Designer: 'Designer',
  StepConfiguration: 'StepConfiguration',
  DataSourceConfiguration: 'DataSourceConfiguration',
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
  EXTERNAL_URL: 'EXTERNAL_URL',
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
export const EnumProcessInput = {
  // A registered resource
  CATALOG: 'CATALOG',
  // The output of a single process step/operation
  OUTPUT: 'OUTPUT',
};

/**
 * SLIPO Toolkit components
 */
export const EnumTool = {
  TripleGeo: 'TripleGeo',
  LIMES: 'LIMES',
  FAGI: 'FAGI',
  DEER: 'DEER',
  CATALOG: 'CATALOG',
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
 * Drag sources
 */
export const EnumDragSource = {
  DataSource: 'DataSource',
  Operation: 'Operation',
  Resource: 'Resource',
  Step: 'Step',
};

/**
 * Drop sources
 */
export const EnumDropTarget = {
  Designer: 'Designer',
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
  Resource: 'Resource',
  Step: 'Step',
  Input: 'Input',
  DataSource: 'DataSource',
}
;
