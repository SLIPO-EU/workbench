import {
  configurationLevels,
} from "../../model/process-designer/configuration/deer";

export function validateConfiguration(config) {
  const errors = {};

  if (Object.keys(errors).length) {
    throw errors;
  }
}

export function readConfiguration(config) {
  const { level = configurationLevels.ADVANCED, profile = null, ...rest } = config;

  return {
    ...rest,
    level,
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
