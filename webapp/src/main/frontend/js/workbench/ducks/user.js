// user.js
const _ = require('lodash');
const moment = require('moment');

const userService = require('../service/user');

// Actions
const REQUEST_LOGIN = 'user/REQUEST_LOGIN';
const LOGIN = 'user/LOGIN';
const REQUEST_LOGOUT = 'user/REQUEST_LOGOUT';
const LOGOUT = 'user/LOGOUT';
const REQUEST_PROFILE = 'user/REQUEST_PROFILE';
const LOAD_PROFILE = 'user/LOAD_PROFILE';
const SET_PROFILE = 'user/SET_PROFILE';
const REQUEST_SAVE_PROFILE = 'user/REQUEST_SAVE_PROFILE';
const SAVED_PROFILE = 'user/SAVED_PROFILE';

const initialState = {
  username: null,
  loggedIn: null,
  profile: null,
};

// Reducer
const reduceUser = (state = initialState, action) => {
  switch (action.type) {
    case REQUEST_LOGIN:
      return state; // no-op
    case LOGIN:
      return {
        username: action.username,
        loggedIn: action.timestamp,
        profile: null,
      };
    case REQUEST_LOGOUT:
      return state; // no-op  
    case LOGOUT:
      return initialState;
    case REQUEST_PROFILE:
      return state; // no-op
    case SET_PROFILE:
      return {
        ...state,
        profile: {
          ...state.profile,
          ...action.profile,
          _updatedAt: action.timestamp,
        },
      };
    case LOAD_PROFILE:
      return {
        ...state,
        profile: {
          ...action.profile,
          _updatedAt: action.timestamp,
          _savedAt: action.timestamp, // in sync with server
        },
      };
    case REQUEST_SAVE_PROFILE:
      return state; // no-op
    case SAVED_PROFILE:
      return {
        ...state,
        profile: {
          ...state.profile,
          _savedAt: state.profile._updatedAt, // in sync again
        },
      };
    default:
      return state;
  }
};

// Action Creators

const requestLogin = (username) => ({
  type: REQUEST_LOGIN,
  username,
});

const loggedIn = (username, token, timestamp) => ({
  type: LOGIN,
  username,
  token,
  timestamp,
});

const requestLogout = () => ({
  type: REQUEST_LOGOUT,
});

const loggedOut = (token, timestamp) => ({
  type: LOGOUT,
  token,
  timestamp,
});

const requestProfile = () => ({
  type: REQUEST_PROFILE,
});

const loadProfile = (profile, timestamp) => ({
  type: LOAD_PROFILE,
  profile,
  timestamp,
});

const setProfile = (profile, timestamp) => ({
  type: SET_PROFILE,
  profile,
  timestamp,
});

const requestSaveProfile = () => ({
  type: REQUEST_SAVE_PROFILE,
});

const savedProfile = () => ({
  type: SAVED_PROFILE,
});


// Thunk actions

const login = (username, password) => (dispatch, getState) => {
  var { meta: { csrfToken: token } } = getState();
  dispatch(requestLogin(username));
  return userService.login(username, password, token).then(
    (r) => {
      var t = moment().valueOf();
      dispatch(loggedIn(username, r.csrfToken, t));
    },
    (err) => {
      console.error('Failed login: ' + err.message);
      throw err;
    });
};

const logout = () => (dispatch, getState) => {
  var { meta: { csrfToken: token } } = getState();
  dispatch(requestLogout());
  return userService.logout(token).then(
    (r) => {
      var t = moment().valueOf();
      dispatch(loggedOut(r.csrfToken, t));
    },
    (err) => {
      console.error('Failed logout');
      throw err;
    });
};

const refreshProfile = () => (dispatch) => {
  dispatch(requestProfile());
  return userService.getProfile().then(
    (p) => {
      var t = moment().valueOf();
      dispatch(loadProfile(p, t));
    },
    (err) => {
      console.warn('Cannot load user profile: ' + err.message);
      throw err;
    });
};

const saveProfile = () => (dispatch, getState) => {
  var { meta: { csrfToken: token }, user: { profile } } = getState();
  if (_.isEmpty(profile))
    return Promise.reject('The user profile is empty!');

  dispatch(requestSaveProfile());
  return userService.saveProfile(profile, token).then(
    () => dispatch(savedProfile()),
    (err) => {
      console.error('Cannot save user profile: ' + err.message);
      throw err;
    });
};

module.exports = {
  LOGIN,
  LOGOUT,
  reduceUser,
  requestLogin,
  requestLogout,
  loggedIn,
  loggedOut,
  requestProfile,
  loadProfile,
  setProfile,
  requestSaveProfile,
  savedProfile,
  login,
  logout,
  refreshProfile,
  saveProfile,
};

