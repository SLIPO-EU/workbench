const ActionTypes = require('../action-types');

function reduceConfiguration(state = {}, action) {
  switch (action.type) {
    case ActionTypes.config.REQUEST_CONFIGURATION:
      return state;
    case ActionTypes.config.LOAD_CONFIGURATION:
      return action.config;
    default:
      return state;
  }
}

module.exports = reduceConfiguration;
