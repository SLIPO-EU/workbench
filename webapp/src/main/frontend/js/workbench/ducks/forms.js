// Actions
const SET = 'forms/SET';
const RESET = 'forms/RESET';
const SET_ERRORS = 'forms/SET_ERRORS';
const RESET_ERRORS = 'forms/RESET_ERRORS';

// Reducer
export default function (state = {}, action) {
  switch (action.type) {

    case SET: {
      const newState = { ...state };
      newState[action.form] = {
        errors: {},
        ...newState[action.form],
        values: { ...action.formData },
      };

      return newState;
    }

    case RESET: {
      const newState = { ...state };
      newState[action.form] = { 
        values: {},
        errors: {},
      };

      return newState;
    }

  case SET_ERRORS: {
    const newState = { ...state };
    newState[action.form] = {
      ...newState[action.form],
      errors: action.errors,
    };
    return newState;
  }

  case RESET_ERRORS: {
    const newState = { ...state };
    newState[action.form] = {
      ...newState[action.form],
      errors: {},
    };
    return newState;
  }

  default:
    return state;
  }
}

// Action creators
export const setForm = function (form, formData) {
  return {
    type: SET,
    form,
    formData,
  };
};

export const resetForm = function (form) {
  return {
    type: RESET,
    form,
  };
};

export const setFormErrors = function (form, errors) {
  return {
    type: SET_ERRORS,
    form,
    errors,
  };
};

export const resetFormErrors = function (form) {
  return {
    type: RESET_ERRORS,
    form,
  };
};

// Validator argument can be sync or async function
//
// if async must return promise
// resolves if validation successful
// rejects if failed with errors dict
//
// if sync must 
// return if validation successful
// throw errors dict

export const validateForm = function (form, validator) {
  return function(dispatch, getState) {
    dispatch(resetFormErrors(form));
    return Promise.resolve()
    .then(() => validator(getState().forms[form] && getState().forms[form].values, getState()))
    .then(() => getState().forms[form].values)
    .catch((errors) => {
      dispatch(setFormErrors(form, errors));
      throw errors;
    });
  };
};
