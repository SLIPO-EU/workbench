// viewport.js

// Actions
const SET = 'ui/process-config-step/SET';
const RESET = 'ui/process-config-step/RESET';

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
export const saveTempConfig = (id, values) => ({
  type: SET,
  id,
  values,
});

export const clearTempConfig = () => ({
  type: RESET,
});

