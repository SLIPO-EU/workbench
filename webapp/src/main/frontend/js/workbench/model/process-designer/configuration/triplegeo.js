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

export const crs = [
  { value: 'EPSG:2100', label: 'EPSG:2100' },
  { value: 'EPSG:4326', label: 'EPSG:4326' },
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
  defaultLang: 'en',
  delimiter: '|',
  encoding: encodings[3].value,
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
  sourceCRS: crs[1].value,
  targetCRS: crs[1].value,
  targetGeoOntology: ontologies[0].value,
};

export const defaultReverseValues = {
  defaultLang: 'en',
  delimiter: '|',
  encoding: encodings[3].value,
  profile: null,
  quote: '"',
  serialization: serializations[2].value,
  sourceCRS: crs[1].value,
  targetCRS: crs[1].value,
};
