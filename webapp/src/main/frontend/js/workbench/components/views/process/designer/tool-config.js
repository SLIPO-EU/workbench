import { EnumTool } from './constants';

/**
 * Static configuration of tool input requirements
 */
export const ToolConfiguration = {
  [EnumTool.TripleGeo]: {
    source: 1,
    poi: 0,
    linked: 0,
    any: 0,
  },
  [EnumTool.LIMES]: {
    source: 0,
    poi: 2,
    linked: 0,
    any: 0,
  }, [EnumTool.FAGI]: {
    source: 0,
    poi: 2,
    linked: 1,
    any: 0,
  }
  , [EnumTool.DEER]: {
    source: 0,
    poi: 1,
    linked: 0,
    any: 0,
  }, [EnumTool.CATALOG]: {
    source: 0,
    poi: 0,
    linked: 0,
    any: 1,
  }
};
