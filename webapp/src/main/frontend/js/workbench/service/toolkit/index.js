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
  }
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
  }
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
  }
}
