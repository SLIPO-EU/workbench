const fetch = require('fetch');
const URLSearchParams = require('url-search-params');

const { checkStatus } = require('../util/check-fetch-status');

const credentials = 'same-origin'; // always send cookies!

module.exports = (message = 'Hello World') => {
  var q = new URLSearchParams();
  q.set('message', message);

  return fetch('action/echo?' + q.toString(), { credentials })
    .then(checkStatus)
    .then(res => res.json());
};
