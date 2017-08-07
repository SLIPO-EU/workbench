const ActionTypes = require('../action-types');

const { getMessages } = require('../service/i18n');

var actions = {

  // Basic actions

  setLocale: (locale) => ({
    type: ActionTypes.locale.SET_LOCALE,
    locale,
  }),

  loadMessages: (locale, messages) => ({
    type: ActionTypes.i18n.LOAD_MESSAGES,
    locale,
    messages,
  }),

  requestMessages: (locale) => ({
    type: ActionTypes.i18n.REQUEST_MESSAGES,
    locale,
  }),

  // Thunk actions

  fetchMessages: (locale) => (dispatch, getState) => {
    dispatch(actions.requestMessages(locale));
    return getMessages(locale)
      .then(r => dispatch(actions.loadMessages(locale, r)));
  },

  changeLocale: (locale) => (dispatch, getState) => (
    dispatch(actions.fetchMessages(locale))
      .then(
      () => dispatch(actions.setLocale(locale)),
      (err) => console.warn("No messages for locale " + locale))
  ),

};

module.exports = actions;
