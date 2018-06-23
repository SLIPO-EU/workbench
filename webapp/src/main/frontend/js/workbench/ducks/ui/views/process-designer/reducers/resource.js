import * as Types from '../types';

import {
  EnumInputType,
} from '../../../../../model/process-designer';

/**
 * Handles {@link ADD_RESOURCE_TO_BAG} action. This action updates several
 * parts of the state
 *
 * @param {any} state
 * @param {any} action
 * @returns the new state
 */
export function addResourceToBagReducer(state, action) {
  if (action.type === Types.ADD_RESOURCE_TO_BAG) {
    // Check if already exists
    const existing = state.resources
      .find((resource) => {
        if (resource.inputType === action.resource.inputType) {
          switch (resource.inputType) {
            case EnumInputType.CATALOG:
              return ((resource.id === action.resource.id) && (resource.version === action.resource.version));
          }
        }
        return false;
      });

    if (existing) {
      return state;
    }

    const resourceKey = (++state.counters.resource).toString();
    return {
      ...state,
      counters: {
        ...state.counters,
        resource: resourceKey,
      },
      resources: [...state.resources, {
        ...action.resource,
        key: resourceKey,
      }]
    };
  }
  return state;
}

export function resourceReducer(state, action) {
  switch (action.type) {
    case Types.LOGOUT:
    case Types.RESET:
      return state
        .filter((resource) => {
          return (resource.inputType === EnumInputType.CATALOG);
        });

    case Types.REMOVE_STEP:
      return state
        .filter((resource) => {
          if (resource.inputType == EnumInputType.OUTPUT) {
            return (resource.stepKey != action.step.key);
          }
          return true;
        });

    case Types.REMOVE_RESOURCE_FROM_BAG:
      return state
        .filter((resource) => {
          if (resource.inputType == action.resource.inputType) {
            switch (resource.inputType) {
              case EnumInputType.CATALOG:
                return ((resource.id !== action.resource.id) || (resource.version != action.resource.version));
            }
          }
          return true;
        });
    default:
      return state;
  }
}

