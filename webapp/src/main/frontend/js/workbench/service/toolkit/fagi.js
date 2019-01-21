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
    // Verbose option must always be true
    verbose: true,
    rulesSpec: rulesSpec ? typeof rulesSpec === 'object' ? rulesSpec.path : rulesSpec : null,
  };
}
