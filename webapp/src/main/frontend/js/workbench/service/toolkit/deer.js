export function validateConfiguration(config) {
  const errors = {};

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const { profile = null, ...rest } = config;

  return {
    ...rest,
    profile,
  };
}

export function writeConfiguration(config) {
  const { spec, ...rest } = config;

  return {
    ...rest,
    spec: spec ? typeof spec === 'object' ? spec.path : spec : null,
  };
}
