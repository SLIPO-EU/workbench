const _ = require('lodash');
const fetch = require('fetch');
const URLSearchParams = require('url-search-params');

const { checkStatus } = require('../util/check-fetch-status');
const { checkError } = require('../util/check-json');

const credentials = 'same-origin'; // always send cookies!

var api = {

  getProfile: () => {
    var reqOpts = { credentials };
    return fetch('action/user/profile', reqOpts)
      .then(checkStatus)
      .then(res => res.json())
      .then(checkError);
  },

  saveProfile: ({ username, email, givenName, familyName }, token) => {
    if (_.isEmpty(username) || _.isEmpty(email))
      return Promise.reject('A non-empty profile is expected');

    var headers = {
      'content-type': 'application/json',
      'x-csrf-token': token,
    };

    var body = JSON.stringify({ email, givenName, familyName });

    var reqOpts = { method: 'POST', credentials, headers, body };
    return fetch('action/user/profile/save', reqOpts)
      .then(checkStatus);
  },

  login: (username, password, token) => {
    if (username == null || username.length == 0)
      return Promise.reject('A non-empty username is expected');

    var headers = {
      'content-type': 'application/x-www-form-urlencoded',
      'x-csrf-token': token,
    };

    var q = new URLSearchParams();
    q.set('username', username);
    q.set('password', password);

    var reqOpts = { method: 'POST', credentials, headers, body: q.toString() };
    return fetch('login', reqOpts)
      .then(checkStatus)
      .then(res => res.json())
      .then(checkError)
      .then(r => r.result);
  },

  logout: (token) => {
    var headers = { 'x-csrf-token': token };

    var reqOpts = { method: 'POST', credentials, headers };
    return fetch('logout', reqOpts)
      .then(checkStatus)
      .then(res => res.json())
      .then(checkError)
      .then(r => r.result);
  },
};

module.exports = api;
