const ActionTypes = require('../action-types');

function reduceMeta(state = {}, action) {
  switch (action.type) {
    case ActionTypes.meta.SET_CSRF_TOKEN: // token read from meta tag
    case ActionTypes.user.LOGIN:  // token replaced by new session 
    case ActionTypes.user.LOGOUT: // -- 
      return { csrfToken: action.token };
    default:
      return state;
  }
}

module.exports = reduceMeta;
