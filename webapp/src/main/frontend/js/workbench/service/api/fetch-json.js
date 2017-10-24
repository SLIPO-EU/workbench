const fetch = require('fetch');

const { checkStatus } = require('../util/check-fetch-status');
const { checkError } = require('../util/check-json');

module.exports = (url, method, token, body, headers) => fetch(url, {
  method, 
  headers: {
    ...headers,
    'x-csrf-token': token,
  },
  credentials: 'same-origin',
  body,
})
  .then(checkStatus)
  .then(res => res.json())
  .then(checkError)
  .then(r => r.result);

