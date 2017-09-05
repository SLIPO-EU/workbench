// config.js

// Actions
const REQUEST_CONFIGURATION = 'config/REQUEST_CONFIGURATION';
const LOAD_CONFIGURATION = 'config/LOAD_CONFIGURATION';

// Reducer
const reduceConfig = (state = {}, action) => {
  switch (action.type) {
    case REQUEST_CONFIGURATION:
      return state;
    case LOAD_CONFIGURATION:
      return action.config;
    default:
      return state;
  }
};

// Action Creators
const requestConfiguration = () => ({
  type: REQUEST_CONFIGURATION,
});

const loadConfiguration = (config) => ({
  type: LOAD_CONFIGURATION,
  config,
});

// Thunk actions
const getConfiguration = () => (dispatch) => {
  // Request and load configuration from server
  dispatch(requestConfiguration());
  // Todo fetch and load
};
  
module.exports = {
  reduceConfig,
  requestConfiguration,
  loadConfiguration,
  getConfiguration,
};
