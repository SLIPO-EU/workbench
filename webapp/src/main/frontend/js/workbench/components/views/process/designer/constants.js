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
  // A data source implementation
  DataSource: 'DataSource',
  // A harvester implementation used for loading a resource
  Harvester: 'Harvester',
  // A SLIPO Toolkit component operation
  Operation: 'Operation',
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
  // A dynamic resource loaded from an external data source or a harvester
  TRANSIENT: 'TRANSIENT',
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
  Harvester: 'Harvester',
  Operation: 'Operation',
  Resource: 'Resource',
};

/**
 * Drop sources
 */
export const EnumDropTarget = {
  Designer: 'Designer',
};
