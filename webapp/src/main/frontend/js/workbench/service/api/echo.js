import fetch from 'fetch';
import URLSearchParams from 'url-search-params';

import {
  checkStatus,
} from '../util/check-fetch-status';

const credentials = 'same-origin';

export const echo = (message = 'Hello World') => {
  var q = new URLSearchParams();
  q.set('message', message);

  return fetch('/action/echo?' + q.toString(), { credentials })
    .then(checkStatus)
    .then(res => res.json());
};

export default {
  echo,
};
