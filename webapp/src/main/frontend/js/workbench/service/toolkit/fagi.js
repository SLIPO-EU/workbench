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
  const { rulesSpec, ...rest } = config;

  return {
    ...rest,
    rulesSpec: rulesSpec ? typeof rulesSpec === 'object' ? rulesSpec.path : rulesSpec : null,
  };
}
