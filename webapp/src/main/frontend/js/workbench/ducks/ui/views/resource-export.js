// Actions
import { LOGOUT } from "../../user";

const SET = 'ui/resource-export/SET';
const RESET = 'ui/resource-export/RESET';

// Reducer
const initialState = {
  step: null,
  values: {},
};

export default (state = initialState, action) => {
  switch (action.type) {
    case LOGOUT:
      return initialState;

    case SET:
      return {
        step: action.id,
        values: action.values,
      };

    case RESET:
      return initialState;

    default:
      return state;
  }
};

// Action creators
export const setTemp = (id, values) => ({
  type: SET,
  id,
  values,
});

export const clearTemp = () => ({
  type: RESET,
});

