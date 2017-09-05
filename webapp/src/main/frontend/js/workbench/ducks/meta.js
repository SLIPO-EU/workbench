// meta.js

const { LOGIN, LOGOUT } = require('./user');

// Actions
const SET_CSRF_TOKEN = 'meta/SET_CSRF_TOKEN';

// Reducer
export const reduceMeta = (state = {}, action) => {
  switch (action.type) {
    case SET_CSRF_TOKEN: // token read from meta tag
    case LOGIN:  // token replaced by new session 
    case LOGOUT: // -- 
      return { csrfToken: action.token };
    default:
      return state;
  }
};


// Action Creators
export const setCsrfToken = (token) => ({
  type: SET_CSRF_TOKEN,
  token,
});
