import * as Types from '../types';

export function counterReducer(state, action) {
  switch (action.type) {
    case Types.LOGOUT:
    case Types.RESET:
      // Do not reset resource counter since resource bag items
      // of type EnumInputType.CATALOG are not deleted
      return {
        ...state,
        step: 0,
      };

    default:
      return state;
  }
}
