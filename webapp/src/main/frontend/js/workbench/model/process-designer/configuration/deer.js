import {
  EnumDataFormat,
} from "../enum";

export const configurationLevels = {
  AUTO: 'AUTO',
  ADVANCED: 'ADVANCED',
};

export const configurationLevelOptions = [{
  value: configurationLevels.AUTO,
  label: 'Auto',
  iconClass: 'fa fa-magic',
}, {
  value: configurationLevels.ADVANCED,
  label: 'Advanced',
  iconClass: 'fa fa-user-plus'
}];

export const defaultValues = {
  level: configurationLevels.ADVANCED,
  inputFormat: EnumDataFormat.N_TRIPLES,
  outputFormat: EnumDataFormat.N_TRIPLES,
  outputDir: "output",
  spec: null,
  profile: null,
};
