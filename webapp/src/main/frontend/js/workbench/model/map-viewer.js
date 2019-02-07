export const EnumLayerType = {
  Input: 'Input',
  Output: 'Output',
};

export const EnumPane = {
  FeatureCollection: 'FeatureCollection',
  FeatureEvolution: 'Evolution',
  FeatureProvenance: 'Provenance',
};

export const EnumSymbol = {
  Circle: 'circle',
  Cross: 'cross',
  Polygon: 'polygon',
  Square: 'square',
  Triangle: 'triangle',
};

export const Symbols = [{
  value: EnumSymbol.Circle,
  label: 'Circle',
}, {
  value: EnumSymbol.Cross,
  label: 'Cross',
}, {
  value: EnumSymbol.Polygon,
  label: 'Polygon',
}, {
  value: EnumSymbol.Square,
  label: 'Square',
}, {
  value: EnumSymbol.Triangle,
  label: 'Triangle',
}];

export const EnumCellValueType = {
  Text: 'Text',
  Email: 'Email',
  Image: 'Image',
  Link: 'Link',
};

export const FilterTypes = [{
  label: 'Empty',
  value: 'null'
}, {
  label: 'Not Empty',
  value: 'notNull'
}, {
  label: 'Starts With',
  value: 'startsWith'
}, {
  label: 'Ends With',
  value: 'endsWith'
}, {
  label: 'Contains',
  value: 'contains'
}, {
  label: '=',
  value: 'equal'
}, {
  label: '<',
  value: 'less'
}, {
  label: '<=',
  value: 'lessOrEqual'
}, {
  label: '>',
  value: 'greater'
}, {
  label: '>=',
  value: 'greaterOrEqual'
}];

export const ATTRIBUTE_PROPERTIES = '__properties_update';
export const ATTRIBUTE_GEOMETRY = '__geometry_update';

// TODO : Replace with attributes from SLIPO ontology
export const Attributes = [{
  title: 'Display User Updates',
  key: ATTRIBUTE_PROPERTIES,
  editable: false,
}, {
  title: 'Country',
  key: 'country',
  editable: true,
}, {
  title: 'Description',
  key: 'description',
  editable: true,
}, {
  title: 'Email',
  key: 'email',
  editable: true,
}, {
  title: 'Fax',
  key: 'fax',
  editable: true,
}, {
  title: 'Home Page',
  key: 'homepage',
  editable: true,
}, {
  title: 'Id',
  key: 'id',
  editable: false,
}, {
  title: 'Image',
  key: 'image',
  editable: true,
}, {
  title: 'Locality',
  key: 'locality',
  editable: true,
}, {
  title: 'Name',
  key: 'name',
  editable: true,
}, {
  title: 'Open Hours',
  key: 'open_hours',
  editable: true,
}, {
  title: 'Phone',
  key: 'phone',
  editable: true,
}, {
  title: 'Postcode',
  key: 'postcode',
  editable: true,
}, {
  title: 'Street Number',
  key: 'street_num',
  editable: true,
}, {
  title: 'Type',
  key: 'type',
  editable: true,
}, {
  title: 'Wikipedia',
  key: 'wikipedia',
  editable: true,
}, {
  title: 'URI',
  key: 'uri',
  editable: false,
}, {
  title: 'Timestamp',
  key: 'timestamp',
  editable: true,
}, {
  title: 'Geometry',
  key: ATTRIBUTE_GEOMETRY,
  editable: false,
}];
