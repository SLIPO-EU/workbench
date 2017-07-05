const fetch = require('fetch');
const URLSearchParams = require('url-search-params');

const checkStatus = require('../util/check-fetch-status');

const credentials = 'same-origin';

var api = {

  getMessages: (locale) => {
    return fetch(`/i18n/${locale}/messages.json`, {credentials})
      .then(checkStatus)
      .then(res => res.json());
  },

};

module.exports = api;
