import * as Types from '../types';

import {
  EnumSelection,
} from '../../../../../model/process-designer';

export function activeReducer(state, action) {
  switch (action.type) {
    case Types.LOGOUT:
    case Types.RESET:
    case Types.REMOVE_STEP:
    case Types.REMOVE_STEP_INPUT:
    case Types.REMOVE_RESOURCE_FROM_BAG:
    case Types.RESET_SELECTION:
      return {
        type: null,
        step: null,
        item: null,
      };

    case Types.SELECT_PROCESS:
      return {
        type: EnumSelection.Process,
        step: null,
        item: action.process,
      };

    case Types.SELECT_STEP:
      return {
        type: EnumSelection.Step,
        step: action.step.key,
        item: null,
      };

    case Types.SELECT_STEP_INPUT:
      return {
        type: EnumSelection.Input,
        step: action.step.key,
        item: action.resource.key,
      };

    case Types.SELECT_STEP_DATA_SOURCE:
      return {
        type: EnumSelection.DataSource,
        step: action.step.key,
        item: action.dataSource.key,
      };

    case Types.SELECT_RESOURCE:
      return {
        type: EnumSelection.Resource,
        step: null,
        item: action.resource.key,
      };

    default:
      return state;
  }
}
