// locale.js

const { getMessages } = require('../service/i18n');

// Actions
const SET_LOCALE = 'locale/SET_LOCALE';
const REQUEST_MESSAGES = 'locale/REQUEST_MESSAGES';
const LOAD_MESSAGES = 'locale/LOAD_MESSAGES';

const initialState = {
  locale: '',
  messages: {},
};

// Reducers
export const reduceI18n = (state = initialState, action) => {
  switch (action.type) {
    case REQUEST_MESSAGES:
      return state; // no-op
    case LOAD_MESSAGES: {
      const newState = { ...state };
      newState.messages[action.locale] = action.messages;
      return newState;
    }
    case SET_LOCALE:
      return {
        ...state,
        locale: action.locale,
      };
    default:
      return state;
  }
};

// Action Creators
export const setLocale = (locale) => ({
  type: SET_LOCALE,
  locale,
});

const loadMessages = (locale, messages) => ({
  type: LOAD_MESSAGES,
  locale,
  messages,
});

const requestMessages = (locale) => ({
  type: REQUEST_MESSAGES,
  locale,
});

// Thunk actions
export const fetchMessages = (locale) => (dispatch) => {
  dispatch(requestMessages(locale));
  return getMessages(locale)
  .then(r => dispatch(loadMessages(locale, r)));
};

export const changeLocale = (locale) => (dispatch) => {
  dispatch(fetchMessages(locale))
    .then(
    () => dispatch(setLocale(locale)),
    () => console.warn("No messages for locale " + locale));
};
