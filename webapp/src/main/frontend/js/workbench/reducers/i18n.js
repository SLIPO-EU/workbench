const Redux = require('redux');
const _ = require('lodash');

const ActionTypes = require('../action-types');

function reduceMessages(state = {}, action) {
  switch (action.type) {
    case ActionTypes.i18n.REQUEST_MESSAGES:
      return state; // no-op
    case ActionTypes.i18n.LOAD_MESSAGES:
      var { locale, messages } = action;
      return _.assign({}, state, { [locale]: messages });
    default:
      return state;
  }
}

module.exports = Redux.combineReducers({
  messages: reduceMessages,
});
