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
  const { profile = null, serialization, ...rest } = config;

  return {
    ...rest,
    profile,
    serialization: fromEnumValue(serialization, serializations),
  };
}

export function writeConfiguration(config) {
  const { quote, sparqlFile, ...rest } = config;

  return {
    ...rest,
    sparqlFile: sparqlFile ? typeof sparqlFile === 'object' ? sparqlFile.path : sparqlFile : null,
    quote: quote || '',
  };
}
