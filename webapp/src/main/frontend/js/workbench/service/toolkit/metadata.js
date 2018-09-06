export function validateConfiguration(config) {
  const errors = {};

  if (!config.name) {
    errors.name = 'File name required';
  }
  if (!config.description || config.description.length < 5) {
    errors.description = 'Description should be longer than 5 characters';
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const { metadata } = config;

  return {
    ...metadata,
  };
}

export function writeConfiguration(config) {
  const { ...rest } = config;

  return {
    metadata: {
      ...rest,
    },
  };
}
