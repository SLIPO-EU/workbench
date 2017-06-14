const _ = require('lodash');
const fetch = require('fetch');
const URLSearchParams = require('url-search-params');

const checkStatus = require('../../util/check-fetch-status');

const credentials = 'same-origin'; // always send cookies!

var api = {

  getProfile: () => (
    fetch('/api/action/user/profile', {credentials})
      .then(checkStatus)
      .then(res => res.json())
  ),

  saveProfile: ({username, email, givenName, familyName}) => {
    if (_.isEmpty(username) || _.isEmpty(email))
      return Promise.reject('A non-empty profile is expected');
    
    var headers = {
      'content-type': 'application/json',
    };
    
    var q = {email, givenName, familyName};
    
    var p = fetch('/api/action/user/profile/save', {
      method: 'POST', credentials, headers, body: JSON.stringify(q),
    });
    return p.then(checkStatus);
  },

  login: (username, password = '') => {  
    if (username == null || username.length == 0)
      return Promise.reject('A non-empty username is expected');
      
    var headers = {
      'content-type': 'application/x-www-form-urlencoded',
    };

    var q = new URLSearchParams();
    q.set('username', username);
    q.set('password', password);

    var p = fetch('/login', {
      method: 'POST', credentials, headers, body: q.toString(),
    });
    return p.then(checkStatus);
  },

  logout: () => (
    fetch('/logout', {method: 'POST', credentials})
      .then(checkStatus)
  ),
};

module.exports = api;
