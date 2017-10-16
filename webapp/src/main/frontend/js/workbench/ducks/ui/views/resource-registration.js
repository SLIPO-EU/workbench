// viewport.js

// Actions
const SET = 'resources/SET';
const RESET = 'resources/RESET';

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

