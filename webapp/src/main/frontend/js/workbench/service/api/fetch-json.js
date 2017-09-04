const fetch = require('fetch');

const { checkStatus } = require('../util/check-fetch-status');
const { checkError } = require('../util/check-json');

const headers = {
  'accept': 'application/json',
  'x-requested-with': 'XMLHttpRequest',
};

module.exports = (url, method, token, body) => fetch(url, {
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

