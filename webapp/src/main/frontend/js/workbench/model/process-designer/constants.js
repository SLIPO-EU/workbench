import {
  EnumDataSource,
  EnumDeerOutputPart,
  EnumFagiOutputPart,
  EnumHarvester,
  EnumInputType,
  EnumLimesOutputPart,
  EnumOperation,
  EnumResourceType,
  EnumTool,
  EnumTriplegeoOutputPart,
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
  [EnumTool.TripleGeo]: 'Transform',
  [EnumTool.LIMES]: 'Interlink',
  [EnumTool.FAGI]: 'Fuse',
  [EnumTool.DEER]: 'Enrich',
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
export const ToolConfigurationSettings = {
  [EnumTool.TripleGeo]: {
    source: 1,
    poi: 0,
    linked: 0,
    any: 0,
    editable: true,
    output: Object.keys(EnumTriplegeoOutputPart),
  },
  [EnumTool.LIMES]: {
    source: 0,
    poi: 2,
    linked: 0,
    any: 0,
    editable: false,
    output: Object.keys(EnumLimesOutputPart),
  },
  [EnumTool.FAGI]: {
    source: 0,
    poi: 0,
    linked: 1,
    any: 0,
    editable: false,
    output: Object.keys(EnumFagiOutputPart),
  },
  [EnumTool.DEER]: {
    source: 0,
    poi: 1,
    linked: 0,
    any: 0,
    editable: false,
    output: Object.keys(EnumDeerOutputPart),
  },
  [EnumTool.CATALOG]: {
    source: 0,
    poi: 1,
    linked: 0,
    any: 0,
    editable: true,
    output: [],
  }
};

/**
 * Default process input type icons
 */
export const ProcessInputIcons = {
  [EnumInputType.CATALOG]: 'fa fa-book',
  [EnumInputType.OUTPUT]: 'fa fa-cog',
};

/**
 * Default layer colors
 */
export const Colors = [
  '#B80000',
  '#DB3E00',
  '#FCCB00',
  '#008B02',
  '#006B76',
  '#1273DE',
  '#004DCF',
  '#5300EB',
  '#EB9694',
  '#FAD0C3',
  '#FEF3BD',
  '#C1E1C5',
  '#BEDADC',
  '#C4DEF6',
  '#BED3F3',
  '#D4C4FB',
];
