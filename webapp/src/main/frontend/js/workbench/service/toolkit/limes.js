import {
  configurationLevels,
} from "../../model/process-designer/configuration/limes";

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
  const { ...rest } = config;

  return {
    ...rest,
  };
}
