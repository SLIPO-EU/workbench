// Actions
export const REGISTER = 'forms/REGISTER';
export const RESET = 'forms/RESET';
export const SET_VALUES = 'forms/SET_VALUES';
export const RESET_VALUES = 'forms/RESET_VALUES';
export const UPDATE_VALUES = 'forms/UPDATE_VALUES';
export const SET_ERRORS = 'forms/SET_ERRORS';
export const RESET_ERRORS = 'forms/RESET_ERRORS';


// Reducer
export default function (state = {}, action) {
  switch (action.type) {
    case REGISTER: {
      const newState = { ...state };
      if (newState[action.form]) {
        console.warn(`Form ${action.form} already exists. Overriding`);
      }
      newState[action.form] = {
        errors: {},
        values: action.initialValues || {},
        initialValues: action.initialValues || {},
      };
      return newState;
    }

    case RESET: {
      const newState = { ...state };
      newState[action.form] = {
        ...newState[action.form],
        errors: {},
        values: newState[action.form].initialValues,
      };
      return newState;
    }

    case SET_VALUES: {
      const newState = { ...state };
      newState[action.form] = {
        ...newState[action.form],
        values: action.values,
      };
      return newState;
    }

    case UPDATE_VALUES: {
      const newState = { ...state };
      newState[action.form] = {
        ...newState[action.form],
        values: {
          ...newState[action.form].values,
          ...action.values,
        },
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

export const registerForm = function (form, initialValues) {
  return {
    type: REGISTER,
    form,
    initialValues,
  };
};

export const resetForm = function (form) {
  return {
    type: RESET,
    form,
  };
};

export const setFormValues = function (form, values) {
  return {
    type: SET_VALUES,
    form,
    values,
  };
};

export const updateFormValues = function (form, values) {
  return {
    type: UPDATE_VALUES,
    form,
    values,
  };
};

export const resetFormValues = function (form) {
  return {
    type: RESET_VALUES,
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
export const validateForm = function (form, validator, accessor = (state => state.forms)) {
  return function(dispatch, getState) {
    const formState = accessor(getState());

    dispatch(resetFormErrors(form));
    return Promise.resolve()
    .then(() => validator(formState[form] && formState[form].values, getState()))
    .then(() => formState[form].values)
    .catch((errors) => {
      dispatch(setFormErrors(form, errors));
      throw errors;
    });
  };
};
