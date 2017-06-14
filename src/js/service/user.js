const _ = require('lodash');

const api = require('./api/user');

module.exports = {
  
  getProfile: () => (
    api.getProfile().then(res => {
      if (res.user == null || !_.isObject(res.user))
        throw new Error('Expected a non-empty user object!');
      else
        return res.user;
    })
  ),

  saveProfile: (profile) => api.saveProfile(profile),

  login: (username = '', password = '') => api.login(username, password),

  logout: () => api.logout(),
};
