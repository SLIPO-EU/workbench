const _ = require('lodash');

const api = require('./api/user');

module.exports = {
  
  getProfile: () => (
    api.getProfile().then(res => {
      var p = res.result;
      if (p == null || !_.isObject(p))
        throw new Error('Expected a non-empty user profile!');
      else
        return p;
    })
  ),

  saveProfile: (profile, token) => api.saveProfile(profile, token),

  login: (username, password, token) => api.login(username, password, token),

  logout: (token) => api.logout(token),
};
