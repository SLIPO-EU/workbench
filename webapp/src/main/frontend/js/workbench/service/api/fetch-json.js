import fetch from 'fetch';

import {
  checkStatus,
} from '../util/check-fetch-status';

import {
  checkError,
} from '../util/check-json';

export default (url, method, token, body, headers) => fetch(url, {
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

