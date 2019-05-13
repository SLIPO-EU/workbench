import {
  EnumTool,
} from '../../model/process-designer';

import {
  validateConfiguration as validateTripleGeo,
  readConfiguration as readTripleGeo,
  writeConfiguration as writeTripleGeo,
} from './triplegeo';

import {
  validateConfiguration as validateLimes,
  readConfiguration as readLimes,
  writeConfiguration as writeLimes,
} from './limes';

import {
  validateConfiguration as validateFagi,
  readConfiguration as readFagi,
  writeConfiguration as writeFagi,
} from './fagi';

import {
  validateConfiguration as validateDeer,
  readConfiguration as readDeer,
  writeConfiguration as writeDeer,
} from './deer';

import {
  validateConfiguration as validateMetadata,
  readConfiguration as readMetadata,
  writeConfiguration as writeMetadata,
} from './metadata';

import {
  validateConfiguration as validateReverseTripleGeo,
  readConfiguration as readReverseTripleGeo,
  writeConfiguration as writeReverseTripleGeo,
} from './triplegeo-reverse';

export function validateConfiguration(tool, config) {
  switch (tool) {
    case EnumTool.TripleGeo:
      return validateTripleGeo(config);
    case EnumTool.LIMES:
      return validateLimes(config);
    case EnumTool.FAGI:
      return validateFagi(config);
    case EnumTool.DEER:
      return validateDeer(config);
    case EnumTool.CATALOG:
      return validateMetadata(config);
    case EnumTool.ReverseTripleGeo:
      return validateReverseTripleGeo(config);
    case EnumTool.IMPORTER:
      return true;
  }
  throw new Error(`Tool ${tool} is not supported`);
}

export function readConfiguration(tool, config) {
  switch (tool) {
    case EnumTool.TripleGeo:
      return readTripleGeo(config);
    case EnumTool.LIMES:
      return readLimes(config);
    case EnumTool.FAGI:
      return readFagi(config);
    case EnumTool.DEER:
      return readDeer(config);
    case EnumTool.CATALOG:
      return readMetadata(config);
    case EnumTool.ReverseTripleGeo:
      return readReverseTripleGeo(config);
    case EnumTool.IMPORTER:
      return config;
  }
  throw new Error(`Tool ${tool} is not supported`);
}

export function writeConfiguration(tool, config) {
  switch (tool) {
    case EnumTool.TripleGeo:
      return writeTripleGeo(config);
    case EnumTool.LIMES:
      return writeLimes(config);
    case EnumTool.FAGI:
      return writeFagi(config);
    case EnumTool.DEER:
      return writeDeer(config);
    case EnumTool.CATALOG:
      return writeMetadata(config);
    case EnumTool.ReverseTripleGeo:
      return writeReverseTripleGeo(config);
  }
  throw new Error(`Tool ${tool} is not supported`);
}
