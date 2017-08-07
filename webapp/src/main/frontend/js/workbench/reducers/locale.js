const ActionTypes = require('../action-types');

function reduceLocale(state = "", action) {
  switch (action.type) {
    case ActionTypes.locale.SET_LOCALE:
      return action.locale;
    default:
      return state;
  }
}

module.exports = reduceLocale;
