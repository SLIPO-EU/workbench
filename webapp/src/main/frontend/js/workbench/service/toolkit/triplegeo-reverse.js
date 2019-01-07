import {
  serializations,
} from "../../model/process-designer/configuration/triplegeo";

function fromEnumValue(value, list) {
  const result = list.find((item) => ((item.value === value) || (item.label === value)));
  return (result ? result.value : value);
}

export function validateConfiguration(config) {
  const errors = {};
  if (!config['outputFormat']) {
    errors['outputFormat'] = 'Required';
  }
  if ((!config['profile']) && (!config['sparqlFile'])) {
    errors['sparqlFile'] = 'Required';
  }
  if (config['outputFormat'] === 'CSV') {
    if (!config['delimiter']) {
      errors['delimiter'] = 'Required for CSV';
    }
  }
  if (!config['serialization']) {
    errors['serialization'] = 'Required';
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const { profile = null, serialization, sourceCRS = null, targetCRS = null, ...rest } = config;

  return {
    ...rest,
    profile,
    serialization: fromEnumValue(serialization, serializations),
    sourceCRS: sourceCRS ? sourceCRS.split(':')[1] : null,
    targetCRS: targetCRS ? targetCRS.split(':')[1] : null,
  };
}

export function writeConfiguration(config) {
  const { quote, sparqlFile, sourceCRS = null, targetCRS = null, ...rest } = config;

  return {
    ...rest,
    sparqlFile: sparqlFile ? typeof sparqlFile === 'object' ? sparqlFile.path : sparqlFile : null,
    quote: quote || '',
    sourceCRS: sourceCRS ? 'EPSG:' + sourceCRS : null,
    targetCRS: targetCRS ? 'EPSG:' + targetCRS : null,
  };
}
