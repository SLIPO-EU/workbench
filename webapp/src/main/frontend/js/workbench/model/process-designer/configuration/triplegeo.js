import {
  EnumDataFormat,
} from "../enum";

export const configurationLevels = {
  AUTO: 'AUTO',
  SIMPLE: 'SIMPLE',
  ADVANCED: 'ADVANCED',
};

export const configurationLevelOptions = [{
  value: configurationLevels.AUTO,
  label: 'Auto',
  iconClass: 'fa fa-magic',
}, {
  value: configurationLevels.SIMPLE,
  label: 'Simple',
  iconClass: 'fa fa-user'
}, {
  value: configurationLevels.ADVANCED,
  label: 'Advanced',
  iconClass: 'fa fa-user-plus'
}];

export const inputFormats = [
  { value: 'CSV', label: 'CSV' },
  { value: 'GEOJSON', label: 'GEOJSON' },
  { value: 'GPX', label: 'GPX' },
  { value: 'JSON', label: 'JSON' },
  { value: 'OSM_PBF', label: 'OSM PBF' },
  { value: 'OSM_XML', label: 'OSM XML' },
  { value: 'SHAPEFILE', label: 'SHAPEFILE' },
];

export const outputFormats = [
  { value: 'CSV', label: 'CSV' },
  { value: 'SHAPEFILE', label: 'SHAPEFILE' },
];

export const modes = [
  { value: 'GRAPH', label: 'GRAPH' },
  { value: 'STREAM', label: 'STREAM' },
  { value: 'RML', label: 'RML' },
];

export const encodings = [
  { value: 'ISO-8859-1', label: 'ISO-8859-1' },
  { value: 'ISO-8859-7', label: 'ISO-8859-7' },
  { value: 'WINDOWS-1253', label: 'WINDOWS-1253' },
  { value: 'UTF-8', label: 'UTF-8' },
];

export const ontologies = [
  { value: 'GEOSPARQL', label: 'GeoSPARQL' },
  { value: 'VIRTUOSO', label: 'Virtuoso' },
  { value: 'WGS84', label: 'WGS84' },
];

export const serializations = [
  { value: 'RDF_XML', label: 'RDF/XML' },
  { value: 'RDF_XML_ABBREV', label: 'RDF/XML-ABBREV' },
  { value: 'N_TRIPLES', label: 'N-TRIPLES' },
  { value: 'TURTLE', label: 'TURTLE' },
  { value: 'N3', label: 'N3' },
];

const defaultNamespaces = [{
  prefix: 'slipo',
  namespace: 'http://slipo.eu/def#',
}, {
  prefix: 'geo',
  namespace: 'http://www.opengis.net/ont/geosparql#',
}, {
  prefix: 'xsd',
  namespace: 'http://www.w3.org/2001/XMLSchema#',
}, {
  prefix: 'rdfs',
  namespace: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
}, {
  prefix: 'wgs84_pos',
  namespace: 'http://www.w3.org/2003/01/geo/wgs84_pos#',
}];

export const defaultValues = {
  classifyByName: false,
  autoMappings: null,
  userMappings: null,
  defaultLang: 'en',
  delimiter: '|',
  encoding: encodings[3].value,
  level: configurationLevels.ADVANCED,
  mode: modes[1].value,
  nsClassificationURI: 'http://slipo.eu/id/classification/',
  nsClassURI: 'http://slipo.eu/id/term/',
  nsDataSourceURI: 'http://slipo.eu/id/poisource/',
  nsFeatureURI: 'http://slipo.eu/id/poi/',
  nsGeometry: 'http://www.opengis.net/ont/geosparql#',
  nsOntology: 'http://slipo.eu/def#',
  prefixes: [...defaultNamespaces],
  profile: null,
  quote: '"',
  registerFeatures: true,
  serialization: serializations[2].value,
  sourceCRS: '4326',
  targetCRS: '4326',
  targetGeoOntology: ontologies[0].value,
};

export const defaultValuesAuto = {
  attrCategory: null,
  attrGeometry: null,
  attrKey: null,
  attrName: null,
  attrX: null,
  attrY: null,
  classificationSpec: null,
  classifyByName: false,
  autoMappings: null,
  userMappings: null,
  defaultLang: 'en',
  delimiter: '|',
  encoding: encodings[3].value,
  featureSource: null,
  inputFormat: EnumDataFormat.CSV,
  level: configurationLevels.AUTO,
  mappingSpec: null,
  mode: modes[1].value,
  nsClassificationURI: 'http://slipo.eu/id/classification/',
  nsClassURI: 'http://slipo.eu/id/term/',
  nsDataSourceURI: 'http://slipo.eu/id/poisource/',
  nsFeatureURI: 'http://slipo.eu/id/poi/',
  nsGeometry: 'http://www.opengis.net/ont/geosparql#',
  nsOntology: 'http://slipo.eu/def#',
  prefixes: [...defaultNamespaces],
  profile: null,
  quote: '"',
  registerFeatures: true,
  serialization: serializations[2].value,
  sourceCRS: '4326',
  targetCRS: '4326',
  targetGeoOntology: ontologies[0].value,
};

export const defaultValuesSimple = {
  classificationSpec: null,
  autoMappings: null,
  userMappings: null,
  level: configurationLevels.SIMPLE,
  mappingSpec: null,
  mode: modes[1].value,
  nsClassificationURI: 'http://slipo.eu/id/classification/',
  nsClassURI: 'http://slipo.eu/id/term/',
  nsDataSourceURI: 'http://slipo.eu/id/poisource/',
  nsFeatureURI: 'http://slipo.eu/id/poi/',
  nsGeometry: 'http://www.opengis.net/ont/geosparql#',
  nsOntology: 'http://slipo.eu/def#',
  prefixes: [...defaultNamespaces],
  registerFeatures: true,
  serialization: serializations[2].value,
  targetGeoOntology: ontologies[0].value,
};

export const defaultReverseValues = {
  defaultLang: 'en',
  delimiter: '|',
  encoding: encodings[3].value,
  profile: null,
  quote: '"',
  serialization: serializations[2].value,
  sourceCRS: '4326',
  targetCRS: '4326',
};

export const predicateTypes = {
  'SLIPO:NAME': [{
    value: 'alternate',
    label: 'Alternate',
  }, {
    value: 'brandname',
    label: 'Brand Name',
  }, {
    value: 'companyname',
    label: 'Company Name',
  }, {
    value: 'international',
    label: 'International',
  }, {
    value: 'official',
    label: 'Official',
  }, {
    value: 'transliterated',
    label: 'Transliterated',
  }],
  'SLIPO:ACCURACY': [{
    value: 'Geocoding Accuracy Level',
    label: 'Geocoding Accuracy Level',
  }, {
    value: 'Positional Accuracy Level',
    label: 'Positional Accuracy Level',
  }],
  'SLIPO:URL': [{
    value: 'image',
    label: 'Image',
  }],
};

export const predicates = {
  ID: 'SLIPO:POIREF',
  LONGITUDE: 'WGS84_POS:LONG',
  LATITUDE: 'WGS84_POS:LAT',
};

export const surrogatePredicates = {
  WKT: '__WKT_GEOMETRY__',
};
