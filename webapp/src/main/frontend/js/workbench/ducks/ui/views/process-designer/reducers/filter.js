import * as Types from '../types';

export function filterReducer(state, action) {
  if (action.type === Types.SET_RESOURCE_FILTER) {
    if (state.resource === action.filter) {
      return {
        ...state,
        resource: null,
      };
    } else {
      return {
        ...state,
        resource: action.filter,
      };
    }
  }
  return state;
}
