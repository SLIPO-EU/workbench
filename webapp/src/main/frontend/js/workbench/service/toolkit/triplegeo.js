import actions from '../api/fetch-actions';

import {
  configurationLevels,
  ontologies,
  predicates,
  serializations,
  surrogatePredicates,
} from "../../model/process-designer/configuration/triplegeo";

function fromEnumValue(value, list) {
  const result = list.find((item) => ((item.value === value) || (item.label === value)));
  return (result ? result.value : value);
}

export function validateConfiguration(config) {
  const errors = {};

  if ((config['level'] === configurationLevels.SIMPLE) && (!config['profile'])) {
    errors['profile'] = 'Required';
  }

  if (config['level'] === configurationLevels.AUTO) {
    const id = config['userMappings'].find(m => m.predicate === predicates.ID) || null;
    const lon = config['userMappings'].find(m => m.predicate === predicates.LONGITUDE) || null;
    const lat = config['userMappings'].find(m => m.predicate === predicates.LATITUDE) || null;
    const geometry = config['userMappings'].find(m => m.predicate === surrogatePredicates.WKT) || null;

    if (!id) {
      errors[`mapping-${predicates.ID}`] = `A mapping is required for predicate ${predicates.ID}`;
    }
    if (!geometry) {
      if (!lon) {
        errors[`mapping-${predicates.LONGITUDE}`] = `A mapping is required for predicate ${predicates.LONGITUDE}`;
      }
      if (!lat) {
        errors[`mapping-${predicates.LATITUDE}`] = `A mapping is required for predicate ${predicates.LATITUDE}`;
      }
    }
  }

  if (config['level'] !== configurationLevels.AUTO) {
    if (!config['featureSource']) {
      errors['featureSource'] = 'Required';
    }
    if (!config['inputFormat']) {
      errors['inputFormat'] = 'Required';
    }
    if (!config['mode']) {
      errors['mode'] = 'Required';
    }
    if (!config['attrKey']) {
      errors['attrKey'] = 'Required';
    }

    if (config && config.inputFormat !== 'CSV' && config.inputFormat !== 'JSON' && !config['attrGeometry']) {
      errors['attrGeometry'] = 'Required';
    }

    if (config && config.inputFormat === 'CSV') {
      if (!config['delimiter']) {
        errors['delimiter'] = 'Required for CSV';
      }
      if (!config['attrGeometry']) {
        if (!config['attrX']) {
          errors['attrX'] = 'Required for CSV';
        }
        if (!config['attrY']) {
          errors['attrY'] = 'Required for CSV';
        }
      }
    }

    if (config && config.inputFormat === 'JSON') {
      if (!config['attrX']) {
        errors['attrX'] = 'Required for JSON';
      }
      if (!config['attrY']) {
        errors['attrY'] = 'Required for JSON';
      }
    }
  }

  if (!config['encoding']) {
    errors['encoding'] = 'Required';
  }
  if (!config['serialization']) {
    errors['serialization'] = 'Required';
  }
  if (!config['targetGeoOntology']) {
    errors['targetGeoOntology'] = 'Required';
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const {
    level = configurationLevels.ADVANCED,
    prefixes, namespaces, profile = null, serialization, targetGeoOntology, sourceCRS = null, targetCRS = null,
    ...rest
  } = config;
  const prefixArr = prefixes ? prefixes.split(',').map(v => v.trim()) : [];
  const namespaceArr = namespaces ? namespaces.split(',').map(v => v.trim()) : [];

  return {
    ...rest,
    level,
    profile,
    serialization: fromEnumValue(serialization, serializations),
    targetGeoOntology: fromEnumValue(targetGeoOntology, ontologies),
    prefixes: prefixArr && namespaceArr && prefixArr.length === namespaceArr.length ?
      prefixArr
        .map((prefix, index) => {
          if (!prefix) {
            return null;
          }
          return { prefix, namespace: namespaceArr[index] };
        })
        .filter((item) => {
          return item !== null;
        }) : [],
    sourceCRS: sourceCRS ? sourceCRS.split(':')[1] : null,
    targetCRS: targetCRS ? targetCRS.split(':')[1] : null,
    autoMappings: null,
  };
}

export function writeConfiguration(config) {
  const {
    autoMappings, userMappings,
    quote, prefixes, mappingSpec, classificationSpec, sourceCRS = null, targetCRS = null,
    ...rest
  } = config;

  return {
    ...rest,
    prefixes: prefixes.map(v => v.prefix).join(','),
    namespaces: prefixes.map(v => v.namespace).join(','),
    mappingSpec: mappingSpec ? typeof mappingSpec === 'object' ? mappingSpec.path : mappingSpec : null,
    classificationSpec: classificationSpec ? typeof classificationSpec === 'object' ? classificationSpec.path : classificationSpec : null,
    registerFeatures: true,
    quote: quote || '',
    sourceCRS: sourceCRS ? 'EPSG:' + sourceCRS : null,
    targetCRS: targetCRS ? 'EPSG:' + targetCRS : null,
    // Filter out empty predicates
    userMappings: userMappings.filter(m => !!m.predicate),
  };
}

export function getMappings(path, token) {
  if (path.startsWith('/')) {
    path = path.slice(1);
  }

  return actions.get(`/action/triplegeo/mappings?path=${path}`, token).
    then(result => {
      // Convert array to hash
      const mappings = result.reduce((value, mapping) => {
        if (!value[mapping.field]) {
          value[mapping.field] = [];
        }
        value[mapping.field].push({ predicate: mapping.predicate, score: mapping.score });
        return value;
      }, {});

      return mappings;
    });
}

export function getMappingsFileAsText(mappings, token) {

  return actions.post('/action/triplegeo/mappings', token, { mappings }).
    then(yaml => {
      return yaml;
    });
}
