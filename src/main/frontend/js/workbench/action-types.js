const mirrorToPath = require('./util/path-mirror.js');

module.exports = mirrorToPath({
  
  config: {
    REQUEST_CONFIGURATION: null,
    LOAD_CONFIGURATION: null,
  },
  
  user: {
    REQUEST_LOGIN: null,
    LOGIN: null,
    REQUEST_LOGOUT: null,
    LOGOUT: null,
    REQUEST_PROFILE: null,
    LOAD_PROFILE: null,
    SET_PROFILE: null,
    REQUEST_SAVE_PROFILE: null,
    SAVED_PROFILE: null,
  },

});
