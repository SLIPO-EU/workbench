import {
  EnumStepFileType
} from './constants';

export const stepFileTypeToText = (value) => {
  switch (value) {
    case EnumStepFileType.CONFIGURATION:
      return 'Configuration';
    case EnumStepFileType.INPUT:
      return 'Input';
    case EnumStepFileType.OUTPUT:
      return 'Output';
    case EnumStepFileType.SAMPLE:
      return 'Sample';
    case EnumStepFileType.KPI:
      return 'KPI';
    case EnumStepFileType.QA:
      return 'Q&A';
    default:
      return '-';
  }
};
