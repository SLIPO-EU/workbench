// viewport.js

// Actions
const SET = 'ui/resource-registration/SET';
const RESET = 'ui/resource-registration/RESET';

// Reducer
const initialState = {
  step: null,
  values: {},
};

export default (state = initialState, action) => {
  switch (action.type) {
    case SET:
      return { 
        step: action.id,
        values: action.values, 
      };

    case RESET:
      return { ...initialState };

    default:
      return state;
  }
};

// Action creators
export const saveTempResource = (id, values) => ({
  type: SET,
  id,
  values,
});

export const clearTempResource = () => ({
  type: RESET,
});

