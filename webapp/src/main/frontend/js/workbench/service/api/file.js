import fetch from 'fetch';

import {
  EnumErrorLevel,
  ServerError,
} from '../../model';

export const download = (url, token) => {
  return fetch(url, {
    method: 'GET',
    headers: {
      'x-requested-with': 'XMLHttpRequest',
      'x-csrf-token': token,
    },
    credentials: 'same-origin',
  })
    .then(res => {
      if (res.status >= 200 && res.status < 300) {
        return res;
      } else {
        console.error(`Received: ${res.status} ${res.statusText}`);

        throw new ServerError([{
          code: 'UNKNOWN',
          level: EnumErrorLevel.ERROR,
          description: 'Failed to download file',
        }]);
      }
    })
    .then(res => res.blob());
};

export default {
  download,
};
