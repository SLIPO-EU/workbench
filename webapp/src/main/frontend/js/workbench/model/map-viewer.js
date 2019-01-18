export const EnumLayerType = {
  Input: 'Input',
  Output: 'Output',
};

export const EnumPane = {
  FeatureCollection: 'FeatureCollection',
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

// TODO : Replace with attributes from SLIPO ontology
export const Attributes = [{
  text: 'Country',
  key: 'country',
  editable: true,
}, {
  text: 'Description',
  key: 'description',
  editable: true,
}, {
  text: 'Email',
  key: 'email',
  editable: true,
}, {
  text: 'Fax',
  key: 'fax',
  editable: true,
}, {
  text: 'Name',
  key: 'name',
  editable: true,
}, {
  text: 'Phone',
  key: 'phone',
  editable: true,
}, {
  text: 'Postcode',
  key: 'postcode',
  editable: true,
}, {
  text: 'Type',
  key: 'type',
  editable: true,
}, {
  text: 'Image',
  key: 'image',
  editable: true,
}];
