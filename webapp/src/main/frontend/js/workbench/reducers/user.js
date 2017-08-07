const ActionTypes = require('../action-types');

var initialState = {
  username: null,
  loggedIn: null,
  profile: null,
};

function reduceUser(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.user.REQUEST_LOGIN:
      return state; // no-op
    case ActionTypes.user.LOGIN:
      return {
        username: action.username,
        loggedIn: action.timestamp,
        profile: null,
      };
    case ActionTypes.user.REQUEST_LOGOUT:
      return state; // no-op  
    case ActionTypes.user.LOGOUT:
      return initialState;
    case ActionTypes.user.REQUEST_PROFILE:
      return state; // no-op
    case ActionTypes.user.SET_PROFILE:
      return {
        ...state,
        profile: {
          ...state.profile,
          ...action.profile,
          _updatedAt: action.timestamp,
        },
      };
    case ActionTypes.user.LOAD_PROFILE:
      return {
        ...state,
        profile: {
          ...action.profile,
          _updatedAt: action.timestamp,
          _savedAt: action.timestamp, // in sync with server
        },
      };
    case ActionTypes.user.REQUEST_SAVE_PROFILE:
      return state; // no-op
    case ActionTypes.user.SAVED_PROFILE:
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
}

module.exports = reduceUser;
