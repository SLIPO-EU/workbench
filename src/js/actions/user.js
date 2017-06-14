const _ = require('lodash');
const moment = require('moment');

const ActionTypes = require('../action-types');

const {login, logout, getProfile, saveProfile} = require('../service/user');

var actions = {

  // 
  // Basic actions
  //

  requestLogin: (username, redirectUrl = '/') => ({
    type: ActionTypes.user.REQUEST_LOGIN,
    username,
  }), 

  loggedIn: (username, timestamp) => ({
    type: ActionTypes.user.LOGIN,
    username,
    timestamp,
  }),

  requestLogout: () => ({
    type: ActionTypes.user.REQUEST_LOGOUT,
  }),

  loggedOut: () => ({
    type: ActionTypes.user.LOGOUT, 
  }),

  requestProfile: () => ({
    type: ActionTypes.user.REQUEST_PROFILE,
  }), 
  
  loadProfile: (profile, timestamp) => ({
    type: ActionTypes.user.LOAD_PROFILE,
    profile,
    timestamp,
  }),

  setProfile: (profile, timestamp) => ({
    type: ActionTypes.user.SET_PROFILE,
    profile,
    timestamp,
  }), 
  
  requestSaveProfile: () => ({
    type: ActionTypes.user.REQUEST_SAVE_PROFILE,
  }), 

  savedProfile: () => ({
    type: ActionTypes.user.SAVED_PROFILE,
  }), 

  //
  // Thunk actions
  //

  login: (username, password) => (dispatch, getState) => {
    dispatch(actions.requestLogin(username));
    return login(username, password).then(
      (res) => {
        console.info('Logged in');
        var t = moment().valueOf();
        return dispatch(actions.loggedIn(username, t));
      },
      (err) => {
        console.error('Failed login *1*');
        throw err;
      });
  },

  logout: () => (dispatch, getState) => {
    dispatch(actions.requestLogout());
    return logout().then(
      (res) => {
        console.info('Logged out');
        return dispatch(actions.loggedOut());
      },
      (err) => {
        console.error('Failed logout');
        throw err;
      });
  },
  
  refreshProfile: () => (dispatch, getState) => { 
    dispatch(actions.requestProfile());
    return getProfile().then(
      (p) => {
        var t = moment().valueOf();
        return dispatch(actions.loadProfile(p, t));
      },  
      (err) => {
        console.warn('Cannot load user profile: ' + err.message);
        throw err;
      });
  },

  saveProfile: () => (dispatch, getState) => {
    var {user: {profile}} = getState();
    if (_.isEmpty(profile))
      return Promise.reject('The user profile is empty!');

    dispatch(actions.requestSaveProfile());
    return saveProfile(profile).then(
      () => dispatch(actions.savedProfile()),
      (err) => {
        console.error('Cannot save user profile: ' + err.message);
        throw err;
      });
  },
};

module.exports = actions;
