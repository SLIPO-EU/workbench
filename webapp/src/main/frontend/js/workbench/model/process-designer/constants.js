import {
  EnumDataSource,
  EnumHarvester,
  EnumOperation,
  EnumResourceType,
  EnumTool,
  EnumInputType,
} from './enum';

/**
 * Default resource type icons
 */
export const ResourceTypeIcons = {
  [EnumResourceType.POI]: 'fa fa-database',
  [EnumResourceType.LINKED]: 'fa fa-random',
};

/**
 * Default data source icons
 */
export const DataSourceIcons = {
  [EnumDataSource.FILESYSTEM]: 'fa fa-file-code-o',
  [EnumDataSource.URL]: 'fa fa-link',
};

/*
 * Default data source titles
 */
export const DataSourceTitles = {
  [EnumDataSource.FILESYSTEM]: 'File System',
  [EnumDataSource.URL]: 'External URL',
};

/**
 * Default harvester icons
 */
export const HarvesterIcons = {
  [EnumHarvester.OSM]: 'fa fa-map-o',
};

/*
 * Default harvester titles
 */
export const HarvesterTitles = {
  [EnumHarvester.OSM]: 'OSM',
};

/**
 * Default tool icons
 */
export const ToolIcons = {
  [EnumTool.TripleGeo]: 'fa fa-cogs',
  [EnumTool.LIMES]: 'fa fa-random',
  [EnumTool.FAGI]: 'fa fa-object-ungroup',
  [EnumTool.DEER]: 'fa fa-tags',
  [EnumTool.CATALOG]: 'fa fa-book',
};

/*
 * Default tool titles
 */
export const ToolTitles = {
  [EnumTool.TripleGeo]: 'TripleGeo',
  [EnumTool.LIMES]: 'LIMES',
  [EnumTool.FAGI]: 'FAGI',
  [EnumTool.DEER]: 'DEER',
  [EnumTool.CATALOG]: 'Register Resource',
};

/**
 * Default tool operation
 */
export const ToolDefaultOperation = {
  [EnumTool.TripleGeo]: EnumOperation.Transform,
  [EnumTool.LIMES]: EnumOperation.Interlink,
  [EnumTool.FAGI]: EnumOperation.Fusion,
  [EnumTool.DEER]: EnumOperation.Enrichment,
  [EnumTool.CATALOG]: EnumOperation.Registration,
};

/**
 * Static configuration of tool input requirements
 */
export const ToolInputRequirements = {
  [EnumTool.TripleGeo]: {
    source: 1,
    poi: 0,
    linked: 0,
    any: 0,
  },
  [EnumTool.LIMES]: {
    source: 0,
    poi: 2,
    linked: 0,
    any: 0,
  },
  [EnumTool.FAGI]: {
    source: 0,
    poi: 0,
    linked: 1,
    any: 0,
  },
  [EnumTool.DEER]: {
    source: 0,
    poi: 1,
    linked: 0,
    any: 0,
  },
  [EnumTool.CATALOG]: {
    source: 0,
    poi: 0,
    linked: 0,
    any: 1,
  }
};

/**
 * Default process input type icons
 */
export const ProcessInputIcons = {
  [EnumInputType.CATALOG]: 'fa fa-book',
  [EnumInputType.OUTPUT]: 'fa fa-cog',
};
