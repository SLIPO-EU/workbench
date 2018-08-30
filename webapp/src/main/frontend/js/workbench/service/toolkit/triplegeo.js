import {
  ontologies,
  serializations,
} from "../../model/process-designer/configuration/triplegeo";

function fromEnumValue(value, list) {
  const result = list.find((item) => ((item.value === value) || (item.label === value)));
  return (result ? result.value : value);
}

export function validateConfiguration(config) {
  const errors = {};
  if ((!config['mappingSpec']) && (!config['profile'])) {
    errors['mappingSpec'] = 'Required';
  }
  if ((!config['classificationSpec']) && (!config['profile'])) {
    errors['classificationSpec'] = 'Required';
  }
  if (!config['inputFormat']) {
    errors['inputFormat'] = 'Required';
  }
  if (!config['mode']) {
    errors['mode'] = 'Required';
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
  if (!config['attrKey']) {
    errors['attrKey'] = 'Required';
  }

  if (config && config.inputFormat !== 'CSV' && config.inputFormat !== 'JSON' && !config['attrGeometry']) {
    errors['attrGeometry'] = 'Required';
  }

  if (!config['featureSource']) {
    errors['featureSource'] = 'Required';
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

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const { prefixes, namespaces, profile = null, serialization, targetGeoOntology, ...rest } = config;
  const prefixArr = prefixes ? prefixes.split(',').map(v => v.trim()) : [];
  const namespaceArr = namespaces ? namespaces.split(',').map(v => v.trim()) : [];


  return {
    ...rest,
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
  };
}

export function writeConfiguration(config) {
  const { quote, prefixes, mappingSpec, classificationSpec, ...rest } = config;

  return {
    ...rest,
    prefixes: prefixes.map(v => v.prefix).join(','),
    namespaces: prefixes.map(v => v.namespace).join(','),
    mappingSpec: mappingSpec ? typeof mappingSpec === 'object' ? mappingSpec.path : mappingSpec : null,
    classificationSpec: classificationSpec ? typeof classificationSpec === 'object' ? classificationSpec.path : classificationSpec : null,
    registerFeatures: true,
    quote: quote || '',
  };
}

