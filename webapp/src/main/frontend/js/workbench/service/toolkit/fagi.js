import {
  configurationLevels,
} from "../../model/process-designer/configuration/fagi";

export function validateConfiguration(config) {
  const errors = {};

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const {
    enableMLRules = false,
    level = configurationLevels.ADVANCED,
    profile = null,
    ...rest
  } = config;

  return {
    ...rest,
    enableMLRules,
    level,
    // Always ignore server value
    mlModels: null,
    profile,
  };
}

export function writeConfiguration(config) {
  const { rulesSpec, ...rest } = config;

  return {
    ...rest,
    // ML models must always be null
    mlModels: null,
    // Verbose option must always be true
    verbose: true,
    rulesSpec: rulesSpec ? typeof rulesSpec === 'object' ? rulesSpec.path : rulesSpec : null,
  };
}
