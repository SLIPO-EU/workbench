
const ActionTypes = require('../action-types');

var actions = {
  
  setCsrfToken: (token) => ({
    type: ActionTypes.meta.SET_CSRF_TOKEN,
    token,
  }),

};

module.exports = actions;
